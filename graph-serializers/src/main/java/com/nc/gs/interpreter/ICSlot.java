package com.nc.gs.interpreter;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static symbols.io.abstraction._GraphSerializer._R_.CTX_OFFSET;
import static symbols.io.abstraction._GraphSerializer._R_.REF_OFFSET;
import static symbols.io.abstraction._GraphSerializer._R_.STR_OFFSET;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import symbols.io.abstraction._GraphSerializer;
import symbols.io.abstraction._SerializerFactory;
import symbols.io.abstraction._Source;
import symbols.java.lang._Object;

/**
 * A container for generating inline caches.
 * 
 * @author cmuramoto
 */
public class ICSlot {

	public String serializer;
	public String gsp;
	Hierarchy h;
	public String writeName;
	public String writeDesc;
	public String readName;
	public String readDesc;
	public boolean op;

	// public boolean patchCacheMissWithFallback;

	public ICSlot(String serializer, String gsp, Hierarchy h, String writeName, String readName, boolean op) {
		super();
		this.serializer = serializer;
		this.gsp = gsp;
		this.h = h;
		this.writeName = writeName;
		this.readName = readName;
		this.op = op;
	}

	public void emitFieldDeclarations(MethodVisitor clinit) {
		if (h == null || h.sers == null || (h.serNames == null && gsp == null)) {
			return;
		}
		for (int i = 0; i < h.sers.length; i++) {
			if ((h.reified & (1L << i)) == 0) {
				String fn = gsp + i;
				String fDesc = h.sers[i].getDescriptor();

				clinit.visitLdcInsn(h.types[i].type());
				clinit.visitMethodInsn(INVOKESTATIC, _SerializerFactory.name, _SerializerFactory.serializer, _SerializerFactory.serializer_D, false);
				clinit.visitTypeInsn(CHECKCAST, h.sers[i].getInternalName());
				clinit.visitFieldInsn(PUTSTATIC, serializer, fn, fDesc);
			}
		}
	}

	public void patchInlineCaches(ClassVisitor cv) {
		patchInlineCaches(cv, true);
	}

	public void patchInlineCaches(ClassVisitor cv, boolean autoPatchDelegates) {
		int len = (h.types == null) ? 0 : h.types.length;

		int ng = !h.complete ? len + 1 : len;

		if (autoPatchDelegates) {
			for (int i = 0; i < len; i++) {
				if ((h.reified & (1L << i)) == 0) {
					String fn = gsp + i;
					String fDesc = h.sers[i].getDescriptor();
					FieldVisitor fv = cv.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, fn, fDesc, null, null);
					fv.visitEnd();
				}
			}
		}

		MethodVisitor mv = cv.visitMethod(ACC_PRIVATE | ACC_STATIC, writeName, writeDesc == null ? _GraphSerializer.write_D : writeDesc, null, null);
		mv.visitCode();

