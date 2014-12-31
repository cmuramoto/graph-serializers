package com.nc.gs.interpreter;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.PUTSTATIC;

import org.objectweb.asm.MethodVisitor;

import symbols.io.abstraction._SerializerFactory;

public final class IterableField extends SpecialField {

	StreamShape s;

	public IterableField(ExtendedType type, String gsIN, String gsDesc, Shape s, boolean rep, boolean opt) {
		super();
		this.gsIN = gsIN;
		this.gsDesc = gsDesc;
		this.s = new StreamShape(gsIN, type, s, rep, opt);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		IterableField other = (IterableField) obj;

		return s.equals(other.s);
	}

	@Override
	public void initialize(MethodVisitor mv, String owner) {

		StreamShape ss = this.s;
		Shape s = ss.s;

		if (ss.opt) {
			mv.visitLdcInsn(gsIN);
		} else {
			mv.visitInsn(ACONST_NULL);
		}

		ss.onStack(mv);

		if (s.isSet()) {
			mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.forSet, _SerializerFactory.forSet_D, false);
		} else if (s.isArray()) {
			mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.forArray, _SerializerFactory.forArray_D, false);
		} else {
			mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.forCollection, _SerializerFactory.forCollection_D, false);
		}

		mv.visitTypeInsn(CHECKCAST, gsIN);

		mv.visitFieldInsn(PUTSTATIC, owner, name, gsDesc);
	}

}