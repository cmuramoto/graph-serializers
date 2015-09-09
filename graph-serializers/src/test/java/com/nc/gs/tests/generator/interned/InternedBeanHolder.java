package com.nc.gs.tests.generator.interned;

import java.math.BigInteger;

import com.nc.gs.meta.Intern;
import com.nc.gs.meta.LeafNode;

public class InternedBeanHolder {

	@Intern
	InternedBean left;

	@Intern
	@LeafNode
	InternedBean right;

	@Intern
	String leftTag;

	@Intern
	String rightTag;

	@Intern
	BigInteger leftVal;

	@Intern
	@LeafNode
	BigInteger rightVal;

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
		final InternedBeanHolder other = (InternedBeanHolder) obj;
		if (left == null) {
			if (other.left != null) {
				return false;
			}
		} else if (!left.equals(other.left)) {
			return false;
		}
		if (leftTag == null) {
			if (other.leftTag != null) {
				return false;
			}
		} else if (!leftTag.equals(other.leftTag)) {
			return false;
		}
		if (leftVal == null) {
			if (other.leftVal != null) {
				return false;
			}
		} else if (!leftVal.equals(other.leftVal)) {
			return false;
		}
		if (right == null) {
			if (other.right != null) {
				return false;
			}
		} else if (!right.equals(other.right)) {
			return false;
		}
		if (rightTag == null) {
			if (other.rightTag != null) {
				return false;
			}
		} else if (!rightTag.equals(other.rightTag)) {
			return false;
		}
		if (rightVal == null) {
			if (other.rightVal != null) {
				return false;
			}
		} else if (!rightVal.equals(other.rightVal)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (left == null ? 0 : left.hashCode());
		result = prime * result + (leftTag == null ? 0 : leftTag.hashCode());
		result = prime * result + (leftVal == null ? 0 : leftVal.hashCode());
		result = prime * result + (right == null ? 0 : right.hashCode());
		result = prime * result + (rightTag == null ? 0 : rightTag.hashCode());
		result = prime * result + (rightVal == null ? 0 : rightVal.hashCode());
		return result;
	}

}
