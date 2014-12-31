package com.nc.gs.tests.generator.ic.mixed.samples;

import java.math.BigInteger;

import com.nc.gs.meta.LeafNode;
import com.nc.gs.meta.NotNull;
import com.nc.gs.meta.OnlyPayload;

public class Item {

	String label;

	@SuppressWarnings("unused")
	private String target;

	@OnlyPayload
	String state;

	@OnlyPayload
	private String condition;

	@NotNull
	String mandatoryLabel;

	@NotNull
	private String mandatoryTarget;

	@NotNull
	@OnlyPayload
	String mandatoryState;

	@NotNull
	@OnlyPayload
	private String mandatoryCondition;

	@LeafNode
	BigInteger price;

	@LeafNode
	private BigInteger val;

	@LeafNode
	@OnlyPayload
	BigInteger range;

	@LeafNode
	@OnlyPayload
	private BigInteger age;

	@NotNull
	@LeafNode
	BigInteger mandatoryPrice;

	@NotNull
	@LeafNode
	private BigInteger mandatoryVal;

	@NotNull
	@LeafNode
	@OnlyPayload
	BigInteger mandatoryRange;

	@NotNull
	@LeafNode
	@OnlyPayload
	private BigInteger mandatoryAge;

}
