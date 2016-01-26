package com.mygear.junit;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import p.C;

public class GeneralTest {
	private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private static final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private PrintStream originalOutputStream = System.out;

	@Before
	public void setUpStreams() {
		outContent.reset();
		errContent.reset();
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
	
	interface TestInterface{
		Integer interfaceInteger=1;
	}
	
	@Test
	public void testSuperclassFields(){
		class A{
			String superSuperString="Aclass";
			String dispalyThisString="should not show this";
		}
		class B extends A{
			String superString="B extends A class";
		}		
		class D extends B implements TestInterface{
			String dispalyThisString="only this should be displayed";
		}
		D d=new D();
		C.m(d);
		String outPut=outContent.toString();
		assertTrue(outPut.contains("only this should be displayed"));
		assertTrue(outPut.contains("B extends A class"));
		assertTrue(outPut.contains("Aclass"));
		assertFalse(outPut.contains("should not show this"));
	}
	
//	@Test
//	public void testSpecialCharString(){
//		C.m("!@#$%^&*()_+{}\"|:?><");
//		assertTrue(outContent.toString().contains("!@#$%^&*()_+{}\"|:?><"));
//	}
	
	//TODO field name with $
	
	//TODO field value with < > /
	
	//TODO String as parameter
	
	
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
