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
import static symbols.io.abstraction._Tags.MSOptimizer.INSTANCE;
import static symbols.io.abstraction._Tags.MSOptimizer.KGS_OFF;
import static symbols.io.abstraction._Tags.MSOptimizer.SORTED;
import static symbols.io.abstraction._Tags.MSOptimizer.TEMPLATE;
import static symbols.io.abstraction._Tags.MSOptimizer.VGS_OFF;

import java.io.InputStream;
import java.util.Map;
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
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import symbols.io.abstraction._GraphSerializer;
import symbols.io.abstraction._MapSerializer;
import symbols.io.abstraction._SerializerFactory;
import symbols.io.abstraction._Tags;
import symbols.io.abstraction._Tags.ObjectShape;
import symbols.java.lang._Class;
import symbols.java.util._Map;

import com.nc.gs.core.GraphClassLoader;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.Instantiator;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.interpreter.CtorInfo;
import com.nc.gs.interpreter.ExtendedType;
import com.nc.gs.interpreter.Hierarchy;
import com.nc.gs.interpreter.MapShape;
import com.nc.gs.interpreter.Shape;
import com.nc.gs.interpreter.Symbols;
import com.nc.gs.interpreter.VisitationContext;
import com.nc.gs.log.Log;
import com.nc.gs.util.Utils;

public class SimpleMSOptmizer extends ClassVisitor {

	private static GraphSerializer opt(String iN, Class<?> mapType, Class<?> keyType, Shape ks, Class<?> valType, Shape vs, boolean rep) {

		mapType = Utils.nullIfNotConcrete(mapType);

		String targetName = iN != null ? iN : Symbols._R_optimizedMapName(mapType == null ? null : mapType.getName(), ks, vs, rep);

		try {
			return (GraphSerializer) Class.forName(targetName.replace('/', '.')).getDeclaredField(INSTANCE).get(null);
		} catch (Exception e) {
			Log.info("Generating %s", targetName);
		}

		String resource = TEMPLATE;

		try (VisitationContext vc = VisitationContext.current()) {
			int kSuf = Utils.asInt(ks.canBeNull());
			int vSuf = Utils.asInt(vs.canBeNull());

			String methodSuffix = String.format("%dX%dX", kSuf, vSuf);

			GraphSerializer kgs = SerializerFactory.serializer(keyType);
			GraphSerializer vgs = SerializerFactory.serializer(valType);
			Instantiator ctor = mapType == null ? null : SerializerFactory.instantiatorOf(mapType);

			ExtendedType kt = ExtendedType.forRuntime(keyType);

			ExtendedType vt = ExtendedType.forRuntime(valType);

			ExtendedType mt = mapType == null ? null : ExtendedType.forRuntime(mapType);

			try (InputStream is = Utils.streamCode(resource)) {
				ClassReader cr = new ClassReader(is);

				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

				SimpleMSOptmizer opt = new SimpleMSOptmizer(cw);
				opt.targetName = targetName;
				opt.suffix = methodSuffix;
				opt.kgs = Type.getType(kgs.getClass());
				opt.vgs = Type.getType(vgs.getClass());
				opt.ctr = mapType == null ? null : Type.getType(ctor.getClass());
				opt.kt = kt;
				opt.vt = vt;
				opt.mt = mt;
				opt.kfl = (kgs.getClass().isSynthetic() ? ObjectShape.REIFIED_SER : 0) | ks.k | (mapType != null && SortedMap.class.isAssignableFrom(mapType) ? ObjectShape.SORTED_MAP : 0);
				opt.vfl = (vgs.getClass().isSynthetic() ? ObjectShape.REIFIED_SER : 0) | vs.k | (mapType != null && SortedMap.class.isAssignableFrom(mapType) ? ObjectShape.SORTED_MAP : 0);
				opt.implForReplacement = rep;

				cr.accept(opt, ClassReader.SKIP_DEBUG);

				byte[] bc = cw.toByteArray();

				Utils.writeClass(targetName.replace("[]", "_"), bc);

				return (GraphSerializer) GraphClassLoader.INSTANCE.load(null, bc).newInstance();
			}

		} catch (Exception e) {
			return Utils.rethrow(e);
		}
	}

	public static GraphSerializer optimized(String iN, Class<? extends Map<?, ?>> mapType, MapShape ms) {
		Shape ks = ms.ks;
		Shape vs = ms.vs;
		return opt(iN, mapType, ks.hierarchyTypes()[0], ks, vs.hierarchyTypes()[0], vs, ms.rep);
	}

