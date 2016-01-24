package main;

public enum PrimitiveTypes {	
	BOOLEAN(Boolean.class),
	BYTE(Byte.class),
	CHAR(Character.class),
	SHORT(Short.class),
	INT(Integer.class),
	LONG(Long.class),
	FLOAT(Float.class),
	DOUBLE(Double.class);
	
	private final Class wrapper;
	PrimitiveTypes(Class wrapper){
		this.wrapper=wrapper;
	}
	public Class getWrapper(){
		return wrapper;
	}
}
