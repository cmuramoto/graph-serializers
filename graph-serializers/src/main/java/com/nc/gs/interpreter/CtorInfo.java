package com.nc.gs.interpreter;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

import java.util.Arrays;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import symbols.io.abstraction._GraphSerializer;
import symbols.io.abstraction._Source;
import symbols.io.abstraction._Util;
import symbols.java.lang._Class;
import symbols.java.lang._Object;
import symbols.java.lang.reflect._Constructor;
import symbols.sun.misc._Unsafe;

public class CtorInfo {

	public static void createHollowDelegator(ClassInfo ci, ClassVisitor cv,
			String name, String desc, String owner, String typeRef, int base) {
		int acc = ACC_PUBLIC | ACC_FINAL | ((base == 0) ? ACC_STATIC : 0);

		MethodVisitor mv = cv.visitMethod(acc, name, desc, null, null);
		mv.visitCode();

		mv.visitFieldInsn(GETSTATIC, _Util.name, _Util.unsafe, _Unsafe.desc);
		if (typeRef != null) {
			mv.visitFieldInsn(GETSTATIC, owner, typeRef, _Class.desc);
		} else {
			mv.visitLdcInsn(ci.type());
		}
		mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name,
				_Unsafe.allocateInstance, _Unsafe.allocateInstance_D, false);
		mv.visitInsn(ARETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	public static void createInstantiateForArray(ClassVisitor cv,
			String internalName) {
		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_FINAL,
				_GraphSerializer.instantiate, _GraphSerializer.instantiate_D,
				null, null);
		mv.visitCode();

		mv.visitVarInsn(ALOAD, 1);

		mv.visitMethodInsn(_Source.DEC_OPCODE, _Source.Decompressor,
				_Source.unpackI, _Source.unpackI_D, false);
		mv.visitTypeInsn(ANEWARRAY, internalName);

		mv.visitInsn(ARETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	public static void invokeAllocateInstance(ClassInfo root, MethodVisitor mv) {
		mv.visitFieldInsn(GETSTATIC, _Util.name, _Util.unsafe, _Unsafe.desc);
		mv.visitLdcInsn(root.type());
		mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name,
				_Unsafe.allocateInstance, _Unsafe.allocateInstance_D, false);
	}

	public static void invokeDefaultCtor(ClassInfo ci, MethodVisitor mv) {
		invokeDefaultCtor(ci.getName(), mv);
	}

	public static void invokeDefaultCtor(String iN, MethodVisitor mv) {
		mv.visitTypeInsn(NEW, iN);
		mv.visitInsn(DUP);

		mv.visitMethodInsn(INVOKESPECIAL, iN, _Class.ctor, _Class.NO_ARG_VOID,
				false);
	}

	public static void patchDefaultCtor(ClassVisitor cv) {
		patchDefaultCtor(cv, _Object.name);
	}

	public static void patchDefaultCtor(ClassVisitor cv, String parentIN) {
		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, _Object.init,
				_Class.NO_ARG_VOID, null, null);
		patchDefaultCtor(mv, parentIN);
	}

	public static void patchDefaultCtor(MethodVisitor mv) {
		patchDefaultCtor(mv, _Object.name);
	}

