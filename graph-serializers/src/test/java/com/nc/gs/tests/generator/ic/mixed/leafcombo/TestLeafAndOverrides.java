package com.nc.gs.tests.generator.ic.mixed.leafcombo;

import org.junit.Test;

import com.nc.gs.tests.AbstractRoundTripTests;

public class TestLeafAndOverrides extends
		AbstractRoundTripTests {

	@Test
	public void run() {

		Composition cmp = new Composition();
		cmp.a = new Final(42);
		cmp.b = new NonFinal().with(123);
		// overrides @LeafNode from parent with @InternalNode
		cmp.override = new NonFinal().new Child().with(10, 20);
		cmp.redundant = "42";
		cmp.wrong = new Final(0666);
		cmp.wrongString = "wrong";

		roundTrip(cmp);
	}

	@Test(expected = AssertionError.class)
	public void testWrongAssignment() {
		Composition cmp = new Composition();
		cmp.a = new Final(42);
		// declared leaf and type changed
		cmp.b = new NonFinal().new Child().with(10, 20);
		cmp.override = new NonFinal().with(123);
		cmp.redundant = "42";
		cmp.wrong = new Final(0666);
		cmp.wrongString = "wrong";

		roundTrip(cmp);
	}

}
