package com.mygear.junit;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import p.C;

public class GeneralTest {
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private PrintStream originalOutputStream = System.out;

	@Before
	public void setUpStreams() {
	    System.setOut(new PrintStream(outContent));
	    System.setErr(new PrintStream(errContent));
	}

	@After
	public void cleanUpStreams() {
	    System.setOut(null);
	    System.setErr(null);
	}
	
	@Test
	public void testNullArray(){
		String[] nullArray={null,null,null,null,null};
		C.m(nullArray);
		assertTrue(outContent.toString().matches(".*null.*null.*null.*null.*null.*"));
	}
	
	class GenericClass<T>{
		private T t;
		public void print(T t){
			t.toString();
		}
		GenericClass(T o){
			this.t=o;
		}
	}
}
