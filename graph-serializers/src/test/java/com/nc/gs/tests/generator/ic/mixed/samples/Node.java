package com.nc.gs.tests.generator.ic.mixed.samples;

import com.nc.gs.meta.Hierarchy;

public abstract class Node {

	@Hierarchy(types = { Leaf.class, Root.class, Trunk.class })
	Node prev;

	@Hierarchy(types = { Leaf.class, Root.class, Trunk.class })
	Node next;

	Item item;

	final Item fixed = new Item();

}
