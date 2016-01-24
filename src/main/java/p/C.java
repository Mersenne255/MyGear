package p;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.PrimitiveTypes;
import main.Test;

public class C {
	private static int DEPTH_THRESHOLD = 50;
	private static char TAB_CHARACTER = ' ';
	private static int TAB_CHARACTER_MULTIPLIER = 2;
	private static boolean ONLY_LITERALS_VALUE = true;
	private static boolean PRINT_MODIFIERS = true;
	private static boolean PRINT_ARRAY_SIZE = true;
	private static boolean PRINT_OBJECT_ID = true;
	private static boolean PRINT_OBJECT_TYPE = true;
	private static boolean PRINT_PRIVATE = true;
	private static PrintStream DESTINATION = System.out;

	static Set<Object> objects = new HashSet<Object>();

	public static void main(String[] args) {
		try {
			new C().m(new Test());
			// new Main().transform(Calendar.getInstance());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void m(Object o) throws IllegalArgumentException, IllegalAccessException {
		if (o != null) {
			DESTINATION.print("<Parent_object type=\"" + o.getClass().getName() + "\">");
			transform(o, o.getClass(), 1);			
		} else {
			DESTINATION.print("null");
		}
		DESTINATION.print("</Parent_object>");
	}

	static String adjustTextForXML(String originalText) {
		return originalText.replace("[", "ArrayOf").replace(";", "").replace("$", "");
	}

	static void transform(Object o, Class c, int depth) throws IllegalArgumentException, IllegalAccessException {

		if (c.isArray()) {
			if (c.getComponentType().isPrimitive()) {
				for (int i = 0; i < Array.getLength(o); i++) {
					Object arrayObject = Array.get(o, i);
					DESTINATION.print("<" + c.getComponentType().getName() + ">" + arrayObject.toString() + "</"
							+ c.getComponentType().getName() + ">");
				}
			} else if (isWrapperClass(c.getComponentType()) && ONLY_LITERALS_VALUE) {
				for (int i = 0; i < Array.getLength(o); i++) {
					Object arrayObject = Array.get(o, i);
					DESTINATION.print("<" + c.getComponentType().getName() + ">" + arrayObject.toString() + "</"
							+ c.getComponentType().getName() + ">");
				}
			} else {
				for (int i = 0; i < Array.getLength(o); i++) {
					Object arrayObject = Array.get(o, i);
					DESTINATION.print("<" + adjustTextForXML(c.getComponentType().getName()) + ">");
					transform(arrayObject, arrayObject.getClass(), depth + 1);
					DESTINATION.print("</" + adjustTextForXML(c.getComponentType().getName()) + ">");
				}
			}
		} else {
			Field[] fields = c.getDeclaredFields();
			List<Field> nonPrimitiveFields = new ArrayList<Field>();
			for (Field field : fields) {
				if (!PRINT_PRIVATE && !field.isAccessible()) {
					continue;
				}
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				if (field.getType().isPrimitive()) {
					DESTINATION
							.print("<" + adjustTextForXML(field.getName()) + printFieldAttributes(field, field.get(o))
									+ ">" + field.get(o) + "</" + adjustTextForXML(field.getName()) + ">");
				} else {
					nonPrimitiveFields.add(field);
				}
			}
			for (Field field : nonPrimitiveFields) {
				String fieldName = adjustTextForXML(field.getName());
				DESTINATION.print("<" + fieldName + printFieldAttributes(field, field.get(o)) + ">");
				Object innerObject = field.get(o);
				if (innerObject == null) {
					DESTINATION.print("null");
					DESTINATION.print("</" + fieldName + ">");
				} else {
					boolean isWrapperClass = isWrapperClass(innerObject.getClass());
					boolean isEnumClass = innerObject.getClass().isEnum();
					if (objects.contains(innerObject)) {
						DESTINATION.print("<!--Object already in structure-->");
						DESTINATION.print("</" + fieldName + ">");
						continue;
					} else if (depth > DEPTH_THRESHOLD) {
						DESTINATION
								.print("<!--Depth threshold reached. You can adjust using DEPTH_THRESHOLD argument-->");
						DESTINATION.print("</" + fieldName + ">");
						continue;
					} else if (isEnumClass || isWrapperClass && ONLY_LITERALS_VALUE) {
						DESTINATION.print(innerObject.toString());
						DESTINATION.print("</" + fieldName + ">");
						continue;
					} else {
						objects.add(innerObject);
						transform(innerObject, innerObject.getClass(), depth + 1);
						DESTINATION.print("</" + fieldName + ">");
					}
				}
			}
			nonPrimitiveFields = null;
			fields = null;
		}
	}

	private String createIndent(int width) {
		return new String(new char[width * (TAB_CHARACTER_MULTIPLIER >= 0 ? TAB_CHARACTER_MULTIPLIER : 1)])
				.replace('\0', TAB_CHARACTER);
	}

	private static boolean isWrapperClass(Class wrapper) {
		if (wrapper.equals(PrimitiveTypes.BOOLEAN.getWrapper())) {
			return true;
		}
		if (wrapper.equals(PrimitiveTypes.BYTE.getWrapper())) {
			return true;
		}
		if (wrapper.equals(PrimitiveTypes.CHAR.getWrapper())) {
			return true;
		}
		if (wrapper.equals(PrimitiveTypes.SHORT.getWrapper())) {
			return true;
		}
		if (wrapper.equals(PrimitiveTypes.INT.getWrapper())) {
			return true;
		}
		if (wrapper.equals(PrimitiveTypes.LONG.getWrapper())) {
			return true;
		}
		if (wrapper.equals(PrimitiveTypes.FLOAT.getWrapper())) {
			return true;
		}
		if (wrapper.equals(PrimitiveTypes.DOUBLE.getWrapper())) {
			return true;
		}
		if (wrapper.equals(String.class)) {
			return true;
		}
		return false;
	}

	private static String printFieldAttributes(Field field, Object fieldObject) {
		String modifiers = PRINT_MODIFIERS ? " modifiers=\"" + Modifier.toString(field.getModifiers()) + "\"" : "";
		String arraySize = field.getType().getClass().isArray() && PRINT_ARRAY_SIZE
				? " array_size=\"" + Array.getLength(fieldObject) + "\"" : "";
		String objectId = PRINT_OBJECT_ID ? " object_id=\"" + System.identityHashCode(fieldObject) + "\"" : "";
		String objectType = fieldObject == null || field.getType().isPrimitive() ? field.getType().getName()
				: fieldObject.getClass().getName().replace("[", "ArrayOf").replace(";", "");
		String objectTypeText = PRINT_OBJECT_TYPE ? " type=\"" + objectType + "\"" : "";
		return objectTypeText + modifiers + arraySize + objectId;
	}

	private void printPrimitiveArrayObject(Object array) {
		if (array.getClass().getComponentType().getName().equals("boolean")) {
			printPrimitiveArrayObject(array, PrimitiveTypes.BOOLEAN);
		}
		if (array.getClass().getComponentType().getName().equals("byte")) {
			printPrimitiveArrayObject(array, PrimitiveTypes.BYTE);
		}
		if (array.getClass().getComponentType().getName().equals("char")) {
			printPrimitiveArrayObject(array, PrimitiveTypes.CHAR);
		}
		if (array.getClass().getComponentType().getName().equals("short")) {
			printPrimitiveArrayObject(array, PrimitiveTypes.SHORT);
		}
		if (array.getClass().getComponentType().getName().equals("int")) {
			printPrimitiveArrayObject(array, PrimitiveTypes.INT);
		}
		if (array.getClass().getComponentType().getName().equals("long")) {
			printPrimitiveArrayObject(array, PrimitiveTypes.LONG);
		}
		if (array.getClass().getComponentType().getName().equals("float")) {
			printPrimitiveArrayObject(array, PrimitiveTypes.FLOAT);
		}
		if (array.getClass().getComponentType().getName().equals("double")) {
			printPrimitiveArrayObject(array, PrimitiveTypes.DOUBLE);
		}
	}

	private void printPrimitiveArrayObject(Object array, PrimitiveTypes type) {
		for (int i = 0; i < Array.getLength(array); i++) {
			switch (type) {
			case BOOLEAN:
				DESTINATION.print("<boolean>" + Array.getBoolean(array, i) + "</boolean>");
				break;
			case BYTE:
				DESTINATION.print("<byte>" + Array.getByte(array, i) + "</byte>");
				break;
			case CHAR:
				DESTINATION.print("<char>" + Array.getChar(array, i) + "</char>");
				break;
			case SHORT:
				DESTINATION.print("<short>" + Array.getShort(array, i) + "</short>");
				break;
			case INT:
				DESTINATION.print("<int>" + Array.getInt(array, i) + "</int>");
				break;
			case LONG:
				DESTINATION.print("<long>" + Array.getLong(array, i) + "</long>");
				break;
			case FLOAT:
				DESTINATION.print("<float>" + Array.getFloat(array, i) + "</float>");
				break;
			case DOUBLE:
				DESTINATION.print("<double>" + Array.getDouble(array, i) + "</double>");
				break;
			default:
				break;
			}

		}

	}
}
