package com.nc.gs.interpreter;

import static com.nc.gs.util.Utils.abbrevCN;
import static com.nc.gs.util.Utils.abbrevCNs;
import static com.nc.gs.util.Utils.asInt;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_M1;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SIPUSH;

import java.lang.reflect.Array;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

import symbols.io.abstraction._CollectionSerializer;
import symbols.io.abstraction._Context;
import symbols.io.abstraction._GraphSerializer;
import symbols.io.abstraction._GraphSerializer._R_;
import symbols.io.abstraction._Instantiator;
import symbols.io.abstraction._SerializerFactory;
import symbols.io.abstraction._Sink;
import symbols.io.abstraction._Source;
import symbols.io.abstraction._Tags;
import symbols.java.lang._Class;
import symbols.java.lang._Number;
import symbols.java.lang._Object;
import symbols.sun.misc._Unsafe;

public final class Symbols {

	public static String _R_collectionSerializer(String name) {

		return _CollectionSerializer._R_prefix + name;
	}

	public static String _R_inflateDataDesc(String pojoDesc) {
		return String.format("(%s%s%s)V", _Context.desc, _Source.desc, pojoDesc);
	}

	public static void _R_invokeRead(MethodVisitor mv, String owner, ExtendedType et, boolean onlyPayload) {
		_R_invokeRead(mv, owner, et.desc, onlyPayload);
	}

	public static void _R_invokeRead(MethodVisitor mv, String owner, String desc, boolean onlyPayload) {
		String target = onlyPayload ? _R_.readOpaque : _R_.read;

		mv.visitMethodInsn(INVOKESTATIC, owner, target, _R_readDataDesc(desc), false);
	}

	public static void _R_invokeRead(MethodVisitor mv, String owner, Type type, boolean onlyPayload) {
		_R_invokeRead(mv, owner, type.getDescriptor(), onlyPayload);
	}

	public static void _R_invokeWrite(MethodVisitor mv, String owner, ExtendedType type, boolean onlyPayload) {

		_R_invokeWrite(mv, owner, _R_writeDataDesc(type.desc), onlyPayload);
	}

	public static void _R_invokeWrite(MethodVisitor mv, String owner, String desc, boolean onlyPayload) {

		String target = onlyPayload ? _R_.writeData : _R_.write;

		mv.visitMethodInsn(INVOKESTATIC, owner, target, desc, false);
	}

	public static void _R_invokeWrite(MethodVisitor mv, String owner, Type type, boolean onlyPayload) {
		_R_invokeWrite(mv, owner, _R_writeDataDesc(type.getDescriptor()), onlyPayload);
	}

	public static String _R_optimizedArrayName(String type, ExtendedType[] compTypes, com.nc.gs.interpreter.Shape s) {
		if (compTypes.length == 1) {
			String n = type == null ? compTypes[0].name : type;
			return String.format(_Tags.CSOptimizer.CN_ARRAY_TEMPLATE, abbrevCN(n, '_'), s.canBeNull() ? 1 : 0, s.disregardRefs() ? 1 : 0);
		} else {
			return String.format(_Tags.MultiCSOptimizer.CN_ARRAY_TEMPLATE, abbrevCNs(compTypes, "", "|", true, !s.isHierarchyComplete()), s.canBeNull() ? 1 : 0, s.disregardRefs() ? 1 : 0);
		}
	}

	public static String _R_optimizedCollectionName(String colIN, ExtendedType[] compTypes, com.nc.gs.interpreter.Shape s, boolean forRep) {
		String template = compTypes.length == 1 ? _Tags.CSOptimizer.CN_TEMPLATE : _Tags.MultiCSOptimizer.CN_TEMPLATE;

		return String.format(template, colIN == null ? "POLY" : abbrevCN(colIN, '_'),//
				forRep ? "$R$" : "", abbrevCNs(compTypes, "", "|", true, !s.isHierarchyComplete()), s.canBeNull() ? 1 : 0, s.disregardRefs() ? 1 : 0);
	}

	public static String _R_optimizedMapName(String mapIN, com.nc.gs.interpreter.Shape ks, com.nc.gs.interpreter.Shape vs, boolean forRep) {
		String template;

		Hierarchy kh = ks.hierarchy();
		Hierarchy vh = vs.hierarchy();

		if (ks.hasPolymorphicHierarchy() || vs.hasPolymorphicHierarchy()) {
			template = _Tags.MultiMSOptimizer.CN_TEMPLATE;
		} else {
			template = _Tags.MSOptimizer.CN_TEMPLATE;
		}

		return String.format(template, mapIN == null ? "POLY" : abbrevCN(mapIN, '_'), forRep ? "$R$" : "", abbrevCNs(kh.types, "", "|", true, !kh.complete), abbrevCNs(vh.types, "", "|", true, !vh.complete), asInt(ks.canBeNull()), asInt(ks.disregardRefs()), asInt(vs.canBeNull()), asInt(vs.disregardRefs()));

	}

