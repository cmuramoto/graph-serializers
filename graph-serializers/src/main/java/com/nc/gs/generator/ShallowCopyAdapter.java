package com.nc.gs.generator;

import java.io.InputStream;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import symbols.io.abstraction._Util;
import symbols.java.lang._Class;
import symbols.java.lang._Object;
import symbols.sun.misc._Unsafe;

import com.nc.gs.core.GraphClassLoader;
import com.nc.gs.core.ShallowCopyist;
import com.nc.gs.util.Utils;

public class ShallowCopyAdapter extends ClassVisitor implements Opcodes {

	class CopyImpl extends MethodVisitor {

		public CopyImpl(MethodVisitor mv) {
			super(Opcodes.ASM5, mv);
		}

		@SuppressWarnings("restriction")
		@Override
		public void visitEnd() {
			MethodVisitor mv = this.mv;
			mv.visitCode();

			mv.visitVarInsn(ALOAD, 1);
			mv.visitTypeInsn(CHECKCAST, declaringIN);
			mv.visitVarInsn(ASTORE, 3);

			mv.visitVarInsn(ALOAD, 2);
			mv.visitTypeInsn(CHECKCAST, declaringIN);
			mv.visitVarInsn(ASTORE, 4);

			if (needsUnsafe) {
				mv.visitFieldInsn(GETSTATIC, _Util.name, _Util.unsafe,
						_Unsafe.desc);
				mv.visitVarInsn(ASTORE, 5);
			}

			boolean alwaysUnsafe = alwaysUseUnsafe;
			for (Field f : fields) {
				String desc = Type.getDescriptor(f.getType());

				if (alwaysUnsafe || Modifier.isFinal(f.getModifiers())) {

					Class<?> type = f.getType();

					long offset = Utils.U.objectFieldOffset(f);

					mv.visitVarInsn(ALOAD, 5);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitLdcInsn(offset);

					if (!alwaysUnsafe) {
						mv.visitVarInsn(ALOAD, 3);
						mv.visitFieldInsn(GETFIELD, declaringIN, f.getName(),
								desc);
					} else {
						mv.visitVarInsn(ALOAD, 5);
						mv.visitVarInsn(ALOAD, 3);
						mv.visitLdcInsn(offset);

						String get;
						String getD;

						if (type.isPrimitive()) {
							String n = type.getName();
							get = String.format("get%s%s", Character
									.toUpperCase(n.charAt(0)), type.getName()
									.substring(1));
							getD = MethodType.methodType(type, Object.class,
									long.class).toMethodDescriptorString();
						} else {
							get = _Unsafe.getObject;
							getD = _Unsafe.getObject_D;
						}
						mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, get,
								getD, false);
					}
					String put;
					String putD;

					if (type.isPrimitive()) {
						String n = type.getName();
						put = String.format("put%s%sVolatile", Character
								.toUpperCase(n.charAt(0)), type.getName()
								.substring(1));
						putD = MethodType.methodType(void.class, Object.class,
								long.class, type).toMethodDescriptorString();
					} else {
						put = _Unsafe.putObjectV;
						putD = _Unsafe.putObject_D;
					}

					mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, put, putD,
							false);

				} else {
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitFieldInsn(GETFIELD, declaringIN, f.getName(), desc);
					mv.visitFieldInsn(PUTFIELD, declaringIN, f.getName(), desc);
				}
			}

