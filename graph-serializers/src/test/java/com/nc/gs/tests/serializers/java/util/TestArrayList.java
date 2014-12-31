package com.nc.gs.tests.serializers.java.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.nc.gs.core.Context;
import com.nc.gs.io.Sink;
import com.nc.gs.serializers.java.util.CollectionSerializer;
import com.nc.gs.tests.AbstractRoundTripTests;

public class TestArrayList extends AbstractRoundTripTests {

	@SuppressWarnings("unchecked")
	@Test
	public void testInformedList() {

		ArrayList<String> set = new ArrayList<>();

		set.addAll(Arrays.asList("foo", "bar", "roo"));

		Sink bb = new Sink();

		for (Class<? extends Collection<?>> type : newCollTypeArray(null, ArrayList.class, LinkedList.class)) {
			for (Class<?> ct : new Class<?>[]{ null, String.class }) {
				for (boolean l : new boolean[]{ false, true }) {
					for (boolean r : new boolean[]{ false, true }) {
						CollectionSerializer ss = new CollectionSerializer(type, ct, l, r);

						bb.clear();

						try (Context c = Context.writing()) {
							ss.writeRoot(c, bb, set);
						}

						bb.clear();

						List<String> rec;

						try (Context c = Context.reading()) {
							rec = (List<String>) ss.readRoot(c, bb.mirror());
						} catch (Exception e) {
							e.printStackTrace();
							throw e;
						}

						Assert.assertEquals(set, rec);
					}
				}
			}
		}
	}
}