package com.intellij.tapestry.core.util;

import com.intellij.tapestry.core.java.IJavaAnnotation;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.java.IJavaMethod;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for manipulating classes.
 */
public class ClassUtils {

    private static final String PROPERTY_ANNOTATION = "org.apache.tapestry.annotations.Property";

    /**
     * Finds every property declared in a class.
     *
     * @param javaClassType the class to look for properties in.
     * @return every property declared in a class and its super classes. The key of the map is the property name and the value the place in the code where that property is bound to.
     */
    public static Map<String, Object> getClassProperties(IJavaClassType javaClassType) {
        if (javaClassType == null) {
            return new HashMap<String, Object>();
        }

        Collection<IJavaMethod> allPublicMethods = javaClassType.getPublicMethods(true);
        Map<String, Object> getterMethods = new HashMap<String, Object>();

        for (IJavaMethod method : allPublicMethods) {

            String nameProperty = method.getName();

            if (method.getReturnType() != null && (nameProperty.startsWith("get") || (nameProperty.startsWith("is") && method.getReturnType().getName().equals("boolean")))) {

                if (nameProperty.startsWith("get")) {
                    nameProperty = (nameProperty.replaceFirst("get", ""));
                } else {
                    nameProperty = (nameProperty.replaceFirst("is", ""));
                }

                if (!nameProperty.equals("")) {
                    nameProperty = StringUtils.uncapitalize(nameProperty);

                    getterMethods.put(nameProperty, method);
                }
            }
        }

        Map<String, IJavaField> allFields = javaClassType.getFields(true);
        Map<String, Object> propertyFields = new HashMap<String, Object>();

        for (Map.Entry<String, IJavaField> field : allFields.entrySet()) {
            if (field.getValue().getAnnotations().containsKey(PROPERTY_ANNOTATION)) {
                IJavaAnnotation annotation = field.getValue().getAnnotations().get(PROPERTY_ANNOTATION);

                if (annotation.getParameters().containsKey("read") && annotation.getParameters().get("read")[0].equals("false"))
                    continue;

                propertyFields.put(getName(field.getKey()), field.getValue());
            }
        }

        getterMethods.putAll(propertyFields);

        return getterMethods;
    }

    /**
     * Computes the name of a field without any leading $ and _ characters.
     *
     * @param name the name to parse.
     * @return the correct name.
     */
    public static String getName(String name) {
        if (name.startsWith("$") || name.startsWith("_")) {
            return name.substring(1);
        }

        return name;
    }
}