package com.nc.gs.interpreter;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import symbols.io.abstraction._MapShape;
import symbols.java.lang._Class;

public final class MapShape {

	public String optName;

	public Class<?> mapType;

	public Shape ks;

	public Shape vs;

	public boolean rep;

	public boolean opt;

	public MapShape(String optName, Class<?> mapType, Shape ks, Shape vs,
			boolean rep, boolean opt) {
		this(optName,
				mapType == null ? null : ExtendedType.forRuntime(mapType), ks,
				vs, rep, opt);
	}

	public MapShape(String optName, ExtendedType mapType, Shape ks, Shape vs,
			boolean rep, boolean opt) {
		this.optName = optName;
		this.mapType = mapType == null ? null : mapType.runtimeType();
		this.ks = ks;
		this.vs = vs;
		this.rep = rep;
		this.opt = opt;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapShape other = (MapShape) obj;
		if (ks == null) {
			if (other.ks != null)
				return false;
		} else if (!ks.equals(other.ks))
			return false;
		if (mapType == null) {
			if (other.mapType != null)
				return false;
		} else if (!mapType.equals(other.mapType))
			return false;
		if (opt != other.opt)
			return false;
		if (rep != other.rep)
			return false;
		if (vs == null) {
			if (other.vs != null)
				return false;
		} else if (!vs.equals(other.vs))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	public void onStack(MethodVisitor mv) {
		mv.visitTypeInsn(NEW, _MapShape.name);
		mv.visitInsn(DUP);

		if (opt) {
			mv.visitLdcInsn(optName);
		} else {
			mv.visitInsn(ACONST_NULL);
		}

		if (mapType != null) {
			mv.visitLdcInsn(Type.getType(mapType));
		} else {
			mv.visitInsn(ACONST_NULL);
		}

		ks.onStack(mv);
		vs.onStack(mv);

		Symbols.loadBoolean(mv, rep);
		Symbols.loadBoolean(mv, opt);

		mv.visitMethodInsn(INVOKESPECIAL, _MapShape.name, _Class.ctor,
				_MapShape.ctor, false);
	}
}
