package com.nc.gs.tests.arrays;

import org.junit.Test;

import com.nc.gs.tests.AbstractRoundTripTests;

public class TestArrayHolder extends AbstractRoundTripTests {

	private Object createArrayHolder() {
		ArrayHolder rv = new ArrayHolder();
		rv.bools = new boolean[]{ true, false };
		rv.bytes = new byte[]{ 0, 1 };
		rv.chars = new char[]{ '0', '1' };
		rv.shorts = new short[]{ 0, 1 };
		rv.setInts(new int[]{ 0, 1 });
		rv.floats = new float[]{ 0, 1 };
		rv.longs = new long[]{ 0, 1 };
		rv.doubles = new double[]{ 0, 1 };

		rv.nn_bools = new boolean[]{ true, false };
		rv.nn_bytes = new byte[]{ 0, 1 };
		rv.setNn_chars(new char[]{ '0', '1' });
		rv.nn_shorts = new short[]{ 0, 1 };
		rv.nn_ints = new int[]{ 0, 1 };
		rv.nn_floats = new float[]{ 0, 1 };
		rv.nn_longs = new long[]{ 0, 1 };
		rv.nn_doubles = new double[]{ 0, 1 };

		rv.op_bools = new boolean[]{ true, false };
		rv.op_bytes = new byte[]{ 0, 1 };
		rv.op_chars = new char[]{ '0', '1' };
		rv.op_shorts = new short[]{ 0, 1 };
		rv.op_ints = new int[]{ 0, 1 };
		rv.setOp_floats(new float[]{ 0, 1 });
		rv.op_longs = new long[]{ 0, 1 };
		rv.op_doubles = new double[]{ 0, 1 };

		rv.nn_op_bools = new boolean[]{ true, false };
		rv.setNn_op_bytes(new byte[]{ 0, 1 });
		rv.nn_op_chars = new char[]{ '0', '1' };
		rv.nn_op_shorts = new short[]{ 0, 1 };
		rv.nn_op_ints = new int[]{ 0, 1 };
		rv.nn_op_floats = new float[]{ 0, 1 };
		rv.nn_op_longs = new long[]{ 0, 1 };
		rv.nn_op_doubles = new double[]{ 0, 1 };

		return rv;
	}

	@Test
	public void run() {
		roundTrip(createArrayHolder());
	}

}
