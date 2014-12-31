package com.nc.gs.tests.serializers.graphs.cols;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Collections.synchronizedList;
import static java.util.Collections.synchronizedMap;
import static java.util.Collections.synchronizedSet;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.nc.gs.tests.AbstractRoundTripTests;

public class TestSerializersForOpaqueLogicalTypes extends AbstractRoundTripTests {

	@Test
	public void testSingletonList() {
		roundTrip(singletonList("42"));
	}

	@Test
	public void testSingletonMap() {
		roundTrip(singletonMap("42", "YES"));
	}

	@Test
	public void testSingletonSet() {
		roundTrip(singleton("42"));
	}

	@Test
	public void testSynchronizedArrayList() {
		List<String> list = new ArrayList<>();
		list.add("42");
		roundTrip(synchronizedList(list));
	}

	@Test
	public void testSynchronizedSingletonList() {
		roundTrip(synchronizedList(singletonList("42")));
	}

	@Test
	public void testSynchronizedSingletonMap() {
		roundTrip(synchronizedMap(singletonMap("42", "YES")));
	}

	@Test
	public void testSynchronizedSingletonSet() {
		roundTrip(synchronizedSet(singleton("42")));
	}

	@Test
	public void testSynchronizedUnmodifiableArrayListList() {
		ArrayList<String> list = new ArrayList<>();
		list.add("42");
		roundTrip(synchronizedList(unmodifiableList(list)));
	}

	@Test
	public void testSynchronizedUnmodifiableArraysAsListList() {

		roundTrip(synchronizedList(unmodifiableList(asList("42"))));
	}

	@Test
	public void testSynchronizedUnmodifiableSingletonList() {
		roundTrip(synchronizedList(unmodifiableList(singletonList("42"))));
	}

	@Test
	public void testUnmodifiableArrayListList() {
		ArrayList<String> list = new ArrayList<>();
		list.add("42");
		roundTrip(unmodifiableList(list));
	}

	@Test
	public void testUnmodifiableArraysAsListList() {
		roundTrip(unmodifiableList(asList("42")));
	}

	@Test
	public void testUnmodifiableSingletonList() {
		roundTrip(unmodifiableList(singletonList("42")));
	}

}
