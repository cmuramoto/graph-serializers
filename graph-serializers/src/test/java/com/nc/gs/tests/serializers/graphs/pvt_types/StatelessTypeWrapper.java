package com.nc.gs.tests.serializers.graphs.pvt_types;

public class StatelessTypeWrapper {

	private static final class ChildA {
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
			if (s == null) {
				if (other.s != null)
					return false;
			} else if (!s.equals(other.s))
				return false;
			return true;
		}

		public String getS() {
			return s;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((s == null) ? 0 : s.hashCode());
			return result;
		}

		public void setS(String s) {
			this.s = s;
		}

	}

	private static class ChildB {

		int i;

		int j;

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChildB other = (ChildB) obj;
			if (i != other.i)
				return false;
			if (j != other.j)
				return false;
			return true;
		}

		public int getI() {
			return i;
		}

		public int getJ() {
			return j;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + i;
			result = prime * result + j;
			return result;
		}

		public void setI(int i) {
			this.i = i;
		}

		public void setJ(int j) {
			this.j = j;
		}

	}

	ChildA a = new ChildA();

	ChildB b = new ChildB();

	@SuppressWarnings("unused")
	private Object anon = new Object() {
	};
	
	private static Object sanon = new Object() {
	};
	
	Object o =sanon;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StatelessTypeWrapper other = (StatelessTypeWrapper) obj;
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
		return true;
	}

	public int getI() {
		return b.getI();
	}

	public int getJ() {
		return b.getJ();
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
		return result;
	}

	public void setI(int i) {
		b.setI(i);
	}

	public void setJ(int j) {
		b.setJ(j);
	}

	public void setS(String s) {
		a.setS(s);
	}
}
