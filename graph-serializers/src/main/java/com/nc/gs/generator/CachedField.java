package com.nc.gs.generator;

import static org.objectweb.asm.Opcodes.GETSTATIC;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.nc.gs.interpreter.FieldInfo;
import com.nc.gs.interpreter.Symbols;

public class CachedField {

	String targetFieldName;
	String serializerIN;
	boolean isReified;

	public CachedField(String targetFieldName, String serializerIN,
			boolean isReified) {
		super();
		this.targetFieldName = targetFieldName;
		this.serializerIN = serializerIN;
		this.isReified = isReified;
	}

	public void invokeWrite(MethodVisitor mv, FieldInfo fi) {
		if (isReified) {
			Symbols._R_invokeWrite(mv, serializerIN, fi.type(),
					fi.disregardReference());
		} else {
			Symbols.invokeWriteWithOwner(mv, serializerIN,
					fi.disregardReference());
		}
	}

	public void invokeWrite(MethodVisitor mv, FieldInfo fi, Type type) {
		if (isReified) {
			Symbols._R_invokeWrite(mv, serializerIN, type,
					fi.disregardReference());
		} else {
			Symbols.invokeWriteWithOwner(mv, serializerIN,
					fi.disregardReference());
		}
	}

	public void onStack(MethodVisitor mv, String owner) {
		if (!isReified) {
			mv.visitFieldInsn(GETSTATIC, owner, targetFieldName,
					serializerDesc());
		}
	}

	String serializerDesc() {
		return "L" + serializerIN + ";";
	}
}
