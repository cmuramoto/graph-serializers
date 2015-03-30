package com.nc.gs.generator;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.strategy.IdentityHashingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import symbols.io.abstraction._Context;
import symbols.io.abstraction._GraphSerializer;
import symbols.io.abstraction._SerializerFactory;
import symbols.io.abstraction._Sink;
import symbols.io.abstraction._Util;
import symbols.java.lang._Class;
import symbols.java.lang._Object;

import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.interpreter.ClassInfo;
import com.nc.gs.interpreter.ExtendedType;
import com.nc.gs.interpreter.FieldInfo;
import com.nc.gs.interpreter.Hierarchy;
import com.nc.gs.interpreter.ICSlot;
import com.nc.gs.interpreter.SpecialField;
import com.nc.gs.interpreter.Symbols;
import com.nc.gs.util.Pair;

public abstract class GraphClassAdapter extends ClassVisitor {

	private static void collectEnums(Map<String, Type> map, List<FieldInfo> infos) {

		if (infos == null) {
			return;
		}

		for (FieldInfo fi : infos) {
			if (fi.isEnum() && !fi.isAbstractEnum()) {
				String desc = fi.desc;

				if (map.get(desc) == null) {
					map.put(desc, fi.asmType());
				}
			}
		}

	}

	protected static String fieldNameForEnum(String internalName) {
		return internalName.replace('/', '$') + "$VALUES";
	}

	static String targetCachedFieldName(String pojoIN) {
		return pojoIN.replaceAll("[/]", "_") + "_CACHED";
	}

	ClassInfo root;
	GenerationStrategy strategy;

	String targetName;
	String targetDesc;
	String targetSuperName;
	String targetSuperDesc;

	Map<ICKey, ICVal> ics;
	Set<String> icOverloads;

	TCustomHashMap<FieldInfo, SpecialField> fiToS;
	List<SpecialField> sfCache;

	Map<Type, CachedField> cachedFields = new THashMap<>();
	List<FieldInfo> prims;
	List<FieldInfo> nullable;

	List<FieldInfo> nonNullable;

	public GraphClassAdapter(ClassInfo root, GenerationStrategy strategy, ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
		this.root = root;
		this.strategy = strategy;

		Pair<String, Boolean> pA = GenerationStrategy.prefixForSerializer(root);

		targetName = Symbols.graphSerializerName(pA.k);
		targetDesc = "L" + targetName + ";";

		Pair<String, String> pB = strategy.superSerializerNameAndDesc(root);
		targetSuperName = pB.k;
		targetSuperDesc = pB.v;

		prims = strategy.segregatePrimitiveFields(root);
		nullable = strategy.segregateNullableFields(root);
		nonNullable = strategy.segregateNonNullableFields(root);
	}

	private void bindKnownSerializers(MethodVisitor mv) {
		Set<Entry<Type, CachedField>> es = cachedFields.entrySet();

		for (Entry<Type, CachedField> e : es) {
			CachedField cf = e.getValue();
			if ((cf == null) || cf.isReified) {
				continue;
			}
			mv.visitLdcInsn(e.getKey());
			mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.serializer, _SerializerFactory.serializer_D, false);
			mv.visitTypeInsn(CHECKCAST, cf.serializerIN);
			mv.visitFieldInsn(PUTSTATIC, targetName, cf.targetFieldName, cf.serializerDesc());
		}

		List<SpecialField> cfs = sfCache;

