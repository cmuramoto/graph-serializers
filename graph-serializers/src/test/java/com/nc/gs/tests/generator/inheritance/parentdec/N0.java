package com.nc.gs.tests.generator.inheritance.parentdec;

import com.nc.gs.meta.Hierarchy;

@Hierarchy(types = { N0.BasicNode.class, N0.BlackNode.class, N0.RedNode.class })
public class N0 {

	public static class BasicNode extends N0 {

		String label;

		String color;

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			BasicNode other = (BasicNode) obj;
			if (color == null) {
				if (other.color != null) {
					return false;
				}
			} else if (!color.equals(other.color)) {
				return false;
			}
			if (label == null) {
				if (other.label != null) {
					return false;
				}
			} else if (!label.equals(other.label)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((color == null) ? 0 : color.hashCode());
			result = prime * result + ((label == null) ? 0 : label.hashCode());
			return result;
		}

	}

	public static class BlackNode extends BasicNode {

		public BlackNode() {
			color = "BLACK";
		}

	}

	public static class RedNode extends BasicNode {

		public RedNode() {
			color = "RED";
		}
	}

	int index;

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
		N0 other = (N0) obj;
		if (index != other.index) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		return result;
	}
}
