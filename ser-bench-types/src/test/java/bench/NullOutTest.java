package bench;

import static com.nc.gs.util.Utils.U;
import static sun.misc.Unsafe.ARRAY_OBJECT_BASE_OFFSET;
import static sun.misc.Unsafe.ARRAY_OBJECT_INDEX_SCALE;

import java.text.MessageFormat;
import java.util.Arrays;

import org.junit.Test;

import com.nc.gs.util.Bits;

@SuppressWarnings("restriction")
public class NullOutTest {

	static class Stats {
		long cm;
		long sm;
		long ac;
		long fill;
		int sz;

		@Override
		public String toString() {
			return MessageFormat.format("Stats [sz={0}, cm={1}, sm={2}, ac={3}, fill={4}]", sz, cm, sm, ac, fill);
		}

	}

	static void clearWithCopyMem(Object[] array, int off, int len) {
		long o = ARRAY_OBJECT_BASE_OFFSET + off * ARRAY_OBJECT_INDEX_SCALE;
		long l = len * ARRAY_OBJECT_INDEX_SCALE;

		long size = CM_BLOCK;

		while (l > 0) {
			// VM not ready for this yet
			// U.copyMemory(null, NULL_MEM, array, o, size);
			U.copyMemory(NULL_OBJ, ARRAY_OBJECT_BASE_OFFSET, array, o, size);
			l -= size;
			o += size;
		}
	}

	static final long CM_BLOCK = 1024;

	static long NULL_MEM;
	static Object[] NULL_OBJ;

	static {
		NULL_MEM = Bits.allocateMemory(CM_BLOCK);
		U.setMemory(NULL_MEM, CM_BLOCK, (byte) 0);
		NULL_OBJ = new Object[(int) CM_BLOCK];
	}

	static Object REF = new Object();

	@Test
	public void run() {
		test(10);
		test(100);
		test(500);
		test(1000);
		test(2000);
		test(4000);
		test(10000);
		test(20000);
		test(30000);
	}

	private void test(int max) {
		Object[] o = new Object[max];

		Stats stats = new Stats();
		stats.sz = max;
		long e;

		e = 0;
		for (int i = 0; i < 100; i++) {
			Arrays.fill(o, REF);
			long s = System.nanoTime();
			clearWithCopyMem(o, 0, o.length);
			e += System.nanoTime() - s;
		}
		stats.cm = e;

		System.out.println(stats);
	}

}