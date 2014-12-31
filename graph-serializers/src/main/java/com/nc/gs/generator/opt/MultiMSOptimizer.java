package com.nc.gs.generator.opt;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static symbols.io.abstraction._Tags.MSOptimizer.REPLACEMENT;
import static symbols.io.abstraction._Tags.MSOptimizer.SORTED;
import static symbols.io.abstraction._Tags.MultiMSOptimizer.ICK_R;
import static symbols.io.abstraction._Tags.MultiMSOptimizer.ICK_W;
import static symbols.io.abstraction._Tags.MultiMSOptimizer.ICV_R;
import static symbols.io.abstraction._Tags.MultiMSOptimizer.ICV_W;
import static symbols.io.abstraction._Tags.MultiMSOptimizer.INSTANCE;
import static symbols.io.abstraction._Tags.MultiMSOptimizer.KGS_PREF;
import static symbols.io.abstraction._Tags.MultiMSOptimizer.TEMPLATE;
import static symbols.io.abstraction._Tags.MultiMSOptimizer.VGS_PREF;
import static symbols.io.abstraction._Tags.MultiMSOptimizer.templateIN;

import java.io.InputStream;
import java.util.SortedMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import symbols.io.abstraction._GraphSerializer;
import symbols.io.abstraction._MapSerializer;
import symbols.io.abstraction._SerializerFactory;
import symbols.io.abstraction._Tags;
import symbols.java.lang._Class;
import symbols.java.util._Map;

import com.nc.gs.core.GraphClassLoader;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.Instantiator;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.interpreter.CtorInfo;
import com.nc.gs.interpreter.ExtendedType;
import com.nc.gs.interpreter.Hierarchy;
import com.nc.gs.interpreter.ICSlot;
import com.nc.gs.interpreter.MapShape;
import com.nc.gs.interpreter.Shape;
import com.nc.gs.interpreter.Symbols;
import com.nc.gs.interpreter.VisitationContext;
import com.nc.gs.log.Log;
import com.nc.gs.util.Utils;

public class MultiMSOptimizer extends ClassVisitor {

	public static GraphSerializer optimized(String iN, Class<?> mapType, Class<?>[] keyTypes, Class<?>[] valTypes, Shape sk, Shape sv, boolean forRep) {

		mapType = Utils.nullIfNotConcrete(mapType);

		int kSuf = Utils.asInt(sk.canBeNull());
		int vSuf = Utils.asInt(sv.canBeNull());

		String targetName = iN != null ? iN : Symbols._R_optimizedMapName(mapType == null ? null : mapType.getName(), sk, sv, forRep);

		try {
			return (GraphSerializer) Class.forName(targetName.replace('/', '.')).getDeclaredField(INSTANCE).get(null);
		} catch (Exception e) {
			Log.info("Generating %s", targetName);
		}

		try (VisitationContext vc = VisitationContext.current()) {

			String methodSuffix = String.format("%dX%dX", kSuf, vSuf);

			String resource = TEMPLATE;

			Hierarchy kh = sk.hierarchy();
			Hierarchy vh = sv.hierarchy();

			if (kh == null) {
				kh = Hierarchy.from(keyTypes);
			} else {
				kh = kh.markSerializers();
			}

			if (vh == null) {
				vh = Hierarchy.from(valTypes);
			} else {
				vh = vh.markSerializers();
			}

			Instantiator ctor = mapType == null ? null : SerializerFactory.instantiatorOf(mapType);

			ExtendedType mt = mapType == null ? null : ExtendedType.forRuntime(mapType);

			try (InputStream is = Utils.streamCode(resource)) {
				ClassReader cr = new ClassReader(is);

				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

				MultiMSOptimizer opt = new MultiMSOptimizer(cw);
				ICSlot ks = new ICSlot(targetName, KGS_PREF, kh, ICK_W, ICK_R, sk.disregardRefs());
				ICSlot vs = new ICSlot(targetName, VGS_PREF, vh, ICV_W, ICV_R, sv.disregardRefs());

				opt.ks = ks;
				opt.vs = vs;
				opt.suffix = methodSuffix;
				opt.ctr = ctor == null ? null : Type.getType(ctor.getClass());
				opt.mt = mt;
				opt.fl = (mapType != null && SortedMap.class.isAssignableFrom(mapType) ? SORTED : 0) | (forRep ? REPLACEMENT : 0);

				cr.accept(opt, ClassReader.SKIP_DEBUG);

				byte[] bc = cw.toByteArray();

				Utils.writeClass(targetName.replace("[]", "_"), bc);

				return (GraphSerializer) GraphClassLoader.INSTANCE.load(null, bc).getDeclaredField(INSTANCE).get(null);
			}

		} catch (Exception e) {
			return Utils.rethrow(e);
		}
	}

