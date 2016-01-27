package com.mygear.junit;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mygear.junit.util.ClassForArray;
import com.mygear.tools.Unmarshaller;
import com.mygear.tools.Unmarshaller.PrimitiveType;

import p.C;

public class GeneralTest {
	private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private static final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private static PrintStream originalOutputStream = System.out;
	
	String[] nullArray=new String[]{null,null,null};
	Object nullObject = null;
	private int primitiveInt=1;
	transient float primiteFloat=132;
	volatile double primiteDouble=321.321;
	static String stringNullTest =null;
	protected Calendar calendarNullTest =null;
	ClassForArray classForArray=new ClassForArray();
	String b="FDS";
	Calendar myCalendar=Calendar.getInstance();
	Integer i=1;
	Unmarshaller.PrimitiveType pt=PrimitiveType.FLOAT;
	PrimitiveType ptd=PrimitiveType.DOUBLE;
	PrimitiveType pt2=PrimitiveType.BOOLEAN;
	String[] simpleStringArray={"XMLValidator"};
	int[] simpleIntArray={1,2};
	String[][][] threeDStringArray={{{"A","B"},{"Unmarshaller"}},{{"D"},{"E"}},{{"F"},{"G"}}};
	int[][][] threeDIntArray={{{1,2},{3}},{{4},{5}},{{6},{7}}};
	boolean boolTest=false;
	String stringTest="XMLValidator";
	String[] simpleStringArrayTest={"TEST1"};
	List<String> a=new ArrayList<String>(Arrays.asList(new String[]{"A"}));

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
	public void testSpecialCharString(){
		char[] c=new char[((Double)Math.pow(2, 16)).intValue()];
		for(int i=0; i<Math.pow(2, 16);i++){
			c[i]=(char)i;
			System.out.println(i+": "+(char)i);
		}
		C.m("!@#$%^&*()_+{}\"|:?><");
		assertTrue(outContent.toString().contains("!@#$%^&*()_+{}\"|:?><"));
	}
	
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
