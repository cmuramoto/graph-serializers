package com.nc.gs.tests.compress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Assert;
import org.junit.Test;

import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public class TestStringCompression {

	static void assertChars(String exp, String act) {
		if (!exp.equals(act)) {

			for (int i = 0; i < exp.length() && i < act.length(); i++) {
				if (exp.charAt(i) != act.charAt(i)) {

					System.out.printf("%d %d %d", i, (int) exp.charAt(i), (int) act.charAt(i));
					break;
				}
			}

			StringBuilder sb = new StringBuilder("char[] c = {");
			exp.chars().forEach(c -> sb.append(c).append(','));
			sb.replace(sb.length() - 1, sb.length(), "};");
			System.out.println(sb.toString());
		}
		Assert.assertEquals(exp, act);
	}

	private void test(List<String> list) {
		try (@SuppressWarnings("resource")
		Sink dst = new Sink().disposable()) {

			list.stream().forEach(s -> dst.writeUTF(s));

			try (Source src = dst.mirror()) {
				list.stream().forEach(s -> assertChars(s, src.readUTF()));
			}
		}
	}

	@Test
	public void testAscii() {
		List<String> ascii = Arrays.asList("ABCD", "AB", "ABC", "ABCD", "ABCDE", "ABCDF", "ABCDEFG", "ABCDEFGH", "ABCDEFGHI", "ABCDEFGHIJ");

		test(ascii);
	}

	@Test
	public void testMixed() {

		List<String> mixed = Arrays.asList("ABCDÉFG", "ABCDÉFGH", "Á", "ÁB", "ÁBC", "ÁBCD", "ABCDÉ", "ABCDF", "ABCDÉFG", "ABCDÉFGH", "ABCDEFGHI", "ABCDEFGHIJKLMNĨ", new String(new char[]{ 13059, 24630, 5636, 56902 }));

		test(mixed);
	}

	@Test
	public void testRandom() {

		List<String> random = new ArrayList<String>();

		ThreadLocalRandom r = ThreadLocalRandom.current();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10000; i++) {
			sb.setLength(0);

			int len = r.nextInt(300);

			for (int j = 0; j < len; j++) {
				sb.append((char) r.nextInt(Character.MAX_VALUE + 1));
			}

			random.add(sb.toString());
		}

		test(random);
	}

}
