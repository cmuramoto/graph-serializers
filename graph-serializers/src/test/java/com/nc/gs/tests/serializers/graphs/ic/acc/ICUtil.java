package com.nc.gs.tests.serializers.graphs.ic.acc;

import static com.nc.gs.tests.serializers.graphs.ic.acc.BlackNode.Amplitude.High;
import static com.nc.gs.tests.serializers.graphs.ic.acc.BlackNode.Amplitude.Low;

import java.util.ArrayList;
import java.util.List;

import com.nc.gs.core.SerializerFactory;

public class ICUtil {

	@SuppressWarnings("rawtypes")
	public static List<ICNode> createICNodes(Class<? extends List> type, int max) {

		@SuppressWarnings("unchecked")
		List<ICNode> list = (List<ICNode>) SerializerFactory.instantiatorOf(
				type).allocate(max);

		for (int i = 0; i < max; i++) {
			ICNode node = new ICNode();
			node.setId(i);
			node.setLeft((i & 1) == 0 ? new BlackNode(String.valueOf(i),
					i % 3 == 0 ? Low : High) : new SimpleNode("BSN" + i));
			node.setRight((i & 1) == 0 ? new RedNode(i, i * i * i)
					: new SimpleNode("RSN" + i));
			list.add(node);
		}

		return list;
	}

	public static List<ICNode> createICNodes(int max) {
		return createICNodes(ArrayList.class, max);
	}

	public static List<StandardNode> createStdNodes(int max) {

		ArrayList<StandardNode> list = new ArrayList<>(max);

		for (int i = 0; i < max; i++) {
			StandardNode node = new StandardNode();
			node.setId(i);
			node.setLeft((i & 1) == 0 ? new BlackNode(String.valueOf(i),
					i % 3 == 0 ? Low : High) : new SimpleNode("BSN" + i));
			node.setRight((i & 1) == 0 ? new RedNode(i, i * i * i)
					: new SimpleNode("RSN" + i));
			list.add(node);
		}

		return list;
	}

}
