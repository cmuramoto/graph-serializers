package com.nc.gs.generator;

import static org.objectweb.asm.Opcodes.ACC_BRIDGE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.nc.gs.core.GraphClassLoader;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.interpreter.ClassInfo;
import com.nc.gs.interpreter.ExtendedType;
import com.nc.gs.interpreter.FieldTrap;
import com.nc.gs.interpreter.Symbols;
import com.nc.gs.interpreter.VisitationContext;
import com.nc.gs.serializers.java.util.CollectionSerializer;
import com.nc.gs.util.Utils;

import symbols.io.abstraction._Context;
import symbols.io.abstraction._GraphSerializer;
import symbols.io.abstraction._GraphSerializer._R_;
import symbols.io.abstraction._SerializerFactory;
import symbols.io.abstraction._Sink;
import symbols.io.abstraction._Source;
import symbols.java.lang._Class;
import symbols.java.util._Collection;
import symbols.java.util._Iterator;

public class Reifier {

	static class CollectionBody extends ClassVisitor {

		class TypeBinder extends MethodVisitor {

			int fc;

			public TypeBinder(MethodVisitor mv) {
				super(Opcodes.ASM5, mv);
			}

			@Override
			public void visitFieldInsn(int opcode, String owner, String name, String desc) {
				String ow = owner;

				if (ow.equals(origName)) {
					ow = targetName;
				}
				super.visitFieldInsn(opcode, ow, name, desc.replace(_GraphSerializer.desc, gsDesc));
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {

				if (opcode == INVOKESTATIC) {
					super.visitMethodInsn(opcode, owner, name, desc, itf);
					if (owner.equals(_SerializerFactory.name)) {
						super.visitTypeInsn(CHECKCAST, gsName);
					}
					return;
				}

				if (opcode == INVOKESPECIAL && _Class.ctor.equals(name)) {
					super.visitMethodInsn(opcode, owner, name, desc, itf);
					return;
				}

				String mappedDesc = desc.replace(_Iterator.desc, itrType.desc).replace(_Collection.desc, colType.desc);

				if (!name.equals(_Class.ctor)) {
					mappedDesc = mappedDesc.replace(_GraphSerializer.desc, gsDesc);
				}

				String ow = owner;

				if (ow.equals(origName)) {
					ow = targetName;
				}

				if (opcode == INVOKEINTERFACE) {
					if (owner.equals(_Iterator.name)) {
						super.visitMethodInsn(INVOKEVIRTUAL, itrType.name, name, mappedDesc, false);
						return;
					} else if (owner.equals(_Collection.name)) {
						super.visitMethodInsn(INVOKEVIRTUAL, colType.name, name, desc, false);
						if (name.equals(_Collection.iterator)) {
							visitTypeInsn(CHECKCAST, itrType.name);
						}
						return;
					}
				} else if (opcode == INVOKEVIRTUAL) {
					if (owner.equals(_GraphSerializer.name)) {
						super.visitMethodInsn(opcode, gsName, name, desc, itf);
						return;
					}
				}

				super.visitMethodInsn(opcode, ow, name, mappedDesc, itf);
			}

			@Override
			public void visitTypeInsn(int opcode, String type) {
				if (opcode == CHECKCAST && type.equals(_Collection.name)) {
					super.visitTypeInsn(opcode, colType.name);
				} else {
					super.visitTypeInsn(opcode, type);
				}
			}

		}

		String gsName;

		String gsDesc;

		ExtendedType pojo;

		ExtendedType colType;

		ExtendedType itrType;

		String targetName;

		String origName;

		boolean reifySerializerCalls;

		boolean nullabe;

		boolean op;

