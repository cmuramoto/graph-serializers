package symbols.io.abstraction;

import symbols.java.lang._Object;

public interface _GraphSerializer {

	public interface _R_ {

		int CTX_OFFSET = 0;

		int STR_OFFSET = 1;

		int REF_OFFSET = 2;

		int U_OFFSET = 3;

		String PREFIX = "_R_";

		String read = PREFIX + _GraphSerializer.read;
		String readOpaque = PREFIX + _GraphSerializer.readOpaque;
		String inflateData = PREFIX + _GraphSerializer.inflateData;

		String write = PREFIX + _GraphSerializer.write;
		String writeData = PREFIX + _GraphSerializer.writeData;

		String instantiate = "allocate";

		String intern = "intern";

		String unintern = "unintern";
	}

	String ctor_D = "()V";

	String IC_W_PREF = "_IC_W_";

	String IC_R_PREF = "_IC_R_";

	String IC_MULTI_W = "_IC_P_W_";

	String IC_MULTI_R = "_IC_P_R_";

	String name = "com/nc/gs/core/GraphSerializer";

	String desc = "Lcom/nc/gs/core/GraphSerializer;";

	String instantiate = "instantiate";

	String instantiate_D = "(" + _Source.desc + ")Ljava/lang/Object;";

	String inflateData = "inflateData";

	String write = "write";
	String writeData = "writeData";

	String write_D = "(" + _Context.desc + _Sink.desc + _Object.desc + ")V";

	String read = "read";
	String readOpaque = "readData";
	String read_D = "(" + _Context.desc + _Source.desc + ")" + _Object.desc;

	String writeNested = "writeNested";

	String readNested = "readNested";

	String intern = "intern";
	String intern_D = write_D;

	String unintern = "unintern";
	String unintern_D = read_D;
}