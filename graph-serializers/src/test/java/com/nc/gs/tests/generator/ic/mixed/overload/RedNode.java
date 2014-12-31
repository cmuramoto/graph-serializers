package com.nc.gs.tests.generator.ic.mixed.overload;

import com.nc.gs.meta.Hierarchy;

public class RedNode extends Node {

/**
	 * Same name as the parent's field, same type descriptor, however the rules
	 * are distinct!!! This is a very rare corner case for the inline cache
	 * algorithm, and care must be taken to avoid duplicate methods.<br/>
	 * <br/>
	 * 
	 * For the parent's {@link Node#left} fields we expect the generation of one
	 * method that guards the types [{@link Node},{@link RedNode,
	 * 
	 * @link BlueNode} ] and for {@link RedNode#left}'s field we expect guards
	 *       for the types [{@link Node} , {@link BlackNode}] only.
	 * 
	 */
	@Hierarchy(types = { BlackNode.class })
	Node left;

}
