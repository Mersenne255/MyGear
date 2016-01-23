package main;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
	private int DEPTH_THRESHOLD = 20;
	private char TAB_CHARACTER = ' ';
	private int TAB_CHARACTER_MULTIPLIER = 2;

	Set<Object> objects = new HashSet<Object>();

	public static void main(String[] args) {
		try {
			new Main().transform(new Integer(1));
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void transform(Object o) throws IllegalArgumentException, IllegalAccessException {
		if (o != null) {
			System.out.println(o.getClass().getName());
			transform(o, o.getClass(), 1);
		} else {
			System.out.println("null");
		}
	}

	void transform(Object o, Class c, int depth) throws IllegalArgumentException, IllegalAccessException {
		if (depth > DEPTH_THRESHOLD) {
			System.out.println(createIndent(depth) + ">>>>>>Depth limit reached<<<<<<");
			return;
		}
		Field[] fields = c.getDeclaredFields();
		List<Field> nonPrimitiveFields = new ArrayList<Field>();
		for (Field field : fields) {
			field.setAccessible(true);
			if (field.getType().isPrimitive()) {
				System.out.println(
						createIndent(depth) + Modifier.toString(field.getModifiers()) +" "+field.getType().getName() + " " + field.getName() + " " + field.get(o));
			} else {
				nonPrimitiveFields.add(field);
			}
		}
		for (Field field : nonPrimitiveFields) {
			Object innerObject = field.get(o);
			if (innerObject == null) {
				System.out.println(
						createIndent(depth) + Modifier.toString(field.getModifiers()) +" "+field.getType().getName() + " " + field.getName() + " " + "null");
			} else {
				String arraySizeText=innerObject.getClass().isArray()?" Array_size:"+Array.getLength(innerObject):"";
				System.out.print(createIndent(depth) + Modifier.toString(field.getModifiers()) +" "+field.getType().getName() + " " + field.getName() + " Object_ID:"
						+ System.identityHashCode(innerObject)+ arraySizeText);
				if (objects.contains(innerObject)) {
					System.out.println(" >>>>>>Object already in structure<<<<<<");
					return;
				} else {
					System.out.println();
					objects.add(innerObject);
					if (innerObject.getClass().isArray()) {
						if (innerObject.getClass().getComponentType().isPrimitive()) {
							System.out.print(createIndent(depth + 1));
							printPrimitiveArrayObject(innerObject);
						} else {
							for (int i = 0; i < Array.getLength(innerObject); i++) {
								Object arrayObject = Array.get(innerObject, i);
								transform(arrayObject, arrayObject.getClass(), depth + 1);
							}
						}
					} else {
						transform(innerObject, innerObject.getClass(), depth + 1);
					}
				}
			}
		}
		nonPrimitiveFields = null;
		fields = null;
	}

	private String createIndent(int width) {
		return new String(new char[width * (TAB_CHARACTER_MULTIPLIER >= 0 ? TAB_CHARACTER_MULTIPLIER : 1)])
				.replace('\0', TAB_CHARACTER);
	}

	private void printPrimitiveArrayObject(Object array) {
		if (array.getClass().getComponentType().getName().equals("boolean")) {
			printBooleanArray(array, PrimitiveTypes.BOOLEAN);
		}
		if (array.getClass().getComponentType().getName().equals("byte")) {
			printBooleanArray(array, PrimitiveTypes.BYTE);
		}
		if (array.getClass().getComponentType().getName().equals("char")) {
			printBooleanArray(array, PrimitiveTypes.CHAR);
		}
		if (array.getClass().getComponentType().getName().equals("short")) {
			printBooleanArray(array, PrimitiveTypes.SHORT);
		}
		if (array.getClass().getComponentType().getName().equals("int")) {
			printBooleanArray(array, PrimitiveTypes.INT);
		}
		if (array.getClass().getComponentType().getName().equals("long")) {
			printBooleanArray(array, PrimitiveTypes.LONG);
		}
		if (array.getClass().getComponentType().getName().equals("float")) {
			printBooleanArray(array, PrimitiveTypes.FLOAT);
		}
		if (array.getClass().getComponentType().getName().equals("double")) {
			printBooleanArray(array, PrimitiveTypes.DOUBLE);
		}
	}

	private void printBooleanArray(Object array, PrimitiveTypes type) {
		System.out.print("[");
		for (int i = 0; i < Array.getLength(array); i++) {
			switch (type) {
			case BOOLEAN:
				System.out.print(Array.getBoolean(array, i));
				break;
			case BYTE:
				System.out.print(Array.getByte(array, i));
				break;
			case CHAR:
				System.out.print(Array.getChar(array, i));
				break;
			case SHORT:
				System.out.print(Array.getShort(array, i));
				break;
			case INT:
				System.out.print(Array.getInt(array, i));
				break;
			case LONG:
				System.out.print(Array.getLong(array, i));
				break;
			case FLOAT:
				System.out.print(Array.getFloat(array, i));
				break;
			case DOUBLE:
				System.out.print(Array.getDouble(array, i));
				break;
			default:
				break;
			}
			if (i < Array.getLength(array) - 1) {
				System.out.print(", ");
			}
		}
		System.out.println("]");
	}

}
