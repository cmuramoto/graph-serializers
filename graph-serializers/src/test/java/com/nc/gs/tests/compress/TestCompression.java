package com.nc.gs.tests.compress;

import org.junit.Test;

import com.nc.gs.tests.AbstractRoundTripTests;
import com.nc.gs.tests.compress.TypesToCheck.Compressed;
import com.nc.gs.tests.compress.TypesToCheck.Default;
import com.nc.gs.tests.compress.TypesToCheck.IMap;
import com.nc.gs.tests.compress.TypesToCheck.ISeq;

public class TestCompression extends AbstractRoundTripTests {

	private static final String HUGE_STRING = "FOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOBBBBBBBBBBBBBBBBAAAAAAAAAAAAAARRRRRRRRRRRRRWHOOOOOOOOOOOO";

	private void checkPolyMap(IMap<Object, Object> cc, IMap<Object, Object> dc) {
		cc.put(1, 100);
		cc.put(1L, 100L);
		cc.put(HUGE_STRING, "Foo");

		dc.put(1, 100);
		dc.put(1L, 100L);
		dc.put(HUGE_STRING, "Foo");

		roundTripAndCompareLen(cc, dc);
	}

	@Test
	public void checkPolyMapK() {
		checkPolyMap(new Compressed.MultiMapK(), new Default.MultiMapK());
	}

	@Test
	public void checkPolyMapKV() {
		checkPolyMap(new Compressed.MultiMapKV(), new Default.MultiMapKV());
	}

	@Test
	public void checkPolyMapV() {
		checkPolyMap(new Compressed.MultiMapV(), new Default.MultiMapV());
	}

	@Test
	public void checkPolySequence() {

		checkPolySequence(new Compressed.MultiSeq(), new Default.MultiSeq());
	}

	private void checkPolySequence(ISeq<Object> cc, ISeq<Object> dc) {
		cc.add(1);
		cc.add(1L);
		cc.add(HUGE_STRING);
		dc.add(1);
		dc.add(1L);
		dc.add(HUGE_STRING);

		roundTripAndCompareLen(cc, dc);
	}

	@Test
	public void checkSequence() {

		checkSequence(new Compressed.Seq(), new Default.Seq());
	}

	private void checkSequence(ISeq<String> cc, ISeq<String> dc) {
		cc.add(HUGE_STRING);
		dc.add(HUGE_STRING);

		roundTripAndCompareLen(cc, dc);
	}

	@Test
	public void checkSequenceArray() {
		checkSequence(new Compressed.SequenceArray(), new Default.SequenceArray());
	}

	private void checkSequenceMap(IMap<Integer, String> cm, IMap<Integer, String> dm) {

		cm.put(1, HUGE_STRING);
		dm.put(1, HUGE_STRING);

		roundTripAndCompareLen(cm, dm);
	}

	@Test
	public void checkSequenceMapK() {
		checkSequenceMap(new Compressed.MapK(), new Default.MapK());
	}

	@Test
	public void checkSequenceMapKV() {
		checkSequenceMap(new Compressed.MapKV(), new Default.MapKV());
	}

	@Test
	public void checkSequenceMapV() {
		checkSequenceMap(new Compressed.MapV(), new Default.MapV());
	}

	@Test
	public void checkSequenceSet() {
		checkSequence(new Compressed.SequenceSet(), new Default.SequenceSet());
	}

	@Test
	public void checkSimple() {
		Compressed.Simple ct = new Compressed.Simple();
		Default.Simple dt = new Default.Simple();
		ct.v = HUGE_STRING;
		dt.v = HUGE_STRING;

		roundTripAndCompareLen(ct, dt);

	}

}