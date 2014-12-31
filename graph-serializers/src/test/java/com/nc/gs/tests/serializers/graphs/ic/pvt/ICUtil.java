package com.nc.gs.tests.serializers.graphs.ic.pvt;

import static com.nc.gs.tests.serializers.graphs.ic.pvt.BlackNode.Amplitude.High;
import static com.nc.gs.tests.serializers.graphs.ic.pvt.BlackNode.Amplitude.Low;

import java.util.ArrayList;
import java.util.Collection;

import com.nc.gs.core.SerializerFactory;

public class ICUtil {

	@SuppressWarnings("rawtypes")
	public static Collection<ICNode> createICNodes(
			Class<? extends Collection> type, int max) {

		@SuppressWarnings("unchecked")
		Collection<ICNode> list = (Collection<ICNode>) SerializerFactory
				.instantiatorOf(type).allocate(max);

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

	public static Collection<ICNode> createICNodes(int max) {
		return createICNodes(ArrayList.class, max);
	}

	@SuppressWarnings("unchecked")
	public static Collection<StandardNode> createStdNodes(
			@SuppressWarnings("rawtypes") Class<? extends Collection> type,
			int max) {

		Collection<StandardNode> list = (Collection<StandardNode>) SerializerFactory
				.instantiatorOf(type).allocate(max);

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

	public static Collection<StandardNode> createStdNodes(int max) {
		return createStdNodes(ArrayList.class, max);
	}

}
