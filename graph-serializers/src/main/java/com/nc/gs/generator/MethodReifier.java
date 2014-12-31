package com.nc.gs.generator;

import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import symbols.io.abstraction._Context;
import symbols.io.abstraction._GraphSerializer;
import symbols.io.abstraction._GraphSerializer._R_;

import com.nc.gs.core.GraphSerializer;
import com.nc.gs.interpreter.ClassInfo;
import com.nc.gs.interpreter.Symbols;

/**
 * 
 * Reifies: <br/>
 * 
 * {@link GraphSerializer#payload(com.nc.gs.core.Context, java.nio.ByteBuffer)}
 * {@link GraphSerializer#payload(com.nc.gs.core.Context, java.nio.ByteBuffer, Object)}
 * {@link GraphSerializer#readData(com.nc.gs.core.Context, java.nio.ByteBuffer)}
 * {@link GraphSerializer#writeOpaque(com.nc.gs.core.Context, java.nio.ByteBuffer, Object)}
 * {@link GraphSerializer#nullSafeRead(com.nc.gs.core.Context, java.nio.ByteBuffer)}
 * {@link GraphSerializer#nullSafeWrite(com.nc.gs.core.Context, java.nio.ByteBuffer, Object)}
 * {@link GraphSerializer#read(com.nc.gs.core.Context, java.nio.ByteBuffer)}
 * {@link GraphSerializer#write(com.nc.gs.core.Context, java.nio.ByteBuffer, Object)}
 * 
 * @author cmuramoto
 * 
 */
class MethodReifier extends MethodVisitor {

	String targetName;
	ClassInfo root;
	boolean isWrite;

	public MethodReifier(ClassInfo root, String targetName, boolean isWrite,
			MethodVisitor mv) {
		super(Opcodes.ASM5, mv);
		this.root = root;
		this.targetName = targetName;
		this.isWrite = isWrite;
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {

		if (isWrite) {
			if (owner.equals(_GraphSerializer.name)) {
				super.visitMethodInsn(INVOKESTATIC, targetName, _R_.writeData,
						com.nc.gs.interpreter.Symbols
								.reifiedWritePayloadDesc(root.type()), false);
				return;
			}
		} else {
			if (opcode == INVOKEVIRTUAL) {
				if (owner.equals(_GraphSerializer.name)) {
					if (name.equals(_GraphSerializer.inflateData)) {
						super.visitMethodInsn(INVOKESTATIC, targetName,
								_R_.inflateData, com.nc.gs.interpreter.Symbols
										.reifiedInflateDataDesc(root.type()),
								false);
						return;
					} else if (name.equals(_GraphSerializer.instantiate)) {
						// mv.visitInsn(Opcodes.POP);
						super.visitMethodInsn(
								INVOKESTATIC,
								targetName,
								_R_.instantiate,
								Symbols.reifiedInstantiateDesc(root.basic().desc),
								false);
						return;
					}
				} else if (owner.equals(_Context.name) && name.equals("from")) {
					super.visitMethodInsn(opcode, owner, name, desc, itf);
					mv.visitTypeInsn(CHECKCAST, root.getName());
					return;
				}
			}
		}

		super.visitMethodInsn(opcode, owner, name, desc, itf);
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
		if (var == 0) {
			return;
		}
		super.visitVarInsn(opcode, var - 1);
	}

}