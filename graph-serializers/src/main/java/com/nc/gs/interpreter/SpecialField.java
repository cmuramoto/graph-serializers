package com.nc.gs.interpreter;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.RETURN;
import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.strategy.IdentityHashingStrategy;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import symbols.io.abstraction._Context;
import symbols.io.abstraction._GraphSerializer;
import symbols.io.abstraction._SerializerFactory;
import symbols.io.abstraction._Sink;
import symbols.io.abstraction._Source;
import symbols.java.lang._Object;

public abstract class SpecialField {

	String gsIN;
	String gsDesc;

	String name;
	TCustomHashMap<FieldInfo, String[]> slots;

	public final void emitDeclaration(ClassVisitor cv) {
		FieldVisitor fv = cv.visitField(ACC_PUBLIC + ACC_STATIC + ACC_FINAL, name, gsDesc, null, null);
		fv.visitEnd();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SpecialField other = (SpecialField) obj;
		if (gsDesc == null) {
			if (other.gsDesc != null) {
				return false;
			}
		} else if (!gsDesc.equals(other.gsDesc)) {
			return false;
		}
		if (gsIN == null) {
			if (other.gsIN != null) {
				return false;
			}
		} else if (!gsIN.equals(other.gsIN)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return gsIN.hashCode();
	}

	public abstract void initialize(MethodVisitor mv, String owner);

	public final void invokeRead(MethodVisitor mv, FieldInfo fi) {

		if (fi.canBeOpaque()) {
			String[] slot = slots.get(fi);
			mv.visitMethodInsn(INVOKESTATIC, slot[0], slot[3], slot[4], false);
		} else {
			mv.visitMethodInsn(INVOKEVIRTUAL, gsIN, fi.disregardReference() ? _GraphSerializer.readOpaque : _GraphSerializer.read, _GraphSerializer.read_D, false);
		}
	}

	public final void invokeWrite(MethodVisitor mv, FieldInfo fi) {
		if (fi.canBeOpaque()) {
			String[] slot = slots.get(fi);
			mv.visitMethodInsn(INVOKESTATIC, slot[0], slot[1], slot[2], false);
		} else {
			mv.visitMethodInsn(INVOKEVIRTUAL, gsIN, fi.disregardReference() ? _GraphSerializer.writeData : _GraphSerializer.write, _GraphSerializer.write_D, false);
		}
	}

	public final void onStack(MethodVisitor mv, String owner, FieldInfo fi) {
		if (!fi.canBeOpaque()) {
			mv.visitFieldInsn(GETSTATIC, owner, name, gsDesc);
		}
	}

	public final void patchOpaqueGuards(ClassVisitor cv, String owner, FieldInfo fi, int seqNum) {
		String[] sl = new String[5];
		sl[0] = owner;

		MethodVisitor mv = cv.visitMethod(ACC_STATIC, sl[1] = String.format("writeMaybeOpaque%s", seqNum < 0 ? "" : Integer.toString(seqNum)), sl[2] = _GraphSerializer.write_D, null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, _GraphSerializer._R_.REF_OFFSET);
		mv.visitMethodInsn(INVOKEVIRTUAL, _Object.name, _Object.getClass, _Object.getClass_D, false);
		mv.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.isProbablyOpaque, _SerializerFactory.isProbablyOpaque_D, false);

		Label nonOpaque = new Label();
		mv.visitJumpInsn(IFEQ, nonOpaque);

		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(ICONST_0);
		mv.visitMethodInsn(INVOKEVIRTUAL, _Sink.name, _Sink.putB, _Sink.putB_D, false);
		if (_Sink.fluent) {
			mv.visitInsn(POP);
		}
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEVIRTUAL, _Context.name, fi.disregardReference() ? _Context.writeTypeAndData : _Context.writeRefAndData, _Context.writeNested_D, false);
		Label exit = new Label();
		mv.visitJumpInsn(GOTO, exit);

		mv.visitLabel(nonOpaque);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(ICONST_1);
		mv.visitMethodInsn(INVOKEVIRTUAL, _Sink.desc, _Sink.putB, _Sink.putB_D, false);
		if (_Sink.fluent) {
			mv.visitInsn(POP);
		}

		mv.visitFieldInsn(GETSTATIC, owner, name, gsDesc);
		mv.visitVarInsn(ALOAD, _GraphSerializer._R_.CTX_OFFSET);
		mv.visitVarInsn(ALOAD, _GraphSerializer._R_.STR_OFFSET);
		mv.visitVarInsn(ALOAD, _GraphSerializer._R_.REF_OFFSET);
		mv.visitMethodInsn(INVOKEVIRTUAL, gsIN, fi.disregardReference() ? _GraphSerializer.writeData : _GraphSerializer.write, _GraphSerializer.write_D, false);

		mv.visitLabel(exit);
		mv.visitInsn(RETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();

		mv = cv.visitMethod(ACC_STATIC, sl[3] = String.format("readMaybeOpaque%s", seqNum < 0 ? "" : Integer.toString(seqNum)), sl[4] = Symbols._R_readDataDesc(fi.type().desc), null, null);
		mv.visitCode();

		mv.visitVarInsn(ALOAD, _GraphSerializer._R_.STR_OFFSET);
		mv.visitMethodInsn(INVOKEVIRTUAL, _Source.name, _Source.getB, _Source.getB_D, false);

		nonOpaque = new Label();
		mv.visitJumpInsn(IFNE, nonOpaque);
		mv.visitVarInsn(ALOAD, _GraphSerializer._R_.CTX_OFFSET);
		mv.visitVarInsn(ALOAD, _GraphSerializer._R_.STR_OFFSET);
		mv.visitMethodInsn(INVOKEVIRTUAL, _Context.name, fi.disregardReference() ? _Context.readTypeAndData : _Context.readRefAndData, _Context.readNested_D, false);
		mv.visitTypeInsn(CHECKCAST, fi.getTypeInternalName());
		mv.visitInsn(ARETURN);

		mv.visitLabel(nonOpaque);
		mv.visitFieldInsn(GETSTATIC, owner, name, gsDesc);
		mv.visitVarInsn(ALOAD, _GraphSerializer._R_.CTX_OFFSET);
		mv.visitVarInsn(ALOAD, _GraphSerializer._R_.STR_OFFSET);
		mv.visitMethodInsn(INVOKEVIRTUAL, gsIN, fi.disregardReference() ? _GraphSerializer.readOpaque : _GraphSerializer.read, _GraphSerializer.read_D, false);
		mv.visitTypeInsn(CHECKCAST, fi.getTypeInternalName());
		mv.visitInsn(ARETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();

		if (slots == null) {
			slots = new TCustomHashMap<>(IdentityHashingStrategy.INSTANCE);
		}

		slots.put(fi, sl);
	}

	public final void setName(String name) {
		this.name = name;
	}

}