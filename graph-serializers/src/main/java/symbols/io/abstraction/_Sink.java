package symbols.io.abstraction;

import symbols.java.lang._Object;

public interface _Sink {
	String name = "com/nc/gs/io/Sink";
	String desc = "Lcom/nc/gs/io/Sink;";
	boolean fluent = false;
	String Compressor = "com/nc/gs/io/Sink";
	String Compressor_D = "Lcom/nc/gs/io/Sink;";
	boolean CompressorFluent = false;

	String putB = "writeByte";
	String putS = "writeShort";
	String putC = "writeChar";
	String putI = "writeInt";
	String putF = "writeFloat";
	String putL = "writeLong";
	String putD = "writeDouble";
	String putZ = "writeBoolean";

	String putB_D = "(B)" + "V";
	String putS_D = "(S)" + "V";
	String putC_D = "(C)" + "V";
	String putI_D = "(I)" + "V";
	String putF_D = "(F)" + "V";
	String putL_D = "(J)" + "V";
	String putD_D = "(D)" + "V";
	String putZ_D = "(Z)" + "V";

	String packZ = "writeBoolean";
	String packZ_D = putZ_D;
	String packB = "writeByte";
	String packB_D = putB_D;
	String packC = "writeVarChar";
	String packC_D = putC_D;
	String packS = "writeVarShort";
	String packS_D = putS_D;
	String packI = "writeVarInt";
	String packI_D = putI_D;
	String packF = "writeVarFloat";
	String packF_D = putF_D;
	String packL = "writeVarLong";
	String packL_D = putL_D;
	String packD = "writeVarDouble";
	String packD_D = putD_D;

	String writePrimitiveArray = "writePrimitiveArray";

	String writePrimitiveArray_D = "(ILjava/lang/Object;)V";

	String bitMapGuard = "guard";
	String write = "write";
	String writePrimitiveArrayAndRef_D = "(" + _Context.desc + "I" + _Object.desc + "I" + ")V";
}