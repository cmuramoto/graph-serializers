package com.nc.gs.tests.generator.ic.accessible.cyclic;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

public class Parent extends Human {

	Child c;

	public void run() throws IOException {
		InputStream is = Parent.class.getClassLoader().getResourceAsStream("Poo.bib");

		ASMifier asm = new ASMifier();
		ClassReader cr = new ClassReader(is);

		cr.accept(new TraceClassVisitor(null, asm, new PrintWriter(System.out)), 0);
	}

}
