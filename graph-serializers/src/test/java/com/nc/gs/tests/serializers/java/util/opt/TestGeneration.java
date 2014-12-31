package com.nc.gs.tests.serializers.java.util.opt;

import gnu.trove.set.hash.THashSet;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import org.junit.Test;

import com.nc.gs.log.Log;
import com.nc.gs.tests.AbstractRoundTripTests;

public class TestGeneration extends AbstractRoundTripTests {

	@Test
	public void testCanGenerateForEveryCombination() {

		boolean[] states = { false, true };

		Class<? extends Collection<?>>[] samples = newCollTypeArray(
				TreeSet.class, LinkedHashSet.class, THashSet.class);

		for (Class<? extends Collection<?>> sample : samples) {
			for (Class<?> c : new Class<?>[] { String.class, BigInteger.class }) {

				for (boolean left : states) {
					for (boolean right : states) {
						Log.info("Loaded ",
								rawForCollection(sample, c, true, left, right));
					}
				}
			}
		}
	}

}