	public static GraphSerializer optimized(String iN, Class<?> mapType, MapShape ms) {
		Shape ks = ms.ks;
		Shape vs = ms.vs;
		return optimized(iN, mapType, ks.hierarchyTypes(), vs.hierarchyTypes(), ks, vs, ms.rep);
	}

	public static GraphSerializer rawOptimized(Class<?> mapType, Class<?>[] keyTypes, Class<?>[] valTypes, Shape ks, Shape vs, boolean hc) {
		ks.state = new Hierarchy(Object.class, keyTypes, hc);
		vs.state = new Hierarchy(Object.class, valTypes, hc);
		return optimized(null, mapType, keyTypes, valTypes, ks, vs, false);
	}

	ICSlot ks;

	ICSlot vs;

	Type ctr;

	ExtendedType mt;

	MethodNode r;

	MethodNode w;

	MethodNode i;

	String suffix;

	int fl;

	public MultiMSOptimizer(ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
	}

	private void changeInstantiate(MethodNode mn) {
		if (ctr != null) {
			InsnList insnList = mn.instructions;

			AbstractInsnNode curr = insnList.getFirst();

			while (curr != null) {
				if (curr instanceof FieldInsnNode) {
					FieldInsnNode n = (FieldInsnNode) curr;
					n.desc = ctr.getDescriptor();
					n.owner = ks.serializer;
				}
				curr = curr.getNext();
			}
		}
	}

	private void changeRW(MethodNode mn) {

		InsnList insnList = mn.instructions;

		AbstractInsnNode curr = insnList.getFirst();

		ExtendedType mt = this.mt;

		boolean rep = (fl & REPLACEMENT) != 0;
		boolean useConcInsn = mt != null && !rep;

		while (curr != null) {
			AbstractInsnNode next = null;

			if (curr instanceof TypeInsnNode) {
				TypeInsnNode n = (TypeInsnNode) curr;
				if (useConcInsn && n.getOpcode() == CHECKCAST && n.desc.equals(_Map.name)) {
					n.desc = mt.getInternalName();
				}
			} else if (curr instanceof FieldInsnNode) {
				FieldInsnNode n = (FieldInsnNode) curr;

				if (n.getOpcode() == GETSTATIC) {
					n.owner = ks.serializer;

					if (mt != null && n.name.equals(_Tags.MultiCSOptimizer.ctr)) {
						n.desc = ctr.getDescriptor();
					}
				}

			} else if (curr instanceof MethodInsnNode) {
				MethodInsnNode n = (MethodInsnNode) curr;

				int opcode = n.getOpcode();

				if (opcode == INVOKESTATIC) {
					if (n.owner.equals(_MapSerializer.name)) {
						if (mt != null) {
							if ((fl & SORTED) == 0) {
								next = curr.getNext();

								insnList.remove(curr.getPrevious().getPrevious().getPrevious().getPrevious());
								insnList.remove(curr.getPrevious().getPrevious().getPrevious());
								insnList.remove(curr.getPrevious().getPrevious());
								insnList.remove(curr.getPrevious());
								insnList.remove(curr);
							} else {
								if (n.name.equals(_MapSerializer.writeExtensions)) {
									insnList.remove(curr.getPrevious());

									insnList.insertBefore(curr, new InsnNode(ICONST_0));
								}
							}
						} else {
							if (n.name.equals(_MapSerializer.readExtensions)) {
								insnList.remove(curr.getPrevious());

								insnList.insertBefore(curr, new InsnNode(ACONST_NULL));
							}
						}
					} else if (n.owner.equals(templateIN)) {
						n.owner = ks.serializer;
					}
				} else {
					if (mt != null && n.owner.equals(_Map.name)) {
						if (useConcInsn) {
							n.owner = mt.name;
							n.itf = false;
							n.setOpcode(INVOKEVIRTUAL);
						}
					}
				}
			} else if (curr instanceof FrameNode) {
				next = curr.getNext();
				insnList.remove(curr);
			}
			curr = next == null ? curr.getNext() : next;
		}
	}

