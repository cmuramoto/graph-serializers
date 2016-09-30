package com.nc.gs.generator;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IAND;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static symbols.io.abstraction._GraphSerializer._R_.CTX_OFFSET;
import static symbols.io.abstraction._GraphSerializer._R_.REF_OFFSET;
import static symbols.io.abstraction._GraphSerializer._R_.STR_OFFSET;
import static symbols.io.abstraction._GraphSerializer._R_.U_OFFSET;

import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.nc.gs.interpreter.ClassInfo;
import com.nc.gs.interpreter.ExtendedType;
import com.nc.gs.interpreter.FieldInfo;
import com.nc.gs.interpreter.SpecialField;
import com.nc.gs.interpreter.Symbols;
import com.nc.gs.util.Utils;

import symbols.io.abstraction._Context;
import symbols.io.abstraction._GraphSerializer._R_;
import symbols.io.abstraction._Sink;
import symbols.io.abstraction._Source;
import symbols.io.abstraction._Util;
import symbols.sun.misc._Unsafe;

public final class InheritanceGraphClassAdapterV3 extends GraphClassAdapter {

	public InheritanceGraphClassAdapterV3(ClassInfo ci, ClassVisitor cv) {
		super(ci, GenerationStrategy.FULL_HIERARCHY, cv);
	}

	boolean checkNeedsUnsafe(List<FieldInfo> infos, boolean forWrite) {
		if (infos == null) {
			return false;
		}

		for (FieldInfo fi : infos) {
			if (forWrite) {
				if (!fi.isReadAccessible(root.basic())) {
					return true;
				}
			} else {
				if (!fi.isWriteAccessible(root.basic())) {
					return true;
				}
			}
		}

		return false;
	}

