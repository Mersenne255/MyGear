package com.mygear.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Unmarshaller {
	private int DEPTH_THRESHOLD = 5;
	private String TAB_STRING = "\t";
	private boolean ONLY_WRAPPER_VALUE = true;
	private boolean PRINT_MODIFIERS = true;
	private boolean PRINT_ARRAY_SIZE = true;
	private boolean PRINT_OBJECT_ID = true;
	private boolean PRINT_OBJECT_TYPE = true;
	private boolean PRINT_PRIVATE = true;
	private boolean PRINT_SYNTHETIC = false;
	private String EXPORT_FILE_PATH = null;
	private PrintStream DESTINATION = null;

	Set<Object> objects = new HashSet<Object>();

	public void m(Object o, String args) {
		transform(o);
	}

	public void m(Object o) {
		transform(o);
	}
	
	public static void main(String[] args){
		new Unmarshaller().createStructure(new Date());
	}

	// TODO refactoring
	public void createStructure(Object o) {
		objects = new HashSet<Object>();
		Element element = new Element();
		element.setObject(o);
		if (o == null) {
			element.setValue("null");
		} else {
			element.setObjectClass(o.getClass());
		}
		createStructure(element, 0);
		System.out.println("");
	}

	private Element createStructure(Element element, int depth) {
		Object parentObject=element.getObject();
		Class parentClass=element.getObjectClass();
		if (parentObject == null) {
			element.value = "null";
			return element;
		}
		if (parentClass.isArray()) {
			try {
				for (int i = 0; i < Array.getLength(parentObject); i++) {
					Element arrayElement = new Element();
					Object arrayObject = Array.get(parentObject, i);
					arrayElement.setObject(arrayObject);
					arrayElement.setObjectClass(parentClass.getComponentType());
					if (parentClass.getComponentType().isPrimitive()
							|| isWrapperClass(parentClass.getComponentType()) && ONLY_WRAPPER_VALUE) {
						String objectValue = arrayObject == null ? "null" : arrayObject.toString();
						arrayElement.setValue(objectValue);
					} else {
						element.getChildren().add(createStructure(arrayElement, depth + 1));
					}
				}
			} catch (Exception e1) {
				element.setPrematureEndText("Exception in array field");
				e1.printStackTrace();
			}
		} else {
			Map<Field, String> fieldsInheritenceMap = findAllClassFields(parentClass);
			for (Field field : fieldsInheritenceMap.keySet()) {
				try {
					if (!PRINT_PRIVATE && !field.isAccessible()) {
						continue;
					}
					if (!PRINT_SYNTHETIC && field.isSynthetic()) {
						continue;
					}
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					Object innerObject = field.get(parentObject);
					Element fieldElement = new Element();
					fieldElement.setField(field);
					fieldElement.setObjectClass(field.getType());
					fieldElement.setObject(innerObject);
					fieldElement.setInheritedFrom(fieldsInheritenceMap.get(field));
					
					boolean isWrapperClass = isWrapperClass(field.getType());
					boolean isEnumClass = field.getType().isEnum();
					if (objects.contains(innerObject)) {
						fieldElement.setPrematureEndText("Object already in structure");
						continue;
					} else if (depth > DEPTH_THRESHOLD) {
						fieldElement.setPrematureEndText("Depth threshold reached. You can adjust using DEPTH_THRESHOLD argument");
						continue;
					} else if (isEnumClass || isWrapperClass && ONLY_WRAPPER_VALUE) {
						fieldElement.setValue(innerObject.toString());
						continue;
					} else {
						element.getChildren().add(createStructure(fieldElement, depth + 1));
					}
					
				} catch (Exception e) {
					element.setPrematureEndText("Field Exception");
					e.printStackTrace();
				}
			}
			fieldsInheritenceMap = null;
		}
		return element;
	}

	public void transform(Object o) {
		try {
			if (EXPORT_FILE_PATH == null) {
				DESTINATION = System.out;
			} else {
				File outputFile = new File(EXPORT_FILE_PATH);
				outputFile.getParentFile().mkdirs();
				outputFile.createNewFile();
				DESTINATION = new PrintStream(outputFile);
			}
			objects = new HashSet<Object>();
			if (o != null) {
				DESTINATION.print("<Parent_object type=\"" + o.getClass().getName() + "\">");
				transform(o, o.getClass(), 1);
			} else {
				DESTINATION.print("null");
			}
			DESTINATION.println("</Parent_object>");
			DESTINATION.flush();
			if (DESTINATION != System.out) {
				DESTINATION.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	String adjustFieldNameForXML(String originalText) {
		return originalText.replace("[", "ArrayOf").replaceAll("\\s", " ").replaceAll("\\$", "").replaceAll("<", "")
				.replaceAll(">", "");
	}

	Map<Field, String> findAllClassFields(Class c) {
		return findAllClassFields(c, new HashSet<String>());
	}

	private Map<Field, String> findAllClassFields(Class c, Set<String> fieldNames) {
		if (c == null) {
			return new HashMap<Field, String>();
		}
		Map<Field, String> fieldList = new HashMap<Field, String>();
		for (Field field : c.getDeclaredFields()) {
			if (!fieldNames.contains(field.getName())) {
				fieldNames.add(field.getName());
				fieldList.put(field, c.getName());
			}
		}
		fieldList.putAll(findAllClassFields(c.getSuperclass(), fieldNames));
		for (Class implementedInterface : c.getInterfaces()) {
			fieldList.putAll(findAllClassFields(implementedInterface, fieldNames));
		}
		fieldNames = null;
		return fieldList;
	}

	// TODO handle special characters
	String adjustFieldValueForXML(String fieldValue) {
		return fieldValue.replaceAll("<", "").replaceAll(">", "").replaceAll("\\s", " ").replaceAll("\\&", "")
				.replaceAll("\\x00", "").replaceAll("\\x01", "");
	}

	private void transform(Object o, Class c, int depth) {
		DESTINATION.flush();
		if (c.isArray()) {
			try {
				if (c.getComponentType().isPrimitive() || isWrapperClass(c.getComponentType()) && ONLY_WRAPPER_VALUE) {
					for (int i = 0; i < Array.getLength(o); i++) {
						Object arrayObject = Array.get(o, i);
						String objectValue = arrayObject == null ? "null"
								: adjustFieldValueForXML(arrayObject.toString());
						DESTINATION.print("<" + c.getComponentType().getName() + ">" + objectValue + "</"
								+ c.getComponentType().getName() + ">");
					}
				} else {
					for (int i = 0; i < Array.getLength(o); i++) {
						Object arrayObject = Array.get(o, i);
						DESTINATION.print("<" + adjustFieldNameForXML(c.getComponentType().getName()) + ">");
						if (arrayObject == null) {
							DESTINATION.print("null");
						} else {
							transform(arrayObject, arrayObject.getClass(), depth + 1);
						}
						DESTINATION.print("</" + adjustFieldNameForXML(c.getComponentType().getName()) + ">");
					}
				}
			} catch (Exception e) {
				DESTINATION.print("<!--Array Exception-->");
				e.printStackTrace();
			}
		} else {
			Map<Field, String> fields = findAllClassFields(c);
			List<Field> nonPrimitiveFields = new ArrayList<Field>();
			for (Field field : fields.keySet()) {
				try {
					if (!PRINT_PRIVATE && !field.isAccessible()) {
						continue;
					}
					if (!PRINT_SYNTHETIC && field.isSynthetic()) {
						continue;
					}
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					if (field.getType().isPrimitive()) {
						DESTINATION.print("<" + adjustFieldNameForXML(field.getName())
								+ printFieldAttributes(field, field.get(o), fields.get(field)) + ">"
								+ adjustFieldValueForXML(field.get(o).toString()) + "</"
								+ adjustFieldNameForXML(field.getName()) + ">");
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
					String fieldName = adjustFieldNameForXML(field.getName());
					DESTINATION.print(
							"<" + fieldName + printFieldAttributes(field, field.get(o), fields.get(field)) + ">");
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
						} else if (isEnumClass || isWrapperClass && ONLY_WRAPPER_VALUE) {
							DESTINATION.print(adjustFieldValueForXML(innerObject.toString()));
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
		return new String(new char[width]).replaceAll("\0", TAB_STRING);
	}

	private boolean isWrapperClass(Class wrapper) {
		if (wrapper.equals(PrimitiveType.BOOLEAN.getWrapper())) {
			return true;
		}
		if (wrapper.equals(PrimitiveType.BYTE.getWrapper())) {
			return true;
		}
		if (wrapper.equals(PrimitiveType.CHAR.getWrapper())) {
			return true;
		}
		if (wrapper.equals(PrimitiveType.SHORT.getWrapper())) {
			return true;
		}
		if (wrapper.equals(PrimitiveType.INT.getWrapper())) {
			return true;
		}
		if (wrapper.equals(PrimitiveType.LONG.getWrapper())) {
			return true;
		}
		if (wrapper.equals(PrimitiveType.FLOAT.getWrapper())) {
			return true;
		}
		if (wrapper.equals(PrimitiveType.DOUBLE.getWrapper())) {
			return true;
		}
		if (wrapper.equals(String.class)) {
			return true;
		}
		return false;
	}

	private String printFieldAttributes(Field field, Object fieldObject, String inheritedFrom) {
		String modifiers = PRINT_MODIFIERS ? " modifiers=\"" + Modifier.toString(field.getModifiers()) + "\"" : "";
		String arraySize = field.getType().getClass().isArray() && PRINT_ARRAY_SIZE
				? " array_size=\"" + Array.getLength(fieldObject) + "\"" : "";
		String objectId = PRINT_OBJECT_ID ? " object_id=\"" + System.identityHashCode(fieldObject) + "\"" : "";
		String objectType = fieldObject == null || field.getType().isPrimitive() ? field.getType().getName()
				: fieldObject.getClass().getName().replace("[", "ArrayOf").replace(";", "");
		String objectTypeText = PRINT_OBJECT_TYPE ? " type=\"" + objectType + "\"" : "";
		String inheritedFromText = " inherited_from=\"" + inheritedFrom + "\"";
		return objectTypeText + modifiers + arraySize + objectId + inheritedFromText;
	}

	public enum PrimitiveType {
		BOOLEAN(Boolean.class), BYTE(Byte.class), CHAR(Character.class), SHORT(Short.class), INT(Integer.class), LONG(
				Long.class), FLOAT(Float.class), DOUBLE(Double.class);

		private final Class wrapper;

		PrimitiveType(Class wrapper) {
			this.wrapper = wrapper;
		}

		public Class getWrapper() {
			return wrapper;
		}
	}

	public enum PropType {
		MODIFIERS("modifiers"), ARRAY_SIZE("array_size"), OBJECT_ID("object_id"), OBJECT_TYPE(
				"object_type"), INHERITED_FROM("inherited_from");

		private final String type;

		PropType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}
	}

	public class Element {

		private List<Element> children = new ArrayList<Element>();
		private String value;
		private Object object;
		private Class objectClass;
		private Field field;
		private String inheritedFrom;
		private String prematureEndText = null;

		public List<Element> getChildren() {
			return children;
		}

		public void setChildren(List<Element> children) {
			this.children = children;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public Object getObject() {
			return object;
		}

		public void setObject(Object object) {
			this.object = object;
		}

		public Class getObjectClass() {
			return objectClass;
		}

		public void setObjectClass(Class objectClass) {
			this.objectClass = objectClass;
		}

		public Field getField() {
			return field;
		}

		public void setField(Field field) {
			this.field = field;
		}

		public String getPrematureEndText() {
			return prematureEndText;
		}

		public void setPrematureEndText(String prematureEndText) {
			this.prematureEndText = prematureEndText;
		}

		public String getInheritedFrom() {
			return inheritedFrom;
		}

		public void setInheritedFrom(String inheritedFrom) {
			this.inheritedFrom = inheritedFrom;
		}

	}

	// Builder methods
	public Unmarshaller setDepthTreshold(int treshold) {
		this.DEPTH_THRESHOLD = treshold;
		return this;
	}

	public Unmarshaller setTabCharacter(String tabString) {
		this.TAB_STRING = tabString;
		return this;
	}

	public Unmarshaller setOnlyWrapperValue(boolean onlyValue) {
		this.ONLY_WRAPPER_VALUE = onlyValue;
		return this;
	}

	public Unmarshaller setPrintModifiers(boolean printModifiers) {
		this.PRINT_MODIFIERS = printModifiers;
		return this;
	}

	public Unmarshaller setPrintArraySize(boolean printArraySize) {
		this.PRINT_ARRAY_SIZE = printArraySize;
		return this;
	}

	public Unmarshaller setPrintObjectId(boolean printObjectId) {
		this.PRINT_OBJECT_ID = printObjectId;
		return this;
	}

	public Unmarshaller setPrintObjectType(boolean printObjectType) {
		this.PRINT_OBJECT_TYPE = printObjectType;
		return this;
	}

	public Unmarshaller setPrintSynthetic(boolean printSynthetic) {
		this.PRINT_SYNTHETIC = printSynthetic;
		return this;
	}

	public Unmarshaller setPrintPrivate(boolean printPrivate) {
		this.PRINT_PRIVATE = printPrivate;
		return this;
	}

	public Unmarshaller setExportFilePath(String exportFilePath) {
		this.EXPORT_FILE_PATH = exportFilePath;
		return this;
	}
}