			if (nextParentWithFields != null) {
				mv.visitFieldInsn(GETSTATIC, targetIN, "next",
						Type.getDescriptor(ShallowCopyist.class));
				mv.visitVarInsn(ALOAD, 3);
				mv.visitVarInsn(ALOAD, 4);
				mv.visitMethodInsn(INVOKEINTERFACE,
						Type.getInternalName(ShallowCopyist.class), "copy", "("
								+ _Object.desc + _Object.desc + ")V", true);
			}

			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			super.visitEnd();
		}

	}

	static ConcurrentMap<Class<?>, ShallowCopyist> cache = new ConcurrentHashMap<Class<?>, ShallowCopyist>(
			2);

	static ShallowCopyist make(Class<?> c) {

		ClassLoader cl = c.getClassLoader();
		boolean alwaysUseUnsafe = cl == null;

		if (!alwaysUseUnsafe && cl != ShallowCopyist.class.getClassLoader()) {
			try {
				cl.loadClass(ShallowCopyist.class.getName());
			} catch (Throwable e) {
				alwaysUseUnsafe = true;
			}
		}

		try (InputStream is = Utils.streamCode(ShallowCopyist.class)) {
			ClassReader cr = new ClassReader(is);

			ClassWriter cw = GenerationStrategy.newClassWriter();
			ShallowCopyAdapter adapter = new ShallowCopyAdapter(c,
					alwaysUseUnsafe, cw);

			cr.accept(adapter, ClassReader.SKIP_DEBUG);

			byte[] bc = cw.toByteArray();

			Utils.writeClass(adapter.targetIN, bc.clone());

			Class<?> res = alwaysUseUnsafe ? GraphClassLoader.INSTANCE.load(
					null, bc) : GraphClassLoader.INSTANCE.loadAnonymous(c, bc);

			return (ShallowCopyist) res.newInstance();
			// return (ShallowCopier) GraphClassLoader.INSTANCE.load(null, bc)
			// .newInstance();
		} catch (Exception e) {
			Utils.rethrow(e);
			return null;
		}
	}

	public static ShallowCopyist of(Class<?> type) {
		ShallowCopyist copier = cache.get(type);

		if (copier == null) {
			ShallowCopyist old = cache.putIfAbsent(type, copier = make(type));
			if (old != null) {
				copier = old;
			}
		}

		return copier;
	}

	Class<?> declaring;
	String declaringIN;
	Class<?> nextParentWithFields;

	String targetIN;

	List<Field> fields;
	boolean needsUnsafe;
	boolean alwaysUseUnsafe;

	public ShallowCopyAdapter(Class<?> src, boolean alwaysUseUnsafe,
			ClassVisitor cv) {
		super(Opcodes.ASM5, cv);

		this.alwaysUseUnsafe = alwaysUseUnsafe;
		this.needsUnsafe = alwaysUseUnsafe;
		this.declaring = src;
		Field[] fields = src.getDeclaredFields();
		ArrayList<Field> list = new ArrayList<>();

		for (Field f : fields) {
			if (!Modifier.isStatic(f.getModifiers())) {
				list.add(f);
				needsUnsafe |= Modifier.isFinal(f.getModifiers());
			}
		}
		this.fields = list;

		Class<?> next = src.getSuperclass();
		boolean nextHasFields = false;

		loop: while (next != null && next != Object.class) {
			fields = next.getDeclaredFields();
			for (Field f : fields) {
				if (!Modifier.isStatic(f.getModifiers())) {
					nextHasFields = true;
					break loop;
				}
			}
			next = next.getSuperclass();
		}

		if (nextHasFields) {
			this.nextParentWithFields = next;
		}
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		declaringIN = Type.getInternalName(declaring);
		targetIN = name + "/Of/" + declaringIN;
		super.visit(version, ACC_PUBLIC + ACC_FINAL, targetIN, signature,
				superName, new String[] { name });
	}

	@Override
	public void visitEnd() {

		if (nextParentWithFields != null) {
			String desc = Type.getDescriptor(ShallowCopyist.class);
			FieldVisitor fv = super.visitField(ACC_STATIC + ACC_FINAL, "next",
					desc, null, null);
			fv.visitEnd();

			MethodVisitor mv = super.visitMethod(ACC_STATIC,
					_Class.ClassInitializer, _Class.NO_ARG_VOID, null, null);
			mv.visitCode();
			mv.visitLdcInsn(Type.getType(nextParentWithFields));
			mv.visitMethodInsn(INVOKESTATIC, Type
					.getInternalName(ShallowCopyAdapter.class), "of",
					MethodType.methodType(ShallowCopyist.class, Class.class)
							.toMethodDescriptorString(), false);
			mv.visitFieldInsn(PUTSTATIC, targetIN, "next", desc);
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}

		MethodVisitor mv = super.visitMethod(ACC_PUBLIC, _Class.ctor,
				_Class.NO_ARG_VOID, null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, _Object.name, _Object.init,
				_Class.NO_ARG_VOID, false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		super.visitEnd();
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		return new CopyImpl(super.visitMethod(ACC_PUBLIC, name, desc,
				signature, exceptions));
	}
}
