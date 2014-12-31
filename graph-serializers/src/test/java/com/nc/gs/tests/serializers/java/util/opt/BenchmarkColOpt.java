package com.nc.gs.tests.serializers.java.util.opt;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.log.Log;
import com.nc.gs.tests.AbstractRoundTripTests;
import com.nc.gs.tests.StopWatch;
import com.nc.gs.tests.StopWatch.TaskInfo;
import com.nc.gs.util.Utils;

public class BenchmarkColOpt extends AbstractRoundTripTests {

	@AfterClass
	public static void display() {

		Log.info("\n-----------SUMMARY-----------\n");

		TreeSet<TaskInfo> ts = new TreeSet<TaskInfo>();

		for (StopWatch v : watches) {
			Log.info("%s", v.prettyPrintTimed(true));
			ts.addAll(v.tasks());
		}

		Log.info("Best: %s", ts.first());
		Log.info("Worst: %s", ts.last());
	}

	private static final int TOUCH_LOOPS = 1000;

	private static final int LOOPS = 100000;

	static List<StopWatch> watches = new LinkedList<>();

	static final int DATASET_SIZE = 200;

	private void doCompare(GraphSerializer gs, Collection<String> list, Collection<String> rec) {

		try {
			list.getClass().getDeclaredMethod("equals", Object.class);
			Assert.assertEquals("Failed for " + gs.getClass(), list, rec);

		} catch (NoSuchMethodException e) {
			Log.info("Class %s does not implement equals. Falling back to array comparison", list.getClass());

			Object[] l = list.toArray();

			Object[] r = rec.toArray();

			try {
				Assert.assertArrayEquals("Failed for " + gs.getClass() + " [Using array]", l, r);
			} catch (ArrayComparisonFailure ex) {
				System.out.println(gs.getClass());

				Arrays.sort(l);
				Arrays.sort(r);

				Assert.assertArrayEquals("Failed for " + gs.getClass() + " [Using array]", l, r);
			}
		}

	}

	@SuppressWarnings("unchecked")
	private Collection<String> fillFor(Class<?> c, int max) {
		Collection<String> list;
		try {
			list = (Collection<String>) c.getDeclaredConstructor().newInstance();
			for (int i = 0; i < max; i++) {
				list.add(String.format("%dLeString%d", i, i * 2));
			}
			return list;
		} catch (Exception e) {
			return Utils.rethrow(e);
		}

	}

	@SuppressWarnings("unchecked")
	private void loop(Collection<String> list, Sink buffer, GraphSerializer[] all, StopWatch sw, int max, boolean verify, boolean n, boolean op) {

		ConcurrentHashMap<Object, Object> cs = verify ? new ConcurrentHashMap<>() : null;

		for (GraphSerializer gs : all) {

			if (sw != null) {
				sw.start(gs.toString());
			}

			boolean v = verify;

			for (int i = 0; i < max; i++) {

				try (Context c = Context.writing()) {
					gs.write(c, buffer, list);
				}

				if (v) {
					cs.putIfAbsent(gs.getClass().getName(), buffer.position());
				}

				buffer.clear();

				Collection<String> rec;

				try (Context c = Context.reading()) {
					rec = (Collection<String>) gs.read(c, buffer.mirror());
				}

				buffer.clear();

				if (v) {
					doCompare(gs, list, rec);
					v = false;
				}
			}

			if (sw != null) {
				sw.stop();
			}

		}
	}

	@Test
	public void run() {

		Sink buffer = new Sink(1024 * 1024 * 8);

		Class<? extends Collection<?>>[] colTypes = newCollTypeArray(ArrayList.class, LinkedList.class, THashSet.class, HashSet.class, ConcurrentLinkedDeque.class, ConcurrentLinkedQueue.class);

		boolean[] states = new boolean[]{ false, true };

		for (Class<? extends Collection<?>> c : colTypes) {

			Collection<String> list = fillFor(c, DATASET_SIZE);

			for (boolean left : states) {
				for (boolean right : states) {
					GraphSerializer op = rawForCollection(c, String.class, left, right, true);
					GraphSerializer cs = rawForCollection(c, String.class, left, right, false);

					GraphSerializer[] arr = new GraphSerializer[]{ op, cs };

					loop(list, buffer, arr, null, TOUCH_LOOPS, true, left, right);
				}
			}
		}

		for (Class<? extends Collection<?>> c : colTypes) {
			StopWatch sw = new StopWatch(String.format("%s:[loops: %d,dataset: %d,io ops:2x%d]", c.getSimpleName(), LOOPS, DATASET_SIZE, LOOPS * DATASET_SIZE));
			Collection<String> list = fillFor(c, DATASET_SIZE);

			for (boolean left : states) {
				for (boolean right : states) {
					GraphSerializer op = rawForCollection(c, String.class, left, right, true);
					GraphSerializer cs = rawForCollection(c, String.class, left, right, false);

					GraphSerializer[] arr = new GraphSerializer[]{ op, cs };

					loop(list, buffer, arr, sw, LOOPS, true, left, right);

					for (int i = 0; i < 5; i++) {
						loop(list, buffer, arr, sw, LOOPS, false, left, right);
					}
				}
			}

			Log.info(sw.shortSummary());
			watches.add(sw);
		}
	}
}
