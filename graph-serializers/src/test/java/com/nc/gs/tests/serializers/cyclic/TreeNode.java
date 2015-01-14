package com.nc.gs.tests.serializers.cyclic;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.nc.gs.meta.Collection;
import com.nc.gs.meta.Compress;
import com.nc.gs.meta.Hierarchy;
import com.nc.gs.meta.OnlyPayload;
import com.nc.gs.meta.Shape;

public class TreeNode {

	public static class KV {

		String key;

		String val;

		long creationTime;

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			KV other = (KV) obj;
			if (creationTime != other.creationTime) {
				return false;
			}
			if (key == null) {
				if (other.key != null) {
					return false;
				}
			} else if (!key.equals(other.key)) {
				return false;
			}
			if (val == null) {
				if (other.val != null) {
					return false;
				}
			} else if (!val.equals(other.val)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (creationTime ^ creationTime >>> 32);
			result = prime * result + (key == null ? 0 : key.hashCode());
			result = prime * result + (val == null ? 0 : val.hashCode());
			return result;
		}

		@Override
		public String toString() {
			return MessageFormat.format("KV [{0}, {1}, {2}]", key, val, creationTime);
		}

	}

	@OnlyPayload
	String label;

	TreeNode prev;

	TreeNode next;

	@OnlyPayload
	@Collection(optimize = true, shape = @Shape(hierarchy = @Hierarchy(complete = true), onlyPayload = true))
	KV[] kvs;

	@OnlyPayload
	@com.nc.gs.meta.Map(optimize = true, key = @Shape(hierarchy = @Hierarchy(complete = true), onlyPayload = true, nullable = false), val = @Shape(hierarchy = @Hierarchy(complete = true), onlyPayload = true, nullable = false))
	Map<String, String> headers;

	@Compress
	int depth;

	public void addHeader(String k, String v) {
		if (headers == null) {
			headers = new HashMap<String, String>();
		}
		headers.put(k, v);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TreeNode other = (TreeNode) obj;
		if (depth != other.depth) {
			return false;
		}
		if (!Arrays.equals(kvs, other.kvs)) {
			return false;
		}

		TreeNode next = this.next;
		TreeNode oNext = other.next;

		while (next != null) {
			if (!next.equals(oNext)) {
				return false;
			}
			next = next.next;
			oNext = oNext.next;
		}

		return true;
	}

	public int getDepth() {
		return depth;
	}

	public KV[] getKvs() {
		return kvs;
	}

	public TreeNode getNext() {
		return next;
	}

	public TreeNode getPrev() {
		return prev;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + depth;
		result = prime * result + Arrays.hashCode(kvs);
		result = prime * result + (label == null ? 0 : label.hashCode());
		result = prime * result + (next == null ? 0 : next.hashCode());
		result = prime * result + (prev == null ? 0 : prev.hashCode());
		return result;
	}

	@Test
	public void run() {

	}

	public void setKvs(KV[] kvs) {
		this.kvs = kvs;
	}

	public void setNext(TreeNode next) {
		this.next = next;
	}

	public void setPrev(TreeNode prev) {
		this.prev = prev;
		if (prev != null) {
			depth = prev.depth + 1;
		} else {
			depth = 0;
		}
	}

	@Override
	public String toString() {
		return "TreeNode [label=" + label + ", prev=" + prev + ", next=" + next + ", kvs=" + Arrays.toString(kvs) + ", depth=" + depth + "]";
	}
}