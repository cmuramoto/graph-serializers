package com.nc.gs.tests.serializers.graphs.cols;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedTransferQueue;

import org.junit.Test;

import com.nc.gs.tests.AbstractRoundTripTests;
import com.nc.gs.tests.serializers.graphs.ic.pvt.ICUtil;
import com.nc.gs.tests.serializers.graphs.ic.pvt.OptTree;
import com.nc.gs.tests.serializers.graphs.ic.pvt.SubOptTree;

public class TestTrees extends AbstractRoundTripTests {

	
	@Test
	public void runOpt() {

		for (int i = 0; i < 300; i++) {

			OptTree tree = new OptTree(ICUtil.createICNodes(i),
					ICUtil.createICNodes(LinkedList.class, i),
					ICUtil.createICNodes(ConcurrentLinkedQueue.class, i),
					ICUtil.createICNodes(LinkedTransferQueue.class, i));

			roundTrip(tree);
		}

	}

	@Test
	public void runSub() {

		for (int i = 0; i < 300; i++) {
			SubOptTree tree = new SubOptTree(ICUtil.createStdNodes(i),
					ICUtil.createStdNodes(LinkedList.class, i),
					ICUtil.createStdNodes(ConcurrentLinkedQueue.class, i),
					ICUtil.createStdNodes(LinkedTransferQueue.class, i));

			roundTrip(tree);
		}
	}

}
