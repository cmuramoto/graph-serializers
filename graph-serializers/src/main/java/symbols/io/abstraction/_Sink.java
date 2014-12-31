package symbols.io.abstraction;

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

	String packZ = putZ + "P";
	String packZ_D = putZ_D;
	String packB = putB;
	String packB_D = putB_D;
	String packC = putC + "P";
	String packC_D = putC_D;
	String packS = putS + "P";
	String packS_D = putS_D;
	String packI = putI + "P";
	String packI_D = putI_D;
	String packF = putF + "P";
	String packF_D = putF_D;
	String packL = putL + "P";
	String packL_D = putL_D;
	String packD = putD + "P";
	String packD_D = putD_D;

	String bitMapGuard = "guard";
}