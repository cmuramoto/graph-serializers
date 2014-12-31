package com.nc.gs.generator;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import symbols.io.abstraction._GraphSerializer._R_;

import com.nc.gs.interpreter.ClassInfo;
import com.nc.gs.interpreter.Symbols;

class DelegatingToReifiedMV extends MethodVisitor {

	ClassInfo root;
	String targetName;
	String delegate;
	boolean abs;
	boolean written;

	public DelegatingToReifiedMV(MethodVisitor mv, boolean abs, ClassInfo root, String delegate, String targetName) {
		super(Opcodes.ASM5, mv);
		this.abs = abs;
		this.root = root;
		this.delegate = delegate;
		this.targetName = targetName;
	}

	@Override
	public void visitCode() {
		MethodVisitor mv = this.mv;
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitTypeInsn(CHECKCAST, root.getName());
		if (delegate.equals(_R_.inflateData)) {
			mv.visitMethodInsn(INVOKESTATIC, targetName, delegate, Symbols._R_inflateDataDesc(root.basic().desc), false);
		} else {
			mv.visitMethodInsn(INVOKESTATIC, targetName, delegate, Symbols._R_writeDataDesc(root.basic().desc), false);
		}
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		written = true;
	}

	@Override
	public void visitEnd() {
		if (!written) {
			mv.visitCode();
			visitCode();
		}
		super.visitEnd();
	}

	@Override
	public void visitInsn(int opcode) {
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
	}

}