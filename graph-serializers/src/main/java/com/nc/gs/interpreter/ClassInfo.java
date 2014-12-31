package com.nc.gs.interpreter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import symbols.io.abstraction._Meta;
import symbols.io.abstraction._Tags;
import symbols.java.lang._Class;
import symbols.java.lang._Object;

import com.nc.gs.util.Utils;

public final class ClassInfo extends ClassVisitor {

	static class Fields extends AnnotationVisitor {

		class Collector extends AnnotationVisitor {

			Set<String> s;

			public Collector(Set<String> s) {
				super(Opcodes.ASM5);
				this.s = s;
			}

			@Override
			public void visit(String name, Object value) {
				s.add((String) value);
			}
		}

		final ExtendedType info;

		final ClassInfo ci;

		public Fields(ClassInfo ci) {
			super(Opcodes.ASM5);
			this.info = ci.info;
			this.ci = ci;
		}

		public Fields(ExtendedType info) {
			super(Opcodes.ASM5);
			this.info = info;
			this.ci = null;
		}

		@Override
		public void visit(String name, Object value) {
			switch (name) {
			case _Meta.Fields.welcomesTransient:
				info.access |= _Tags.ExtendedType.ACC_WELCOME_TRANSIENT;
				break;
			case _Meta.Fields.welcomesAllNonTransient:
				if ((boolean) value) {
					info.access &= ~_Tags.ExtendedType.ACC_REJECT_NON_TRANSIENT;
				} else {
					info.access |= _Tags.ExtendedType.ACC_REJECT_NON_TRANSIENT;
				}
			case _Meta.Fields.compressByDefault:
				info.access |= _Tags.ExtendedType.ACC_COMPRESS_PRIMS;
			default:
				break;
			}
		}

		@Override
		public AnnotationVisitor visitArray(String name) {
			if (ci == null) {
				return null;
			}
			switch (name) {
			case _Meta.Fields.include:
				return new Collector(ci.include = new HashSet<String>());
			case _Meta.Fields.exclude:
				return new Collector(ci.exclude = new HashSet<String>());
			default:
				break;
			}
			return super.visitArray(name);
		}

	}

	class Serialized extends AnnotationVisitor {

		public Serialized() {
			super(Opcodes.ASM5);
		}

		@Override
		public void visit(String name, Object value) {
			serializer = (Type) value;
		}
	}

	public static ClassInfo getInfo(Class<?> src) throws IOException {
		return getInfo(src, FieldTrap.DEFAULT, false);
	}

	public static ClassInfo getInfo(Class<?> src, FieldTrap trap, boolean deep) {
		ClassInfo rv;

		if (src.isArray()) {
			Type t = Type.getType(src);

			rv = new ClassInfo();

			rv.info = new ExtendedType(Opcodes.ACC_PUBLIC, t.getInternalName(), null, null, null);
			rv.info.type = t;

		} else {

			try (VisitationContext vc = VisitationContext.current()) {

				String iN = Type.getInternalName(src);
				rv = vc.info(iN, trap);

				if (rv == null) {
					rv = getInfo(iN, trap);
				}

				Class<?> sc;

				if (deep && rv.parent == null && (sc = src.getSuperclass()) != Object.class && sc != null) {
					rv.parent = getInfo(sc, trap, deep);
				}
			}
		}

		return rv;
	}

	static ClassInfo getInfo(String fqn) {
		return getInfo(fqn, FieldTrap.DEFAULT);
	}

	static ClassInfo getInfo(String fqn, FieldTrap trap) {
		try (InputStream is = Utils.streamCode(fqn)) {
			ClassReader cr = new ClassReader(is);
			ClassInfo rv = new ClassInfo();
			rv.trap = trap;
			cr.accept(rv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);

			return rv;
		} catch (IOException e) {
			return Utils.rethrow(new IOException(fqn, e));
		}
	}

	FieldTrap trap;

	ExtendedType info;

	public Boolean allFieldsInSamePackage;

	public Set<String> include;

	public Set<String> exclude;

	public LinkedList<FieldInfo> fields;

	public LinkedList<CtorInfo> ctors;

	public ClassInfo parent;

	Type type;

	public Type serializer;

	public ClassInfo() {
		super(Opcodes.ASM5);
	}

	public boolean areAllFieldsInSamePackage() {
		Boolean rv = this.allFieldsInSamePackage;

		if (rv == null) {
			ClassInfo curr = this;
			ClassInfo parent = curr.parent;

			if (parent != null) {
				String currPN = curr.packageName();
				String parentPN = parent.packageName();
				do {
					if (!currPN.equals(parentPN)) {
						rv = false;
						break;
					}
					parent = parent.parent;
				} while (parent != null);
			}

			if (rv == null) {
				rv = true;
			}

			this.allFieldsInSamePackage = rv;
		}

		return rv;
	}

	public ExtendedType basic() {
		return info;
	}

	public boolean compressPrims() {
		return info.compressPrims();
	}

	public String desc() {
		return info.desc;
	}

	ClassInfo destroy() {
		ClassInfo parent = this.parent;
		this.parent = null;

		if (fields != null) {
			fields.clear();
			fields = null;
		}

		return parent;
	}

	public void destroyAll() {
		ClassInfo curr = this;

		while (curr != null) {
			curr = curr.destroy();
		}
	}

	public CtorInfo findDefaultCtor() {
		return findMatchingCtor(_Object.defaultCtor_D);
	}

