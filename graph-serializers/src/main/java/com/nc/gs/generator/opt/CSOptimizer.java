package com.nc.gs.generator.opt;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import symbols.io.abstraction._CollectionSerializer;
import symbols.io.abstraction._GraphSerializer;
import symbols.io.abstraction._GraphSerializer._R_;
import symbols.io.abstraction._Instantiator;
import symbols.io.abstraction._SerializerFactory;
import symbols.java.lang._Class;
import symbols.java.lang._Object;
import symbols.java.util._Collection;
import symbols.java.util._Iterator;
import symbols.java.util._List;

import com.nc.gs.core.GraphClassLoader;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.generator.GenerationStrategy;
import com.nc.gs.interpreter.ExtendedType;
import com.nc.gs.interpreter.Symbols;
import com.nc.gs.log.Log;
import com.nc.gs.util.Utils;

public class CSOptimizer extends ClassVisitor implements Opcodes {

	static final HashMap<String, String> D2R;

	static {
		HashMap<String, String> m = new HashMap<>();
		m.put(_GraphSerializer.write, _R_.write);
		m.put(_GraphSerializer.writeData, _R_.writeData);
		m.put(_GraphSerializer.read, _R_.read);
		m.put(_GraphSerializer.readOpaque, _R_.readOpaque);

		D2R = m;
	}

