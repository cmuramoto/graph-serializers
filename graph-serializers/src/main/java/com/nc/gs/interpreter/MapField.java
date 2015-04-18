package com.nc.gs.interpreter;

import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.PUTSTATIC;

import org.objectweb.asm.MethodVisitor;

import symbols.io.abstraction._SerializerFactory;

public final class MapField extends SpecialField {

	MapShape ms;

	public MapField(ExtendedType mt, String gsIN, String gsDesc, Shape ks, Shape vs, boolean rep, boolean optimize) {
		super();
		this.gsIN = gsIN;
		this.gsDesc = gsDesc;
		this.ms = new MapShape(gsIN, mt, ks, vs, rep, optimize);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		MapField other = (MapField) obj;

		return ms.equals(other.ms);
	}

	@Override
	public void initialize(MethodVisitor mv, String owner) {

		ms.onStack(mv);

		mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.forMap, _SerializerFactory.forMap_D, false);

		mv.visitTypeInsn(CHECKCAST, ms.optName);

		mv.visitFieldInsn(PUTSTATIC, owner, name, gsDesc);
	}

	public MapShape shape() {
		return ms;
	}

}