	public CtorInfo findMatchingCtor(String desc) {
		LinkedList<CtorInfo> ctors = this.ctors;

		CtorInfo rv = null;

		if (ctors != null && !ctors.isEmpty()) {

			for (CtorInfo ctor : ctors) {
				if (ctor.desc.equals(desc)) {
					rv = ctor;
					break;
				}
			}
		}

		return rv;
	}

	public CtorInfo findPartiallyMatchingCtor(String desc) {
		CtorInfo rv = null;

		LinkedList<CtorInfo> ctors = this.ctors;

		if (ctors != null && !ctors.isEmpty()) {
			String[] args = Symbols.getArgumentDescriptors(desc);

			TreeMap<Integer, CtorInfo> matches = new TreeMap<>();

			for (CtorInfo ctor : ctors) {
				if (ctor.desc.equals(_Object.defaultCtor_D)) {
					continue;
				}
				String[] descs = ctor.getArgumentDescriptors();

				if (descs.length > args.length) {
					continue;
				}

				int cons = 0;

				long consumed = 0;

				for (int j = 0; j < descs.length; j++) {
					String ctorArg = descs[j];

					for (int i = cons; i < args.length; i++) {
						if ((consumed & (1 << i)) != 0) {
							continue;
						}

						String arg = args[i];

						if (ctorArg.equals(arg)) {
							consumed |= (1L << i);
							cons++;
							break;
						}
					}
				}

				if (cons == descs.length) {
					matches.put(cons, ctor);
				}
			}

			if (!matches.isEmpty()) {
				rv = matches.lastEntry().getValue();
			}
		}

		return rv;
	}

	public String getJavaClassName() {
		return type().getClassName();
	}

	public String getName() {
		return info.name;
	}

	public String getSuperName() {
		return info.superName;
	}

	private boolean isExcluded(String name) {
		Set<String> exc = this.exclude;
		return exc != null && exc.contains(name);
	}

	public boolean isFinal() {
		return info.isFinal();
	}

	private boolean isIncluded(String name) {
		Set<String> inc = this.include;
		return inc != null && inc.contains(name);
	}

	public boolean isLeaf() {
		return info.isLeaf();
	}

	public boolean isNonStaticInnerClass() {
		return info.isNonStaticInnerClass();
	}

	public boolean isPrivate() {
		return info.isPrivate();
	}

	public ClassInfo lazyParent() {
		ClassInfo p = parent;

		if (p == null && info.superName != null) {
			try (VisitationContext current = VisitationContext.current()) {
				p = parent = current.info(info.superName, trap);

				if (p == null) {
					p = parent = getInfo(info.superName, trap);
				}
			}
		}

		return p;
	}

	public List<FieldInfo> mergedFieldInfo() {
		ArrayList<FieldInfo> allFields = new ArrayList<>();

		ClassInfo curr = this;

		while (curr != null) {
			if (curr.fields != null) {
				allFields.addAll(curr.fields);
			}
			curr = curr.parent;
		}

		return allFields.isEmpty() ? null : allFields;
	}

	public String packageName() {
		return info.packageName();
	}

	public Class<?> runtimeType() {
		try {
			return Class.forName(type().getClassName(), false, Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public Type superType() {
		return Type.getObjectType(info.superName);
	}

	@Override
	public String toString() {
		return info == null ? null : info.desc;
	}

	public Type type() {
		return info.type();
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

		try (VisitationContext context = VisitationContext.current()) {

			ExtendedType basic = context.basic(name);

			if (basic == null) {
				basic = ExtendedType.forInternalName(name, false);
			}

			info = basic;

			context.visited(this);
		}
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (!visible) {
			if (desc.equals(_Meta.Fields.desc)) {
				return new Fields(this);
			} else if (desc.equals(_Meta.Serialized.desc)) {
				return new Serialized();
			} else if (desc.equals(_Meta.LeafNode.desc)) {
				info.access |= _Tags.ExtendedType.ACC_LEAF;
			}
		}

		return null;
	}

	@Override
	public void visitEnd() {
		if (include != null) {
			include.clear();
			include = null;
		}

		if (exclude != null) {
			exclude.clear();
			exclude = null;
		}
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		FieldInfo fv;

		if (trap == FieldTrap.SKIP || (access & Opcodes.ACC_STATIC) != 0 || serializer != null) {
			fv = null;
		} else {

			boolean excluded = isExcluded(name);

			if (excluded) {
				fv = null;
			} else {
				boolean included = isIncluded(name);

				if (included) {
					fv = new FieldInfo(info, access, name, desc, signature, compressPrims());
					info.fieldDeclared(access);
				} else {
					if ((access & Opcodes.ACC_TRANSIENT) != 0) {
						if (welcomesTransient() || trap.acceptsTransient()) {
							fv = new FieldInfo(info, access, name, desc, signature, compressPrims());
							info.fieldDeclared(access);
						} else {
							fv = null;
						}
					} else if (welcomesAllNonTransient()) {
						fv = new FieldInfo(info, access, name, desc, signature, compressPrims());
						info.fieldDeclared(access);
					} else {
						fv = null;
					}
				}
			}
		}

		if (fv != null) {
			LinkedList<FieldInfo> fields = this.fields;
			if (fields == null) {
				this.fields = fields = new LinkedList<>();
			}
			fields.add(fv);
		}

		return fv;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (name.equals(_Class.ctor)) {
			LinkedList<CtorInfo> ctors = this.ctors;
			if (ctors == null) {
				this.ctors = ctors = new LinkedList<>();
			}
			ctors.add(new CtorInfo(access, desc));
		}

		return null;
	}

	public boolean welcomesAllNonTransient() {
		return info.welcomesAllNonTransient();
	}

	public boolean welcomesTransient() {
		return info.welcomesTransient();
	}
}