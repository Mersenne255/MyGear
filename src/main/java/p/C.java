package p;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	public static void m(Object o, String args) {
		transform(o);
	}

	public static void m(Object o) {
		transform(o);
	}

	public static void transform(Object o) {
		if (o != null) {
			DESTINATION.print("<Parent_object type=\"" + o.getClass().getName() + "\">");
			transform(o, o.getClass(), 1);
		} else {
			DESTINATION.print("null");
		}
		DESTINATION.print("</Parent_object>");
	}

	static String adjustTextForXML(String originalText) {
		return originalText.replace("[", "ArrayOf").replaceAll("\\W", "");
	}

	static void transform(Object o, Class c, int depth) {

		if (c.isArray()) {
			try {
				if (c.getComponentType().isPrimitive()) {
					for (int i = 0; i < Array.getLength(o); i++) {
						Object arrayObject = Array.get(o, i);
						DESTINATION.print("<" + c.getComponentType().getName() + ">" + arrayObject.toString() + "</"
								+ c.getComponentType().getName() + ">");
					}
				} else if (isWrapperClass(c.getComponentType()) && ONLY_LITERALS_VALUE) {
					for (int i = 0; i < Array.getLength(o); i++) {
						Object arrayObject = Array.get(o, i);
						String objectValue = arrayObject == null ? "null" : arrayObject.toString();
						DESTINATION.print("<" + c.getComponentType().getName() + ">" + objectValue + "</"
								+ c.getComponentType().getName() + ">");
					}
				} else {
					for (int i = 0; i < Array.getLength(o); i++) {
						Object arrayObject = Array.get(o, i);
						DESTINATION.print("<" + adjustTextForXML(c.getComponentType().getName()) + ">");
						if (arrayObject == null) {
							DESTINATION.print("null");
						} else {
							transform(arrayObject, arrayObject.getClass(), depth + 1);
						}
						DESTINATION.print("</" + adjustTextForXML(c.getComponentType().getName()) + ">");
					}
				}
			} catch (Exception e) {
				DESTINATION.print("<!--Array Exception-->");
				e.printStackTrace();
			}
		} else {
			Field[] fields = c.getDeclaredFields();
			List<Field> nonPrimitiveFields = new ArrayList<Field>();
			for (Field field : fields) {
				try {
					if (!PRINT_PRIVATE && !field.isAccessible()) {
						continue;
					}
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					if (field.getType().isPrimitive()) {
						DESTINATION.print(
								"<" + adjustTextForXML(field.getName()) + printFieldAttributes(field, field.get(o))
										+ ">" + field.get(o) + "</" + adjustTextForXML(field.getName()) + ">");
					} else {
						nonPrimitiveFields.add(field);
					}
				} catch (Exception e) {
					DESTINATION.print("<!--Primitive Field Exception-->");
					e.printStackTrace();
				}
			}
			for (Field field : nonPrimitiveFields) {
				try {
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
							DESTINATION.print(
									"<!--Depth threshold reached. You can adjust using DEPTH_THRESHOLD argument-->");
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
				} catch (Exception e) {
					DESTINATION.print("<!-- Non-Primitive Field Exception-->");
					e.printStackTrace();
				}
			}
			nonPrimitiveFields = null;
			fields = null;
		}
		DESTINATION.flush();
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

	public enum PrimitiveTypes {
		BOOLEAN(Boolean.class), BYTE(Byte.class), CHAR(Character.class), SHORT(Short.class), INT(Integer.class), LONG(
				Long.class), FLOAT(Float.class), DOUBLE(Double.class);

		private final Class wrapper;

		PrimitiveTypes(Class wrapper) {
			this.wrapper = wrapper;
		}

		public Class getWrapper() {
			return wrapper;
		}
	}
}