	public static GraphSerializer rawOptimized(Class<?> mapType, Class<?> keyType, Shape ks, Class<?> valType, Shape vs) {

		if (ks.state == null) {
			ks.state = new Hierarchy(keyType, new Class[]{ keyType }, true);
		}

		if (vs.state == null) {
			vs.state = new Hierarchy(valType, new Class[]{ valType }, true);
		}

		return opt(null, mapType, keyType, ks, valType, vs, false);
	}

	Type kgs;

	Type vgs;

	Type ctr;

	ExtendedType kt;

	ExtendedType vt;

	ExtendedType mt;

	MethodNode r;

	MethodNode w;

	MethodNode i;

	int kfl;

	int vfl;

	boolean implForReplacement;

	String targetName;

	String suffix;

	public SimpleMSOptmizer(ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
	}

	private void changeInstantiate(MethodNode mn) {
		if (mt == null) {
			return;
		}

		InsnList insnList = mn.instructions;

		AbstractInsnNode curr = insnList.getFirst();

		while (curr != null) {
			if (curr instanceof FieldInsnNode) {
				FieldInsnNode n = (FieldInsnNode) curr;
				n.desc = ctr.getDescriptor();
				n.owner = targetName;
			}
			curr = curr.getNext();
		}
	}

	private void changeRW(MethodNode mn) {

		boolean mapKnown = mt != null;
		boolean kgsR = (kfl & ObjectShape.REIFIED_SER) != 0;
		boolean vgsR = (vfl & ObjectShape.REIFIED_SER) != 0;
		boolean kOp = (kfl & ObjectShape.ONLY_PAYLOAD) != 0;
		boolean vOp = (vfl & ObjectShape.ONLY_PAYLOAD) != 0;
		boolean useConcInsn = mapKnown && !implForReplacement;

		int decr = (kgsR ? 1 : 0) + (vgsR ? 1 : 0);

		InsnList insnList = mn.instructions;

		AbstractInsnNode curr = insnList.getFirst();

		while (curr != null) {
			AbstractInsnNode next = null;

			if (curr instanceof TypeInsnNode) {
				TypeInsnNode n = (TypeInsnNode) curr;
				if (useConcInsn && n.getOpcode() == CHECKCAST && n.desc.equals(_Map.name)) {
					n.desc = mt.getInternalName();
				}
			} else if (curr instanceof VarInsnNode) {
				VarInsnNode n = (VarInsnNode) curr;

				if ((kgsR && n.var == KGS_OFF) || (vgsR && n.var == VGS_OFF)) {
					next = curr.getNext();
					insnList.remove(curr);
				} else {
					if ((n.var - decr) >= KGS_OFF) {// don't shift locals below
													// minimum offset (4)
						n.var -= decr;
					}
				}
			} else if (curr instanceof FieldInsnNode) {
				FieldInsnNode n = (FieldInsnNode) curr;

				if (n.getOpcode() == GETSTATIC) {
					boolean remove = false;
					String newDesc = null;

					if (n.name.equals(_Tags.MSOptimizer.kgs)) {
						if (kgsR) {
							remove = true;
						} else {
							newDesc = kgs.getDescriptor();
						}
					} else if (n.name.equals(_Tags.MSOptimizer.vgs)) {
						if (vgsR) {
							remove = true;
						} else {
							newDesc = vgs.getDescriptor();
						}
					} else if (mapKnown && n.name.equals(_Tags.MSOptimizer.ctr)) {
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

				if (n.owner.equals(_MapSerializer.name)) {
					if (mapKnown) {
						if ((kfl & SORTED) == 0) {
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
				} else if (n.owner.equals(_Map.name)) {
					if (useConcInsn) {
						n.owner = mt.name;
						n.itf = false;
						n.setOpcode(INVOKEVIRTUAL);
					}
				} else if (n.owner.equals(_Map.Entry.name)) {
					if (kgsR && n.name.equals(_Map.Entry.getKey)) {
						insnList.insert(curr, new TypeInsnNode(CHECKCAST, kt.getInternalName()));
					} else if (vgsR && n.name.equals(_Map.Entry.getValue)) {
						insnList.insert(curr, new TypeInsnNode(CHECKCAST, vt.getInternalName()));
					}
				} else if (n.owner.equals(_GraphSerializer.name)) {
					String newOwner = null;
					String newDesc = null;
					String newName = null;
					int newAcc = -1;

					if (n.name.equals(_GraphSerializer.write)) {
						newOwner = kgs.getInternalName();
						if (kgsR) {
							newDesc = Symbols._R_writeDataDesc(kt.desc);
							newAcc = INVOKESTATIC;

							if (kOp) {
								newName = _GraphSerializer._R_.writeData;
							} else {
								newName = _GraphSerializer._R_.write;
							}
						} else {
							if (kOp) {
								newName = _GraphSerializer.writeData;
							}
						}
					} else if (n.name.equals(_GraphSerializer.writeData)) {

						newOwner = vgs.getInternalName();
						if (vgsR) {
							newDesc = Symbols._R_writeDataDesc(vt.desc);
							newAcc = INVOKESTATIC;

							if (vOp) {
								newName = _GraphSerializer._R_.writeData;
							} else {
								newName = _GraphSerializer._R_.write;
							}
						} else {
							if (vOp) {
								newName = _GraphSerializer.writeData;
							}
						}

					} else if (n.name.equals(_GraphSerializer.read)) {
						newOwner = kgs.getInternalName();

						if (kgsR) {
							newDesc = Symbols._R_readDataDesc(kt.desc);
							newAcc = INVOKESTATIC;

							if (kOp) {
								newName = _GraphSerializer._R_.readOpaque;
							} else {
								newName = _GraphSerializer._R_.read;
							}
						} else {
							if (kOp) {
								newName = _GraphSerializer.readOpaque;
							}
						}
					} else if (n.name.equals(_GraphSerializer.readOpaque)) {
						newOwner = vgs.getInternalName();

						if (vgsR) {
							newDesc = Symbols._R_readDataDesc(vt.desc);
							newAcc = INVOKESTATIC;
							if (vOp) {
								newName = _GraphSerializer._R_.readOpaque;
							} else {
								newName = _GraphSerializer._R_.read;
							}
						} else {
							if (vOp) {
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
				}
			} else if (curr instanceof IincInsnNode) {
				// offsets of vars in incNodes are always > minimal base
				((IincInsnNode) curr).var -= decr;
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

		super.visit(version, access, targetName, signature, _GraphSerializer.name, interfaces);
	}

	@Override
	public void visitEnd() {

		updateStacks();

		CtorInfo.patchDefaultCtor(cv, _GraphSerializer.name);

		String desc = "L" + targetName + ";";

		FieldVisitor fv = cv.visitField(ACC_STATIC | ACC_PUBLIC | ACC_FINAL, INSTANCE, desc, null, null);
		fv.visitEnd();

		MethodVisitor mv = cv.visitMethod(ACC_STATIC, _Class.ClassInitializer, _Class.NO_ARG_VOID, null, null);
		mv.visitCode();

		if (mt != null) {
			mv.visitLdcInsn(mt.type());
			mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.instantiatorOf, _SerializerFactory.instantiatorOf_D, false);
			mv.visitTypeInsn(CHECKCAST, ctr.getInternalName());
			mv.visitFieldInsn(PUTSTATIC, targetName, _Tags.MSOptimizer.ctr, ctr.getDescriptor());
		}

		if ((kfl & ObjectShape.REIFIED_SER) == 0) {
			mv.visitLdcInsn(kt.type());
			mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.serializer, _SerializerFactory.serializer_D, false);
			mv.visitTypeInsn(CHECKCAST, kgs.getInternalName());
			mv.visitFieldInsn(PUTSTATIC, targetName, _Tags.MSOptimizer.kgs, kgs.getDescriptor());
		}

		if ((vfl & ObjectShape.REIFIED_SER) == 0) {
			mv.visitLdcInsn(vt.type());
			mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.serializer, _SerializerFactory.serializer_D, false);
			mv.visitTypeInsn(CHECKCAST, vgs.getInternalName());
			mv.visitFieldInsn(PUTSTATIC, targetName, _Tags.MSOptimizer.vgs, vgs.getDescriptor());
		}

		CtorInfo.invokeDefaultCtor(targetName, mv);
		mv.visitFieldInsn(PUTSTATIC, targetName, INSTANCE, desc);

		mv.visitInsn(RETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if (name.equals(_Tags.MSOptimizer.vgs)) {
			if ((vfl & ObjectShape.REIFIED_SER) != 0) {
				return null;
			}
			return super.visitField(access | ACC_FINAL, name, vgs.getDescriptor(), signature, value);
		} else if (name.equals(_Tags.MSOptimizer.kgs)) {
			if ((kfl & ObjectShape.REIFIED_SER) != 0) {
				return null;
			}
			return super.visitField(access | ACC_FINAL, name, kgs.getDescriptor(), signature, value);
		} else {
			if (ctr != null) {
				return super.visitField(access | ACC_FINAL, name, ctr.getDescriptor(), signature, value);
			}
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
				if ((kfl & SORTED) != 0) {
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