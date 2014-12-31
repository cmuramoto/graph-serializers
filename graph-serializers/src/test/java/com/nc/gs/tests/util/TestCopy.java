package com.nc.gs.tests.util;

import org.junit.Assert;
import org.junit.Test;

import com.nc.gs.util.Bits;

public class TestCopy {

	static long base = Bits.allocateMemory(2048 * 2048 * 16);

	@Test
	public void testCopyBytes() {

		byte[] src = "dasldnlaskdjd".getBytes();

		byte[] dst = new byte[src.length];

		Bits.copyFrom(base, src, 0, src.length);

		Bits.copyTo(base, dst, 0, dst.length);

		Assert.assertArrayEquals(src, dst);
	}

	@Test
	public void testCopyChars() {

		char[] src = "dasldnlaskdjd".toCharArray();

		char[] dst = new char[src.length];

		Bits.copyFrom(base, src, 0, src.length);

		Bits.copyTo(base, dst, 0, dst.length);

		Assert.assertArrayEquals(src, dst);
	}

	@Test
	public void testCopyCharsLarge() {

		char[] src = new char[2048 * 2048];

		for (int i = 0; i < src.length; i++) {
			src[i] = (char) i;
		}

		char[] dst = new char[src.length];

		Bits.copyFrom(base, src, 0, src.length);

		Bits.copyTo(base, dst, 0, dst.length);

		Assert.assertArrayEquals(src, dst);
	}

	@Test
	public void testCopyCopyShortsLarge() {

		short[] src = new short[2048 * 2048];

		for (int i = 0; i < src.length; i++) {
			src[i] = (short) i;
		}

		short[] dst = new short[src.length];

		Bits.copyFrom(base, src, 0, src.length);

		Bits.copyTo(base, dst, 0, dst.length);

		Assert.assertArrayEquals(src, dst);
	}

	@Test
	public void testCopyIntsLarge() {

		int[] src = new int[2048 * 2048];

		for (int i = 0; i < src.length; i++) {
			src[i] = i;
		}

		int[] dst = new int[src.length];

		Bits.copyFrom(base, src, 0, src.length);

		Bits.copyTo(base, dst, 0, dst.length);

		Assert.assertArrayEquals(src, dst);
	}

	@Test
	public void testDoublesLarge() {

		double[] src = new double[2048 * 2048];

		for (int i = 0; i < src.length; i++) {
			src[i] = Math.random();
		}

		double[] dst = new double[src.length];

		Bits.copyFrom(base, src, 0, src.length);

		Bits.copyTo(base, dst, 0, dst.length);

		Assert.assertArrayEquals(src, dst, 0.001d);
	}

	@Test
	public void testFloatsLarge() {

		float[] src = new float[2048 * 2048];

		for (int i = 0; i < src.length; i++) {
			src[i] = (float) Math.random();
		}

		float[] dst = new float[src.length];

		Bits.copyFrom(base, src, 0, src.length);

		Bits.copyTo(base, dst, 0, dst.length);

		Assert.assertArrayEquals(src, dst, 0.001f);
	}
}
