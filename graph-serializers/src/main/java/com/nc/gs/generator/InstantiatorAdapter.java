package com.nc.gs.generator;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

import java.io.InputStream;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import symbols.io.abstraction._Instantiator;
import symbols.io.abstraction._Util;
import symbols.java.lang._Class;
import symbols.java.lang._Object;
import symbols.java.lang.reflect._Constructor;
import symbols.sun.misc._Unsafe;

import com.nc.gs.core.GraphClassLoader;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.Instantiator;
import com.nc.gs.interpreter.ClassInfo;
import com.nc.gs.interpreter.CtorInfo;
import com.nc.gs.interpreter.FieldInfo;
import com.nc.gs.interpreter.FieldTrap;
import com.nc.gs.interpreter.Symbols;
import com.nc.gs.interpreter.VisitationContext;
import com.nc.gs.util.Utils;

public class InstantiatorAdapter extends ClassVisitor {

	class CopyImpl extends MethodVisitor {

		public CopyImpl(MethodVisitor mv) {
			super(Opcodes.ASM5, mv);
		}

		@Override
		public void visitEnd() {
			MethodVisitor mv = this.mv;
			mv.visitCode();

			// ExtendedType et = ci.basic();

			// boolean shouldUseUnsafe=pvtAcc || et.declaresFinalField(true)
			// || et.declaresPrivateField(true)
			// || !ci.areAllFieldsInSamePackage();

			mv.visitFieldInsn(GETSTATIC, _Util.name, _Util.unsafe, _Unsafe.desc);
			mv.visitVarInsn(ASTORE, 3);

			ClassInfo curr = ci;

			LinkedList<FieldInfo> allFields = new LinkedList<>();

			while (curr != null) {
				LinkedList<FieldInfo> fields = curr.fields;

				if (fields != null) {
					allFields.addAll(fields);
				}

				curr = curr.parent;
			}

			Collections.sort(allFields);

			FieldInfo fi;

			while ((fi = allFields.pollFirst()) != null) {
				String get;
				String getD;
				String put;
				String putD;

				boolean isFinal = fi.isFinal();

				if (fi.isPrimitive()) {
					String n = fi.type().getClassName();
					Class<?> rt = fi.type().runtimeType();

					String suff = n.substring(1);
					get = String.format("get%s%s",
							Character.toUpperCase(n.charAt(0)), suff);
					getD = MethodType.methodType(rt, Object.class, long.class)
							.toMethodDescriptorString();

					put = String.format("put%s%s%s", Character.toUpperCase(n
							.charAt(0)), suff, isFinal ? "Volatile" : "");
					putD = MethodType.methodType(void.class, Object.class,
							long.class, rt).toMethodDescriptorString();

				} else {
					get = _Unsafe.getObject;
					getD = _Unsafe.getObject_D;

					put = isFinal ? _Unsafe.putObjectV : _Unsafe.putObject;
					putD = _Unsafe.putObject_D;

				}

				long off = fi.objectFieldOffset();

				mv.visitVarInsn(ALOAD, 3);
				mv.visitVarInsn(ALOAD, 2);
				mv.visitLdcInsn(off);

				mv.visitVarInsn(ALOAD, 3);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitLdcInsn(off);

				mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, get, getD,
						false);
				mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, put, putD,
						false);

			}
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			super.visitEnd();
		}

	}

	public static Instantiator of(Class<?> type) {

		String cn = Symbols.instantiatorBinaryName(type.getName());
		Instantiator rv = resolveFromCL(cn);

		if (rv == null) {
			synchronized (type) {
				rv = resolveFromCL(cn);
				if (rv == null) {

					try (VisitationContext vc = VisitationContext.current();
							InputStream is = Utils
									.streamCode(Instantiator.class)) {

						boolean privateAccess = type.getClassLoader() != GraphSerializer.class
								.getClassLoader()
								&& !Modifier.isPublic(type.getModifiers());

						ClassReader cr = new ClassReader(is);
						ClassWriter cw = GenerationStrategy.newClassWriter();
						ClassInfo ci = ClassInfo.getInfo(type,
								FieldTrap.ALL_INSTANCE, true);

						InstantiatorAdapter inst = new InstantiatorAdapter(ci,
								privateAccess, cw);

						cr.accept(inst, 0);

						byte[] bc = cw.toByteArray();

						Utils.writeClass(inst.targetName, bc);

						rv = (Instantiator) GraphClassLoader.INSTANCE.load(
								null, bc).newInstance();

						// rv = (Instantiator) GraphClassLoader.INSTANCE
						// .loadAnonymous(type, bc).newInstance();
					} catch (Exception e) {
						return Utils.rethrow(e);
					}
				}
			}
		}

		return rv;
	}

	private static Instantiator resolveFromCL(String cn) {
		Class<?> c = GraphClassLoader.INSTANCE.findClass(cn);

		if (c != null) {
			try {
				return (Instantiator) c.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				return Utils.rethrow(e);
			}
		}
		return null;
	}

	String targetName;
	ClassInfo ci;

	boolean pvtAcc;

	List<String> descs = new ArrayList<>();

	public InstantiatorAdapter(ClassInfo ci, boolean pvtAcc, ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
		targetName = Symbols.instantiatorInternalName(ci.getName());
		this.ci = ci;
		this.pvtAcc = pvtAcc;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {

		super.visit(version, ACC_PUBLIC + ACC_FINAL, targetName, signature,
				superName, new String[] { name });
	}

	@Override
	public void visitEnd() {
		CtorInfo.patchDefaultCtor(cv);

		if (pvtAcc) {
			patchTypeRef();
		}
	}

	private void patchTypeRef() {

		FieldVisitor fv = cv.visitField(ACC_STATIC|ACC_FINAL, "C", _Class.desc, null, null);
		fv.visitEnd();
		
		MethodVisitor mv = cv.visitMethod(ACC_STATIC, _Class.ClassInitializer,
				_Class.NO_ARG_VOID, null, null);
		mv.visitCode();

		mv.visitLdcInsn(ci.basic().getClassName());
		mv.visitMethodInsn(INVOKESTATIC, _Class.name, _Class.forName,
				_Class.forName_D, false);
		mv.visitVarInsn(ASTORE, 0);
		
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(PUTSTATIC, targetName, "C", _Class.desc);

		mv.visitVarInsn(ALOAD, 0);

		for (int i = 0; i < descs.size(); i++) {
			mv.visitVarInsn(ALOAD, 0);

			String desc = descs.get(i);

			Type[] types = Type.getArgumentTypes(desc);
			Symbols.loadNumber(mv, types.length);
			mv.visitTypeInsn(ANEWARRAY, _Class.name);

			for (int j = 0; j < types.length; j++) {
				Type type = types[j];
				mv.visitInsn(DUP);
				Symbols.loadNumber(mv, j);
				Symbols.loadType(mv, type);
				mv.visitInsn(AASTORE);
			}

			mv.visitMethodInsn(INVOKEVIRTUAL, _Class.name,
					_Class.getDeclaredConstructor,
					_Class.getDeclaredConstructor_D, false);
			mv.visitVarInsn(ASTORE, 1);

			mv.visitVarInsn(ALOAD, 1);
			Symbols.loadBoolean(mv, true);
			mv.visitMethodInsn(INVOKEVIRTUAL, _Constructor.name,
					_Constructor.setAccessible, _Constructor.setAccessible_D,
					false);

			mv.visitVarInsn(ALOAD, 1);
			mv.visitFieldInsn(PUTSTATIC, targetName, "C" + i, _Constructor.desc);
		}

		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		mv.visitEnd();
	}

	@Override
	public MethodVisitor visitMethod(int access, final String name,
			final String desc, String signature, String[] exceptions) {
		MethodVisitor rv;
		if (name.equals(_Instantiator.copy)) {
			rv = new CopyImpl(super.visitMethod(ACC_PUBLIC | ACC_FINAL, name,
					desc, signature, exceptions));
		} else if (name.endsWith(_Instantiator.allocateHollow)) {
			CtorInfo.createHollowDelegator(ci, cv, name, desc, targetName,
					pvtAcc ? "C" : null, 1);
			rv = null;
		} else {

			ClassInfo root = ci;

			CtorInfo ctor = root.findMatchingCtor(desc);

			if (ctor == null) {
				ctor = root.findPartiallyMatchingCtor(desc);

				if (ctor == null) {
					ctor = root.findMatchingCtor(_Object.defaultCtor_D);
				}
			}

			if (pvtAcc || (ctor != null && ctor.isPrivate())) {
				if (ctor != null) {
					if (!descs.contains(ctor.desc)) {
						String ctorName = "C" + (descs.size());

						descs.add(ctor.desc);

						ctor.patchFieldDeclaration(cv, ctorName);

						ctor.createReflectionDelegator(root, cv, name, desc,
								targetName, ctorName, 1);
					}
				} else {
					CtorInfo.createHollowDelegator(root, cv, name, desc,
							targetName, pvtAcc ? "C" : null, 1);
				}
			} else {
				if (ctor != null) {
					ctor.createDelegator(root, cv, name, desc, 1);
				} else {
					CtorInfo.createHollowDelegator(root, cv, name, desc,
							targetName, pvtAcc ? "C" : null, 1);
				}
			}

			rv = null;
		}
		return rv;
	}

}