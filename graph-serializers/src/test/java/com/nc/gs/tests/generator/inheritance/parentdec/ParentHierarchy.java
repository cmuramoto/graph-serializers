package com.nc.gs.tests.generator.inheritance.parentdec;

import org.junit.Test;

import com.nc.gs.tests.AbstractRoundTripTests;
import com.nc.gs.tests.generator.inheritance.parentdec.N0.BasicNode;
import com.nc.gs.tests.generator.inheritance.parentdec.N0.BlackNode;
import com.nc.gs.tests.generator.inheritance.parentdec.N0.RedNode;

public class ParentHierarchy extends AbstractRoundTripTests {

	static N0 black(int i, String color) {
		BlackNode rv = new BlackNode();
		rv.index = i;
		rv.color = color;

		return rv;
	}

	static N0 red(int ix, String label) {
		RedNode rv = new RedNode();
		rv.index = ix;
		rv.label = label;
		return rv;
	}

	private N0 basic(int ix, String color) {
		BasicNode rv = new N0.BasicNode();

		rv.color = color;
		rv.index = ix;
		rv.label = "basic";

		return rv;
	}

	private N0 leaf(int ix) {
		N0 n0 = new N0();
		n0.index = ix;
		return n0;
	}

	private N0 root(int ix) {
		N0 n0 = new N0();
		n0.index = ix;
		return n0;
	}

	@Test
	public void run() {
		G1 g = new G1();

		g.addRb(red(1, "r"));
		g.addRb(black(2, "b"));

		g.addLeaf(leaf(3));

		g.addRoot(root(4));

		g.addAny(red(5, "rn"));
		g.addAny(black(6, "rn"));
		g.addAny(leaf(7));
		g.addAny(root(8));

		g.addNode(basic(9, "white"));

		roundTrip(g);
	}

	@Test
	public void runTypeUse() {
		G0 g = new G0();

		g.addRb(red(1, "r"));
		g.addRb(black(2, "b"));

		g.addLeaf(leaf(3));

		g.addRoot(root(4));

		g.addAny(red(5, "rn"));
		g.addAny(black(6, "rn"));
		g.addAny(leaf(7));
		g.addAny(root(8));

		g.addNode(basic(9, "white"));

		roundTrip(g);
	}
}
