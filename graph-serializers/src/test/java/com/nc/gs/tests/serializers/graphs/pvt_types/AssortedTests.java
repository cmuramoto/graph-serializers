package com.nc.gs.tests.serializers.graphs.pvt_types;

import java.math.BigInteger;

import org.junit.Test;

import com.nc.gs.tests.AbstractRoundTripTests;

public class AssortedTests extends AbstractRoundTripTests {


	@Test
	public void testCanSerializeStatefulTypeWithPrivateInnerClass()
			throws Exception {

		StatefulTypeWrapper tw = new StatefulTypeWrapper();

		tw.setI(1);
		tw.setJ(2);
		tw.setK(new BigInteger(
				"3141592653589793238462643383279502884197169399375105820974944592307816406286"));
		tw.setS("42");
		tw.setD(35);
		tw.setId("FIN");

		roundTrip(tw);
	}

	@Test
	public void testCanSerializeStatelessTypeWithPrivateInnerClass()
			throws Exception {

		StatelessTypeWrapper tw = new StatelessTypeWrapper();

		tw.setI(1);
		tw.setJ(2);
		tw.setS("42");

		roundTrip(tw);
	}
}
