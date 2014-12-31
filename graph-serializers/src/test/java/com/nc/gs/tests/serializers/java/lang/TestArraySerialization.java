package com.nc.gs.tests.serializers.java.lang;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.nc.gs.core.SerializerFactory;
import com.nc.gs.serializers.java.lang.StringSerializer;
import com.nc.gs.tests.AbstractRoundTripTests;

public class TestArraySerialization extends AbstractRoundTripTests {

	enum Color {
		R, G, B;
	}

	enum Side {
		Left, Right;
	}

	@Test
	public void run() {
		SerializerFactory.register(String.class, new StringSerializer());

		Color R = Color.R;
		Color G = Color.G;
		Color B = Color.B;

		Side Left = Side.Left;
		Side Right = Side.Right;

		Object[] o = new Object[]{ new int[]{ 0, 1 }, "42", new boolean[]{ true, true, true, true, true, true, false, true, true, false, false, true, false }, new int[]{ 1, 2 }, new int[]{ 3, 4 }, new String[][]{ { "foo" }, { "bar", null, null, null, null, null, null, "foo", null, null, null, "baz", null } },
				new Object[]{ new Color[]{ R, G, B, R, R, R, G, G, G, B, B, B, G, R, B, B, R, G }, new Side[][]{ new Side[]{ Left, Right, Right, Right, Left, Left } } }, null };

		roundTrip(o);

		o[o.length - 1] = o;

		Object[] rec = probeNoValidate(o);

		Assert.assertNotSame(o, rec);
		Assert.assertSame(rec[rec.length - 1], rec);

		o[o.length - 1] = null;
		rec[rec.length - 1] = null;

		Assert.assertTrue(Arrays.deepEquals(o, rec));

		o[o.length - 1] = o;
		rec[rec.length - 1] = rec;

		String dts = Arrays.deepToString(rec);
		System.out.println(dts);
		Assert.assertTrue(dts.equals(Arrays.deepToString(o)));
	}

}