	private void doPrimitiveArray(MethodVisitor mv, FieldInfo fi, int arrayIx, boolean accessible) {
		ExtendedType et = fi.type().basicComponentType();

		if (fi.disregardReference()) {
			mv.visitVarInsn(ALOAD, STR_OFFSET);
			mv.visitMethodInsn(INVOKEVIRTUAL, _Source.Decompressor, _Source.unpackI, _Source.unpackI_D, false);
			mv.visitIntInsn(NEWARRAY, et.newArrayOpcode());
			mv.visitVarInsn(ASTORE, arrayIx);

		} else {
			mv.visitVarInsn(ALOAD, STR_OFFSET);
			mv.visitMethodInsn(INVOKEVIRTUAL, _Source.Decompressor, _Source.unpackI, _Source.unpackI_D, false);
			mv.visitVarInsn(ISTORE, arrayIx + 1);

			mv.visitVarInsn(ALOAD, CTX_OFFSET);
			mv.visitVarInsn(ILOAD, arrayIx + 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, _Context.name, _Context.from, _Context.from_D, false);
			mv.visitVarInsn(ASTORE, arrayIx);

			mv.visitVarInsn(ALOAD, arrayIx);

			Label isNull = new Label();
			mv.visitJumpInsn(IFNONNULL, isNull);

			mv.visitVarInsn(ALOAD, STR_OFFSET);
			mv.visitMethodInsn(INVOKEVIRTUAL, _Source.name, _Source.unpackI, _Source.unpackI_D, false);
			mv.visitIntInsn(NEWARRAY, et.newArrayOpcode());
			mv.visitVarInsn(ASTORE, arrayIx);

			mv.visitVarInsn(ALOAD, CTX_OFFSET);
			mv.visitVarInsn(ALOAD, arrayIx);
			mv.visitVarInsn(ILOAD, arrayIx + 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, _Context.name, _Context.mark, _Context.mark_D, false);

			mv.visitLabel(isNull);
		}

		mv.visitVarInsn(ALOAD, STR_OFFSET);
		mv.visitVarInsn(ALOAD, arrayIx);
		mv.visitTypeInsn(CHECKCAST, fi.type().desc);
		mv.visitMethodInsn(INVOKEVIRTUAL, _Source.name, _Source.inflate, "(" + fi.type().desc + ")V", false);

		if (accessible) {
			mv.visitVarInsn(ALOAD, REF_OFFSET);
			mv.visitVarInsn(ALOAD, arrayIx);
			mv.visitTypeInsn(CHECKCAST, fi.type().desc);
			mv.visitFieldInsn(PUTFIELD, fi.owner().name, fi.name, fi.desc);
		} else {
			mv.visitVarInsn(ALOAD, U_OFFSET);
			mv.visitVarInsn(ALOAD, REF_OFFSET);
			mv.visitLdcInsn(fi.objectFieldOffset());
			mv.visitVarInsn(ALOAD, arrayIx);
			mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.putObject, _Unsafe.putObject_D, false);
		}
	}

	private void implementReadNonNull(MethodVisitor mv, boolean needsUnsafe) {
		List<FieldInfo> infos = nonNullable;

		if (infos == null || infos.isEmpty()) {
			return;
		}

		final List<FieldInfo> nulls = nullable;
		final int baseOffset = needsUnsafe ? 3 : 2;
		final int storeIx = (nulls == null || nulls.isEmpty()) ? baseOffset + 1 : baseOffset + 2;

		for (FieldInfo fi : infos) {
			// push value on stack, but don't map it to a local var
			if (fi.isWriteAccessible(root)) {
				if (fi.isEnum()) {
					String cvName = fieldNameForEnum(fi.getTypeInternalName());
					String arrayDesc = "[" + fi.desc;
					mv.visitVarInsn(ALOAD, REF_OFFSET);
					mv.visitFieldInsn(GETSTATIC, targetName, cvName, arrayDesc);
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					mv.visitMethodInsn(INVOKEVIRTUAL, _Source.name, _Source.unpackI, _Source.unpackI_D, false);
					mv.visitInsn(AALOAD);

					mv.visitFieldInsn(PUTFIELD, fi.owner().name, fi.name, fi.desc);
				} else if (fi.isOneDimensionPrimitiveArray()) {
					doPrimitiveArray(mv, fi, storeIx, true);
				} else if (fi.isWrapper()) {
					mv.visitVarInsn(ALOAD, REF_OFFSET);
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					Symbols.readAndBox(mv, fi);
					mv.visitFieldInsn(PUTFIELD, fi.owner().name, fi.name, fi.desc);
				} else if (fi.isSpecial()) {
					SpecialField cf = specialFieldFor(fi);

					mv.visitVarInsn(ALOAD, REF_OFFSET);

					cf.onStack(mv, targetName, fi);

					mv.visitVarInsn(ALOAD, CTX_OFFSET);
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					cf.invokeRead(mv, fi);
					if (!fi.canBeOpaque()) {
						mv.visitTypeInsn(CHECKCAST, fi.getTypeInternalName());
					}
					mv.visitFieldInsn(PUTFIELD, fi.owner().name, fi.name, fi.desc);
				} else if (fi.isTypeFinalOrLeaf()) {
					CachedField cf = serializerNameFor(fi.asmType(), fi.isCompressed());

					if (cf.isReified) {
						mv.visitVarInsn(ALOAD, REF_OFFSET);
						mv.visitVarInsn(ALOAD, CTX_OFFSET);
						mv.visitVarInsn(ALOAD, STR_OFFSET);
						Symbols.invokeReadReified(mv, cf.serializerIN, fi);
						mv.visitFieldInsn(PUTFIELD, fi.owner().name, fi.name, fi.desc);
					} else {
						mv.visitVarInsn(ALOAD, REF_OFFSET);
						cf.onStack(mv, targetName);

						mv.visitVarInsn(ALOAD, CTX_OFFSET);
						mv.visitVarInsn(ALOAD, STR_OFFSET);
						Symbols.invokeReadWithOwner(mv, cf.serializerIN, fi);
						mv.visitTypeInsn(CHECKCAST, fi.getTypeInternalName());
						mv.visitFieldInsn(PUTFIELD, fi.owner().name, fi.name, fi.desc);
					}
				} else if (fi.hasDeclaredHierarchy()) {
					mv.visitVarInsn(ALOAD, REF_OFFSET);

					mv.visitVarInsn(ALOAD, CTX_OFFSET);
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					ICKey key = new ICKey(fi);
					ICVal val = ics.get(key);
					mv.visitMethodInsn(INVOKESTATIC, targetName, val.readMethod, val.readMethodDesc, false);

					mv.visitFieldInsn(PUTFIELD, fi.owner().name, fi.name, fi.desc);
				} else {
					mv.visitVarInsn(ALOAD, REF_OFFSET);

					mv.visitVarInsn(ALOAD, CTX_OFFSET);
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					Symbols.nullSafeRead(mv, fi);

					mv.visitTypeInsn(CHECKCAST, fi.asmType().getInternalName());
					mv.visitFieldInsn(PUTFIELD, fi.owner().name, fi.name, fi.desc);
				}
			} else {
				if (fi.isEnum()) {
					String cvName = fieldNameForEnum(fi.asmType().getInternalName());
					String arrayDesc = "[" + fi.desc;

					mv.visitVarInsn(ALOAD, U_OFFSET);
					mv.visitVarInsn(ALOAD, REF_OFFSET);
					mv.visitLdcInsn(fi.objectFieldOffset());

					mv.visitFieldInsn(GETSTATIC, targetName, cvName, arrayDesc);
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					mv.visitMethodInsn(INVOKEVIRTUAL, _Source.Decompressor, _Source.unpackI, _Source.unpackI_D, false);
					mv.visitInsn(AALOAD);

					mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.putObject, _Unsafe.putObject_D, false);
				} else if (fi.isOneDimensionPrimitiveArray()) {
					doPrimitiveArray(mv, fi, storeIx, false);
				} else if (fi.isWrapper()) {
					mv.visitVarInsn(ALOAD, U_OFFSET);
					mv.visitVarInsn(ALOAD, REF_OFFSET);
					mv.visitLdcInsn(fi.objectFieldOffset());
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					Symbols.readAndBox(mv, fi);
					mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.putObject, _Unsafe.putObject_D, false);
				} else if (fi.isSpecial()) {
					SpecialField cf = specialFieldFor(fi);

					mv.visitVarInsn(ALOAD, U_OFFSET);
					mv.visitVarInsn(ALOAD, REF_OFFSET);
					mv.visitLdcInsn(fi.objectFieldOffset());

					cf.onStack(mv, targetName, fi);
					mv.visitVarInsn(ALOAD, CTX_OFFSET);
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					cf.invokeRead(mv, fi);

					mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.putObject, _Unsafe.putObject_D, false);
				} else if (fi.isTypeFinalOrLeaf()) {

					CachedField cf = serializerNameFor(fi.asmType(), fi.isCompressed());

					if (cf.isReified) {
						mv.visitVarInsn(ALOAD, U_OFFSET);
						mv.visitVarInsn(ALOAD, REF_OFFSET);
						mv.visitLdcInsn(fi.objectFieldOffset());

						mv.visitVarInsn(ALOAD, CTX_OFFSET);
						mv.visitVarInsn(ALOAD, STR_OFFSET);
						Symbols.invokeReadReified(mv, cf.serializerIN, fi);

						mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.putObject, _Unsafe.putObject_D, false);
					} else {
						mv.visitVarInsn(ALOAD, U_OFFSET);
						mv.visitVarInsn(ALOAD, REF_OFFSET);
						mv.visitLdcInsn(fi.objectFieldOffset());

						cf.onStack(mv, targetName);
						mv.visitVarInsn(ALOAD, CTX_OFFSET);
						mv.visitVarInsn(ALOAD, STR_OFFSET);
						cf.invokeWrite(mv, fi);

						mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.putObject, _Unsafe.putObject_D, false);

					}
				} else if (fi.hasDeclaredHierarchy()) {
					mv.visitVarInsn(ALOAD, U_OFFSET);
					mv.visitVarInsn(ALOAD, REF_OFFSET);
					mv.visitLdcInsn(fi.objectFieldOffset());

					mv.visitVarInsn(ALOAD, CTX_OFFSET);
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					ICKey key = new ICKey(fi);
					ICVal val = ics.get(key);
					mv.visitMethodInsn(INVOKESTATIC, targetName, val.readMethod, val.readMethodDesc, false);

					mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.putObject, _Unsafe.putObject_D, false);
				} else {
					mv.visitVarInsn(ALOAD, U_OFFSET);
					mv.visitVarInsn(ALOAD, REF_OFFSET);
					mv.visitLdcInsn(fi.objectFieldOffset());

					mv.visitVarInsn(ALOAD, CTX_OFFSET);
					mv.visitVarInsn(ALOAD, STR_OFFSET);

					Symbols.nullSafeRead(mv, fi);
					mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.putObject, _Unsafe.putObject_D, false);

				}
			}
		}
	}

	private void implementReadNull(MethodVisitor mv, boolean unsafeOnStack) {
		List<FieldInfo> infos = nullable;

		if (infos == null || infos.isEmpty()) {
			return;
		}

		int baseOffset = unsafeOnStack ? 3 : 2;
		int flIx = baseOffset + 1;
		final int storeIx = flIx + 1;

		List<List<FieldInfo>> partition = Utils.partition(infos, 16);

		// boolean first = true;
		for (List<FieldInfo> list : partition) {

			mv.visitVarInsn(ALOAD, STR_OFFSET);
			// mv.visitMethodInsn(INVOKESTATIC, _Sink.Compressor, _Source.unpackI,
			// _Source.unpackI_D, false);
			mv.visitMethodInsn(INVOKEVIRTUAL, _Source.Decompressor, _Source.unpackI, _Source.unpackI_D, false);
			mv.visitVarInsn(ISTORE, flIx);

			int shift = 0;

			for (FieldInfo fi : list) {
				mv.visitVarInsn(ILOAD, flIx);
				Symbols.loadNumber(mv, 1 << shift);
				mv.visitInsn(IAND);

				Label l = new Label();
				mv.visitJumpInsn(IFEQ, l);

				if (fi.isWriteAccessible(root.basic())) {
					if (fi.isEnum()) {
						String cvName = fieldNameForEnum(fi.asmType().getInternalName());
						String arrayDesc = "[" + fi.desc;
						mv.visitVarInsn(ALOAD, REF_OFFSET);
						mv.visitFieldInsn(GETSTATIC, targetName, cvName, arrayDesc);
						mv.visitVarInsn(ALOAD, STR_OFFSET);
						mv.visitMethodInsn(INVOKEVIRTUAL, _Source.Decompressor, _Source.unpackI, _Source.unpackI_D, false);
						mv.visitInsn(AALOAD);

						mv.visitFieldInsn(PUTFIELD, fi.owner().name, fi.name, fi.desc);
					} else if (fi.isOneDimensionPrimitiveArray()) {
						doPrimitiveArray(mv, fi, storeIx, true);
					} else if (fi.isWrapper()) {
						mv.visitVarInsn(ALOAD, REF_OFFSET);
						mv.visitVarInsn(ALOAD, STR_OFFSET);
						Symbols.readAndBox(mv, fi);
						mv.visitFieldInsn(PUTFIELD, fi.owner().name, fi.name, fi.desc);
					} else if (fi.hasDeclaredHierarchy()) {
						mv.visitVarInsn(ALOAD, REF_OFFSET);
						mv.visitVarInsn(ALOAD, CTX_OFFSET);
						mv.visitVarInsn(ALOAD, STR_OFFSET);
						ICKey key = new ICKey(fi);
						ICVal val = ics.get(key);
						mv.visitMethodInsn(INVOKESTATIC, targetName, val.readMethod, val.readMethodDesc, false);
						mv.visitFieldInsn(PUTFIELD, fi.owner().name, fi.name, fi.desc);
					} else if (fi.isSpecial()) {
						SpecialField sf = specialFieldFor(fi);
						mv.visitVarInsn(ALOAD, REF_OFFSET);

						sf.onStack(mv, targetName, fi);
						mv.visitVarInsn(ALOAD, CTX_OFFSET);
						mv.visitVarInsn(ALOAD, STR_OFFSET);

						sf.invokeRead(mv, fi);
						if (!fi.canBeOpaque()) {
							mv.visitTypeInsn(CHECKCAST, fi.asmType().getInternalName());
						}
						mv.visitFieldInsn(PUTFIELD, fi.owner().name, fi.name, fi.desc);

					} else if (fi.isTypeFinalOrLeaf()) {

						CachedField cf = serializerNameFor(fi.asmType(), fi.isCompressed());

						if (cf.isReified) {
							mv.visitVarInsn(ALOAD, REF_OFFSET);
							mv.visitVarInsn(ALOAD, CTX_OFFSET);
							mv.visitVarInsn(ALOAD, STR_OFFSET);
							Symbols.invokeReadReified(mv, cf.serializerIN, fi);
							mv.visitFieldInsn(PUTFIELD, fi.owner().name, fi.name, fi.desc);
						} else {
							mv.visitVarInsn(ALOAD, REF_OFFSET);
							mv.visitFieldInsn(GETSTATIC, targetName, cf.targetFieldName, cf.serializerDesc());
							mv.visitVarInsn(ALOAD, CTX_OFFSET);
							mv.visitVarInsn(ALOAD, STR_OFFSET);
							Symbols.invokeReadWithOwner(mv, cf.serializerIN, fi);
							mv.visitTypeInsn(CHECKCAST, fi.asmType().getInternalName());
							mv.visitFieldInsn(PUTFIELD, fi.owner().name, fi.name, fi.desc);
						}
					} else {
						mv.visitVarInsn(ALOAD, REF_OFFSET);
						mv.visitVarInsn(ALOAD, CTX_OFFSET);
						mv.visitVarInsn(ALOAD, STR_OFFSET);
						Symbols.nullSafeRead(mv, fi);
						mv.visitTypeInsn(CHECKCAST, fi.asmType().getInternalName());
						mv.visitFieldInsn(PUTFIELD, fi.owner().name, fi.name, fi.desc);
					}
				} else {
					if (fi.isEnum()) {
						String cvName = fieldNameForEnum(fi.asmType().getInternalName());
						String arrayDesc = "[" + fi.desc;

						mv.visitVarInsn(ALOAD, U_OFFSET);
						mv.visitVarInsn(ALOAD, REF_OFFSET);
						mv.visitLdcInsn(fi.objectFieldOffset());

						mv.visitFieldInsn(GETSTATIC, targetName, cvName, arrayDesc);
						mv.visitVarInsn(ALOAD, STR_OFFSET);
						mv.visitMethodInsn(INVOKEVIRTUAL, _Source.Decompressor, _Source.unpackI, _Source.unpackI_D, false);
						mv.visitInsn(AALOAD);

						mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.putObject, _Unsafe.putObject_D, false);
					} else if (fi.isOneDimensionPrimitiveArray()) {
						doPrimitiveArray(mv, fi, storeIx, false);
					} else if (fi.isWrapper()) {
						mv.visitVarInsn(ALOAD, U_OFFSET);
						mv.visitVarInsn(ALOAD, REF_OFFSET);
						mv.visitLdcInsn(fi.objectFieldOffset());

						mv.visitVarInsn(ALOAD, STR_OFFSET);
						Symbols.readAndBox(mv, fi);
						mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.putObject, _Unsafe.putObject_D, false);
					} else if (fi.isSpecial()) {
						SpecialField cf = specialFieldFor(fi);

						mv.visitVarInsn(ALOAD, U_OFFSET);
						mv.visitVarInsn(ALOAD, REF_OFFSET);
						mv.visitLdcInsn(fi.objectFieldOffset());

						cf.onStack(mv, targetName, fi);
						mv.visitVarInsn(ALOAD, CTX_OFFSET);
						mv.visitVarInsn(ALOAD, STR_OFFSET);
						cf.invokeRead(mv, fi);

						mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.putObject, _Unsafe.putObject_D, false);

					} else if (fi.isTypeFinalOrLeaf()) {

						CachedField cf = serializerNameFor(fi.asmType(), fi.isCompressed());

						if (cf.isReified) {
							mv.visitVarInsn(ALOAD, U_OFFSET);
							mv.visitVarInsn(ALOAD, REF_OFFSET);
							mv.visitLdcInsn(fi.objectFieldOffset());

							mv.visitVarInsn(ALOAD, CTX_OFFSET);
							mv.visitVarInsn(ALOAD, STR_OFFSET);
							Symbols.invokeReadReified(mv, cf.serializerIN, fi);

							mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.putObject, _Unsafe.putObject_D, false);
						} else {
							mv.visitVarInsn(ALOAD, U_OFFSET);
							mv.visitVarInsn(ALOAD, REF_OFFSET);
							mv.visitLdcInsn(fi.objectFieldOffset());

							mv.visitFieldInsn(GETSTATIC, targetName, cf.targetFieldName, cf.serializerDesc());
							mv.visitVarInsn(ALOAD, CTX_OFFSET);
							mv.visitVarInsn(ALOAD, STR_OFFSET);
							Symbols.invokeReadWithOwner(mv, cf.serializerIN, fi);

							mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.putObject, _Unsafe.putObject_D, false);

						}
					} else if (fi.hasDeclaredHierarchy()) {
						mv.visitVarInsn(ALOAD, U_OFFSET);
						mv.visitVarInsn(ALOAD, REF_OFFSET);
						mv.visitLdcInsn(fi.objectFieldOffset());

						mv.visitVarInsn(ALOAD, CTX_OFFSET);
						mv.visitVarInsn(ALOAD, STR_OFFSET);
						ICKey key = new ICKey(fi);
						ICVal val = ics.get(key);
						mv.visitMethodInsn(INVOKESTATIC, targetName, val.readMethod, val.readMethodDesc, false);
						mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.putObject, _Unsafe.putObject_D, false);
					} else {
						mv.visitVarInsn(ALOAD, U_OFFSET);
						mv.visitVarInsn(ALOAD, REF_OFFSET);
						mv.visitLdcInsn(fi.objectFieldOffset());

						mv.visitVarInsn(ALOAD, CTX_OFFSET);
						mv.visitVarInsn(ALOAD, STR_OFFSET);
						Symbols.nullSafeRead(mv, fi);

						mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.putObject, _Unsafe.putObject_D, false);
					}
				}

				mv.visitLabel(l);

				// if (first) {
				// mv.visitFrame(F_APPEND, 1, new Object[] {}, 0, null);
				// } else {
				// mv.visitFrame(F_SAME, 0, null, 0, null);
				// }

				shift++;
			}

		}

	}

	private void implementReadPayload() {
		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC + ACC_STATIC, _R_.inflateData, Symbols.reifiedInflateDataDesc(root.type()), null, null);

		boolean needsUnsafe = verifyNeedForUnsafe(mv, false);

		implementReadNull(mv, needsUnsafe);

		implementReadNonNull(mv, needsUnsafe);

		implementReadPrimitives(mv, prims);

		mv.visitCode();
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private void implementReadPrimitives(MethodVisitor mv, List<FieldInfo> prims) {
		if (prims == null) {
			return;
		}

		for (FieldInfo fi : prims) {

			Type type = fi.asmType();

			if (fi.isReadAccessible(root.basic())) {
				Symbols.readAccessiblePrimitive(mv, 1, 2, fi.owner().name, fi);
			} else {
				Symbols.readInaccessiblePrimitive(mv, 1, 2, 3, type, fi.objectFieldOffset(), fi.isCompressed());
			}
		}

	}

	private void implementWriteNonNull(MethodVisitor mv, boolean unsafeOnStack) {
		List<FieldInfo> infos = nonNullable;

		if (infos == null || infos.isEmpty()) {
			return;
		}

		for (FieldInfo fi : infos) {

			CachedField cf = null;
			SpecialField sf = null;
			boolean oneDArray = fi.isOneDimensionPrimitiveArray();
			String iN = fi.getTypeInternalName();

			// primitives will not consume the context
			if (!fi.isEnum() && !fi.isWrapper() && !fi.isPrimitive()) {
				if (fi.isTypeFinalOrLeaf()) {
					cf = serializerNameFor(fi.asmType(), fi.isCompressed());

					cf.onStack(mv, targetName);
				} else if (fi.isSpecial() && !oneDArray) {
					sf = specialFieldFor(fi);

					sf.onStack(mv, targetName, fi);
				}

				if (!oneDArray) {
					mv.visitVarInsn(ALOAD, CTX_OFFSET);
				}
			}

			if (!oneDArray) {
				mv.visitVarInsn(ALOAD, STR_OFFSET);
				pushForRead(mv, fi);
			}
			// push value on stack

			// now we got [CachedSerializer?] [Context?] [_Sink] [Value]

			if (fi.isEnum()) {
				mv.visitTypeInsn(CHECKCAST, iN);
				mv.visitMethodInsn(INVOKEVIRTUAL, iN, "ordinal", "()I", false);
				mv.visitMethodInsn(INVOKEVIRTUAL, _Sink.name, _Sink.packI, _Sink.packI_D, false);
			} else if (oneDArray) {
				ExtendedType at = fi.type().basicComponentType();

				if (fi.disregardReference()) {
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					pushForRead(mv, fi);
					mv.visitTypeInsn(CHECKCAST, iN);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitMethodInsn(INVOKEVIRTUAL, _Sink.Compressor, _Sink.packI, _Sink.packI_D, false);

					mv.visitVarInsn(ALOAD, STR_OFFSET);
					pushForRead(mv, fi);
					mv.visitTypeInsn(CHECKCAST, iN);
					mv.visitMethodInsn(INVOKEVIRTUAL, _Sink.name, _Sink.write, "(" + fi.type().desc + ")V", false);
				} else {
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					mv.visitVarInsn(ALOAD, CTX_OFFSET);
					mv.visitLdcInsn(at.getSort());
					pushForRead(mv, fi);
					pushForRead(mv, fi);
					mv.visitTypeInsn(CHECKCAST, iN);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitMethodInsn(INVOKEVIRTUAL, _Sink.name, _Sink.writePrimitiveArray, _Sink.writePrimitiveArrayAndRef_D, false);
				}
			} else if (fi.isWrapper()) {
				mv.visitTypeInsn(CHECKCAST, iN);
				Symbols.unboxAndWrite(mv, fi);
			} else if (fi.isSpecial()) {
				sf.invokeWrite(mv, fi);
			} else if (fi.hasDeclaredHierarchy()) {
				mv.visitTypeInsn(CHECKCAST, iN);

				ICKey key = new ICKey(fi);
				ICVal val = ics.get(key);
				mv.visitMethodInsn(INVOKESTATIC, targetName, val.writeMethod, val.writeMethodDesc, false);
			} else if (fi.isTypeFinalOrLeaf()) {
				if (cf.isReified) {
					mv.visitTypeInsn(CHECKCAST, iN);
				}
				cf.invokeWrite(mv, fi);
			} else {
				// Fallback to Polymorphic invocation
				Symbols.nullSafeWrite(mv, fi);
			}
		}

	}

	private void implementWriteNull(MethodVisitor mv, boolean unsafeOnStack) {
		List<FieldInfo> infos = nullable;

		if (infos == null || infos.isEmpty()) {
			return;
		}

		final int baseOffset = unsafeOnStack ? 3 : 2;
		int ix = baseOffset + 1;

		// copy to local vars
		for (FieldInfo fi : infos) {
			String iN = fi.asmType().getInternalName();

			if (fi.isReadAccessible(root.basic())) {
				mv.visitVarInsn(ALOAD, REF_OFFSET);
				mv.visitFieldInsn(GETFIELD, fi.owner().name, fi.name, fi.desc);
			} else {
				mv.visitVarInsn(ALOAD, U_OFFSET);
				mv.visitVarInsn(ALOAD, REF_OFFSET);
				mv.visitLdcInsn(fi.objectFieldOffset());
				mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.getObject, _Unsafe.getObject_D, false);
				mv.visitTypeInsn(CHECKCAST, iN);
			}
			mv.visitVarInsn(ASTORE, ix++);
			// mv.visitInsn(Opcodes.POP);
		}

		int prevIx = ix = baseOffset + 1;

		List<List<FieldInfo>> partition = Utils.partition(infos, 16);

		for (List<FieldInfo> partInfo : partition) {

			ix = prevIx;

			mv.visitVarInsn(ALOAD, STR_OFFSET);
			for (int i = 0; i < partInfo.size(); i++) {
				mv.visitVarInsn(ALOAD, ix++);
			}

			mv.visitMethodInsn(INVOKESTATIC, _Util.name, _Sink.bitMapGuard, Utils.guardDesc(partInfo.size()), false);

			ix = prevIx;

			// boolean first = true;

			for (FieldInfo fi : partInfo) {
				// null guard
				Label notNull = new Label();
				mv.visitVarInsn(ALOAD, ix);
				mv.visitJumpInsn(IFNULL, notNull);

				Type type = fi.asmType();
				String iN = type.getInternalName();

				if (fi.isEnum()) {
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					mv.visitVarInsn(ALOAD, ix);
					mv.visitMethodInsn(INVOKEVIRTUAL, iN, "ordinal", "()I", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, _Sink.Compressor, _Sink.packI, _Sink.packI_D, false);
				} else if (fi.isOneDimensionPrimitiveArray()) {

					if (fi.disregardReference()) {

						mv.visitVarInsn(ALOAD, STR_OFFSET);
						mv.visitVarInsn(ALOAD, ix);
						mv.visitInsn(ARRAYLENGTH);
						mv.visitMethodInsn(INVOKEVIRTUAL, _Sink.Compressor, _Sink.packI, _Sink.packI_D, false);

						mv.visitVarInsn(ALOAD, STR_OFFSET);
						mv.visitVarInsn(ALOAD, ix);
						mv.visitMethodInsn(INVOKEVIRTUAL, _Sink.name, _Sink.write, "(" + fi.type().desc + ")V", false);
					} else {
						ExtendedType at = fi.type().basicComponentType();

						mv.visitVarInsn(ALOAD, STR_OFFSET);
						mv.visitVarInsn(ALOAD, CTX_OFFSET);
						mv.visitLdcInsn(at.getSort());
						mv.visitVarInsn(ALOAD, ix);
						mv.visitVarInsn(ALOAD, ix);
						mv.visitInsn(ARRAYLENGTH);
						mv.visitMethodInsn(INVOKEVIRTUAL, _Sink.name, _Sink.writePrimitiveArray, _Sink.writePrimitiveArrayAndRef_D, false);
					}
				} else if (fi.isWrapper()) {
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					mv.visitVarInsn(ALOAD, ix);
					Symbols.unboxAndWrite(mv, fi);

				} else if (fi.isSpecial()) {
					SpecialField cf = specialFieldFor(fi);

					cf.onStack(mv, targetName, fi);
					mv.visitVarInsn(ALOAD, CTX_OFFSET);
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					mv.visitVarInsn(ALOAD, ix);

					cf.invokeWrite(mv, fi);

				} else if (fi.isTypeFinalOrLeaf()) {

					CachedField cf = serializerNameFor(fi.asmType(), fi.isCompressed());

					if (cf.isReified) {
						mv.visitVarInsn(ALOAD, CTX_OFFSET);
						mv.visitVarInsn(ALOAD, STR_OFFSET);
						mv.visitVarInsn(ALOAD, ix);
						cf.invokeWrite(mv, fi);
					} else {
						mv.visitFieldInsn(GETSTATIC, targetName, cf.targetFieldName, cf.serializerDesc());

						mv.visitVarInsn(ALOAD, CTX_OFFSET);
						mv.visitVarInsn(ALOAD, STR_OFFSET);
						mv.visitVarInsn(ALOAD, ix);
						cf.invokeWrite(mv, fi);
					}

				} else if (fi.hasDeclaredHierarchy()) {
					mv.visitVarInsn(ALOAD, CTX_OFFSET);
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					mv.visitVarInsn(ALOAD, ix);

					ICKey key = new ICKey(fi);
					ICVal val = ics.get(key);
					mv.visitMethodInsn(INVOKESTATIC, targetName, val.writeMethod, val.writeMethodDesc, false);
				} else {
					mv.visitVarInsn(ALOAD, CTX_OFFSET);
					mv.visitVarInsn(ALOAD, STR_OFFSET);
					mv.visitVarInsn(ALOAD, ix);

					Symbols.nullSafeWrite(mv, fi);
				}

				mv.visitLabel(notNull);
				// if (first) {
				// mv.visitFrame(F_FULL, 0, new Object[] {}, 0,
				// new Object[] {});
				// } else {
				// mv.visitFrame(F_SAME, 0, null, 0, null);
				// }

				// first = false;
				ix++;
			}

			prevIx = ix;
		}
	}

	private void implementWritePayload() {

		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC + ACC_STATIC, _R_.writeData, Symbols.reifiedWritePayloadDesc(root.type()), null, null);

		boolean needsUnsafe = verifyNeedForUnsafe(mv, true);

		implementWriteNull(mv, needsUnsafe);

		implementWriteNonNull(mv, needsUnsafe);

		implementWritePrimitives(mv, prims);

		mv.visitCode();
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

	}

	private void implementWritePrimitives(MethodVisitor mv, List<FieldInfo> prims) {

		if (prims == null) {
			return;
		}

		for (FieldInfo fi : prims) {

			Type type = fi.asmType();

			if (fi.isReadAccessible(root.basic())) {
				Symbols.writeAccessiblePrimitive(mv, STR_OFFSET, REF_OFFSET, fi.owner().name, fi);
			} else {
				Symbols.writeInaccessiblePrimitive(mv, STR_OFFSET, REF_OFFSET, U_OFFSET, type, fi.objectFieldOffset(), fi.isCompressed());
			}
		}
	}

	void pushForRead(MethodVisitor mv, FieldInfo fi) {
		if (fi.isReadAccessible(root.basic())) {
			mv.visitVarInsn(ALOAD, REF_OFFSET);
			mv.visitFieldInsn(GETFIELD, fi.owner().name, fi.name, fi.desc);
		} else {
			mv.visitVarInsn(ALOAD, U_OFFSET);
			mv.visitVarInsn(ALOAD, REF_OFFSET);
			mv.visitLdcInsn(fi.objectFieldOffset());
			mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, _Unsafe.getObject, _Unsafe.getObject_D, false);
		}
	}

	void reifyIntern() {
		Reifier.reifyIntern(cv, root.type(), targetName);
	}

	void reifyUnintern() {
		Reifier.reifyUnintern(cv, root.type(), targetName, true);
	}

	private boolean verifyNeedForUnsafe(MethodVisitor mv, boolean forWrite) {

		boolean needsUnsafe = checkNeedsUnsafe(prims, forWrite) //
				|| checkNeedsUnsafe(nullable, forWrite) //
				|| checkNeedsUnsafe(nonNullable, forWrite);
		// || !strategy.isHierarchyInSameNamespace(root);

		if (needsUnsafe) {
			mv.visitFieldInsn(GETSTATIC, _Util.name, "U", _Unsafe.desc);
			mv.visitVarInsn(ASTORE, U_OFFSET);
		}

		return needsUnsafe;
	}

	@Override
	public void visitEnd() {

		emitInlineCaches();

		verifyCollectionSerializers();

		implementWritePayload();

		implementReadPayload();

		staticFieldsForInvariants();

		reifyIntern();

		reifyUnintern();

		clinit();

		init();
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		return Reifier.switchVisit(root, targetName, strategy, cv, ACC_PUBLIC | ACC_STATIC, name, desc, signature, exceptions);
	}

}