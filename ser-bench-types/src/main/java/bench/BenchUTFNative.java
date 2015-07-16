package bench;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;

import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.util.Bits;

public class BenchUTFNative {

	static long toMillis(long nanos) {
		return TimeUnit.MILLISECONDS.convert(nanos, TimeUnit.NANOSECONDS);
	}

	private static final int INNER_LOOPS = 10000;

	static int[] BLOCK_LENS = { 17, 37, 113, 517, 1037, 2023, Bits.nextPrime(10000) };

	void doGc() {
		System.gc();
		System.gc();
		System.gc();
		System.gc();
	}

	String makeString(int n, int len) {
		StringBuilder sb = new StringBuilder();
		ThreadLocalRandom r = ThreadLocalRandom.current();
		for (int i = 0; i < n * len; i++) {
			sb.append((char) r.nextInt(127));
			// sb.append('A');
		}

		return sb.toString();
	}

	public void run() {

		try (Sink s = new Sink()) {

			for (int bl : BLOCK_LENS) {
				System.out.printf("-------------(%d)-----------\n", bl);
				System.out.printf("%s:\tScalar:\tV>S?\tΔ:\tRatio:\tLen:\n", Bits.SIMD_INSN_SET);

				for (int i = 1; i < 10; i++) {
					s.reset();

					String v = makeString(i, bl);
					s.writeUTF(v);

					Source source = s.mirror();

					run(source, v);

				}

				System.out.println("---------------------------");
			}
		}
	}

	private boolean run(Source source, String v) {
		char[] buff = new char[v.length()];
		source.reset();
		Assert.assertEquals(v, source.readUTF());

		source.reset();
		source.readVarInt();
		source.inflateCharSIMD(buff);
		Assert.assertEquals(v, new String(buff));

		for (int i = 0; i < 100000; i++) {
			source.reset();
			source.readVarInt();
			source.inflateCharScalar(buff);
		}

		for (int i = 0; i < 100000; i++) {
			source.reset();
			source.readVarInt();
			source.inflateCharSIMD(buff);
		}

		doGc();

		long sse = 0;

		for (int i = 0; i < INNER_LOOPS; i++) {
			source.reset();
			source.readVarInt();
			long start = System.nanoTime();
			source.inflateCharSIMD(buff);
			sse += System.nanoTime() - start;
		}

		doGc();
		long scalar = 0;
		for (int i = 0; i < INNER_LOOPS; i++) {
			source.reset();
			source.readVarInt();
			long start = System.nanoTime();
			source.inflateCharScalar(buff);
			scalar += System.nanoTime() - start;
		}

		// System.out.printf("SSE:\t%d\t Scalar:\t%d\t SSE improvement?\t%s\t Δ:\t%d\t Ratio: \t%.2f\t Len: \t%d\n",
		// sse, scalar, scalar > sse, scalar - sse, (double) scalar / sse, v.length());
		System.out.printf("%d\t%d\t%s\t%d\t%.2f\t%d\n", toMillis(sse), toMillis(scalar), scalar > sse, toMillis(scalar - sse), (double) scalar / sse, v.length());

		return scalar > sse;
	}
}
