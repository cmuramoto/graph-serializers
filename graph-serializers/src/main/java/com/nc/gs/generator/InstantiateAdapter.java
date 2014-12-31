package com.nc.gs.generator;

import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import symbols.io.abstraction._GraphSerializer;

import com.nc.gs.interpreter.ClassInfo;
import com.nc.gs.interpreter.CtorInfo;
import com.nc.gs.interpreter.ExtendedType;

class InstantiateAdapter extends MethodVisitor {

	GenerationStrategy strategy;
	ClassInfo root;
	ClassVisitor cv;
	boolean hasDC;

	public InstantiateAdapter(GenerationStrategy strategy, ClassInfo root,
			ClassVisitor cv, MethodVisitor mv) {
		super(Opcodes.ASM5, mv);
		this.root = root;
		this.cv = cv;
		this.strategy = strategy;
	}

	@Override
	public void visitCode() {

		ClassInfo ci = root;
		CtorInfo info = ci.isPrivate() || ci.isNonStaticInnerClass() ? null
				: ci.findDefaultCtor();
		ExtendedType et = ci.basic();

		boolean defaultCtor;

		if (info != null && !et.declaresFinalField(true)) {
			if (!(defaultCtor = info.isPublic())) {

				defaultCtor = !et.isSystemResource() && info.isAtLeastDefault();
			}
		} else {
			defaultCtor = false;
		}

		hasDC = defaultCtor;

		if (defaultCtor) {
			CtorInfo.invokeDefaultCtor(ci, this);
		} else {
			CtorInfo.invokeAllocateInstance(ci, this);
			visitTypeInsn(CHECKCAST, ci.getName());
		}

		visitInsn(ARETURN);
		visitMaxs(0, 0);
	}

	@Override
	public void visitEnd() {
		visitCode();

		MethodVisitor mv = cv.visitMethod(
				strategy.serializerAccessModifier(root),
				_GraphSerializer.instantiate, _GraphSerializer.instantiate_D,
				null, null);
		if (hasDC) {
			CtorInfo.invokeDefaultCtor(root, mv);
		} else {
			CtorInfo.invokeAllocateInstance(root, mv);
		}

		mv.visitInsn(ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		super.visitEnd();
	}
}