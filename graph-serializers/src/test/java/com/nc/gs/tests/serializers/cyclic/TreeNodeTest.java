package com.nc.gs.tests.serializers.cyclic;

import java.io.IOException;

import org.junit.Test;

import com.nc.gs.core.Genesis;
import com.nc.gs.tests.AbstractRoundTripTests;

public class TreeNodeTest extends AbstractRoundTripTests {

	private void addHeaders(TreeNode tn, int h) {
		for (int i = 0; i < h; i++) {
			tn.addHeader(String.valueOf(i), String.valueOf(i * i * i));
		}

	}

	private TreeNode.KV[] kv(int max) {
		TreeNode.KV[] kv = new TreeNode.KV[max];

		for (int i = 0; i < kv.length; i++) {
			TreeNode.KV o = new TreeNode.KV();
			o.creationTime = System.nanoTime();
			o.key = String.valueOf(i);
			o.val = String.valueOf(i * i);
			kv[i] = o;
		}

		return kv;
	}

	private Object makeNode(int max) {
		TreeNode root = new TreeNode();
		root.kvs = kv(1);

		TreeNode curr = root;

		for (int i = 1; i < max; i++) {
			TreeNode tn = new TreeNode();
			tn.label = String.valueOf(i);
			tn.kvs = kv(i);
			addHeaders(tn, i);

			curr.setNext(tn);
			tn.setPrev(curr);
			curr = tn;
		}

		return root;
	}

	@Test
	public void run() throws IOException {
		Genesis.bootstrap();
		roundTrip(makeNode(1));
		roundTripIO(makeNode(1));

		roundTrip(makeNode(3));
		roundTripIO(makeNode(3));

		roundTrip(makeNode(7));
		roundTripIO(makeNode(3));

		roundTrip(makeNode(10));
		roundTripIO(makeNode(10));
	}

}