		public CollectionBody(Class<?> gs, ExtendedType pojo, ExtendedType colType, ExtendedType itrType, boolean reifySerializerCalls, boolean nullabe, boolean op, ClassVisitor cv) {
			super(Opcodes.ASM5, cv);
			Type gst = Type.getType(gs);
			gsName = gst.getInternalName();
			gsDesc = gst.getDescriptor();
			this.pojo = pojo;
			this.colType = colType;
			this.itrType = itrType;
			this.reifySerializerCalls = reifySerializerCalls;
			this.nullabe = nullabe;
			this.op = op;
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			origName = name;
			targetName = Symbols._R_collectionSerializer(pojo.name);

			super.visit(version, access, targetName, signature, superName, interfaces);
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {

			return /* reifySerializerCalls ? null : */super.visitField(access, name, desc.replace(_GraphSerializer.desc, gsDesc), signature, value);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

			String mappedDesc = desc.replace(_Collection.desc, colType.desc).replace(_GraphSerializer.desc, gsDesc);

			if (itrType != null) {
				mappedDesc = mappedDesc.replace(_Iterator.desc, itrType.desc);
			}

			String mappedSig = signature == null ? null : signature.replace(_Collection.name, colType.name);

			if (itrType != null && mappedSig != null) {
				mappedSig = mappedSig.replace(_Iterator.name, itrType.name);
			}

			// boolean nulls = nullabe;
			// if (nulls && (name.equals("writeEls") || name.equals("readEls")))
			// {
			// return null;
			// }
			//
			// if (!nulls
			// && (name.equals("writeBitMask") || name
			// .equals("readBitMask"))) {
			// return null;
			// }

			return new TypeBinder(super.visitMethod(access, name, mappedDesc, mappedSig, exceptions));
		}

	}

	static class PojoBody extends ClassVisitor {

		List<FieldNode> fields = new ArrayList<>();

		List<MethodNode> methods = new ArrayList<>();

		MethodNode write;

		MethodNode read;

		MethodNode instantiate;

		ExtendedType t;

		MethodInsnNode lastInvocation;

		TypeInsnNode lastTypeInsn;

		boolean inflateOverride;

		public PojoBody(Class<? extends GraphSerializer> impl) {
			super(Opcodes.ASM5);
			t = ExtendedType.forRuntime(impl);
		}

		public PojoBody(String impl) {
			super(Opcodes.ASM5);
			t = ExtendedType.forInternalName(impl, false);
		}

		public void adapt(ClassVisitor cv, ExtendedType ser, ExtendedType type) {
			adaptStaticMethods(cv, ser);
			adaptStaticFields(cv, ser);

			reifyWrite(cv, ser, type);
			reifyInflate(cv, ser, type);
			reifyInstantiate(cv, ser, type);
			reifyIntern(cv, type.type(), ser.name);
			reifyUnintern(cv, type.type(), ser.name, inflateOverride);
		}

		private void adaptStaticFields(ClassVisitor cv, ExtendedType nt) {
			List<FieldNode> fields = this.fields;

			for (FieldNode fn : fields) {
				if (fn.desc.equals(t.desc)) {
					fn.desc = nt.desc;
				}

				fn.accept(cv);
			}

		}

		public void adaptStaticMethods(ClassVisitor cv, ExtendedType ser) {
			for (MethodNode m : methods) {
				MethodVisitor mv = cv.visitMethod(m.access, m.name, m.desc, m.signature, null);
				mv.visitCode();

				AbstractInsnNode node = m.instructions.getFirst();

				while (node != null) {
					remap(node, ser, false, false);

					node.accept(mv);
					node = node.getNext();
				}

				mv.visitMaxs(0, 0);
				mv.visitEnd();
			}
		}

		boolean isReturnTypeExpected(ExtendedType type) {
			TypeInsnNode ctor = lastTypeInsn;
			if (ctor != null && ctor.getOpcode() == NEW && ctor.desc.equals(type.name)) {
				return true;
			}

			MethodInsnNode factory = lastInvocation;

			if (factory != null && Type.getReturnType(factory.desc).getDescriptor().equals(type.desc)) {
				return true;
			}

			return false;
		}

		private void reifyInflate(ClassVisitor cv, ExtendedType ser, ExtendedType type) {

			MethodNode w = read;

			if (w != null) {
				InsnList insnList = w.instructions;

				if (insnList != null) {
					MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, _R_.inflateData, reifiedReadPayloadDesc(type), null, null);
					mv.visitCode();

					AbstractInsnNode node = insnList.getFirst();

					while (node != null) {
						remap(node, ser, true, false).accept(mv);
						node = node.getNext();
					}
					mv.visitMaxs(0, 0);
					mv.visitEnd();

					inflateOverride = true;
				}
			}
		}

