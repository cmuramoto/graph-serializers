package com.nc.gs.generator.opt;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static symbols.io.abstraction._Tags.CSOptimizer.GS_OFF;
import static symbols.io.abstraction._Tags.CSOptimizer.INSTANCE;
import static symbols.io.abstraction._Tags.CSOptimizer.TEMPLATE;
import static symbols.io.abstraction._Tags.CSOptimizer.TEMPLATE_ARRAY;
import static symbols.io.abstraction._Tags.CSOptimizer.TEMPLATE_RA;
import static symbols.io.abstraction._Tags.CSOptimizer.TEMPLATE_SET;

import java.io.InputStream;
import java.util.Collection;
import java.util.RandomAccess;

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
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import symbols.io.abstraction._CollectionSerializer;
import symbols.io.abstraction._Context;
import symbols.io.abstraction._GraphSerializer;
import symbols.io.abstraction._SerializerFactory;
import symbols.io.abstraction._Tags;
import symbols.io.abstraction._Tags.ObjectShape;
import symbols.java.lang._Class;
import symbols.java.lang._Object;
import symbols.java.util._Collection;
import symbols.java.util._Iterator;
import symbols.java.util._List;
import symbols.java.util._Set;

import com.nc.gs.core.GraphClassLoader;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.Instantiator;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.interpreter.CtorInfo;
import com.nc.gs.interpreter.ExtendedType;
import com.nc.gs.interpreter.Shape;
import com.nc.gs.interpreter.Symbols;
import com.nc.gs.interpreter.VisitationContext;
import com.nc.gs.log.Log;
import com.nc.gs.util.Utils;

public class SimpleCSOptimizer extends ClassVisitor {

	public static GraphSerializer optmized(String iN, Class<? extends Collection<?>> colType, Class<?> type, Shape shape, boolean forRep) {

		colType = Utils.nullIfNotConcrete(colType);

		String suf = shape.canBeNull() ? "1" : "0";

		String targetName = iN;

		String resource;

		if (shape.isSet()) {
			resource = TEMPLATE_SET;

			if (targetName == null) {
				targetName = Symbols._R_optimizedSetName(colType == null ? null : colType.getName(), ExtendedType.forRuntime(new Class<?>[]{ type }), shape);
			}
		} else if (shape.isArray()) {
			resource = TEMPLATE_ARRAY;

			if (targetName == null) {
				targetName = Symbols._R_optimizedArrayName(colType == null ? null : colType.getName(), ExtendedType.forRuntime(new Class<?>[]{ type }), shape);
			}
		} else if (shape.isCollection() || colType == null) {
			if (colType != null && !forRep && RandomAccess.class.isAssignableFrom(colType)) {
				resource = TEMPLATE_RA;
			} else {
				resource = TEMPLATE;
			}
			if (targetName == null) {
				targetName = Symbols._R_optimizedCollectionName(colType == null ? null : colType.getName(), ExtendedType.forRuntime(new Class<?>[]{ type }), shape,forRep);
			}
		} else {
			throw new IllegalArgumentException("Unsupported Shape: " + shape);
		}

		try {
			return (GraphSerializer) Class.forName(targetName.replace('/', '.')).getDeclaredField(INSTANCE).get(null);
		} catch (Exception e) {
			Log.info("Generating %s", targetName);
		}

		try (VisitationContext vc = VisitationContext.current()) {

			GraphSerializer kgs = SerializerFactory.serializer(type);
			Instantiator ctor = colType == null ? null : SerializerFactory.instantiatorOf(colType);

			ExtendedType kt = ExtendedType.forRuntime(type);

			ExtendedType ct = colType == null ? null : ExtendedType.forRuntime(colType);

			try (InputStream is = Utils.streamCode(resource)) {
				ClassReader cr = new ClassReader(is);

				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

				SimpleCSOptimizer opt = new SimpleCSOptimizer(cw);
				opt.targetName = targetName;
				opt.suffix = suf;
				opt.gs = Type.getType(kgs.getClass());
				opt.ctr = ctor == null ? null : Type.getType(ctor.getClass());
				opt.t = kt;
				opt.ct = ct;
				opt.fl = (kgs.getClass().isSynthetic() ? ObjectShape.REIFIED_SER : 0) | (forRep ? ObjectShape.REPLACEMENT : 0);

				opt.s = shape.k | Shape.of(colType);

				cr.accept(opt, ClassReader.SKIP_DEBUG);

				byte[] bc = cw.toByteArray();

				Utils.writeClass(targetName, bc);

				return (GraphSerializer) GraphClassLoader.INSTANCE.load(null, bc).getDeclaredField(INSTANCE).get(null);
			}

		} catch (Exception e) {
			return Utils.rethrow(e);
		}
	}