		if (len == 0) {
			mv.visitVarInsn(ALOAD, CTX_OFFSET);
			mv.visitVarInsn(ALOAD, STR_OFFSET);
			mv.visitVarInsn(ALOAD, REF_OFFSET);
			Symbols.nullSafeWrite(mv, op);
			mv.visitInsn(RETURN);
		} else {

			if (!h.complete) {
				mv.visitVarInsn(ALOAD, REF_OFFSET);
				mv.visitMethodInsn(INVOKEVIRTUAL, _Object.name, _Object.getClass, _Object.getClass_D, false);
				mv.visitVarInsn(Opcodes.ASTORE, REF_OFFSET + 1);
			}

			Label ret = new Label();

			for (int i = 0; i < ng; i++) {
				Label l = i < (ng - 1) ? new Label() : null;

				if (l != null) {
					if (!h.complete) {
						mv.visitVarInsn(ALOAD, REF_OFFSET + 1);
						mv.visitLdcInsn(h.types[i].type());
						mv.visitJumpInsn(Opcodes.IF_ACMPNE, l);
					} else {
						mv.visitVarInsn(ALOAD, REF_OFFSET);
						mv.visitTypeInsn(INSTANCEOF, h.types[i].name);
						mv.visitJumpInsn(IFEQ, l);
					}
				}

				if (ng > 1) {
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					Symbols.loadNumber(mv, i);
					Symbols.putByte(mv);
				}

				if (i < len && (h.reified & (1L << i)) == 0) {
					if (h.serNames == null) {
						mv.visitFieldInsn(GETSTATIC, serializer, gsp + i, h.sers[i].getDescriptor());
					} else {
						mv.visitFieldInsn(GETSTATIC, serializer, h.serNames[i], h.sers[i].getDescriptor());
					}
				}

				mv.visitVarInsn(ALOAD, CTX_OFFSET);
				mv.visitVarInsn(ALOAD, STR_OFFSET);
				mv.visitVarInsn(ALOAD, REF_OFFSET);

				if (i < len) {

					if ((h.reified & (1L << i)) == 0) {
						Symbols.invokeWriteWithOwner(mv, h.sers[i].getInternalName(), op);
					} else {
						mv.visitTypeInsn(CHECKCAST, h.types[i].getInternalName());

						Symbols._R_invokeWrite(mv, h.sers[i].getInternalName(), h.types[i], op);
					}
				} else {
					Symbols.nullSafeWrite(mv, op);
				}

				if (l != null) {
					mv.visitJumpInsn(GOTO, ret);
					mv.visitLabel(l);
				}
			}

			if (ng > 1) {
				mv.visitLabel(ret);
			}
			mv.visitInsn(RETURN);
		}
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		mv = cv.visitMethod(ACC_PRIVATE | ACC_STATIC, readName, readDesc == null ? _GraphSerializer.read_D : readDesc, null, null);
		mv.visitCode();

		if (len == 0) {
			mv.visitVarInsn(ALOAD, CTX_OFFSET);
			mv.visitVarInsn(ALOAD, STR_OFFSET);

			Symbols.nullSafeRead(mv, op);

			mv.visitInsn(ARETURN);
		} else if (ng == 1) {
			if ((h.reified & (1L)) == 0) {
				if (h.serNames == null) {
					mv.visitFieldInsn(GETSTATIC, serializer, gsp + 0, h.sers[0].getDescriptor());
				} else {
					mv.visitFieldInsn(GETSTATIC, serializer, h.serNames[0], h.sers[0].getDescriptor());
				}
			}

			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);

			if ((h.reified & (1L)) == 0) {
				Symbols.invokeReadWithOwner(mv, h.sers[0].getInternalName(), op);
				if (readDesc != null) {
					mv.visitTypeInsn(CHECKCAST, h.types[0].name);
				}
			} else {
				Symbols._R_invokeRead(mv, h.sers[0].getInternalName(), h.types[0], op);
			}

			mv.visitInsn(ARETURN);
		} else {

			mv.visitVarInsn(ALOAD, STR_OFFSET);
			mv.visitMethodInsn(INVOKEVIRTUAL, _Source.name, _Source.getB, _Source.getB_D, false);

			Label[] lbls = new Label[ng];
			Label def = new Label();

			for (int i = 0; i < lbls.length; i++) {
				lbls[i] = new Label();
			}

			mv.visitTableSwitchInsn(0, lbls.length - 1, def, lbls);

			for (int i = 0; i < lbls.length; i++) {
				mv.visitLabel(lbls[i]);

				if (i < len && (h.reified & (1L << i)) == 0) {
					if (h.serNames == null) {
						mv.visitFieldInsn(GETSTATIC, serializer, gsp + i, h.sers[i].getDescriptor());
					} else {
						mv.visitFieldInsn(GETSTATIC, serializer, h.serNames[i], h.sers[i].getDescriptor());
					}
				}

				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ALOAD, 1);

				if (i < len) {
					if ((h.reified & (1L << i)) == 0) {
						Symbols.invokeReadWithOwner(mv, h.sers[i].getInternalName(), op);
						if (readDesc != null) {
							mv.visitTypeInsn(CHECKCAST, h.types[i].name);
						}
					} else {
						Symbols._R_invokeRead(mv, h.sers[i].getInternalName(), h.types[i], op);
					}
				} else {
					Symbols.nullSafeRead(mv, op);
					if (readDesc != null) {
						mv.visitTypeInsn(CHECKCAST, h.superType.name);
					}
				}

				mv.visitInsn(ARETURN);
			}

			mv.visitLabel(def);
			mv.visitInsn(ACONST_NULL);
			mv.visitInsn(ARETURN);
		}

		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

}