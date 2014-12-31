package com.nc.gs.interpreter;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import symbols.io.abstraction._StreamShape;
import symbols.java.lang._Class;

public class StreamShape {

	public String optName;

	public Class<?> colType;

	public Shape s;

	public boolean rep;

	public boolean opt;

	public StreamShape(String optName, Class<?> colType, Shape s, boolean rep, boolean opt) {
		this(optName, colType == null ? null : ExtendedType.forRuntime(colType), s, rep, opt);
	}

	public StreamShape(String optName, ExtendedType colType, Shape s, boolean rep, boolean opt) {
		this.optName = optName;
		this.colType = colType == null ? null : colType.runtimeType();
		this.s = s;
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
		StreamShape other = (StreamShape) obj;
		if (colType == null) {
			if (other.colType != null)
				return false;
		} else if (!colType.equals(other.colType))
			return false;
		if (opt != other.opt)
			return false;
		if (rep != other.rep)
			return false;
		if (s == null) {
			if (other.s != null)
				return false;
		} else if (!s.equals(other.s))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	public void onStack(MethodVisitor mv) {
		mv.visitTypeInsn(NEW, _StreamShape.name);
		mv.visitInsn(DUP);

		if (opt) {
			mv.visitLdcInsn(optName);
		} else {
			mv.visitInsn(ACONST_NULL);
		}

		if (colType != null) {
			mv.visitLdcInsn(Type.getType(colType));
		} else {
			mv.visitInsn(ACONST_NULL);
		}

		s.onStack(mv);

		Symbols.loadBoolean(mv, rep);
		Symbols.loadBoolean(mv, opt);

		mv.visitMethodInsn(INVOKESPECIAL, _StreamShape.name, _Class.ctor, _StreamShape.ctor, false);
	}

}
