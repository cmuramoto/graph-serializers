package symbols.io.abstraction;

import symbols.java.lang._Class;

public interface _SerializerFactory {
	String name = "com/nc/gs/core/SerializerFactory";

	String desc = "Lcom/nc/gs/core/SerializerFactory;";

	String serializer = "serializer";

	String serializer_D = "(" + _Class.desc + ")" + _GraphSerializer.desc;

	String register = "register";

	String register_D = "(" + _Class.desc + _GraphSerializer.desc + ")V";

	String genClassSuffix = "$NC_GS";

	String genClassSuffix_D = "$NC_GS;";

	String copyistOf = "copyistOf";

	String instantiatorOf = "instantiatorOf";

	String copyistOf_D = "(" + _Class.desc + ")" + _ShallowCopyist.desc;

	String instantiatorOf_D = "(Ljava/lang/Class;)Lcom/nc/gs/core/Instantiator;";

	String forCollection = "forCollection";

	String forCollection_D = "(" + _StreamShape.desc + ")" + _GraphSerializer.desc;

	String forSet = "forSet";

	String forSet_D = "(" + _StreamShape.desc + ")" + _GraphSerializer.desc;

	String forMap = "forMap";

	String forMap_D = "(" + _MapShape.desc + ")" + _GraphSerializer.desc;

	String forArray = "forArray";

	String forArray_D = "(" + _StreamShape.desc + ")" + _GraphSerializer.desc;

	String isProbablyOpaque = "isProbablyOpaque";

	String isProbablyOpaque_D = "(Ljava/lang/Class;)Z";

}