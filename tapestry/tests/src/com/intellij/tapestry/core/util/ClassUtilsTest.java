package com.intellij.tapestry.core.util;

import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.java.IJavaMethod;
import com.intellij.tapestry.core.mocks.JavaAnnotationMock;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import com.intellij.tapestry.core.mocks.JavaFieldMock;
import com.intellij.tapestry.core.mocks.JavaPrimitiveTypeMock;
import static org.easymock.EasyMock.*;
import org.testng.annotations.Test;

import java.util.Map;

public class ClassUtilsTest {

    @Test
    public void testConstructor() {
        new ClassUtils();
    }

    @Test
    public void getClassProperties_null() {
        assert ClassUtils.getClassProperties(null).size() == 0;
    }

    @Test
    public void getClassProperties_no_properties() {
        // a class with no methods
        JavaClassTypeMock noMethodsClassMock = new JavaClassTypeMock();

        assert ClassUtils.getClassProperties(noMethodsClassMock).size() == 0;

        // a class with no getter methods.
        IJavaMethod notGetterMethodsMock = createMock(IJavaMethod.class);
        expect(notGetterMethodsMock.getName()).andReturn("setProperty");
        expect(notGetterMethodsMock.getReturnType()).andReturn(null);

        IJavaMethod notGetterMethodsMock2 = createMock(IJavaMethod.class);
        expect(notGetterMethodsMock2.getName()).andReturn("getProperty2");
        expect(notGetterMethodsMock2.getReturnType()).andReturn(null);

        IJavaMethod notGetterMethodsMock3 = createMock(IJavaMethod.class);
        expect(notGetterMethodsMock3.getName()).andReturn("get");
        expect(notGetterMethodsMock3.getReturnType()).andReturn(new JavaPrimitiveTypeMock("char")).anyTimes();

        replay(notGetterMethodsMock, notGetterMethodsMock2, notGetterMethodsMock3);

        JavaClassTypeMock noGetterMethodsClassMock = new JavaClassTypeMock();
        noGetterMethodsClassMock.addPublicMethod(notGetterMethodsMock).addPublicMethod(notGetterMethodsMock2).addPublicMethod(notGetterMethodsMock3);

        assert ClassUtils.getClassProperties(noGetterMethodsClassMock).size() == 0;
    }

    @Test
    public void getClassProperties_with_properties() {
        IJavaMethod getterMethodMock = createMock(IJavaMethod.class);
        expect(getterMethodMock.getName()).andReturn("getProperty1").anyTimes();
        expect(getterMethodMock.getReturnType()).andReturn(new JavaPrimitiveTypeMock("boolean")).anyTimes();

        IJavaMethod getterMethod2Mock = createMock(IJavaMethod.class);
        expect(getterMethod2Mock.getName()).andReturn("getPropertyProp2").anyTimes();
        expect(getterMethod2Mock.getReturnType()).andReturn(new JavaPrimitiveTypeMock("boolean")).anyTimes();

        IJavaMethod isMethodMock = createMock(IJavaMethod.class);
        expect(isMethodMock.getName()).andReturn("isProperty2").anyTimes();
        expect(isMethodMock.getReturnType()).andReturn(new JavaPrimitiveTypeMock("boolean")).anyTimes();

        IJavaMethod isMethodNotBooleanMock = createMock(IJavaMethod.class);
        expect(isMethodNotBooleanMock.getName()).andReturn("isProperty3").anyTimes();
        expect(isMethodNotBooleanMock.getReturnType()).andReturn(new JavaPrimitiveTypeMock("short")).anyTimes();

        replay(getterMethodMock, getterMethod2Mock, isMethodMock, isMethodNotBooleanMock);

        JavaClassTypeMock getterMethodsClassMock = new JavaClassTypeMock();
        getterMethodsClassMock.addPublicMethod(getterMethodMock).addPublicMethod(isMethodMock).addPublicMethod(isMethodNotBooleanMock).addPublicMethod(getterMethod2Mock);

        Map<String, Object> properties = ClassUtils.getClassProperties(getterMethodsClassMock);

        assert properties.size() == 3;

        assert properties.get("propertyProp2") != null;
    }

    @Test
    public void getClassProperties_with_annotated_properties() {
        IJavaField annotatedField = new JavaFieldMock("_myProp", true).addAnnotation(new JavaAnnotationMock("org.apache.tapestry5.annotations.Property"));

        IJavaField notAnnotatedField = new JavaFieldMock("MyField", true);

        JavaClassTypeMock classMock = new JavaClassTypeMock();
        classMock.addField(annotatedField).addField(notAnnotatedField);

        Map<String, Object> properties = ClassUtils.getClassProperties(classMock);

        assert properties.size() == 1;

        assert properties.get("myProp") != null;
    }

    @Test
    public void getName() {
        String name = "_field1";

        assert ClassUtils.getName(name).equals("field1");

        name = "$field1";

        assert ClassUtils.getName(name).equals("field1");

        name = "field1";

        assert ClassUtils.getName(name).equals("field1");
    }
}
