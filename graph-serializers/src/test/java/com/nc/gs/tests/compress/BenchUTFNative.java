package com.nc.gs.tests.compress;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Assert;
import org.junit.Test;

import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public class BenchUTFNative {

	private static final int INNER_LOOPS = 10000;
	static int[] BLOCK_LENS = { 17, 37, 113, 517, 1037 };

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

	@Test
	public void run() {
		try (Sink s = new Sink()) {

			for (int bl : BLOCK_LENS) {
				System.out.printf("-------------(%d)-----------\n", bl);

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
		source.inflateCharAVX(buff);
		Assert.assertEquals(v, new String(buff));

		for (int i = 0; i < 100000; i++) {
			source.reset();
			source.readVarInt();
			source.inflateCharScalar(buff);
		}

		for (int i = 0; i < 100000; i++) {
			source.reset();
			source.readVarInt();
			source.inflateCharAVX(buff);
		}

		doGc();

		long sse = 0;

		for (int i = 0; i < INNER_LOOPS; i++) {
			source.reset();
			source.readVarInt();
			long start = System.nanoTime();
			source.inflateCharAVX(buff);
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

		System.out.printf("SSE: %d. Scalar: %d. SSE improvement? %s. Δ: %d. Ratio: %.2f Len: %d\n", sse, scalar, scalar > sse, scalar - sse, (double) scalar / sse, v.length());

		return scalar > sse;
	}
}