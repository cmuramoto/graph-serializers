package com.nc.gs.generator.opt;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static symbols.io.abstraction._Tags.MultiCSOptimizer.GS_PREF;
import static symbols.io.abstraction._Tags.MultiCSOptimizer.IC_READ;
import static symbols.io.abstraction._Tags.MultiCSOptimizer.IC_WRITE;
import static symbols.io.abstraction._Tags.MultiCSOptimizer.INSTANCE;
import static symbols.io.abstraction._Tags.MultiCSOptimizer.TEMPLATE;
import static symbols.io.abstraction._Tags.MultiCSOptimizer.TEMPLATE_ARRAY;
import static symbols.io.abstraction._Tags.MultiCSOptimizer.TEMPLATE_SET;
import static symbols.io.abstraction._Tags.MultiCSOptimizer.templateIN;
import static symbols.io.abstraction._Tags.MultiCSOptimizer.templateINARRAY;
import static symbols.io.abstraction._Tags.MultiCSOptimizer.templateINSET;

import java.io.InputStream;
import java.util.Collection;

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

import com.nc.gs.core.GraphClassLoader;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.Instantiator;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.interpreter.CtorInfo;
import com.nc.gs.interpreter.ExtendedType;
import com.nc.gs.interpreter.Hierarchy;
import com.nc.gs.interpreter.ICSlot;
import com.nc.gs.interpreter.Shape;
import com.nc.gs.interpreter.Symbols;
import com.nc.gs.interpreter.VisitationContext;
import com.nc.gs.log.Log;
import com.nc.gs.util.Utils;

import symbols.io.abstraction._CollectionSerializer;
import symbols.io.abstraction._Context;
import symbols.io.abstraction._GraphSerializer;
import symbols.io.abstraction._SerializerFactory;
import symbols.io.abstraction._Tags;
import symbols.io.abstraction._Tags.ObjectShape;
import symbols.java.lang._Class;
import symbols.java.lang._Object;
import symbols.java.util._Collection;
import symbols.java.util._List;
import symbols.java.util._Set;

public class MultiCSOptimizer extends ClassVisitor {

