package com.mygear.junit.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import p.C;

public class QuickUnofficialTest {

	public static void main(String[] args) throws IOException, SecurityException, NoSuchFieldException {
//		int[] array=new int[5];
//		String[] array2=new String[5];
//		array2[4]="dsa";
//		C.m(new Date(), "FILE=D:\\Data\\test.xml");
//		
//		
//		Field field = A.class.getDeclaredField("l");
//		System.out.println(field.getType());
//		System.out.println(field.getGenericType());
		D d=new QuickUnofficialTest().new D();
		System.out.println("");
	}
	
	class A{
		int b =10;
		List<String> l;
	}
	
	interface B{
		int b = 11;
	}
	class D extends A implements B{
		
	}

}
