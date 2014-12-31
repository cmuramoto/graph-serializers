package com.nc.gs.tests.serializers.graphs.ic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.log.Log;
import com.nc.gs.tests.AbstractRoundTripTests;
import com.nc.gs.tests.StopWatch;
import com.nc.gs.tests.StopWatch.TaskInfo;
import com.nc.gs.tests.serializers.graphs.ic.acc.ICNode;
import com.nc.gs.tests.serializers.graphs.ic.acc.ICUtil;
import com.nc.gs.tests.serializers.graphs.ic.acc.Node;
import com.nc.gs.tests.serializers.graphs.ic.acc.StandardNode;

public class BenchNodes extends AbstractRoundTripTests {

	static Random r = new Random();

	@SuppressWarnings("unchecked")
	private <T extends Node> void bench(Sink bb, Class<T> type, List<T> nodes, StopWatch sw, int itrs, String name) {

		GraphSerializer gs = rawForCollection(ArrayList.class, type, false, false, false);

		sw.start(name);

		for (int i = 0; i < itrs; i++) {
			bb.clear();

			try (Context c = Context.writing()) {
				gs.write(c, bb, nodes);
			}

			bb.clear();

			try (Context c = Context.reading()) {
				Assert.assertEquals(nodes.size(), ((List<T>) gs.read(c, bb.mirror())).size());
			}
		}

		sw.stop();
	}

	@Test
	public void run() {
		StopWatch ic = new StopWatch("IC");
		StopWatch std = new StopWatch("STD");

		List<ICNode> icNodes = ICUtil.createICNodes(100);

		for (ICNode icN : icNodes) {
			roundTrip(icN);
		}

		List<StandardNode> stdNodes = ICUtil.createStdNodes(100);

		for (StandardNode sn : stdNodes) {
			roundTrip(sn);
		}

		Sink direct = new Sink(1024 * 1024);

		for (int i = 0; i < 5; i++) {
			bench(direct, ICNode.class, icNodes, ic, 100000, "IC#" + i);
			bench(direct, StandardNode.class, stdNodes, std, 100000, "STD#" + i);
		}

		Log.info(ic.prettyPrintTimed(true));
		Log.info(std.prettyPrintTimed(true));

		TaskInfo icF = ic.fastestTask();
		TaskInfo stdF = std.fastestTask();

		Log.info(icF.reportPctVs(stdF));

		Log.info(stdF.reportPctVs(icF));

	}

}