	public static GraphSerializer gen(Class<?> pojo, Collection<?> sample, boolean nullable, boolean op) {

		String targetIN = Symbols._R_optimizedCollectionName(Type.getInternalName(sample.getClass()), new ExtendedType[]{ ExtendedType.forRuntime(pojo) }, null,false);

		try {
			return (GraphSerializer) Class.forName(targetIN.replace('/', '.')).newInstance();
		} catch (Throwable e) {
			Log.info("Generating opt collection serializer for %s. Cfg: [p:%s,n:%s,op:%s]", sample.getClass().getSimpleName(), pojo.getSimpleName(), nullable, op);
		}

		String res = Symbols.optimizedCollectionResource(nullable, op, sample instanceof RandomAccess);

		try (InputStream is = Utils.streamCode(res)) {
			ClassReader cr = new ClassReader(is);

			ClassWriter cw = GenerationStrategy.newClassWriter();

			CSOptimizer r = new CSOptimizer(pojo, sample, nullable, targetIN, cw);

			cr.accept(r, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

			byte[] bc = cw.toByteArray();

			Utils.writeClass(targetIN, bc);

			return (GraphSerializer) GraphClassLoader.INSTANCE.load(null, bc).newInstance();

		} catch (Exception e) {
			return Utils.rethrow(e);
		}

	}

	String typeIN;
	String typeD;
	String serIN;
	String serD;
	String itrIN;
	String itrD;
	Type colT;
	String colIN;
	String colD;
	String instIN;
	String instD;
	String originalIN;
	String targetIN;
	String writeDesc;
	String readDesc;
	boolean nullable;
	boolean psOpt;
	boolean itrOpt;

	List<MethodNode> mn = new ArrayList<>(3);

	public CSOptimizer(Class<?> pojo, Collection<?> sample, boolean nullable, String targetIN, ClassVisitor cv) {
		super(Opcodes.ASM5, cv);

		Type pt = Type.getType(pojo);
		GraphSerializer gs = SerializerFactory.serializer(pojo);
		Class<?> itrType = sample.iterator().getClass();
		Class<?> instType = SerializerFactory.instantiatorOf(sample.getClass()).getClass();

		boolean synthetic = gs.getClass().isSynthetic();

		this.typeIN = pt.getInternalName();
		this.typeD = pt.getDescriptor();
		this.serIN = Type.getInternalName(gs.getClass());
		this.serD = Type.getDescriptor(gs.getClass());

		this.itrIN = (itrOpt = Modifier.isPublic(itrType.getModifiers())) ? Type.getInternalName(itrType) : _Iterator.name;
		this.itrD = itrOpt ? Type.getDescriptor(itrType) : _Iterator.desc;

		this.colT = Type.getType(sample.getClass());
		this.colIN = colT.getInternalName();
		this.colD = colT.getDescriptor();
		this.instIN = Type.getInternalName(instType);
		this.instD = Type.getDescriptor(instType);
		this.writeDesc = synthetic ? Symbols._R_writeDataDesc(typeD) : _GraphSerializer.write_D;
		this.readDesc = synthetic ? Symbols._R_readDataDesc(typeD) : _GraphSerializer.read_D;

		this.psOpt = synthetic;

		this.targetIN = targetIN;
		this.nullable = nullable;
	}

	MethodVisitor fromNode(MethodNode mv) {
		return cv.visitMethod(mv.access, mv.name, mv.desc, mv.signature, null);
	}

	private void optData(MethodNode node, int gsIx) {
		MethodVisitor mv = fromNode(node);
		mv.visitCode();

		InsnList list = node.instructions;

		AbstractInsnNode n = list.getFirst();
		AbstractInsnNode c = n;

		boolean opt = this.psOpt;
		boolean optItr = this.itrOpt;

		while (c != null) {
			AbstractInsnNode next = null;
			if (c instanceof FieldInsnNode) {
				if (c.getOpcode() == GETSTATIC) {
					if (opt) {
						next = c.getNext();
						list.remove(c);
					} else {
						FieldInsnNode f = (FieldInsnNode) c;

						if (f.desc.equals(_GraphSerializer.desc)) {
							f.desc = serD;
							f.owner = targetIN;
						} else if (f.desc.equals(_Instantiator.desc)) {
							f.desc = instD;
							f.owner = targetIN;
						}

					}
				}
			} else if (c instanceof VarInsnNode) {
				if (opt) {
					VarInsnNode v = (VarInsnNode) c;
					int oc = v.getOpcode();
					if (v.var == gsIx && (oc == ALOAD || oc == ASTORE)) {
						next = c.getNext();
						list.remove(c);
					} else if (v.var >= gsIx) {
						v.var--;
					}
				}
			} else if (c instanceof MethodInsnNode) {
				MethodInsnNode m = (MethodInsnNode) c;
				String name = m.name;
				String desc = m.desc;

				int opcode = m.getOpcode();
				if (opcode == INVOKEVIRTUAL) {

					String target = D2R.get(name);

					if (target != null) {
						if (opt) {
							m.setOpcode(INVOKESTATIC);
							m.name = target;
							m.desc = target.contains(_GraphSerializer.write) ? writeDesc : readDesc;
						}
						m.owner = serIN;
					}
				} else if (opcode == INVOKEINTERFACE) {
					if (name.equals(_Iterator.next)) {
						if (optItr) {
							m.setOpcode(INVOKEVIRTUAL);
							m.itf = false;
							m.owner = itrIN;
						}
						if (opt) {
							TypeInsnNode cast = new TypeInsnNode(CHECKCAST, typeIN);
							list.insert(m, cast);
						}
					} else if (name.equals(_Collection.size) || name.equals(_Collection.add)) {
						m.setOpcode(INVOKEVIRTUAL);
						m.itf = false;
						m.owner = colIN;
					} else if (name.equals(_Collection.iterator)) {
						m.setOpcode(INVOKEVIRTUAL);
						m.itf = false;
						m.owner = colIN;
						if (optItr) {
							TypeInsnNode cast = new TypeInsnNode(CHECKCAST, itrIN);
							list.insert(m, cast);
						}
					} else if (name.equals(_List.get) && desc.equals(_List.get_D)) {
						m.setOpcode(INVOKEVIRTUAL);
						m.itf = false;
						m.owner = colIN;
						if (opt) {
							TypeInsnNode cast = new TypeInsnNode(CHECKCAST, typeIN);
							list.insert(m, cast);
						}
					}
				} else if (opcode == INVOKESTATIC) {
					if (m.owner.equals(originalIN) || m.owner.equals(targetIN)) {
						m.owner = targetIN;
						m.desc = m.desc.replace(_Collection.name, colIN).replace(_List.name, colIN);
						if (itrOpt) {
							m.desc = m.desc.replace(_Iterator.desc, itrD);
						}
					}
				}
			} else if (c instanceof TypeInsnNode) {
				TypeInsnNode t = (TypeInsnNode) c;
				if (t.getOpcode() == CHECKCAST && (t.desc.equals(_Collection.name) || t.desc.equals(_List.name))) {
					t.desc = colIN;
				}
			} else if (c instanceof IincInsnNode) {
				if (opt && gsIx < Integer.MAX_VALUE) {
					((IincInsnNode) c).var--;
				}
			}
			c = next == null ? c.getNext() : next;
		}

		n = list.getFirst();

		while (n != null) {
			n.accept(mv);
			n = n.getNext();
		}

		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private void optInstantiate(MethodNode node) {
		MethodVisitor mv = fromNode(node);
		mv.visitCode();

		InsnList list = node.instructions;

		AbstractInsnNode n = list.getFirst();
		AbstractInsnNode c = n;

		while (c != null) {
			if (c instanceof FieldInsnNode) {
				FieldInsnNode f = (FieldInsnNode) c;
				f.desc = instD;
				f.owner = targetIN;
				break;
			}

			c = c.getNext();
		}

		c = n;
		while (c != null) {
			c.accept(mv);
			c = c.getNext();
		}

		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		originalIN = name;
		super.visit(version, access, targetIN, signature, _GraphSerializer.name, interfaces);
	}

	@Override
	public void visitEnd() {

		Iterator<MethodNode> itr = mn.iterator();

		while (itr.hasNext()) {
			MethodNode node = itr.next();
			switch (node.name) {
			case _GraphSerializer.instantiate:
				optInstantiate(node);
				break;
			case _GraphSerializer.inflateData:
			case _GraphSerializer.writeData:
				optData(node, nullable ? Integer.MAX_VALUE : 4);
				break;
			case _CollectionSerializer.tagAndWrite:
				optData(node, 5);
				break;
			case _CollectionSerializer.untagAndRead:
				optData(node, 6);
				break;
			default:

				break;
			}

			itr.remove();
		}

		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, _Object.init, _Class.NO_ARG_VOID, null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, _GraphSerializer.name, _Object.init, _GraphSerializer.ctor_D, false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		mv = cv.visitMethod(ACC_STATIC, _Class.ClassInitializer, _Class.NO_ARG_VOID, null, null);
		mv.visitCode();

		mv.visitLdcInsn(colT);
		mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.instantiatorOf, _SerializerFactory.instantiatorOf_D, false);
		mv.visitTypeInsn(CHECKCAST, instIN);
		mv.visitFieldInsn(PUTSTATIC, targetIN, "ctr", instD);

		if (!psOpt) {
			mv.visitLdcInsn(Type.getType(typeD));
			mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.serializer, _SerializerFactory.serializer_D, false);
			mv.visitTypeInsn(CHECKCAST, serIN);
			mv.visitFieldInsn(PUTSTATIC, targetIN, "gs", serD);
		}

		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if ("gs".equals(name)) {
			if (!psOpt) {
				super.visitField(access | ACC_FINAL, name, this.serD, signature, value);
			}
			return null;
		}
		return super.visitField(access | ACC_FINAL, name, instD, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (name.equals(_Object.init)) {
			return null;
		}

		String colIN = this.colIN;
		String targetDesc = desc.replace(_Collection.name, colIN).replace(_List.name, colIN);
		String targetSig = signature == null ? null : signature.replace(_Collection.name, colIN).replace(_List.name, colIN);

		if (itrOpt) {
			targetDesc = targetDesc.replace(_Iterator.desc, itrD);
			if (targetSig != null) {
				targetSig = targetSig.replace(_Iterator.name, itrIN);
			}
		}

		MethodNode e = new MethodNode(access, name, targetDesc, targetSig, null);
		mn.add(e);
		return e;
	}

}