	public static void patchDefaultCtor(MethodVisitor mv, String parentIN) {
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, parentIN, _Object.init,
				_Class.NO_ARG_VOID, false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	int access;

	public String desc;

	public CtorInfo(int access, String desc) {
		super();
		this.access = access;
		this.desc = desc;
	}

	public void createDelegator(ClassInfo ci, ClassVisitor cv, String name,
			String desc, int base) {

		int acc = ACC_PUBLIC | ACC_FINAL | ((base == 0) ? ACC_STATIC : 0);

		MethodVisitor mv = cv.visitMethod(acc, name, desc, null, null);

		mv.visitTypeInsn(NEW, ci.info.name);
		mv.visitInsn(DUP);

		// fast-path
		if (this.desc.equals(_Object.defaultCtor_D)) {
			mv.visitMethodInsn(INVOKESPECIAL, ci.info.name, _Object.init,
					_Object.defaultCtor_D, false);
		} else {
			Type[] ctorArgs = Type.getArgumentTypes(this.desc);
			Type[] delegatorArgs = Type.getArgumentTypes(desc);

			if (delegatorArgs.length < ctorArgs.length) {
				throw new IllegalArgumentException(
						String.format(
								"Delegate has too few args [%s] to match with constructor args %s",
								Arrays.toString(delegatorArgs),
								Arrays.toString(ctorArgs)));
			}

			int ix = base;
			int cons = 0;

			for (int i = 0; i < delegatorArgs.length && cons < ctorArgs.length; i++) {
				Type type = delegatorArgs[i];

				Type ctorArg = ctorArgs[cons];

				if (ctorArg.equals(type)) {
					cons++;
					Symbols.loadLocal(mv, type, ix);
				}

				ix += type.getSize();
			}

			if (cons < ctorArgs.length) {
				throw new IllegalArgumentException(
						String.format(
								"Delegate has too few args [%s] to match with constructor args %s",
								Arrays.toString(delegatorArgs),
								Arrays.toString(ctorArgs)));
			}

			mv.visitMethodInsn(INVOKESPECIAL, ci.info.name, _Object.init,
					this.desc, false);

		}

		mv.visitInsn(ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	public String[] getArgumentDescriptors() {
		return Symbols.getArgumentDescriptors(desc);
	}

	public boolean isAtLeastDefault() {
		return (access & ACC_PRIVATE) == 0;
	}

	public boolean isPublic() {
		return (access & ACC_PUBLIC) != 0;
	}

	public boolean isPrivate() {
		return (access & ACC_PRIVATE) != 0;
	}

	public void createReflectionDelegator(ClassInfo ci, ClassVisitor cv,
			String name, String desc, String owner, String ctorName, int base) {
		int acc = ACC_PUBLIC | ACC_FINAL | ((base == 0) ? ACC_STATIC : 0);

		MethodVisitor mv = cv.visitMethod(acc, name, desc, null, null);
		mv.visitCode();

		Type[] ctorArgs = Type.getArgumentTypes(this.desc);
		Type[] delegatorArgs = Type.getArgumentTypes(desc);
		int[] matching = new int[delegatorArgs.length];
		Arrays.fill(matching, -1);

		if (delegatorArgs.length < ctorArgs.length) {
			throw new IllegalArgumentException(
					String.format(
							"Delegate has too few args [%s] to match with constructor args %s",
							Arrays.toString(delegatorArgs),
							Arrays.toString(ctorArgs)));
		}

		int ix = base;
		int cons = 0;

		for (int i = 0; i < delegatorArgs.length && cons < ctorArgs.length; i++) {
			Type type = delegatorArgs[i];

			Type ctorArg = ctorArgs[cons];

			if (ctorArg.equals(type)) {
				cons++;
				// Symbols.loadLocal(mv, type, ix);
				matching[i] = ix;
			}

			ix += type.getSize();
		}

		if (cons < ctorArgs.length) {
			throw new IllegalArgumentException(
					String.format(
							"Delegate has too few args [%s] to match with constructor args %s",
							Arrays.toString(delegatorArgs),
							Arrays.toString(ctorArgs)));
		}

		mv.visitFieldInsn(GETSTATIC, owner, ctorName, _Constructor.desc);

		Symbols.loadNumber(mv, cons);

		mv.visitTypeInsn(ANEWARRAY, _Object.name);

		ix = 0;

		for (int i = 0; i < matching.length; i++) {
			int m = matching[i];

			if (m >= 0) {
				mv.visitInsn(DUP);
				Symbols.loadNumber(mv, ix++);

				Type type = delegatorArgs[i];

				int s = type.getSort();
				if (s > 0 && s < 9) {
					Symbols.loadAndBox(mv, type, m);
				} else {
					mv.visitVarInsn(ALOAD, m);
				}

				mv.visitInsn(AASTORE);
			}
		}

		mv.visitMethodInsn(INVOKEVIRTUAL, _Constructor.name,
				_Constructor.newInstance, _Constructor.newInstance_D, false);

		mv.visitInsn(ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	public void patchFieldDeclaration(ClassVisitor cv, String name) {
		FieldVisitor fv = cv.visitField(ACC_PUBLIC | ACC_STATIC | ACC_FINAL,
				name, _Constructor.desc, null, null);
		fv.visitEnd();
	}

	public void patchCtorReferenceInitialization(ClassInfo ci,
			MethodVisitor clinit, String targetName, String name) {

		Type[] ctorArgs = Type.getArgumentTypes(this.desc);

		Symbols.loadNumber(clinit, ctorArgs.length);

		clinit.visitTypeInsn(ANEWARRAY, _Class.name);

		for (int i = 0; i < ctorArgs.length; i++) {
			clinit.visitInsn(DUP);
			Symbols.loadNumber(clinit, i);
			clinit.visitLdcInsn(ctorArgs[i]);
			clinit.visitInsn(AASTORE);
		}

		clinit.visitLdcInsn(ci.basic().getClassName());
		clinit.visitMethodInsn(INVOKESTATIC, _Class.name, _Class.forName,
				_Class.forName_D, false);

		clinit.visitMethodInsn(INVOKEVIRTUAL, _Class.name,
				_Class.getDeclaredConstructor, _Class.getDeclaredConstructor_D,
				false);
		clinit.visitFieldInsn(PUTSTATIC, targetName, name, _Constructor.desc);

		clinit.visitFieldInsn(GETSTATIC, targetName, name, _Constructor.desc);
		Symbols.loadBoolean(clinit, true);
		clinit.visitMethodInsn(INVOKEVIRTUAL, _Constructor.name,
				_Constructor.setAccessible, _Constructor.setAccessible_D, false);

		clinit.visitInsn(RETURN);
		clinit.visitMaxs(0, 0);
		clinit.visitEnd();
		clinit.visitEnd();
	}
}