	public static String _R_optimizedSetName(String setIN, ExtendedType[] types, com.nc.gs.interpreter.Shape s) {
		String template = types.length == 1 ? _Tags.CSOptimizer.CN_SET_TEMPLATE : _Tags.MultiCSOptimizer.CN_SET_TEMPLATE;

		return String.format(template, setIN == null ? "POLY" : abbrevCN(setIN, '_'), abbrevCNs(types, "", "|", true, !s.isHierarchyComplete()), s.canBeNull() ? 1 : 0, s.disregardRefs() ? 1 : 0);
	}

	public static String _R_readDataDesc(String pojoDesc) {
		return String.format("(%s%s)%s", _Context.desc, _Source.desc, pojoDesc);
	}

	public static String _R_writeDataDesc(String pojoDesc) {
		return String.format("(%s%s%s)V", _Context.desc, _Sink.desc, pojoDesc);
	}

	public static AbstractInsnNode forNum(final int i) {
		AbstractInsnNode rv;
		switch (i) {
		case -1:
			rv = new InsnNode(ICONST_M1);
			break;
		case 0:
			rv = new InsnNode(ICONST_0);
			break;
		case 1:
			rv = new InsnNode(ICONST_1);
			break;
		case 2:
			rv = new InsnNode(ICONST_2);
			break;
		case 3:
			rv = new InsnNode(ICONST_3);
			break;
		case 4:
			rv = new InsnNode(ICONST_4);
			break;
		default:
			if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
				rv = new IntInsnNode(BIPUSH, i);
			} else if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) {
				rv = new IntInsnNode(SIPUSH, i);
			} else {
				rv = new LdcInsnNode(i);
			}
		}

		return rv;
	}

	public static String[] getArgumentDescriptors(String desc) {
		Type[] types = Type.getArgumentTypes(desc);

		String[] rv = new String[types.length];

		for (int i = 0; i < rv.length; i++) {
			rv[i] = types[i].getDescriptor().intern();
		}

		return rv;
	}

	/**
	 * {@link Object#getClass()}
	 *
	 * @param mv
	 */
	public static void getClass(MethodVisitor mv) {
		mv.visitMethodInsn(INVOKEVIRTUAL, _Object.name, _Object.getClass, _Object.getClass_D, false);
	}

	public static void getField(MethodVisitor mv, String name, String owner, String desc) {
		mv.visitFieldInsn(GETFIELD, owner, name, desc);
	}

	public static void getInaccessibleField(MethodVisitor mv, Type type, int unsafeStackIx, int objStackIx, long offset, boolean accessible) {

		mv.visitVarInsn(ALOAD, unsafeStackIx);
		mv.visitVarInsn(ALOAD, objStackIx);
		mv.visitLdcInsn(offset);

		String m;

		String d;

		switch (type.getSort()) {
		case Type.BOOLEAN:
			m = _Unsafe.getBoolean;
			d = _Unsafe.getBoolean_D;
			break;
		case Type.BYTE:
			m = _Unsafe.getByte;
			d = _Unsafe.getByte_D;
			break;
		case Type.SHORT:
			m = _Unsafe.getShort;
			d = _Unsafe.getShort_D;
			break;
		case Type.CHAR:
			m = _Unsafe.getChar;
			d = _Unsafe.getChar_D;
			break;
		case Type.INT:
			m = _Unsafe.getInt;
			d = _Unsafe.getInt_D;
			break;
		case Type.FLOAT:
			m = _Unsafe.getFloat;
			d = _Unsafe.getFloat_D;
			break;
		case Type.LONG:
			m = _Unsafe.getLong;
			d = _Unsafe.getLong_D;
			break;
		case Type.DOUBLE:
			m = _Unsafe.getDouble;
			d = _Unsafe.getDouble_D;
			break;
		default:
			m = _Unsafe.getBoolean;
			d = _Unsafe.getBoolean_D;
			break;
		}

		mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, m, d, false);
	}

	public static String graphSerializerName(String k) {
		return k + _SerializerFactory.genClassSuffix;
	}

	public static String instantiatorBinaryName(String name) {
		return _Instantiator.prefix + name;
	}

	public static String instantiatorInternalName(String typeIN) {
		return _Instantiator.prefixIN + typeIN;
	}

	public static void invokeRead(MethodVisitor mv, FieldInfo fi) {
		invokeReadWithOwner(mv, _GraphSerializer.name, fi);
	}

	public static void invokeReadNested(MethodVisitor mv) {
		mv.visitMethodInsn(INVOKESTATIC, _GraphSerializer.name, _GraphSerializer.readNested, _GraphSerializer.read_D, false);
	}

	public static void invokeReadReified(MethodVisitor mv, String sN, FieldInfo fi) {
		String target = fi.disregardReference() ? _R_.readOpaque : _R_.read;

		mv.visitMethodInsn(INVOKESTATIC, sN, target, fi.readInlineDesc(), false);
	}

	public static void invokeReadWithOwner(MethodVisitor mv, String sN, boolean op) {
		String target = op ? _GraphSerializer.readOpaque : _GraphSerializer.read;

		mv.visitMethodInsn(INVOKEVIRTUAL, sN, target, _GraphSerializer.read_D, false);
	}

	public static void invokeReadWithOwner(MethodVisitor mv, String sN, FieldInfo fi) {
		invokeReadWithOwner(mv, sN, fi.disregardReference());
	}

	public static void invokeWrite(MethodVisitor mv, boolean onlyPayload) {
		invokeWriteWithOwner(mv, _GraphSerializer.name, onlyPayload);
	}

	public static void invokeWriteNested(MethodVisitor mv) {
		mv.visitMethodInsn(INVOKESTATIC, _GraphSerializer.name, _GraphSerializer.writeNested, _GraphSerializer.write_D, false);
	}

	public static void invokeWriteWithOwner(MethodVisitor mv, String sN, boolean onlyPayload) {
		String target;

		if (onlyPayload) {
			target = _GraphSerializer.writeData;
		} else {
			target = _GraphSerializer.write;
		}

		mv.visitMethodInsn(INVOKEVIRTUAL, sN, target, _GraphSerializer.write_D, false);
	}

	public static void loadAndBox(final MethodVisitor mv, final Type type, final int stackIndex) {

		switch (type.getSort()) {
		case Type.BOOLEAN:
			mv.visitVarInsn(ILOAD, stackIndex);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
			break;

		case Type.BYTE:
			mv.visitVarInsn(ILOAD, stackIndex);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
			break;

		case Type.CHAR:
			mv.visitVarInsn(ILOAD, stackIndex);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
			break;

		case Type.SHORT:
			mv.visitVarInsn(ILOAD, stackIndex);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
			break;

		case Type.INT:
			mv.visitVarInsn(ILOAD, stackIndex);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			break;

		case Type.FLOAT:
			mv.visitVarInsn(FLOAD, stackIndex);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
			break;

		case Type.LONG:
			mv.visitVarInsn(LLOAD, stackIndex);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
			break;

		case Type.DOUBLE:
			mv.visitVarInsn(DLOAD, stackIndex);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
			break;
		default:
			mv.visitVarInsn(ALOAD, stackIndex);
		}
	}

	public static void loadBoolean(MethodVisitor mv, boolean v) {
		mv.visitInsn(v ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
	}

	public static void loadLocal(MethodVisitor mv, Type type, int stack) {
		switch (type.getSort()) {
		case Type.BOOLEAN:
		case Type.BYTE:
		case Type.SHORT:
		case Type.CHAR:
		case Type.INT:
			mv.visitVarInsn(ILOAD, stack);
			break;
		case Type.FLOAT:
			mv.visitVarInsn(FLOAD, stack);
			break;
		case Type.LONG:
			mv.visitVarInsn(LLOAD, stack);
			break;
		case Type.DOUBLE:
			mv.visitVarInsn(DLOAD, stack);
			break;
		default:
			mv.visitVarInsn(ALOAD, stack);
			break;
		}
	}

	/**
	 * Loads a number onto the stack, using compressed instructions for small numbers
	 *
	 * @param mv
	 * @param i
	 */
	public static void loadNumber(final MethodVisitor mv, final int i) {
		switch (i) {
		case -1:
			mv.visitInsn(ICONST_M1);
			break;
		case 0:
			mv.visitInsn(ICONST_0);
			break;
		case 1:
			mv.visitInsn(ICONST_1);
			break;
		case 2:
			mv.visitInsn(ICONST_2);
			break;
		case 3:
			mv.visitInsn(ICONST_3);
			break;
		case 4:
			mv.visitInsn(ICONST_4);
			break;
		default:
			if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
				mv.visitIntInsn(BIPUSH, i);
			} else if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) {
				mv.visitIntInsn(SIPUSH, i);
			} else {
				mv.visitLdcInsn(i);
			}
		}
	}

	public static void loadNumber(final MethodVisitor mv, final long l) {
		if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
			loadNumber(mv, (int) l);
		} else {
			mv.visitLdcInsn(l);
		}
	}

	public static void loadType(final MethodVisitor mv, final Type type) {

		switch (type.getSort()) {
		case Type.VOID:
			mv.visitMethodInsn(GETSTATIC, "java/lang/Void", "TYPE", _Class.desc, false);
			break;
		case Type.BOOLEAN:
			mv.visitMethodInsn(GETSTATIC, "java/lang/Boolean", "TYPE", _Class.desc, false);
			break;
		case Type.CHAR:
			mv.visitMethodInsn(GETSTATIC, "java/lang/Character", "TYPE", _Class.desc, false);
			break;

		case Type.BYTE:
			mv.visitMethodInsn(GETSTATIC, "java/lang/Byte", "TYPE", _Class.desc, false);
			break;

		case Type.SHORT:
			mv.visitMethodInsn(GETSTATIC, "java/lang/Short", "TYPE", _Class.desc, false);
			break;

		case Type.INT:
			mv.visitMethodInsn(GETSTATIC, "java/lang/Integer", "TYPE", _Class.desc, false);
			break;

		case Type.FLOAT:
			mv.visitMethodInsn(GETSTATIC, "java/lang/Float", "TYPE", _Class.desc, false);
			break;

		case Type.LONG:
			mv.visitMethodInsn(GETSTATIC, "java/lang/Long", "TYPE", _Class.desc, false);
			break;

		case Type.DOUBLE:
			mv.visitMethodInsn(GETSTATIC, "java/lang/Double", "TYPE", _Class.desc, false);
			break;
		default:
			mv.visitLdcInsn(type);
		}
	}

	public static void nullSafeRead(MethodVisitor mv, boolean op) {

		String target = op ? _Context.readTypeAndData : _Context.readRefAndData;

		mv.visitMethodInsn(INVOKEVIRTUAL, _Context.name, target, _Context.readNested_D, false);
	}

	public static void nullSafeRead(MethodVisitor mv, FieldInfo fi) {

		nullSafeRead(mv, fi.disregardReference());
	}

	public static void nullSafeWrite(MethodVisitor mv, boolean op) {

		String target = op ? _Context.writeTypeAndData : _Context.writeRefAndData;

		mv.visitMethodInsn(INVOKEVIRTUAL, _Context.name, target, _Context.writeNested_D, false);
	}

	public static void nullSafeWrite(MethodVisitor mv, FieldInfo fi) {

		nullSafeWrite(mv, fi.disregardReference());
	}

	public static String optimizedCollectionResource(boolean nullable, boolean op, boolean ra) {

		if (ra) {
			if (nullable) {
				return op ? _CollectionSerializer.optRATemplate11 : _CollectionSerializer.optRATemplate10;
			} else {
				return op ? _CollectionSerializer.optRATemplate01 : _CollectionSerializer.optRATemplate00;
			}
		} else {

			if (nullable) {
				return op ? _CollectionSerializer.optTemplate11 : _CollectionSerializer.optTemplate10;
			} else {
				return op ? _CollectionSerializer.optTemplate01 : _CollectionSerializer.optTemplate00;
			}
		}

		// return nullable ? (op ? _CollectionSerializer.optTemplate11
		// : _CollectionSerializer.optTemplate01)
		// : (op ? _CollectionSerializer.optTemplate10
		// : _CollectionSerializer.optTemplate00);
	}

	public static java.lang.Class<?> primitiveTypeOrArray(Type type) {
		int s = type.getSort();

		Class<?> rv = s > 0 && s < 9 ? PM[s] : null;

		if (rv == null) {
			if (s == Type.ARRAY) {
				String desc = type.getDescriptor();

				if (desc.endsWith(";")) {
					return null;
				}

				int dim = desc.lastIndexOf('[') + 1;

				Class<?> t = primitiveTypeOrArray(Type.getType(desc.substring(dim)));

				for (int i = 0; i < dim; i++) {
					t = Array.newInstance(t, 0).getClass();
				}

				return t;
			}
		}

		return null;
	}

	public static void putByte(MethodVisitor mv) {
		mv.visitMethodInsn(INVOKEVIRTUAL, _Sink.name, _Sink.putB, _Sink.putB_D, false);
		if (_Sink.fluent) {
			mv.visitInsn(POP);
		}
	}

	public static void readAccessiblePrimitive(MethodVisitor mv, int streamIx, int objStackIx, String owner, FieldInfo fi) {
		mv.visitVarInsn(ALOAD, objStackIx);
		mv.visitVarInsn(ALOAD, streamIx);

		boolean compressed = fi.isCompressed();
		boolean canCompress = compressed;

		String get;
		String getD;

		Type type = fi.asmType();

		switch (type.getSort()) {
		case Type.BOOLEAN:
			get = _Source.unpackZ;
			getD = _Source.unpackZ_D;
			canCompress = true;
			break;
		case Type.BYTE:
			get = _Source.getB;
			getD = _Source.getB_D;
			canCompress = false;
			break;
		case Type.SHORT:
			if (compressed) {
				get = _Source.unpackS;
				getD = _Source.unpackS_D;
			} else {
				get = _Source.getS;
				getD = _Source.getS_D;
			}
			break;
		case Type.CHAR:
			if (compressed) {
				get = _Source.unpackC;
				getD = _Source.unpackC_D;
			} else {
				get = _Source.getC;
				getD = _Source.getC_D;
			}
			break;
		case Type.INT:
			if (compressed) {
				get = _Source.unpackI;
				getD = _Source.unpackI_D;
			} else {
				get = _Source.getI;
				getD = _Source.getI_D;
			}
			break;
		case Type.FLOAT:
			if (compressed) {
				get = _Source.unpackF;
				getD = _Source.unpackF_D;
			} else {
				get = _Source.getF;
				getD = _Source.getF_D;
			}
			break;
		case Type.LONG:
			if (compressed) {
				get = _Source.unpackL;
				getD = _Source.unpackL_D;
			} else {
				get = _Source.getL;
				getD = _Source.getL_D;
			}
			break;

		case Type.DOUBLE:
			if (compressed) {
				get = _Source.unpackD;
				getD = _Source.unpackD_D;
			} else {
				get = _Source.getD;
				getD = _Source.getD_D;
			}

			break;
		default:
			throw new IllegalArgumentException(type.getInternalName());
		}

		mv.visitMethodInsn(INVOKEVIRTUAL, _Source.name, get, getD, false);

		mv.visitFieldInsn(PUTFIELD, owner, fi.name, fi.desc);
	}

	public static void readAndBox(MethodVisitor mv, FieldInfo fi) {
		boolean compressed = fi.isCompressed();
		boolean canCompress = compressed;

		String targetType;
		final String targetMethod = "valueOf";
		String targetMethodDesc;
		String get;
		String getD;

		switch (fi.desc) {
		case _Number.Boolean_D:
			targetType = _Number.Boolean;
			targetMethodDesc = _Number.Boolean_valueOf_D;
			get = _Source.unpackZ;
			getD = _Source.unpackZ_D;
			canCompress = true;
			break;
		case _Number.Byte_D:
			targetType = _Number.Byte;
			targetMethodDesc = _Number.Byte_valueOf_D;
			get = _Source.getB;
			getD = _Source.getB_D;
			canCompress = false;
			break;
		case _Number.Short_D:
			targetType = _Number.Short;
			targetMethodDesc = _Number.Short_valueOf_D;
			if (compressed) {
				get = _Source.unpackS;
				getD = _Source.unpackS_D;
			} else {
				get = _Source.getS;
				getD = _Source.getS_D;
			}
			break;
		case _Number.Character_D:
			targetType = _Number.Character;
			targetMethodDesc = _Number.Character_valueOf_D;

			if (compressed) {
				get = _Source.unpackC;
				getD = _Source.unpackC_D;
			} else {
				get = _Source.getC;
				getD = _Source.getC_D;
			}
			break;
		case _Number.Integer_D:
			targetType = _Number.Integer;
			targetMethodDesc = _Number.Integer_valueOf_D;

			if (compressed) {
				get = _Source.unpackI;
				getD = _Source.unpackI_D;
			} else {
				get = _Source.getI;
				getD = _Source.getI_D;
			}
			break;
		case _Number.Float_D:
			targetType = _Number.Float;
			targetMethodDesc = _Number.Float_valueOf_D;
			if (compressed) {
				get = _Source.unpackF;
				getD = _Source.unpackF_D;
			} else {
				get = _Source.getF;
				getD = _Source.getF_D;
			}
			break;
		case _Number.Long_D:
			targetType = _Number.Long;
			targetMethodDesc = _Number.Long_valueOf_D;
			if (compressed) {
				get = _Source.unpackL;
				getD = _Source.unpackL_D;
			} else {
				get = _Source.getL;
				getD = _Source.getL_D;
			}
			break;
		case _Number.Double_D:
			targetType = _Number.Double;
			targetMethodDesc = _Number.Double_valueOf_D;
			if (compressed) {
				get = _Source.unpackD;
				getD = _Source.unpackD_D;
			} else {
				get = _Source.getD;
				getD = _Source.getD_D;
			}
			break;
		default:
			throw new UnsupportedOperationException("Description not for wrapper type: " + fi.desc);
		}

		if (canCompress) {
			mv.visitMethodInsn(INVOKESTATIC, _Sink.Compressor, get, getD, false);
		} else {
			mv.visitMethodInsn(INVOKEVIRTUAL, _Source.name, get, getD, false);
		}

		mv.visitMethodInsn(INVOKESTATIC, targetType, targetMethod, targetMethodDesc, false);

	}

	public static void readInaccessiblePrimitive(MethodVisitor mv, int streamIx, int objStackIx, int unsafeStackIx, Type type, long offset, boolean compressed) {
		mv.visitVarInsn(ALOAD, unsafeStackIx);
		mv.visitVarInsn(ALOAD, objStackIx);
		mv.visitLdcInsn(offset);
		mv.visitVarInsn(ALOAD, streamIx);

		String putU;
		String putU_D;
		String getO;
		String getO_D;

		boolean canCompress = compressed;

		switch (type.getSort()) {
		case Type.BOOLEAN:
			putU = _Unsafe.putBoolean;
			putU_D = _Unsafe.putBoolean_D;
			getO = _Source.unpackZ;
			getO_D = _Source.unpackZ_D;
			canCompress = true;
			break;
		case Type.BYTE:
			putU = _Unsafe.putByte;
			getO = _Source.getB;
			putU_D = _Unsafe.putByte_D;
			getO_D = _Source.getB_D;
			canCompress = false;
			break;
		case Type.SHORT:
			putU = _Unsafe.putShort;
			putU_D = _Unsafe.putShort_D;
			if (compressed) {
				getO = _Source.unpackS;
				getO_D = _Source.unpackS_D;
			} else {
				getO = _Source.getS;
				getO_D = _Source.getS_D;
			}
			break;
		case Type.CHAR:
			putU = _Unsafe.putChar;
			putU_D = _Unsafe.putChar_D;
			if (compressed) {
				getO = _Source.unpackC;
				getO_D = _Source.unpackC_D;
			} else {
				getO = _Source.getC;
				getO_D = _Source.getC_D;
			}
			break;
		case Type.INT:
			putU = _Unsafe.putInt;
			putU_D = _Unsafe.putInt_D;
			if (compressed) {
				getO = _Source.unpackI;
				getO_D = _Source.unpackI_D;
			} else {
				getO = _Source.getI;
				getO_D = _Source.getI_D;
			}
			break;
		case Type.FLOAT:
			putU = _Unsafe.putFloat;
			putU_D = _Unsafe.putFloat_D;
			getO_D = _Source.getF;
			if (compressed) {
				getO = _Source.unpackF;
				getO_D = _Source.unpackF_D;
			} else {
				getO = _Source.getF;
				getO_D = _Source.getF_D;
			}
			break;
		case Type.LONG:
			putU = _Unsafe.putLong;
			putU_D = _Unsafe.putLong_D;
			getO_D = _Source.getL;
			if (compressed) {
				getO = _Source.unpackL;
				getO_D = _Source.unpackL_D;
			} else {
				getO = _Source.getL;
				getO_D = _Source.getL_D;
			}
			break;

		case Type.DOUBLE:
			putU = _Unsafe.putDouble;
			putU_D = _Unsafe.putDouble_D;
			getO_D = _Source.getD;
			if (compressed) {
				getO = _Source.unpackD;
				getO_D = _Source.unpackD_D;
			} else {
				getO = _Source.getD;
				getO_D = _Source.getD_D;
			}

			break;
		default:
			throw new IllegalArgumentException(type.getInternalName());
		}

		if (canCompress) {
			mv.visitMethodInsn(INVOKEVIRTUAL, _Source.name, getO, getO_D, false);
		} else {
			mv.visitMethodInsn(INVOKEVIRTUAL, _Source.name, getO, getO_D, false);
		}

		mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, putU, putU_D, false);

	}

	public static String reifiedInflateDataDesc(Type t) {
		return Symbols.reifiedReadPayloadDesc(t);
	}

	public static String reifiedInstantiateDesc(String pojoDesc) {
		return "(" + _Source.desc + ")" + pojoDesc;
	}

	public static String reifiedReadDesc(Type t) {
		return String.format("(%s%s)%s", _Context.desc, _Sink.desc, t.getDescriptor());
	}

	private static String reifiedReadPayloadDesc(Type t) {
		return String.format("(%s%s%s)V", _Context.desc, _Source.desc, t.getDescriptor());
	}

	public static String reifiedWritePayloadDesc(Type t) {
		return String.format("(%s%s%s)V", _Context.desc, _Sink.desc, t.getDescriptor());
	}

	public static void returnValue(MethodVisitor mv, Type type) {
		switch (type.getSort()) {
		case Type.VOID:
			mv.visitInsn(RETURN);
		case Type.BOOLEAN:
		case Type.BYTE:
		case Type.SHORT:
		case Type.CHAR:
		case Type.INT:
			mv.visitInsn(IRETURN);
			break;
		case Type.FLOAT:
			mv.visitInsn(FRETURN);
			break;
		case Type.LONG:
			mv.visitInsn(LRETURN);
			break;
		case Type.DOUBLE:
			mv.visitInsn(DRETURN);
			break;
		default:
			mv.visitInsn(ARETURN);
			break;
		}
	}

	public static void unboxAndWrite(MethodVisitor mv, FieldInfo fi) {

		boolean compressed = fi.isCompressed();
		boolean canCompress = compressed;

		String targetType;
		String targetMethod;
		String targetMethodDesc;
		String put;
		String putD;

		switch (fi.desc) {
		case _Number.Boolean_D:
			targetType = _Number.Boolean;
			targetMethod = "booleanValue";
			targetMethodDesc = "()Z";
			put = _Sink.packZ;
			putD = _Sink.packZ_D;
			canCompress = true;
			break;
		case _Number.Byte_D:
			targetType = _Number.Byte;
			targetMethod = "byteValue";
			targetMethodDesc = "()B";
			put = _Sink.putB;
			putD = _Sink.putB_D;
			canCompress = false;
			break;
		case _Number.Short_D:
			targetType = _Number.Short;
			targetMethod = "shortValue";
			targetMethodDesc = "()S";
			if (compressed) {
				put = _Sink.packS;
				putD = _Sink.packS_D;
			} else {
				put = _Sink.putS;
				putD = _Sink.putS_D;
			}
			break;
		case _Number.Character_D:
			targetType = _Number.Character;
			targetMethod = "charValue";
			targetMethodDesc = "()C";
			if (compressed) {
				put = _Sink.packC;
				putD = _Sink.packC_D;
			} else {
				put = _Sink.putC;
				putD = _Sink.putC_D;
			}
			break;
		case _Number.Integer_D:
			targetType = _Number.Integer;
			targetMethod = "intValue";
			targetMethodDesc = "()I";
			if (compressed) {
				put = _Sink.packI;
				putD = _Sink.packI_D;
			} else {
				put = _Sink.putI;
				putD = _Sink.putI_D;
			}
			break;
		case _Number.Float_D:
			targetType = _Number.Float;
			targetMethod = "floatValue";
			targetMethodDesc = "()F";
			if (compressed) {
				put = _Sink.packF;
				putD = _Sink.packF_D;
			} else {
				put = _Sink.putF;
				putD = _Sink.putF_D;
			}
			break;
		case _Number.Long_D:
			targetType = _Number.Long;
			targetMethod = "longValue";
			targetMethodDesc = "()J";
			if (compressed) {
				put = _Sink.packL;
				putD = _Sink.packL_D;
			} else {
				put = _Sink.putL;
				putD = _Sink.putL_D;
			}
			break;
		case _Number.Double_D:
			targetType = _Number.Double;
			targetMethod = "doubleValue";
			targetMethodDesc = "()D";
			if (compressed) {
				put = _Sink.packD;
				putD = _Sink.packD_D;
			} else {
				put = _Sink.putD;
				putD = _Sink.putD_D;
			}
			break;
		default:
			throw new UnsupportedOperationException("Description not for wrapper type: " + fi.desc);
		}

		mv.visitMethodInsn(INVOKEVIRTUAL, targetType, targetMethod, targetMethodDesc, false);

		if (canCompress) {
			mv.visitMethodInsn(INVOKESTATIC, _Sink.Compressor, put, putD, false);
		} else {
			mv.visitMethodInsn(INVOKEVIRTUAL, _Sink.name, put, putD, false);
			if (_Sink.fluent) {
				mv.visitInsn(POP);
			}
		}
	}

	public static void writeAccessiblePrimitive(MethodVisitor mv, int streamIx, int objStackIx, String owner, FieldInfo fi) {
		mv.visitVarInsn(ALOAD, streamIx);
		mv.visitVarInsn(ALOAD, objStackIx);

		boolean compressed = fi.isCompressed();
		boolean canCompress = compressed;

		String put;
		String putD;

		Type type = fi.asmType();

		switch (type.getSort()) {
		case Type.BOOLEAN:
			put = _Sink.packZ;
			putD = _Sink.packZ_D;
			canCompress = true;
			break;
		case Type.BYTE:
			put = _Sink.putB;
			putD = _Sink.putB_D;
			canCompress = false;
			break;
		case Type.SHORT:
			if (compressed) {
				put = _Sink.packS;
				putD = _Sink.packS_D;
			} else {
				put = _Sink.putS;
				putD = _Sink.putS_D;
			}
			break;
		case Type.CHAR:
			if (compressed) {
				put = _Sink.packC;
				putD = _Sink.packC_D;
			} else {
				put = _Sink.putC;
				putD = _Sink.putC_D;
			}
			break;
		case Type.INT:
			if (compressed) {
				put = _Sink.packI;
				putD = _Sink.packI_D;
			} else {
				put = _Sink.putI;
				putD = _Sink.putI_D;
			}
			break;
		case Type.FLOAT:
			if (compressed) {
				put = _Sink.packF;
				putD = _Sink.packF_D;
			} else {
				put = _Sink.putF;
				putD = _Sink.putF_D;
			}
			break;
		case Type.LONG:
			if (compressed) {
				put = _Sink.packL;
				putD = _Sink.packL_D;
			} else {
				put = _Sink.putL;
				putD = _Sink.putL_D;
			}
			break;

		case Type.DOUBLE:
			if (compressed) {
				put = _Sink.packD;
				putD = _Sink.packD_D;
			} else {
				put = _Sink.putD;
				putD = _Sink.putD_D;
			}

			break;
		default:
			throw new IllegalArgumentException(type.getInternalName());
		}

		mv.visitFieldInsn(GETFIELD, owner, fi.name, fi.desc);

		if (canCompress) {
			mv.visitMethodInsn(INVOKEVIRTUAL, _Sink.Compressor, put, putD, false);
		} else {
			mv.visitMethodInsn(INVOKEVIRTUAL, _Sink.name, put, putD, false);
			if (_Sink.fluent) {
				mv.visitInsn(POP);
			}
		}
	}

	public static void writeInaccessiblePrimitive(MethodVisitor mv, int streamIx, int objStackIx, int unsafeStackIx, Type type, long offset, boolean compressed) {

		mv.visitVarInsn(ALOAD, streamIx);
		mv.visitVarInsn(ALOAD, unsafeStackIx);
		mv.visitVarInsn(ALOAD, objStackIx);
		mv.visitLdcInsn(offset);

		String getU;
		String getU_D;
		String putO;
		String putO_D;

		boolean canCompress = compressed;

		switch (type.getSort()) {
		case Type.BOOLEAN:
			getU = _Unsafe.getBoolean;
			getU_D = _Unsafe.getBoolean_D;
			putO = _Sink.packZ;
			putO_D = _Sink.packZ_D;
			canCompress = true;
			break;
		case Type.BYTE:
			getU = _Unsafe.getByte;
			putO = _Sink.putB;
			getU_D = _Unsafe.getByte_D;
			putO_D = _Sink.putB_D;
			canCompress = false;
			break;
		case Type.SHORT:
			if (compressed) {
				putO = _Sink.packS;
				putO_D = _Sink.packS_D;
			} else {
				putO = _Sink.putS;
				putO_D = _Sink.putS_D;
			}
			getU = _Unsafe.getShort;
			getU_D = _Unsafe.getShort_D;
			break;
		case Type.CHAR:
			getU = _Unsafe.getChar;
			getU_D = _Unsafe.getChar_D;
			if (compressed) {
				putO = _Sink.packC;
				putO_D = _Sink.packC_D;
			} else {
				putO = _Sink.putC;
				putO_D = _Sink.putC_D;
			}
			break;
		case Type.INT:
			getU = _Unsafe.getInt;
			getU_D = _Unsafe.getInt_D;
			if (compressed) {
				putO = _Sink.packI;
				putO_D = _Sink.packI_D;
			} else {
				putO = _Sink.putI;
				putO_D = _Sink.putI_D;
			}
			break;
		case Type.FLOAT:
			getU = _Unsafe.getFloat;
			getU_D = _Unsafe.getFloat_D;
			putO_D = _Sink.putF;
			if (compressed) {
				putO = _Sink.packF;
				putO_D = _Sink.packF_D;
			} else {
				putO = _Sink.putF;
				putO_D = _Sink.putF_D;
			}
			break;
		case Type.LONG:
			getU = _Unsafe.getLong;
			getU_D = _Unsafe.getLong_D;
			putO_D = _Sink.putL;
			if (compressed) {
				putO = _Sink.packL;
				putO_D = _Sink.packL_D;
			} else {
				putO = _Sink.putL;
				putO_D = _Sink.putL_D;
			}
			break;

		case Type.DOUBLE:
			getU = _Unsafe.getDouble;
			getU_D = _Unsafe.getDouble_D;
			putO_D = _Sink.putD;
			if (compressed) {
				putO = _Sink.packD;
				putO_D = _Sink.packD_D;
			} else {
				putO = _Sink.putD;
				putO_D = _Sink.putD_D;
			}

			break;
		default:
			throw new IllegalArgumentException(type.getInternalName());
		}

		mv.visitMethodInsn(INVOKEVIRTUAL, _Unsafe.name, getU, getU_D, false);

		if (canCompress) {
			mv.visitMethodInsn(INVOKEVIRTUAL, _Sink.Compressor, putO, putO_D, false);
		} else {
			mv.visitMethodInsn(INVOKEVIRTUAL, _Sink.name, putO, putO_D, false);
			if (_Sink.fluent) {
				mv.visitInsn(POP);
			}
		}
	}

	static final Class<?>[] PM = { void.class, boolean.class, char.class, byte.class, short.class, int.class, float.class, long.class, double.class };
}