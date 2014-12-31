package com.nc.gs.tests.serializers.graphs.ic;

import org.junit.Test;

import com.nc.gs.tests.AbstractRoundTripTests;
import com.nc.gs.tests.serializers.graphs.ic.acc.BlackNode;
import com.nc.gs.tests.serializers.graphs.ic.acc.BlackNode.Amplitude;
import com.nc.gs.tests.serializers.graphs.ic.acc.ICNode;
import com.nc.gs.tests.serializers.graphs.ic.acc.RedNode;
import com.nc.gs.tests.serializers.graphs.ic.acc.SimpleNode;

public class TestICObjects extends AbstractRoundTripTests {

	@Test
	public void run() {

		ICNode br = new ICNode();

		br.setId(1);
		br.setLeft(new BlackNode("42", Amplitude.Low));
		br.setRight(new RedNode(1, 42));

		roundTrip(br);

		ICNode sr = new ICNode();

		sr.setId(2);
		sr.setLeft(new SimpleNode("FIN"));
		sr.setRight(new RedNode(1, 42));

		roundTrip(sr);

		ICNode bs = new ICNode();

		bs.setId(3);
		bs.setLeft(new BlackNode("42", Amplitude.High));
		bs.setRight(new SimpleNode("NIF"));

		roundTrip(bs);

		ICNode ss = new ICNode();
		ss.setId(4);
		ss.setLeft(new SimpleNode("Simple"));
		ss.setRight(new SimpleNode("Node"));

		roundTrip(ss);
	}

	@Test(expected = ClassCastException.class)
	public void runViolatingHierarchy() {
		ICNode rb = new ICNode();

		rb.setId(1);
		rb.setLeft(new RedNode(1, 42));
		rb.setRight(new BlackNode("42", Amplitude.Low));

		roundTrip(rb);
	}

}
