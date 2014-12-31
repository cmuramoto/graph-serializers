package com.nc.gs.tests.serializers.java.util.sorted;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.nc.gs.tests.AbstractRoundTripTests;

public class TestSortedSet extends AbstractRoundTripTests {

	static class Holder {

		Set<A> as;

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Holder other = (Holder) obj;
			if (as == null) {
				if (other.as != null)
					return false;
			} else if (!as.equals(other.as))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((as == null) ? 0 : as.hashCode());
			return result;
		}

		public void populate(final Iterable<A> vals, final Comparator<A> c) {
			as = new TreeSet<A>(c);

			for (final A a : vals) {
				as.add(a);
			}
		}

	}

	class ReverseAComparator implements Comparator<A> {

		public ReverseAComparator() {
		}

		@Override
		public int compare(final A left, final A right) {
			return right.compareTo(left);
		}

	}

	class ReverseStatefullComparator implements Comparator<A> {

		@Override
		public int compare(final A left, final A right) {

			return TestSortedSet.this.compare(left, right);
		}
	}

	static Comparator<A> staticRevComp = new Comparator<A>() {

		@Override
		public int compare(final A left, final A right) {
			return right.compareTo(left);
		}
	};

	Comparator<A> instanceReVComp = new ReverseAComparator();

	Comparator<A> anonInstanceRevComp = new Comparator<A>() {

		@Override
		public int compare(final A left, final A right) {
			return right.compareTo(left);
		}
	};

	Comparator<A> anonInstanceStatefulRevComp = new Comparator<A>() {

		@Override
		public int compare(final A left, final A right) {
			return TestSortedSet.this.compare(left, right);
		}
	};

	Comparator<A> instanceStatefullRevComp = new ReverseStatefullComparator();

	public int compare(final A left, final A right) {
		return right.compareTo(left);
	}

	private List<A> createData() {
		final ArrayList<A> list = new ArrayList<A>();

		for (int i = 0; i < 5; i++) {
			final A a = new A();
			a.name = String.valueOf(i);
			list.add(a);
		}

		return list;
	}

	@Test
	public void run() {

		final List<A> data = createData();

		final Holder h0 = new Holder();
		final Holder h1 = new Holder();
		final Holder h2 = new Holder();
		final Holder h3 = new Holder();
		final Holder h4 = new Holder();
		final Holder h5 = new Holder();

		h0.populate(data, null);
		h1.populate(data, staticRevComp);
		h2.populate(data, instanceReVComp);
		h3.populate(data, anonInstanceRevComp);
		h4.populate(data, anonInstanceStatefulRevComp);
		h5.populate(data, instanceStatefullRevComp);

		roundTrip(h0);
		roundTrip(h1);
		roundTrip(h2);
		roundTrip(h3);
		roundTrip(h4);
		roundTrip(h5);
	}
}
