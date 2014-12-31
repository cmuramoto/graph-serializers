package symbols.sun.misc;

import symbols.java.lang._Class;
import symbols.java.lang._Number;
import symbols.java.lang._Object;

public interface _Unsafe {
	String name = "sun/misc/Unsafe";
	String desc = "Lsun/misc/Unsafe;";

	String instance = "theUnsafe";

	String getBoolean = "getBoolean";
	String getBoolean_D = "(" + _Object.desc + _Number.long_D + ")"
			+ _Number.boolean_D;
	String putBoolean = "putBoolean";
	String putBoolean_D = "(" + _Object.desc + _Number.long_D
			+ _Number.boolean_D + ")" + _Class.void_D;

	String getByte = "getByte";
	String getByte_D = "(" + _Object.desc + _Number.long_D + ")"
			+ _Number.byte_D;
	String putByte = "putByte";
	String putByte_D = "(" + _Object.desc + _Number.long_D + _Number.byte_D
			+ ")" + _Class.void_D;

	String getShort = "getShort";
	String getShort_D = "(" + _Object.desc + _Number.long_D + ")"
			+ _Number.short_D;
	String putShort = "putShort";
	String putShort_D = "(" + _Object.desc + _Number.long_D + _Number.short_D
			+ ")" + _Class.void_D;

	String getChar = "getChar";
	String getChar_D = "(" + _Object.desc + _Number.long_D + ")"
			+ _Number.char_D;
	String putChar = "putChar";
	String putChar_D = "(" + _Object.desc + _Number.long_D + _Number.char_D
			+ ")" + _Class.void_D;

	String getInt = "getInt";
	String getInt_D = "(" + _Object.desc + _Number.long_D + ")" + _Number.int_D;
	String putInt = "putInt";
	String putInt_D = "(" + _Object.desc + _Number.long_D + _Number.int_D + ")"
			+ _Class.void_D;

	String getFloat = "getFloat";
	String getFloat_D = "(" + _Object.desc + _Number.long_D + ")"
			+ _Number.float_D;
	String putFloat = "putFloat";
	String putFloat_D = "(" + _Object.desc + _Number.long_D + _Number.float_D
			+ ")" + _Class.void_D;

	String getLong = "getLong";
	String getLong_D = "(" + _Object.desc + _Number.long_D + ")"
			+ _Number.long_D;
	String putLong = "putLong";
	String putLong_D = "(" + _Object.desc + _Number.long_D + _Number.long_D
			+ ")" + _Class.void_D;

	String getDouble = "getDouble";
	String getDouble_D = "(" + _Object.desc + _Number.long_D + ")"
			+ _Number.double_D;
	String putDouble = "putDouble";
	String putDouble_D = "(" + _Object.desc + _Number.long_D + _Number.double_D
			+ ")" + _Class.void_D;

	String getObject = "getObject";
	String getObject_D = "(" + _Object.desc + _Number.long_D + ")"
			+ _Object.desc;

	String putObject = "putObject";
	String putObjectV = "putObjectVolatile";

	String putObject_D = "(" + _Object.desc + _Number.long_D + _Object.desc
			+ ")" + _Class.void_D;

	String allocateInstance = "allocateInstance";

	String allocateInstance_D = "(" + _Class.desc + ")" + _Object.desc;

}