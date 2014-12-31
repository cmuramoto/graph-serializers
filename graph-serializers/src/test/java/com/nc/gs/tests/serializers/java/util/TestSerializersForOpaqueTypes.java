package com.nc.gs.tests.serializers.java.util;

import java.math.BigDecimal;

import org.junit.Test;

import com.nc.gs.tests.AbstractRoundTripTests;

public class TestSerializersForOpaqueTypes extends AbstractRoundTripTests {

	@Test
	public void testBigDecimalInteger() {
		roundTrip(new BigDecimal("1"));

	}

	@Test
	public void testBigDecimalLargeFloat() {

		roundTrip(new BigDecimal(
				"8391809812983912083901209382190380138120938912831.483798123918340"));
	}

	@Test
	public void testBigDecimalSmallFloat() {

		roundTrip(new BigDecimal("1.1"));

	}
}