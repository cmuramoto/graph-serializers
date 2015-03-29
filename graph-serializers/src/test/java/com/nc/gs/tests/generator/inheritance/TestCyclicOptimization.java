package com.nc.gs.tests.generator.inheritance;

import org.junit.Test;

import com.nc.gs.core.Genesis;
import com.nc.gs.tests.AbstractRoundTripTests;
import com.nc.gs.tests.generator.inheritance.Root.A;

public class TestCyclicOptimization extends AbstractRoundTripTests {

	@Test
	public void run() {
		Genesis.bootstrap();

		Root r = new A();
		r.i = 100;
		r.s.add(r);

		roundTrip(r);

	}

}