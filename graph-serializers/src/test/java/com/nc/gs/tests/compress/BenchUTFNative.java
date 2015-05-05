package com.nc.gs.tests.compress;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.io.UTF8Util;

public class BenchUTFNative {

	public static String decodeFlags(long flags) {
		StringBuilder sb = new StringBuilder();

		sb.append("[\n");
		sb.append("\tForce SSE Alignment: ").append((flags & 0x1) != 0).append(", \n");
		sb.append("\tForce AVX2 Alignment: ").append((flags & 0x2) != 0).append(", \n");
		sb.append("\tForce AVX512 Alignment: ").append((flags & 0x2) != 0).append(", \n");
		sb.append("\tTry Aligned Stores: ").append((flags & 0x8) != 0);
		sb.append("\n]");

		return sb.toString();
	}

	@BeforeClass
	public static void init() {
		System.out.println("Compilation Flags: \n" + decodeFlags(UTF8Util.compilationFlags()));
	}

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
			source.inflateCharSSE(buff);
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

		System.out.printf("SSE:\t%d\t Scalar:\t%d\t SSE improvement?\t%s\t Δ:\t%d\t Ratio: \t%.2f\t Len: \t%d\n", sse, scalar, scalar > sse, scalar - sse, (double) scalar / sse, v.length());

		return scalar > sse;
	}
}