package com.nc.gs.tests.generator.inheritance.parentdec;

import java.util.ArrayList;
import java.util.List;

import com.nc.gs.meta.Hierarchy;
import com.nc.gs.meta.LeafNode;
import com.nc.gs.meta.Optimize;

public class G0 {

	@Optimize
	List<N0> any;

	@Optimize
	List<@Hierarchy(types = { N0.BasicNode.class }) N0> nodes;

	@Optimize
	List<@Hierarchy(types = { N0.class }) N0> roots;

	@Optimize
	List<@LeafNode N0> leaves;

	@Optimize
	List<@Hierarchy(types = { N0.RedNode.class, N0.BlackNode.class }) N0> rb;

	public void addAny(N0 n) {
		if (any == null) {
			any = new ArrayList<>();
		}
		any.add(n);
	}

	public void addLeaf(N0 n) {
		if (leaves == null) {
			leaves = new ArrayList<>();
		}
		leaves.add(n);
	}

	public void addNode(N0 n) {
		if (nodes == null) {
			nodes = new ArrayList<>();
		}
		nodes.add(n);
	}

	public void addRb(N0 n) {
		if (rb == null) {
			rb = new ArrayList<>();
		}
		rb.add(n);
	}

	public void addRoot(N0 n) {
		if (roots == null) {
			roots = new ArrayList<>();
		}
		roots.add(n);
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
		G0 other = (G0) obj;
		if (any == null) {
			if (other.any != null) {
				return false;
			}
		} else if (!any.equals(other.any)) {
			return false;
		}
		if (leaves == null) {
			if (other.leaves != null) {
				return false;
			}
		} else if (!leaves.equals(other.leaves)) {
			return false;
		}
		if (nodes == null) {
			if (other.nodes != null) {
				return false;
			}
		} else if (!nodes.equals(other.nodes)) {
			return false;
		}
		if (rb == null) {
			if (other.rb != null) {
				return false;
			}
		} else if (!rb.equals(other.rb)) {
			return false;
		}
		if (roots == null) {
			if (other.roots != null) {
				return false;
			}
		} else if (!roots.equals(other.roots)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((any == null) ? 0 : any.hashCode());
		result = prime * result + ((leaves == null) ? 0 : leaves.hashCode());
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		result = prime * result + ((rb == null) ? 0 : rb.hashCode());
		result = prime * result + ((roots == null) ? 0 : roots.hashCode());
		return result;
	}

}
