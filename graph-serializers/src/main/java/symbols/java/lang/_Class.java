package symbols.java.lang;

import symbols.java.lang.reflect._Constructor;

public interface _Class {

	String name = "java/lang/Class";

	String desc = "Ljava/lang/Class;";

	String array01_D = "[Ljava/lang/Class;";

	String newInstance = "newInstance";

	String newInstance_D = "()" + _Object.desc;

	String Enum = "java/lang/Enum";

	String Enum_D = "Ljava/lang/Enum;";

	String ClassInitializer = "<clinit>";

	String ctor = "<init>";

	String NO_ARG_VOID = "()V";

	String void_D = "V";

	String forName = "forName";

	String forName_D = "(" + _String.desc + ")" + desc;

	String getDeclaredConstructor = "getDeclaredConstructor";

	String getDeclaredConstructor_D = "(" + array01_D + ")" + _Constructor.desc;

}