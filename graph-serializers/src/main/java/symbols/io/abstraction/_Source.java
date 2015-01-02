package symbols.io.abstraction;

import org.objectweb.asm.Opcodes;

public interface _Source {
	String name = "com/nc/gs/io/Source";
	String desc = "Lcom/nc/gs/io/Source;";
	boolean fluent = false;

	int DEC_OPCODE = Opcodes.INVOKEVIRTUAL;
	String Decompressor = "com/nc/gs/io/Source";
	String Decompressor_D = "Lcom/nc/gs/io/Source;";
	boolean decompressorFluent = false;

	String getB = "readByte";
	String getS = "readShort";
	String getC = "readChar";
	String getI = "readInt";
	String getF = "readFloat";
	String getL = "readLong";
	String getD = "readDouble";
	String getZ = "readBoolean";

	String getB_D = "()B";
	String getS_D = "()S";
	String getC_D = "()C";
	String getI_D = "()I";
	String getF_D = "()F";
	String getL_D = "()J";
	String getD_D = "()D";
	String getZ_D = "()Z";

	String unpackZ = "readBoolean";
	String unpackZ_D = getZ_D;
	String unpackB = "readByte";
	String unpackB_D = getB_D;
	String unpackC = "readVarChar";
	String unpackC_D = getC_D;
	String unpackS = "readVarShort";
	String unpackS_D = getS_D;
	String unpackI = "readVarInt";
	String unpackI_D = getI_D;
	String unpackF = "readVarFloat";
	String unpackF_D = getF_D;
	String unpackL = "readVarLong";
	String unpackL_D = getL_D;
	String unpackD = "readVarDouble";
	String unpackD_D = getD_D;
	String mark = "mark";

	String mark_D = "()V";
	String reset = "reset";
	String reset_D = "()V";

}