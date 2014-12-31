package symbols.io.abstraction;

public interface _Tags {

	interface CSOptimizer {

		String CN_TEMPLATE = "CS/of/%s%s{%s}%d%d";

		String CN_ARRAY_TEMPLATE = "AS/of/%s%d%d";

		String CN_SET_TEMPLATE = "SS/of/%s{%s}%d%d";

		String INSTANCE = "I";
		String ctr = "ctor";
		String gs = "gs";

		int GS_OFF/*    */= 4;

		String TEMPLATE = "com.nc.gs.serializers.java.util.opt.CS";
		String TEMPLATE_ARRAY = "com.nc.gs.serializers.java.util.opt.AS";
		String TEMPLATE_SET = "com.nc.gs.serializers.java.util.opt.SS";
		String TEMPLATE_RA = "com.nc.gs.serializers.java.util.opt.RACS";
	}

	public interface ExtendedType {
		long ACC_LEAF/*             */= 0x100000000L;
		long ACC_WELCOME_TRANSIENT/**/= 0x200000000L;
		long ACC_REJECT_NON_TRANSIENT = 0x400000000L;
		long ACC_COMPRESS_PRIMS/*   */= 0x800000000L;
		long ACC_DECL_FINAL/*       */= 0x1000000000L;
		long ACC_DECL_PVT/*         */= 0x2000000000L;
		long ACC_JAVA_TYPE/*        */= 0x4000000000L;
		long ACC_INNER/*            */= 0x8000000000L;
		long ACC_DECL_INNER/*       */= 0x10000000000L;
	}

	public interface FieldInfo {
		long ACC_COMPRESSED/*        */= 0x100000000L;
		long ACC_NON_NULL/*          */= 0x200000000L;
		long ACC_ONLY_DATA/*         */= 0x400000000L;
		long ACC_LEAF/*              */= 0x800000000L;
		long ACC_IN/*                */= 0x1000000000L;
		long ACC_COL_CMP/*           */= 0x2000000000L;
		long ACC_COL/*               */= 0x4000000000L;
		long ACC_SET_CMP/*           */= 0x8000000000L;
		long ACC_SET/*               */= 0x10000000000L;
		long ACC_MAP_CMP/*           */= 0x20000000000L;
		long ACC_MAP/*               */= 0x40000000000L;
		long ACC_MAYBE_OPAQUE/*      */= 0x80000000000L;
		long ACC_COMPLETE_HIERARCHY/**/= 0x100000000000L;
		long ACC_NON_SERIALIZED/*    */= 0x200000000000L;
	}

	interface MSOptimizer {
		String CN_TEMPLATE = "MS/of/%s%s{%s,%s}%d%d%d%d";

		String INSTANCE = "I";
		String ctr = "ctor";
		String kgs = "ks";
		String vgs = "vs";

		int KGS_OFF/**/= 4;
		int VGS_OFF/**/= 5;
		int KGS_R/*  */= 0x1;
		int VGS_R/*  */= 0x2;
		int K_N/*    */= 0x4;
		int K_OP/*   */= 0x8;
		int V_N/*    */= 0x10;
		int V_OP/*   */= 0x20;
		int SORTED/* */= 0x40;
		int REPLACEMENT = 0x80;

		String TEMPLATE = "com.nc.gs.serializers.java.util.opt.MS";
	}

	interface MultiCSOptimizer {

		String CN_TEMPLATE = "MCS/of/%s%s{%s}%d%d";

		String CN_ARRAY_TEMPLATE = "MAS/of/{%s}%d%d";

		String CN_SET_TEMPLATE = "SS/of/{%s}%d%d";

		String templateIN = "com/nc/gs/serializers/java/util/opt/CSMulti";
		String templateINSET = "com/nc/gs/serializers/java/util/opt/SSMulti";
		String templateINARRAY = "com/nc/gs/serializers/java/util/opt/ASMulti";

		String INSTANCE = "I";
		String ctr = "ctor";
		String GS_PREF = "gs";

		String IC_READ = "IC_R";

		String IC_WRITE = "IC_W";

		String TEMPLATE = "com.nc.gs.serializers.java.util.opt.CSMulti";
		String TEMPLATE_SET = "com.nc.gs.serializers.java.util.opt.SSMulti";
		String TEMPLATE_ARRAY = "com.nc.gs.serializers.java.util.opt.ASMulti";
	}

	interface MultiMSOptimizer {

		String CN_TEMPLATE = "MMS/of/%s%s{%s,%s}%d%d%d%d";

		String templateIN = "com/nc/gs/serializers/java/util/opt/MSMulti";

		String INSTANCE = "I";
		String ctr = "ctor";
		String KGS_PREF = "ks";
		String VGS_PREF = "vs";

		String ICK_R = "ICK_R";
		String ICK_W = "ICK_W";

		String ICV_R = "ICV_R";
		String ICV_W = "ICV_W";

		String TEMPLATE = "com.nc.gs.serializers.java.util.opt.MSMulti";
	}

	public interface ObjectShape {

		byte NON_SORTED/*     */= 0;
		byte SORTED/*         */= 0x1;
		byte SIZED/*          */= 0x2;
		byte ENUM/*           */= 0x4;
		byte LEAF/*           */= 0x8;
		int BLOCKING/*        */= 0x10;
		int COLLECTION/*      */= 0x20;
		int SET/*             */= 0x40;
		int SORTED_SET/*      */= SET | SORTED;
		int QUEUE/*           */= 0x80 | COLLECTION;
		int PRIORITY_QUEUE/*  */= QUEUE | SORTED | SIZED;
		int PRIORITY_B_QUEUE/**/= QUEUE | SORTED | SIZED | BLOCKING;
		int MAP/*             */= 0x100;
		int SORTED_MAP/*      */= MAP | SORTED;
		int ARRAY/*           */= 0x200;
		int LEAF_ARRAY/*      */= ARRAY | LEAF;
		int ENUM_SET/*        */= SET | ENUM;
		int ENUM_MAP/*        */= MAP | ENUM;
		int REPLACEMENT/*     */= 0x04000000;
		int NULLABLE/*        */= 0x08000000;
		int REIFIED_SER/*     */= 0x10000000;
		int ONLY_PAYLOAD/*    */= 0x20000000;
		int OPTIMIZE/*        */= 0x40000000;
		int OPAQUE/*          */= 0x80000000;
		int UNKONW/*          */= Integer.MIN_VALUE;
	}

	public interface Serializer {
		byte NULL/*        */= 0;
		byte FALSE/*       */= 0;
		byte MAYBE_OPAQUE/**/= 0;
		int NULL_I/*       */= 0;
		byte TYPE_ID/*     */= 1;
		byte TRUE/*        */= 1;
		byte NON_OPAQUE/*  */= 1;
		byte REF/*         */= 2;
		int REF_I/*        */= 2;
		int ID_BASE/*      */= 3;
	}
}