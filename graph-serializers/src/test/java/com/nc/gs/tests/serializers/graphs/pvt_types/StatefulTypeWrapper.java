package com.nc.gs.tests.serializers.graphs.pvt_types;

import java.math.BigInteger;

import com.nc.gs.meta.LeafNode;

public class StatefulTypeWrapper {

	private final class ChildA {
		String s;

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChildA other = (ChildA) obj;
			// no can do, equals from parent becomes cyclic
			// if (!getOuterType().equals(other.getOuterType()))
			// return false;
			if (s == null) {
				if (other.s != null)
					return false;
			} else if (!s.equals(other.s))
				return false;
			return true;
		}

		@SuppressWarnings("unused")
		public StatefulTypeWrapper getOuterType() {
			return StatefulTypeWrapper.this;
		}

		public String getS() {
			return s;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			// result = prime * result + getOuterType().hashCode();
			result = prime * result + ((s == null) ? 0 : s.hashCode());
			return result;
		}

		public void setS(String s) {
			this.s = s;
		}

	}

	private class ChildB {

		int i;

		int j;

		@LeafNode
		BigInteger k;

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChildB other = (ChildB) obj;
			// if (!getOuterType().equals(other.getOuterType()))
			// return false;
			if (i != other.i)
				return false;
			if (j != other.j)
				return false;
			if (k == null) {
				if (other.k != null)
					return false;
			} else if (!k.equals(other.k))
				return false;
			return true;
		}

		public int getI() {
			return i;
		}

		public int getJ() {
			return j;
		}

		public BigInteger getK() {
			return k;
		}

		@SuppressWarnings("unused")
		public StatefulTypeWrapper getOuterType() {
			return StatefulTypeWrapper.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			// result = prime * result + getOuterType().hashCode();
			result = prime * result + i;
			result = prime * result + j;
			result = prime * result + ((k == null) ? 0 : k.hashCode());
			return result;
		}

		public void setI(int i) {
			this.i = i;
		}

		public void setJ(int j) {
			this.j = j;
		}

		public void setK(BigInteger k) {
			this.k = k;
		}

	}

	@LeafNode
	private class ChildC {
		int d;

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChildC other = (ChildC) obj;
			// if (!getOuterType().equals(other.getOuterType()))
			// return false;
			if (d != other.d)
				return false;
			return true;
		}

		public int getD() {
			return d;
		}

		@SuppressWarnings("unused")
		StatefulTypeWrapper getOuterType() {
			return StatefulTypeWrapper.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			// result = prime * result + getOuterType().hashCode();
			result = prime * result + d;
			return result;
		}

		public void setD(int d) {
			this.d = d;
		}
	}

	private class ChildD {

		private class ChildE {

			String id;

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				ChildE other = (ChildE) obj;
				// if (!getOuterType().equals(other.getOuterType()))
				// return false;
				if (id == null) {
					if (other.id != null)
						return false;
				} else if (!id.equals(other.id))
					return false;
				return true;
			}

			public String getId() {
				return id;
			}

			@SuppressWarnings("unused")
			private ChildD getOuterType() {
				return ChildD.this;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				// result = prime * result + getOuterType().hashCode();
				result = prime * result + ((id == null) ? 0 : id.hashCode());
				return result;
			}

			public void setId(String id) {
				this.id = id;
			}

		}

		private final ChildE e = new ChildE();

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChildD other = (ChildD) obj;
			// if (!getOuterType().equals(other.getOuterType()))
			// return false;
			if (e == null) {
				if (other.e != null)
					return false;
			} else if (!e.equals(other.e))
				return false;
			return true;
		}

		public String getId() {
			return e.getId();
		}

		private StatefulTypeWrapper getOuterType() {
			return StatefulTypeWrapper.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((e == null) ? 0 : e.hashCode());
			return result;
		}

		public void setId(String id) {
			e.setId(id);
		}

	}

	ChildA a = new ChildA();

	ChildB b = new ChildB();

	ChildC c = new ChildC();

	private final ChildD d = new ChildD();

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StatefulTypeWrapper other = (StatefulTypeWrapper) obj;
		if (a == null) {
			if (other.a != null)
				return false;
		} else if (!a.equals(other.a))
			return false;
		if (b == null) {
			if (other.b != null)
				return false;
		} else if (!b.equals(other.b))
			return false;
		if (c == null) {
			if (other.c != null)
				return false;
		} else if (!c.equals(other.c))
			return false;
		if (d == null) {
			if (other.d != null)
				return false;
		} else if (!d.equals(other.d))
			return false;
		return true;
	}

	public int getD() {
		return c.getD();
	}

	public int getI() {
		return b.getI();
	}

	public String getId() {
		return d.getId();
	}

	public int getJ() {
		return b.getJ();
	}

	public BigInteger getK() {
		return b.getK();
	}

	public String getS() {
		return a.getS();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		result = prime * result + ((c == null) ? 0 : c.hashCode());
		result = prime * result + ((d == null) ? 0 : d.hashCode());
		return result;
	}

	public void setD(int d) {
		c.setD(d);
	}

	public void setI(int i) {
		b.setI(i);
	}

	public void setId(String id) {
		d.setId(id);
	}

	public void setJ(int j) {
		b.setJ(j);
	}

	public void setK(BigInteger k) {
		b.setK(k);
	}

	public void setS(String s) {
		a.setS(s);
	}

}