	public static GraphSerializer optmized(String iN, Class<? extends Collection<?>> colType, Shape shape, boolean forRep) {
		return optmized(iN, colType, shape.hierarchy().uniqueConcrete(), shape, forRep);
	}

	Type gs;

	Type ctr;

	ExtendedType t;

	ExtendedType ct;

	MethodNode r;

	MethodNode w;

	MethodNode i;

	int fl;

	int s;

	String targetName;

	String suffix;

	public SimpleCSOptimizer(ClassVisitor cv) {
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
						n.owner = targetName;
					} else {
						next = curr.getNext();
						insnList.insert(curr, Symbols.forNum(s));
						insnList.remove(curr);
					}
				}
				curr = next != null ? next : curr.getNext();
			}
		} else if ((fl & ObjectShape.ARRAY) != 0) {
			CtorInfo.createInstantiateForArray(cv, t.getInternalName());
		}
	}

	private void changeRW(MethodNode mn) {

		boolean ctKnown = ct != null;
		boolean array = (s & ObjectShape.ARRAY) != 0;
		boolean gsR = (fl & ObjectShape.REIFIED_SER) != 0;
		boolean op = (s & ObjectShape.ONLY_PAYLOAD) != 0;
		boolean rep = (fl & ObjectShape.REPLACEMENT) != 0;

		InsnList insnList = mn.instructions;

		AbstractInsnNode curr = insnList.getFirst();

		while (curr != null) {
			AbstractInsnNode next = null;

			if (curr instanceof TypeInsnNode) {
				TypeInsnNode n = (TypeInsnNode) curr;
				if (n.getOpcode() == CHECKCAST) {
					if (array && n.desc.equals(_Object.array_01)) {
						n.desc = "[" + t.desc;
					} else if (ctKnown && !rep && (n.desc.equals(_Collection.name) || n.desc.equals(_List.name) || n.desc.equals(_Set.name))) {
						n.desc = ct.getInternalName();
					}
				} else if (n.getOpcode() == ANEWARRAY && gsR) {
					n.desc = t.getInternalName();
				}
			} else if (curr instanceof VarInsnNode) {
				VarInsnNode n = (VarInsnNode) curr;

				if (gsR) {
					if (n.var == GS_OFF) {
						next = curr.getNext();
						insnList.remove(curr);
					} else if ((n.var - 1) >= GS_OFF) {
						// don't shift locals below minimum offset (4)
						n.var--;
					}
				}
			} else if (curr instanceof FieldInsnNode) {
				FieldInsnNode n = (FieldInsnNode) curr;

				if (n.getOpcode() == GETSTATIC) {
					boolean remove = false;
					String newDesc = null;

					if (n.name.equals(_Tags.CSOptimizer.gs)) {
						if (gsR) {
							remove = true;
						} else {
							newDesc = gs.getDescriptor();
						}
					} else if (ctr != null && n.name.equals(_Tags.CSOptimizer.ctr)) {
						newDesc = ctr.getDescriptor();
					}

					if (remove) {
						next = curr.getNext();
						insnList.remove(curr);
					} else if (newDesc != null) {
						n.desc = newDesc;
					}

					n.owner = targetName;

				}
			} else if (curr instanceof MethodInsnNode) {
				MethodInsnNode n = (MethodInsnNode) curr;

				if (n.owner.equals(_Context.name)) {
					if (ctKnown) {
						next = curr.getNext();

						insnList.remove(curr.getPrevious().getPrevious().getPrevious());
						insnList.remove(curr.getPrevious().getPrevious());
						insnList.remove(curr.getPrevious());
						insnList.remove(curr);
					}
				}
				if (n.owner.equals(_Collection.name) || n.owner.equals(_List.name) || n.owner.equals(_Set.name)) {

					if (ctKnown && !rep) {
						n.owner = ct.name;
						n.itf = false;
						n.setOpcode(INVOKEVIRTUAL);
					}

					if (gsR && n.name.equals(_List.get)) {
						insnList.//
							insert(curr, new TypeInsnNode(CHECKCAST, t.getInternalName()));
					}

				} else if (n.owner.equals(_Iterator.name)) {
					if (gsR && n.name.equals(_Iterator.next)) {
						insnList.//
							insert(curr, new TypeInsnNode(CHECKCAST, t.getInternalName()));
					}
				} else if (n.owner.equals(_GraphSerializer.name)) {
					String newOwner = null;
					String newDesc = null;
					String newName = null;
					int newAcc = -1;

					if (n.name.equals(_GraphSerializer.write)) {
						newOwner = gs.getInternalName();
						if (gsR) {
							newDesc = Symbols._R_writeDataDesc(t.desc);
							newAcc = INVOKESTATIC;

							if (op) {
								newName = _GraphSerializer._R_.writeData;
							} else {
								newName = _GraphSerializer._R_.write;
							}
						} else {
							if (op) {
								newName = _GraphSerializer.writeData;
							}
						}
					} else if (n.name.equals(_GraphSerializer.read)) {
						newOwner = gs.getInternalName();

						if (gsR) {
							newDesc = Symbols._R_readDataDesc(t.desc);
							newAcc = INVOKESTATIC;

							if (op) {
								newName = _GraphSerializer._R_.readOpaque;
							} else {
								newName = _GraphSerializer._R_.read;
							}
						} else {
							if (op) {
								newName = _GraphSerializer.readOpaque;
							}
						}
					}
					if (newOwner != null) {
						n.owner = newOwner;
					}

					if (newName != null) {
						n.name = newName;
					}

					if (newDesc != null) {
						n.desc = newDesc;
					}

					if (newAcc > 0) {
						n.setOpcode(newAcc);
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
			} else if (curr instanceof IincInsnNode) {
				// offsets of vars in incNodes are always > minimal base
				if (gsR) {
					((IincInsnNode) curr).var--;
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

		super.visit(version, access | ACC_FINAL, targetName, signature, _GraphSerializer.name, interfaces);
	}

	@Override
	public void visitEnd() {

		updateStacks();

		String desc = "L" + targetName + ";";

		FieldVisitor fv = cv.visitField(ACC_STATIC | ACC_PUBLIC | ACC_FINAL, INSTANCE, desc, null, null);
		fv.visitEnd();

		CtorInfo.patchDefaultCtor(cv, _GraphSerializer.name);

		MethodVisitor mv = cv.visitMethod(ACC_STATIC, _Class.ClassInitializer, _Class.NO_ARG_VOID, null, null);
		mv.visitCode();

		if (ct != null) {
			mv.visitLdcInsn(ct.type());
			mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.instantiatorOf, _SerializerFactory.instantiatorOf_D, false);
			mv.visitTypeInsn(CHECKCAST, ctr.getInternalName());
			mv.visitFieldInsn(PUTSTATIC, targetName, _Tags.CSOptimizer.ctr, ctr.getDescriptor());
		}
		if ((fl & ObjectShape.REIFIED_SER) == 0) {
			mv.visitLdcInsn(t.type());
			mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.serializer, _SerializerFactory.serializer_D, false);
			mv.visitTypeInsn(CHECKCAST, gs.getInternalName());
			mv.visitFieldInsn(PUTSTATIC, targetName, _Tags.CSOptimizer.gs, gs.getDescriptor());
		}

		CtorInfo.invokeDefaultCtor(targetName, mv);
		mv.visitFieldInsn(PUTSTATIC, targetName, INSTANCE, desc);

		mv.visitInsn(RETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if (name.equals(_Tags.CSOptimizer.gs)) {
			if ((fl & ObjectShape.REIFIED_SER) != 0) {
				return null;
			}
			return super.visitField(access | ACC_FINAL, name, gs.getDescriptor(), signature, value);
		} else {
			if (ct != null && name.equals(_Tags.CSOptimizer.ctr)) {
				return super.visitField(access | ACC_FINAL, name, ctr.getDescriptor(), signature, value);

			}
		}

		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor rv = null;

		if (i == null && name.startsWith(_GraphSerializer.instantiate)) {
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