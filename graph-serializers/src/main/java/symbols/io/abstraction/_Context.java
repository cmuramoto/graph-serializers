package symbols.io.abstraction;

import symbols.java.lang._Object;

public interface _Context {

	int CTX_OFFSET = 0;

	String name = "com/nc/gs/core/Context";

	String desc = "Lcom/nc/gs/core/Context;";

	String visited = "visited";

	String register = "register";

	String visited_D = "(" + _Sink.desc + _Object.desc + ")Z";

	String register_D = "(Ljava/lang/Object;I)V";

	String from = "from";

	String from_D = "(I)Ljava/lang/Object;";

	String writeRefAndData = "writeRefAndData";

	String writeTypeAndData = "writeTypeAndData";

	String writeNested_D = "(" + _Sink.desc + _Object.desc + ")V";

	String readNested_D = "(" + _Source.desc + ")" + _Object.desc;

	String readRefAndData = "readRefAndData";

	String readTypeAndData = "readTypeAndData";
}