	public void updateStacks() {

		changeInstantiate(i);

		changeRW(r);

		changeRW(w);

		for (MethodNode mn : new MethodNode[]{ i, r, w }) {

			MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_FINAL, mn.name, mn.desc, mn.signature, null);
			mv.visitCode();

			AbstractInsnNode c = mn.instructions.getFirst();

			while (c != null) {
				c.accept(mv);
				c = c.getNext();
			}

			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

		super.visit(version, access | ACC_FINAL, ks.serializer, signature, _GraphSerializer.name, interfaces);
	}

	@Override
	public void visitEnd() {

		ks.patchInlineCaches(cv);
		vs.patchInlineCaches(cv);

		updateStacks();

		String desc = "L" + ks.serializer + ";";

		FieldVisitor fv = cv.visitField(ACC_STATIC | ACC_PUBLIC | ACC_FINAL, INSTANCE, desc, null, null);
		fv.visitEnd();

		CtorInfo.patchDefaultCtor(cv, _GraphSerializer.name);

		MethodVisitor mv = cv.visitMethod(ACC_STATIC, _Class.ClassInitializer, _Class.NO_ARG_VOID, null, null);
		mv.visitCode();

		ks.emitFieldDeclarations(mv);
		vs.emitFieldDeclarations(mv);

		if (mt != null) {
			mv.visitLdcInsn(mt.type());

			mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.instantiatorOf, _SerializerFactory.instantiatorOf_D, false);
			mv.visitTypeInsn(CHECKCAST, ctr.getInternalName());
			mv.visitFieldInsn(PUTSTATIC, ks.serializer, _Tags.MultiMSOptimizer.ctr, ctr.getDescriptor());
		}

		CtorInfo.invokeDefaultCtor(ks.serializer, mv);
		mv.visitFieldInsn(PUTSTATIC, ks.serializer, INSTANCE, desc);

		mv.visitInsn(RETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
		ks = null;
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if (ctr != null && name.equals(_Tags.MultiMSOptimizer.ctr)) {
			return super.visitField(access | ACC_FINAL, name, ctr.getDescriptor(), signature, value);
		}

		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor rv = null;

		if (name.startsWith(_GraphSerializer.instantiate)) {
			MethodNode node = new MethodNode(access, _GraphSerializer.instantiate, desc, signature, exceptions);

			if (mt == null) {
				if (name.endsWith("U")) {
					rv = i = node;
				}
			} else {
				if ((fl & SORTED) != 0) {
					if (name.endsWith("CMP")) {
						rv = i = node;
					}
				} else {
					if (name.endsWith("SZ")) {
						rv = i = node;
					}
				}
			}
		} else if (name.endsWith(suffix)) {

			MethodNode node = new MethodNode(access, name.replace(suffix, ""), desc, signature, exceptions);

			if (name.startsWith(_GraphSerializer.inflateData)) {
				r = node;
			} else {
				w = node;
			}
			rv = node;
		}

		return rv;
	}

}