		private void reifyInstantiate(ClassVisitor cv, ExtendedType ser, ExtendedType type) {

			MethodNode w = instantiate;

			MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_FINAL, _GraphSerializer.instantiate, _GraphSerializer.instantiate_D, null, null);
			mv.visitCode();

			AbstractInsnNode node = w.instructions.getFirst();

			while (node != null) {
				remap(node, ser, false, false).accept(mv);
				node = node.getNext();
			}

			mv.visitMaxs(0, 0);
			mv.visitEnd();

			mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, _R_.instantiate, Symbols.reifiedInstantiateDesc(type.desc), null, null);
			mv.visitCode();

			node = w.instructions.getFirst();

			AbstractInsnNode next;

			while (node != null) {
				next = node.getNext();
				if (next == null) {
					if (!isReturnTypeExpected(type)) {
						new TypeInsnNode(CHECKCAST, type.name).accept(mv);
					}
				}
				remap(node, ser, true, true).accept(mv);
				node = next;
			}

			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}

		private void reifyWrite(ClassVisitor cv, ExtendedType ser, ExtendedType type) {
			MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, _R_.writeData, reifiedWritePayloadDesc(type), null, null);
			mv.visitCode();

			MethodNode w = write;

			AbstractInsnNode node = w.instructions.getFirst();