	public static GraphSerializer optmized(String iN, Class<? extends Collection<?>> colType, Class<?>[] types, Shape shape, boolean forRep) {

		colType = Utils.nullIfNotConcrete(colType);

		String suf = shape.canBeNull() ? "1" : "0";

		String targetName = iN;

		String resource;

		if (shape.isSet()) {
			resource = TEMPLATE_SET;

			if (targetName == null) {
				targetName = Symbols._R_optimizedSetName(colType == null ? null : colType.getName(), ExtendedType.forRuntime(types), shape);
			}

		} else if (shape.isArray()) {

			resource = TEMPLATE_ARRAY;
			if (targetName == null) {
				targetName = Symbols._R_optimizedArrayName(null, ExtendedType.forRuntime(types), shape);
			}

		} else if (shape.isCollection() || (colType == null)) {

			resource = TEMPLATE;
			if (targetName == null) {
				targetName = Symbols._R_optimizedCollectionName(colType == null ? null : colType.getName(), ExtendedType.forRuntime(types), shape, forRep);
			}
		} else {

			throw new IllegalArgumentException();
		}

		String javaName = targetName.replace('/', '.');

		try {
			return (GraphSerializer) Class.forName(javaName).getDeclaredField(INSTANCE).get(null);
		} catch (Exception e) {
			Log.info("Generating %s", targetName);
		}

		byte[] bc;

		try (VisitationContext vc = VisitationContext.current()) {

			Hierarchy h = shape.hierarchy();

			if (h == null) {
				h = Hierarchy.from(types, shape.isCompressed());
			} else {
				h = h.markSerializers(shape.isCompressed());
			}

			Instantiator ctor = colType == null ? null : SerializerFactory.instantiatorOf(colType);

			ExtendedType ct = colType == null ? null : ExtendedType.forRuntime(colType);

			try (InputStream is = Utils.streamCode(resource)) {
				ClassReader cr = new ClassReader(is);

				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

				MultiCSOptimizer opt = new MultiCSOptimizer(cw);
				ICSlot slot = new ICSlot(targetName, GS_PREF, h, IC_WRITE, IC_READ, shape.disregardRefs(), false);
				opt.slot = slot;
				opt.suffix = suf;
				opt.ctr = ctor == null ? null : Type.getType(ctor.getClass());
				opt.ct = ct;
				opt.fl = shape.k | (forRep ? ObjectShape.REPLACEMENT : 0);
				opt.s = shape.k | Shape.of(colType);

				cr.accept(opt, ClassReader.SKIP_DEBUG);

				bc = cw.toByteArray();

				Utils.writeClass(slot.serializer.replace("[]", "_"), bc);
			}

		} catch (Exception e) {
			return Utils.rethrow(e);
		} catch (LinkageError le) {
			try {
				return (GraphSerializer) Class.forName(javaName).getDeclaredField(INSTANCE).get(null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		try {
			return (GraphSerializer) GraphClassLoader.INSTANCE.load(null, bc).getDeclaredField(INSTANCE).get(null);
		} catch (Throwable le) {
			try {
				return (GraphSerializer) Class.forName(javaName).getDeclaredField(INSTANCE).get(null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static GraphSerializer optmized(String iN, Class<? extends Collection<?>> colType, Shape shape, boolean forRep) {
		return optmized(iN, colType, shape.hierarchyTypes(), shape, forRep);
	}

	ICSlot slot;

	Type ctr;

	ExtendedType ct;

	MethodNode r;

	MethodNode w;

	MethodNode i;

	String suffix;

	int fl;

	int s;

	public MultiCSOptimizer(ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
	}

	private void changeInstantiate(MethodNode mn) {
		if (ct != null) {

			InsnList insnList = mn.instructions;

			AbstractInsnNode curr = insnList.getFirst();

			while (curr != null) {
				AbstractInsnNode next = null;
				if (curr instanceof FieldInsnNode) {
					FieldInsnNode n = (FieldInsnNode) curr;

					if (n.name.equals(_Tags.CSOptimizer.ctr)) {
						n.desc = ctr.getDescriptor();
						n.owner = slot.serializer;
					} else {
						next = curr.getNext();
						insnList.insert(curr, Symbols.forNum(s));
						insnList.remove(curr);
					}
				}
				curr = next != null ? next : curr.getNext();
			}
		} else if ((fl & ObjectShape.ARRAY) != 0) {
			CtorInfo.createInstantiateForArray(cv, _Object.name);
		}
	}

	private void changeRW(MethodNode mn) {

		boolean ctKnown = ct != null;
		InsnList insnList = mn.instructions;
		boolean coll = ((fl & ObjectShape.COLLECTION) != 0) || !ctKnown;
		boolean array = (fl & ObjectShape.ARRAY) != 0;
		boolean rep = (fl & ObjectShape.REPLACEMENT) != 0;

		AbstractInsnNode curr = insnList.getFirst();

		while (curr != null) {
			AbstractInsnNode next = null;

			if (curr instanceof TypeInsnNode) {
				TypeInsnNode n = (TypeInsnNode) curr;
				if (ctKnown && !rep && (n.getOpcode() == CHECKCAST) && (n.desc.equals(_Collection.name) || n.desc.equals(_List.name) || n.desc.equals(_Set.name))) {
					n.desc = ct.getInternalName();
				}
			} else if (curr instanceof FieldInsnNode) {
				FieldInsnNode n = (FieldInsnNode) curr;

				if (n.getOpcode() == GETSTATIC) {
					n.owner = slot.serializer;

					if ((ctr != null) && n.name.equals(_Tags.CSOptimizer.ctr)) {
						n.desc = ctr.getDescriptor();
					}

				}
			} else if (curr instanceof MethodInsnNode) {
				MethodInsnNode n = (MethodInsnNode) curr;

				if (n.getOpcode() == INVOKESTATIC) {
					if ((coll && n.owner.equals(templateIN)) || (array && n.owner.equals(templateINARRAY)) || n.owner.equals(templateINSET)) {
						n.owner = slot.serializer;
					}
				}

				if (n.owner.equals(_Context.name)) {
					if (ctKnown) {
						next = curr.getNext();

						insnList.remove(curr.getPrevious().getPrevious().getPrevious());
						insnList.remove(curr.getPrevious().getPrevious());
						insnList.remove(curr.getPrevious());
						insnList.remove(curr);
					}
				} else if (n.owner.equals(_Collection.name) || n.owner.equals(_List.name) || n.owner.equals(_Set.name)) {

					if (ctKnown && !rep) {
						n.owner = ct.name;
						n.itf = false;
						n.setOpcode(INVOKEVIRTUAL);
					}
				} else if (n.owner.equals(_CollectionSerializer.name)) {
					if (ctKnown) {
						if ((s & ObjectShape.SORTED) != 0) {
							insnList.remove(n.getPrevious());
							insnList.insertBefore(n, Symbols.forNum(s));
						} else {
							next = n.getNext();
							if (n.name.equals(_CollectionSerializer.readExtensions)) {
								insnList.remove(n.getPrevious().getPrevious().getPrevious().getPrevious().getPrevious());
							}
							insnList.remove(n.getPrevious().getPrevious().getPrevious().getPrevious());
							insnList.remove(n.getPrevious().getPrevious().getPrevious());
							insnList.remove(n.getPrevious().getPrevious());
							insnList.remove(n.getPrevious());
							insnList.remove(n);
						}
					} else {
						insnList.remove(n.getPrevious());
						if (n.name.equals(_CollectionSerializer.readExtensions)) {
							insnList.remove(n.getPrevious());
							insnList.insertBefore(n, new InsnNode(ACONST_NULL));
						}
						insnList.insertBefore(n, Symbols.forNum(s));
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

		for (MethodNode mn : new MethodNode[]{ r, w, i }) {

			if (mn == null) {
				continue;
			}

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

		super.visit(version, access | ACC_FINAL, slot.serializer, signature, _GraphSerializer.name, interfaces);
	}

	@Override
	public void visitEnd() {

		slot.patchInlineCaches(cv);

		updateStacks();

		String desc = "L" + slot.serializer + ";";

		FieldVisitor fv = cv.visitField(ACC_STATIC | ACC_PUBLIC | ACC_FINAL, INSTANCE, desc, null, null);
		fv.visitEnd();

		CtorInfo.patchDefaultCtor(cv, _GraphSerializer.name);

		MethodVisitor mv = cv.visitMethod(ACC_STATIC, _Class.ClassInitializer, _Class.NO_ARG_VOID, null, null);
		mv.visitCode();

		slot.emitFieldDeclarations(mv);

		if (ct != null) {
			mv.visitLdcInsn(ct.type());

			mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.instantiatorOf, _SerializerFactory.instantiatorOf_D, false);
			mv.visitTypeInsn(CHECKCAST, ctr.getInternalName());
			mv.visitFieldInsn(PUTSTATIC, slot.serializer, _Tags.MultiCSOptimizer.ctr, ctr.getDescriptor());
		}

		CtorInfo.invokeDefaultCtor(slot.serializer, mv);
		mv.visitFieldInsn(PUTSTATIC, slot.serializer, INSTANCE, desc);

		mv.visitInsn(RETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
		slot = null;
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if ((ctr != null) && name.equals(_Tags.MultiCSOptimizer.ctr)) {
			return super.visitField(access | ACC_FINAL, name, ctr.getDescriptor(), signature, value);
		}

		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor rv = null;

		if ((i == null) && name.startsWith(_GraphSerializer.instantiate)) {
			if (ct == null) {
				if (name.endsWith("U")) {
					rv = i = new MethodNode(access | ACC_FINAL, _GraphSerializer.instantiate, desc, signature, exceptions);
				}
			} else {
				if ((s & ObjectShape.SORTED) != 0) {
					if (name.endsWith("KCMP")) {
						rv = i = new MethodNode(access | ACC_FINAL, _GraphSerializer.instantiate, desc, signature, exceptions);
					}
				} else {
					if (name.endsWith("KSZ")) {
						rv = i = new MethodNode(access | ACC_FINAL, _GraphSerializer.instantiate, desc, signature, exceptions);
					}
				}
			}
		} else if (name.endsWith(suffix)) {

			MethodNode node = new MethodNode(access | ACC_FINAL, name.replace(suffix, ""), desc, signature, exceptions);

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