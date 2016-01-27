package com.mygear.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Unmarshaller {

    private int DEPTH_THRESHOLD = 5;
    private char TAB_CHARACTER = ' ';
    private int TAB_CHARACTER_MULTIPLIER = 2;
    private boolean ONLY_LITERALS_VALUE = true;
    private boolean PRINT_MODIFIERS = true;
    private boolean PRINT_ARRAY_SIZE = true;
    private boolean PRINT_OBJECT_ID = true;
    private boolean PRINT_OBJECT_TYPE = true;
    private boolean PRINT_PRIVATE = true;
    private boolean PRINT_SYNTHETIC = true;
    private String DESTINATION_STRING=null;
    private PrintStream DESTINATION = null;

    Set<Object> objects = new HashSet<Object>();

    public void m(Object o, String args) {
        transform(o);
    }

    public void m(Object o) {
        transform(o);
    }

    // TODO refactoring
    public void unmarshall(Object o) {
        objects = new HashSet<Object>();
        Element element = new Element();
        element.setName("ParentObject");
        element.setProperty(PropType.OBJECT_ID, String.valueOf(System.identityHashCode(o)));
        element.setProperty(PropType.OBJECT_TYPE, o == null ? "null" : o.getClass().getName());
    }

    public void transform(Object o) {
        try {
            DESTINATION = DESTINATION_STRING == null?System.out:new PrintStream(new File(DESTINATION_STRING));
            objects = new HashSet<Object>();
            if (o != null) {
                DESTINATION.print("<Parent_object type=\"" + o.getClass().getName() + "\">");
                transform(o, o.getClass(), 1);
            } else {
                DESTINATION.print("null");
            }
            DESTINATION.print("</Parent_object>");
            DESTINATION.flush();
            DESTINATION.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    String adjustFieldNameForXML(String originalText) {
        return originalText.replace("[", "ArrayOf").replaceAll("\\s", " ").replaceAll("\\$", "").replaceAll("<", "").replaceAll(">", "");
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
        return fieldValue.replaceAll("<", "").replaceAll(">", "").replaceAll("\\s", " ").replaceAll("\\&", "").replaceAll("\\x00", "").replaceAll("\\x01", "");
    }

    private void transform(Object o, Class c, int depth) {
        DESTINATION.flush();
        if (c.isArray()) {
            try {
                if (c.getComponentType().isPrimitive()) {
                    for (int i = 0; i < Array.getLength(o); i++) {
                        Object arrayObject = Array.get(o, i);
                        DESTINATION.print("<" + c.getComponentType().getName() + ">" + adjustFieldValueForXML(arrayObject.toString()) + "</" + c.getComponentType().getName() + ">");
                    }
                } else if (isWrapperClass(c.getComponentType()) && ONLY_LITERALS_VALUE) {
                    for (int i = 0; i < Array.getLength(o); i++) {
                        Object arrayObject = Array.get(o, i);
                        String objectValue = arrayObject == null ? "null" : adjustFieldValueForXML(arrayObject.toString());
                        DESTINATION.print("<" + c.getComponentType().getName() + ">" + objectValue + "</" + c.getComponentType().getName() + ">");
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
                        DESTINATION.print("<" + adjustFieldNameForXML(field.getName()) + printFieldAttributes(field, field.get(o), fields.get(field)) + ">" + adjustFieldValueForXML(field.get(o).toString()) + "</" + adjustFieldNameForXML(field.getName()) + ">");
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
                    DESTINATION.print("<" + fieldName + printFieldAttributes(field, field.get(o), fields.get(field)) + ">");
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
                            DESTINATION.print("<!--Depth threshold reached. You can adjust using DEPTH_THRESHOLD argument-->");
                            DESTINATION.print("</" + fieldName + ">");
                            continue;
                        } else if (isEnumClass || isWrapperClass && ONLY_LITERALS_VALUE) {
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
        return new String(new char[width * (TAB_CHARACTER_MULTIPLIER >= 0 ? TAB_CHARACTER_MULTIPLIER : 1)]).replace('\0', TAB_CHARACTER);
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
        String arraySize = field.getType().getClass().isArray() && PRINT_ARRAY_SIZE ? " array_size=\"" + Array.getLength(fieldObject) + "\"" : "";
        String objectId = PRINT_OBJECT_ID ? " object_id=\"" + System.identityHashCode(fieldObject) + "\"" : "";
        String objectType = fieldObject == null || field.getType().isPrimitive() ? field.getType().getName() : fieldObject.getClass().getName().replace("[", "ArrayOf").replace(";", "");
        String objectTypeText = PRINT_OBJECT_TYPE ? " type=\"" + objectType + "\"" : "";
        String inheritedFromText = " inherited_from=\"" + inheritedFrom + "\"";
        return objectTypeText + modifiers + arraySize + objectId + inheritedFromText;
    }

    public enum PrimitiveType {
        BOOLEAN(Boolean.class),
        BYTE(Byte.class),
        CHAR(Character.class),
        SHORT(Short.class),
        INT(Integer.class),
        LONG(Long.class),
        FLOAT(Float.class),
        DOUBLE(Double.class);

        private final Class wrapper;

        PrimitiveType(Class wrapper) {
            this.wrapper = wrapper;
        }

        public Class getWrapper() {
            return wrapper;
        }
    }

    public enum PropType {
        MODIFIERS("modifiers"),
        ARRAY_SIZE("array_size"),
        OBJECT_ID("object_id"),
        OBJECT_TYPE("object_type"),
        INHERITED_FROM("inherited_from");

        private final String type;

        PropType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public class Element {

        private String name;
        private List<Element> children = new ArrayList<Element>();
        private Map<PropType, String> allProperties = new HashMap<PropType, String>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Element> getChildren() {
            return children;
        }

        public void setChildren(List<Element> children) {
            this.children = children;
        }

        public Map<PropType, String> getAllProperties() {
            return allProperties;
        }

        public void setProperties(HashMap<PropType, String> allProperties) {
            this.allProperties = allProperties;
        }

        public void setProperty(PropType key, String value) {
            this.allProperties.put(key, value);
        }

        public String getProperty(PropType key) {
            return this.allProperties.get(key);
        }
    }
}