			while (node != null) {
				remap(node, ser, true, false).accept(mv);
				node = node.getNext();
			}

			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}

		private AbstractInsnNode remap(AbstractInsnNode node, ExtendedType nt, boolean virtualToStatic, boolean trapLast) {

			if (node instanceof FieldInsnNode) {
				FieldInsnNode fn = (FieldInsnNode) node;
				if (fn.owner.equals(t.name)) {
					fn.owner = nt.name;
				}
				if (fn.desc.equals(t.desc)) {
					fn.desc = nt.desc;
				}
			} else if (node instanceof MethodInsnNode) {
				MethodInsnNode mn = (MethodInsnNode) node;

				if (trapLast) {
					lastInvocation = new MethodInsnNode(mn.getOpcode(), mn.owner, mn.name, mn.desc, mn.itf);
				}

				if (mn.owner.equals(t.name)) {
					mn.owner = nt.name;
				}

				if (mn.desc.contains(t.desc)) {
					mn.desc = mn.desc.replace(t.desc, nt.desc);
				}
			} else if (node instanceof TypeInsnNode) {
				TypeInsnNode tn = (TypeInsnNode) node;

				if (trapLast) {
					lastTypeInsn = new TypeInsnNode(tn.getOpcode(), tn.desc);
				}

				if (tn.desc.equals(t.name)) {
					tn.desc = nt.name;
				}

			} else if (node instanceof LdcInsnNode) {
				LdcInsnNode ldc = (LdcInsnNode) node;
				if (ldc.cst != null && ldc.cst.equals(t.type())) {
					ldc.cst = nt.type();
				}
			} else if (virtualToStatic && node instanceof VarInsnNode) {
				VarInsnNode v = (VarInsnNode) node;

				if (v.var > 0) {
					v.var--;
					// throw new UnsupportedOperationException();
				}
			}

			return node;
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
			if ((access & ACC_STATIC) != 0) {
				fields.add(new FieldNode(access, name, desc, signature, value));
			}

			return null;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

			if ((access & ACC_BRIDGE) != 0) {
				return null;
			}

			MethodNode node = new MethodNode(access, name, desc, signature, null);
			switch (name) {
			case _GraphSerializer.writeData:
				write = node;
				break;
			case _GraphSerializer.inflateData:
				read = node;
				break;
			case _GraphSerializer.instantiate:
				instantiate = node;
				break;
			default:
				if ((access & ACC_STATIC) != 0) {
					methods.add(node);
				}
				break;
			}

			return node;
		}

	}

	static class Shell extends ClassVisitor {

		class InstantiateAdapter extends MethodVisitor {

			public InstantiateAdapter(MethodVisitor mv) {
				super(Opcodes.ASM5, mv);
			}

			@Override
			public void visitCode() {
				visitTypeInsn(NEW, root.getName());
				visitInsn(DUP);
				visitMethodInsn(INVOKESPECIAL, root.getName(), _Class.ctor, _Class.NO_ARG_VOID, false);
				visitInsn(ARETURN);
				visitMaxs(0, 0);
			}

			@Override
			public void visitEnd() {
				visitCode();

				MethodVisitor mv = cv.visitMethod(ACC_PUBLIC + ACC_FINAL, _GraphSerializer.instantiate, _GraphSerializer.instantiate_D, null, null);
				mv.visitTypeInsn(NEW, root.getName());
				mv.visitInsn(DUP);
				mv.visitMethodInsn(INVOKESPECIAL, root.getName(), _Class.ctor, _Class.NO_ARG_VOID, false);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(0, 0);
				mv.visitEnd();

				super.visitEnd();
			}
		}

		public static String reifiedWritePayloadDesc(Type t) {
			return String.format("(%s%s%s)V", _Context.desc, _Sink.desc, t.getDescriptor());
		}

		GenerationStrategy strategy = GenerationStrategy.FULL_HIERARCHY;

		Class<?> pojo;

		ClassInfo root;

		ExtendedType ser;

		String targetName;

		String targetDesc;

		String targetSuperName;

		String targetSuperDesc;

		PojoBody b;

		MethodNode inflate;

		MethodNode read;

		MethodNode readData;

		public Shell(Class<?> pojo, PojoBody b, ClassVisitor cv, String targetName) throws IOException {
			super(Opcodes.ASM5, cv);
			this.pojo = pojo;
			root = ClassInfo.getInfo(pojo, FieldTrap.DEFAULT, false);

			this.b = b;

			// targetName = GenerationStrategy.prefixForSerializer(root.getName()) +
			// _SerializerFactory.genClassSuffix;
			this.targetName = targetName;
			targetDesc = "L" + targetName + ";";

			targetSuperName = _GraphSerializer.name;
			targetSuperDesc = _GraphSerializer.desc;
		}

		private void adaptRead(MethodNode mn, boolean ov) {
			InsnList list = mn.instructions;

			LinkedList<AbstractInsnNode> toRem = new LinkedList<AbstractInsnNode>();

			AbstractInsnNode node = list.getFirst();

			AbstractInsnNode curr = node;

			while (curr != null) {

				if (curr instanceof MethodInsnNode) {
					MethodInsnNode min = (MethodInsnNode) curr;

					switch (min.owner) {
					case _GraphSerializer.name:
						min.setOpcode(INVOKESTATIC);
						min.owner = targetName;
						if (min.name.equals(_GraphSerializer.inflateData)) {
							if (ov) {
								min.name = _R_.inflateData;
								min.desc = Symbols.reifiedInflateDataDesc(root.type());
							} else {
								toRem.add(curr);
								toRem.add(curr.getPrevious());
								toRem.add(curr.getPrevious().getPrevious());
								toRem.add(curr.getPrevious().getPrevious().getPrevious());
							}
						} else if (min.name.equals(_GraphSerializer.instantiate)) {
							min.name = _R_.instantiate;
							min.desc = Symbols.reifiedInstantiateDesc(root.basic().desc);
						}

						break;
					case _Context.name:
						if (min.name.equals(_Context.from)) {
							list.insert(curr, new TypeInsnNode(CHECKCAST, root.getName()));
						}
					default:
						break;
					}

				} else if (curr instanceof VarInsnNode) {
					VarInsnNode v = (VarInsnNode) curr;
					if (v.var == 0) {
						toRem.add(v);
					} else {
						v.var--;
					}
				} else if (curr instanceof FrameNode) {
					toRem.add(curr);
				}

				curr = curr.getNext();
			}

			for (AbstractInsnNode n : toRem) {
				list.remove(n);
			}

			curr = list.getFirst();

			MethodVisitor mv = cv.visitMethod(mn.access, mn.name, mn.desc, mn.signature, null);
			mv.visitCode();

			while (curr != null) {
				curr.accept(mv);
				curr = curr.getNext();
			}

			mv.visitMaxs(0, 0);
			mv.visitEnd();

		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

			try (VisitationContext vc = VisitationContext.current()) {
				ser = new ExtendedType(ACC_PUBLIC + ACC_FINAL, targetName, targetSuperName, b.t.signature, interfaces);
				vc.visited(ser);
			}

			super.visit(version, ACC_PUBLIC + ACC_FINAL + ACC_SYNTHETIC, targetName, b.t.signature, targetSuperName, interfaces);
		}

		@Override
		public void visitEnd() {
			MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", _Class.NO_ARG_VOID, null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			// mv.visitLdcInsn(Bootstrap.getClassTableImpl().id(pojo));
			mv.visitMethodInsn(INVOKESPECIAL, targetSuperName, "<init>", _GraphSerializer.ctor_D, false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();

			b.adapt(cv, ser, root.basic());

			if (b.inflateOverride) {
				mv = new DelegatingToReifiedMV(cv.visitMethod(inflate.access, inflate.name, inflate.desc, inflate.signature, null), false, root, _R_.inflateData, targetName);
				mv.visitEnd();
			}

			adaptRead(read, b.inflateOverride);
			adaptRead(readData, b.inflateOverride);

		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
			return null;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			// return switchVisit(root, name, strategy, cv, access, name, desc,
			// signature, exceptions,
			// Collections.singleton(core.GraphSerializer.instantiate));
			switch (name) {
			case _GraphSerializer.inflateData:
				inflate = new MethodNode(ACC_PUBLIC | ACC_FINAL, name, desc, signature, exceptions);
				return null;
			case _GraphSerializer.writeData:
				boolean abs = (access & Opcodes.ACC_ABSTRACT) != 0;
				return new DelegatingToReifiedMV(super.visitMethod(ACC_PUBLIC + ACC_FINAL, name, desc, signature, exceptions), abs, root, _R_.writeData, targetName);

			// boolean abs = (access & Opcodes.ACC_ABSTRACT) != 0;
			// MethodVisitor mv = new DelegatingToReifiedMV(
			// super.visitMethod(ACC_PUBLIC + ACC_FINAL, name, desc,
			// signature, exceptions),
			// abs,
			// root,
			// name.equals(_GraphSerializer.inflateData) ? _R_.inflateData
			// : _R_.writeData, targetName);
			// if (name.equals(_GraphSerializer.inflateData)) {
			// inflate = mv;
			// return null;
			// }
			// return mv;

			case _GraphSerializer.readOpaque:
				return readData = new MethodNode(ACC_PUBLIC | ACC_STATIC, _R_.PREFIX + name, Symbols._R_readDataDesc(root.basic().desc), signature, exceptions);
			case _GraphSerializer.read:
				return read = new MethodNode(ACC_PUBLIC | ACC_STATIC, _R_.PREFIX + name, Symbols._R_readDataDesc(root.basic().desc), signature, exceptions);
			case _GraphSerializer.write:
				String mn = _R_.PREFIX + name;
				String dsc = Symbols._R_writeDataDesc(root.basic().desc);

				return new MethodReifier(root, targetName, true, super.visitMethod(ACC_PUBLIC + ACC_STATIC, mn, dsc, signature, exceptions));

			// boolean isWrite = _GraphSerializer.write_D.equals(desc);
			//
			// String mn = _R_.PREFIX + name;
			//
			// String dsc = isWrite ? Symbols
			// ._R_writeDataDesc(root.basic().desc) : Symbols
			// ._R_readDataDesc(root.basic().desc);
			//
			// return new MethodReifier(root, targetName, isWrite,
			// super.visitMethod(ACC_PUBLIC + ACC_STATIC, mn, dsc,
			// signature, exceptions));
			default:
				break;
			}
			return null;
		}
	}

	static String reifiedReadPayloadDesc(ExtendedType t) {
		return String.format("(%s%s%s)V", _Context.desc, _Source.desc, t.desc);
	}

	static String reifiedWritePayloadDesc(ExtendedType t) {
		return String.format("(%s%s%s)V", _Context.desc, _Sink.desc, t.desc);
	}

	public static GraphSerializer reify(Class<?> pojo, Class<? extends GraphSerializer> ser) throws IOException {
		ClassInfo root = ClassInfo.getInfo(pojo, FieldTrap.DEFAULT, false);

		String targetName = GenerationStrategy.prefixForSerializer(root.getName()) + _SerializerFactory.genClassSuffix;

		return reify(pojo, ser, targetName);
	}

	public static GraphSerializer reify(Class<?> pojo, Class<? extends GraphSerializer> ser, String targetName) throws IOException {
		PojoBody b;

		try (VisitationContext c = VisitationContext.current()) {

			try {
				return (GraphSerializer) Class.forName(targetName.replace('/', '.')).newInstance();
			} catch (ClassNotFoundException e) {
				// ignore
			} catch (ReflectiveOperationException e) {
				return Utils.rethrow(e);
			}

			try (InputStream is = Utils.streamCode(ser)) {
				ClassReader cr = new ClassReader(is);

				b = new PojoBody(ser);

				cr.accept(b, ClassReader.SKIP_DEBUG);
			}

			try (InputStream gis = Utils.streamCode(GraphSerializer.class)) {
				ClassReader cr = new ClassReader(gis);

				ClassWriter cw = GenerationStrategy.newClassWriter();

				Shell shell = new Shell(pojo, b, cw, targetName);

				cr.accept(shell, ClassReader.SKIP_DEBUG);

				byte[] bc = cw.toByteArray();

				Utils.writeClass(shell.targetName, bc);

				Class<?> load = GraphClassLoader.INSTANCE.load(null, bc);

				return (GraphSerializer) load.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (Throwable e) {
				try {
					return (GraphSerializer) Class.forName(targetName.replace('/', '.')).newInstance();
				} catch (Throwable err) {
					return Utils.rethrow(e);
				}
			}
		}

	}

	public static GraphSerializer reify(Class<?> pojo, String ser) throws IOException {

		PojoBody b;

		try (VisitationContext c = VisitationContext.current()) {

			try (InputStream is = Utils.streamAnyCode(ser)) {
				ClassReader cr = new ClassReader(is);
				b = new PojoBody(ser);

				cr.accept(b, ClassReader.SKIP_DEBUG);
			}

			try (InputStream gis = Utils.streamCode(GraphSerializer.class)) {
				ClassReader cr = new ClassReader(gis);

				ClassWriter cw = GenerationStrategy.newClassWriter();

				Shell shell = new Shell(pojo, b, cw, GenerationStrategy.prefixForSerializer(pojo.getName()) + _SerializerFactory.genClassSuffix);

				cr.accept(shell, ClassReader.SKIP_DEBUG);

				byte[] bc = cw.toByteArray();

				Utils.writeClass(shell.targetName, bc);

				Class<?> load = GraphClassLoader.INSTANCE.load(null, bc);

				return (GraphSerializer) load.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

	}

	@SuppressWarnings("unchecked")
	public static GraphSerializer reifyCollectionSerializer(Class<?> pojo, Collection<?> sample, boolean nullable, boolean op) {

		ClassLoader cl = sample.getClass().getClassLoader();

		boolean reifyIteratorAccess = cl == GraphSerializer.class.getClassLoader();

		boolean reifySerializerCalls = SerializerFactory.serializer(pojo).getClass().isSynthetic();

		// nothing to do!
		if (!reifyIteratorAccess && !reifySerializerCalls) {
			return new CollectionSerializer((Class<? extends Collection<?>>) sample.getClass(), pojo, op, nullable);
		}

		try (VisitationContext vc = VisitationContext.current()) {
			Class<?> clazz = sample.getClass();
			Class<?> itrClazz = sample.iterator().getClass();

			ExtendedType root = ExtendedType.forRuntime(pojo);
			ExtendedType colType = ExtendedType.forRuntime(clazz);
			ExtendedType itrType = reifyIteratorAccess ? ExtendedType.forRuntime(itrClazz) : null;

			try (InputStream is = Utils.streamCode(CollectionSerializer.class)) {
				ClassReader cr = new ClassReader(is);

				ClassWriter cw = GenerationStrategy.newClassWriter();
				// ClassWriter cw = new ClassWriter(0);
				CollectionBody cv = new CollectionBody(SerializerFactory.serializer(pojo).getClass(), root, colType, itrType, reifySerializerCalls, nullable, op, cw);

				cr.accept(cv, ClassReader.SKIP_DEBUG);

				byte[] bc = cw.toByteArray();

				Utils.writeClass(cv.targetName, bc);

				// return (GraphSerializer) GraphClassLoader.INSTANCE
				// .loadAnonymous(sample.getClass(), bc)
				// .getConstructor(Class.class, boolean.class,
				// boolean.class).newInstance(pojo, op, nullable);

				return (GraphSerializer) GraphClassLoader.INSTANCE.loadAnonymous(GraphSerializer.class, bc).getConstructor(Class.class, Class.class, boolean.class, boolean.class).newInstance(sample.getClass(), pojo, op, nullable);

			} catch (Throwable e) {
				Utils.rethrow(e);
				return null;
			}

		}
	}

	public static void reifyIntern(ClassVisitor cv, Type type, String owner) {
		final String desc = Symbols.reifiedWritePayloadDesc(type);
		final MethodVisitor mv = cv.visitMethod(ACC_PUBLIC + ACC_STATIC, _R_.intern, desc, null, null);

		mv.visitCode();

		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEVIRTUAL, _Context.name, _Context.nullSafeInterned, _Context.nullSafeInterned_D, false);
		final Label ne = new Label();
		mv.visitJumpInsn(IFNE, ne);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKESTATIC, owner, _R_.writeData, desc, false);

		mv.visitLabel(ne);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	public static void reifyUnintern(ClassVisitor cv, Type type, String owner, boolean inflateOverride) {
		final String desc = Symbols.reifiedReadDesc(type);
		final MethodVisitor mv = cv.visitMethod(ACC_PUBLIC + ACC_STATIC, _R_.unintern, desc, null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, _Source.name, _Source.unpackI, _Source.unpackI_D, false);
		mv.visitVarInsn(ISTORE, 2);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ILOAD, 2);
		mv.visitMethodInsn(INVOKEVIRTUAL, _Context.name, _Context.interned, _Context.interned_D, false);
		mv.visitTypeInsn(CHECKCAST, type.getInternalName());
		mv.visitVarInsn(ASTORE, 3);
		mv.visitVarInsn(ALOAD, 3);
		final Label nn = new Label();
		mv.visitJumpInsn(IFNONNULL, nn);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESTATIC, owner, _R_.instantiate, Symbols.reifiedInstantiateDesc(type.getDescriptor()), false);
		mv.visitVarInsn(ASTORE, 3);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitVarInsn(ILOAD, 2);
		mv.visitMethodInsn(INVOKEVIRTUAL, _Context.name, _Context.markInterned, _Context.markInterned_D, false);
		if (inflateOverride) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKESTATIC, owner, _R_.inflateData, Symbols.reifiedInflateDataDesc(type), false);
		}
		mv.visitLabel(nn);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	public static MethodVisitor switchVisit(ClassInfo root, String targetName, GenerationStrategy strategy, ClassVisitor cv, int access, String name, String desc, String signature, String[] exceptions) {
		return switchVisit(root, targetName, strategy, cv, access, name, desc, signature, exceptions, null);
	}

	public static MethodVisitor switchVisit(ClassInfo root, String targetName, GenerationStrategy strategy, ClassVisitor cv, int access, String name, String desc, String signature, String[] exceptions, Set<String> skip) {

		if (skip != null && skip.contains(name)) {
			return null;
		}

		switch (name) {
		case _GraphSerializer.inflateData:
		case _GraphSerializer.writeData:

			boolean abs = (access & Opcodes.ACC_ABSTRACT) != 0;

			String delegate = name.equals(_GraphSerializer.inflateData) ? _R_.inflateData : _R_.writeData;
			return new DelegatingToReifiedMV(cv.visitMethod(strategy.serializerAccessModifier(root), name, desc, signature, exceptions), abs, root, delegate, targetName);

		case _GraphSerializer.instantiate:
			MethodVisitor mv = cv.visitMethod(access, _R_.instantiate, Symbols.reifiedInstantiateDesc(root.basic().desc), signature, exceptions);
			return new InstantiateAdapter(strategy, root, cv, mv);

		case _GraphSerializer.read:
		case _GraphSerializer.readOpaque:
		case _GraphSerializer.write:

			boolean isWrite = _GraphSerializer.write_D.equals(desc);

			String mn = _R_.PREFIX + name;

			String dsc = isWrite ? Symbols._R_writeDataDesc(root.basic().desc) : Symbols._R_readDataDesc(root.basic().desc);

			return new MethodReifier(root, targetName, isWrite, cv.visitMethod(access, mn, dsc, signature, exceptions));
		default:
			break;
		}
		return null;
	}
}