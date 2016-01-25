package com.mygear.junit;

public class GeneralTest {
	
	
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