		if (cfs != null) {
			for (SpecialField cf : cfs) {
				cf.initialize(mv, targetName);
			}
		}

	}

	public CachedField cachedFieldFor(Type pojo, String serializer) {

		CachedField cf = cachedFields.get(pojo);

		if (cf == null) {
			cf = new CachedField(targetCachedFieldName(pojo.getInternalName()), serializer, false);
			cachedFields.put(pojo, cf);
		}

		return cf;
	}

	private void cacheEnumConstants(MethodVisitor mv) {
		Map<String, Type> map = new THashMap<>();

		List<FieldInfo> nullable = this.nullable;
		List<FieldInfo> nonNullable = this.nonNullable;

		collectEnums(map, nullable);
		collectEnums(map, nonNullable);

		Set<Entry<String, Type>> es = map.entrySet();

		for (Entry<String, Type> e : es) {
			String desc = e.getKey();
			Type type = e.getValue();
			String iN = type.getInternalName();
			String fn = fieldNameForEnum(iN);
			String arrayDesc = "[" + desc;

			FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_STATIC + ACC_FINAL, fn, arrayDesc, null, null);
			fv.visitEnd();

			mv.visitLdcInsn(type);

			mv.visitMethodInsn(INVOKESTATIC, _Util.name, _Util.sharedEC, _Util.sharedEC_D, false);
			mv.visitTypeInsn(CHECKCAST, arrayDesc);
			mv.visitFieldInsn(PUTSTATIC, targetName, fn, arrayDesc);
		}
	}

	protected void clinit() {
		MethodVisitor mv = cv.visitMethod(ACC_STATIC, _Class.ClassInitializer, _Class.NO_ARG_VOID, null, null);
		mv.visitCode();

		clinitBody(mv);

		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	protected void clinitBody(MethodVisitor mv) {
		eagerSelfRegister(mv);

		initializeParentSerializer(mv);

		forceLoadingOfKnownSerializers(mv);

		bindKnownSerializers(mv);

		// if (accessor != null) {
		// mv.visitLdcInsn(root.type());
		// mv.visitMethodInsn(INVOKESTATIC,
		// Type.getInternalName(BridgeAccessorFactory.class),
		// "spinAccessor", "(Ljava/lang/Class;)Ljava/lang/Object;",
		// false);
		// // mv.visitTypeInsn(CHECKCAST, accessor.getInternalName());
		// mv.visitFieldInsn(PUTSTATIC, targetName, "accessor",
		// accessor.getDescriptor());
		// }

		cacheEnumConstants(mv);
	}

	private String disambiguate(String writeInlineName) {
		Set<String> ov = lazyGetOverloads();
		String n = writeInlineName;
		int i = 0;
		while (!ov.add(n)) {
			n = writeInlineName + "_ov_" + String.valueOf(i);
		}

		return n;
	}

	/**
	 * First Instruction in the initializer is to call
	 * {@link SerializerFactory#register(Class, com.nc.gs.core.GraphSerializer)}
	 *
	 * @param mv
	 */
	private void eagerSelfRegister(MethodVisitor mv) {
		mv.visitLdcInsn(root.type());
		mv.visitTypeInsn(NEW, targetName);
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, targetName, _Class.ctor, _Class.NO_ARG_VOID, false);
		// mv.visitInsn(POP);
		mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.register, _SerializerFactory.register_D, false);

	}

	void emitInlineCache(FieldInfo fi) {
		if (fi.hasDeclaredHierarchy()) {
			Map<ICKey, ICVal> ics = lazyGetICMap();
			Hierarchy h = fi.hierarchy();
			ExtendedType[] gens = h.types();
			ICKey key = new ICKey(fi, gens);
			ICVal val = ics.get(key);

			if (val == null) {
				long mask = 0;
				val = new ICVal();
				Type[] sers = new Type[gens.length];
				String[] names = new String[gens.length];

				for (int i = 0; i < gens.length; i++) {
					CachedField cf = serializerNameFor(gens[i].type());
					sers[i] = Type.getType(cf.serializerDesc());
					if (cf.isReified) {
						mask |= (1L << i);
					} else {
						names[i] = cf.targetFieldName;
					}
				}

				h.serNames = names;
				h.sers = sers;
				h.reified = mask;

				ICSlot slot = new ICSlot(targetName, null, h, val.writeMethod = disambiguate(fi.writeInlineName()), val.readMethod = disambiguate(fi.readInlineName()), fi.disregardReference());
				slot.writeDesc = val.writeMethodDesc = fi.writeInlineDesc();
				slot.readDesc = val.readMethodDesc = fi.readInlineDesc();
				slot.patchInlineCaches(cv, false);

				ics.put(key, val);
			}
		}
	}

	public void emitInlineCaches() {
		List<FieldInfo> nullable = this.nullable;
		List<FieldInfo> nonNullable = this.nonNullable;

		if (nullable != null) {
			for (FieldInfo fi : nullable) {
				emitInlineCache(fi);
			}
		}

		if (nonNullable != null) {
			for (FieldInfo fi : nonNullable) {
				emitInlineCache(fi);
			}
		}
	}

	TCustomHashMap<FieldInfo, SpecialField> fieldToSpecial() {
		TCustomHashMap<FieldInfo, SpecialField> rv = fiToS;

		if (rv == null) {
			rv = fiToS = new TCustomHashMap<>(IdentityHashingStrategy.INSTANCE);
		}

		return rv;
	}

	private void forceLoadingOfKnownSerializers(MethodVisitor mv) {

		for (Entry<Type, CachedField> pendingGen : cachedFields.entrySet()) {
			CachedField value = pendingGen.getValue();
			if ((value == null) || !value.isReified) {
				continue;
			}
			mv.visitLdcInsn(pendingGen.getKey());
			mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.serializer, _SerializerFactory.serializer_D, false);
			mv.visitInsn(POP);
		}
	}

	/**
	 * Creates a no-arg constructor for the generated type
	 */
	final void init() {
		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, _Object.init, _Class.NO_ARG_VOID, null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		// mv.visitLdcInsn(Bootstrap.getClassTableImpl().id(root.runtimeType()));
		mv.visitMethodInsn(INVOKESPECIAL, targetSuperName, _Object.init, _GraphSerializer.ctor_D, false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private void initializeParentSerializer(MethodVisitor mv) {
		if (strategy.usesParentDelegation()) {
			mv.visitLdcInsn(root.superType());
			mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.serializer, _SerializerFactory.serializer_D, false);
			mv.visitTypeInsn(CHECKCAST, targetSuperName);
			mv.visitFieldInsn(PUTSTATIC, targetName, "SUPER", targetSuperDesc);
		}
	}

	protected Map<ICKey, ICVal> lazyGetICMap() {
		Map<ICKey, ICVal> rv = ics;
		if (rv == null) {
			rv = ics = new THashMap<>(4);
		}

		return rv;
	}

	protected Set<String> lazyGetOverloads() {
		Set<String> rv = icOverloads;
		if (rv == null) {
			rv = icOverloads = new THashSet<>(4);
		}
		return rv;
	}

	public String reifiedWriteRefOrPaylodDesc(Type t) {
		return String.format("(%s%s%s)V", _Context.desc, _Sink.desc, t.getDescriptor());
	}

	public String serializerDescFor(Type t) {
		try {
			Class<?> rt = Class.forName(t.getClassName(), false, Thread.currentThread().getContextClassLoader());
			GraphSerializer lookup = SerializerFactory.lookup(rt);

			if (lookup == null) {
				cachedFields.put(t, null);
				return "L" + t.getInternalName() + _SerializerFactory.genClassSuffix_D;
			}

			return Type.getDescriptor(lookup.getClass());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	// public String typedWritePayloadDesc(Type t) {
	// return String.format("(%s%s%s)V", core.Context.desc, Sink.desc,
	// t.getDescriptor());
	// }

	public CachedField serializerNameFor(Type t) {
		try {
			CachedField rv = cachedFields.get(t);

			if (rv == null) {

				String serIN;
				Class<?> rt = Class.forName(t.getClassName(), false, Thread.currentThread().getContextClassLoader());

				boolean gen;
				GraphSerializer lookup = SerializerFactory.lookup(rt);

				if (lookup == null) {
					gen = rt.getClassLoader() != null;
					serIN = t.getInternalName() + _SerializerFactory.genClassSuffix;
				} else {
					gen = lookup.getClass().isSynthetic();
					serIN = Type.getInternalName(lookup.getClass());
				}

				cachedFields.put(t, rv = new CachedField(targetCachedFieldName(t.getInternalName()), serIN, gen));
			}
			return rv;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	List<SpecialField> sfCacheLazy() {
		List<SpecialField> cfs = sfCache;
		if (cfs == null) {
			cfs = sfCache = new ArrayList<>(2);
		}

		return cfs;
	}

	SpecialField specialFieldFor(FieldInfo fi) {
		SpecialField sf = fieldToSpecial().get(fi);

		if (sf == null) {
			sf = fi.asSpecial();

			List<SpecialField> map = sfCacheLazy();

			int ix = map.indexOf(sf);

			if (ix >= 0) {
				sf = map.get(ix);
			} else {
				String pref = fi.specialFieldPrefix();
				String name = pref + map.size();
				sf.setName(name);
				map.add(sf);
			}

			fieldToSpecial().put(fi, sf);
		}
		return sf;
	}

	void staticFieldsForInvariants() {
		FieldVisitor fv;

		if (strategy.usesParentDelegation()) {
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC + ACC_FINAL, "SUPER", targetSuperDesc, null, null);
			fv.visitEnd();
		}

		Set<Entry<Type, CachedField>> es = cachedFields.entrySet();

		for (Entry<Type, CachedField> e : es) {
			CachedField cf = e.getValue();
			if ((cf == null) || cf.isReified) {
				continue;
			}
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC + ACC_FINAL, cf.targetFieldName, cf.serializerDesc(), null, null);
			fv.visitEnd();
		}
	}

	void verifyCollectionSerializers() {
		List<FieldInfo> list = nullable;

		int seq = -1;
		if (list != null) {
			for (FieldInfo fi : list) {
				if (fi.isSpecial()) {
					SpecialField sf = specialFieldFor(fi);

					if (fi.canBeOpaque()) {
						sf.patchOpaqueGuards(cv, targetName, fi, seq++);
					}
				}
			}
		}

		list = nonNullable;

		if (list != null) {
			for (FieldInfo fi : list) {
				if (fi.isSpecial()) {
					SpecialField sf = specialFieldFor(fi);

					if (fi.canBeOpaque()) {
						sf.patchOpaqueGuards(cv, targetName, fi, seq++);
					}
				}
			}
		}

		List<SpecialField> map = sfCache;

		if (map != null) {
			for (SpecialField cf : map) {
				cf.emitDeclaration(cv);
			}
		}

	}

	@Override
	public final void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

		super.visit(version, strategy.serializerAccessModifier(root), targetName, signature, targetSuperName, interfaces);
	}

	@Override
	public void visitEnd() {
		emitInlineCaches();

		verifyCollectionSerializers();

		staticFieldsForInvariants();

		clinit();

		init();
	}
}