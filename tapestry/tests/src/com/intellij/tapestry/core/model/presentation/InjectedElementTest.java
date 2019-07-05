package com.intellij.tapestry.core.model.presentation;

import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.mocks.*;
import com.intellij.tapestry.core.model.TapestryLibrary;
import com.intellij.tapestry.core.resource.xml.XmlTag;
import org.testng.annotations.Test;

public class InjectedElementTest {

    @Test
    public void constructor_with_field() {
        JavaFieldMock fieldMock = null;
        TapestryComponent componentMock = null;

        InjectedElement injectedElement = new InjectedElement(fieldMock, componentMock);

        assert injectedElement.getElement() == null;

        assert injectedElement.getField() == null;
    }

    @Test
    public void constructor_with_tag() {
        XmlTag tag = null;
        TapestryComponent componentMock = null;

        InjectedElement injectedElement = new InjectedElement(tag, componentMock);

        assert injectedElement.getElement() == null;

        assert injectedElement.getTag() == null;
    }

    @Test
    public void getElementId_component_without_id_and_tag_null() {
        JavaFieldMock fieldMock = new JavaFieldMock("field1", true).addAnnotation(new JavaAnnotationMock(TapestryConstants.COMPONENT_ANNOTATION));

        TapestryComponent componentMock = org.easymock.EasyMock.createMock(TapestryComponent.class);
        InjectedElement injectedElement = new InjectedElement(fieldMock, componentMock);

        assert injectedElement.getElementId().equals("field1");
    }

    @Test
    public void getElementId_component_without_id_and_field_null() {
        XmlTagMock tagMock = new XmlTagMock("tag1");

        JavaClassTypeMock componentClassMock = new JavaClassTypeMock("com.app.components.SomeComponent").setPublic(true).setDefaultConstructor(true);
        TapestryProject tapestryProjectMock = org.easymock.EasyMock.createMock(TapestryProject.class);
        TapestryLibrary libraryMock = new TapestryLibrary("id", "com.app", tapestryProjectMock);

        TapestryComponent componentMock = new TapestryComponent(libraryMock, componentClassMock, tapestryProjectMock);

        InjectedElement injectedElement = new InjectedElement(tagMock, componentMock);

        assert injectedElement.getElementId().equals("SomeComponent");

        XmlTagMock tagMock2 = new XmlTagMock("someComponent");

        InjectedElement injectedElement2 = new InjectedElement(tagMock2, componentMock);

        assert injectedElement2.getElementId().equals("someComponent");
    }

    @Test
    public void getElementId_component_with_id_and_field_null() {
        XmlTagMock tagMock = new XmlTagMock("tag1").addAttribute(new XmlAttributeMock("id", "tag2"));

        TapestryComponent componentMock = org.easymock.EasyMock.createMock(TapestryComponent.class);
        InjectedElement injectedElement = new InjectedElement(tagMock, componentMock);

        assert injectedElement.getElementId().equals("tag2");
    }

    @Test
    public void getElementId_component_with_id_and_tag_null() {
        JavaFieldMock fieldMock = new JavaFieldMock("field1", true).addAnnotation(new JavaAnnotationMock(TapestryConstants.COMPONENT_ANNOTATION).addParameter("id", new String[]{"field2"}));

        TapestryComponent componentMock = org.easymock.EasyMock.createMock(TapestryComponent.class);
        InjectedElement injectedElement = new InjectedElement(fieldMock, componentMock);

        assert injectedElement.getElementId().equals("field2");
    }

    @Test
    public void getElementId_null_values() {
        JavaFieldMock fieldMock = null;
        TapestryComponent componentMock = null;
        XmlTagMock tagMock = null;

        InjectedElement injectedElementWithField = new InjectedElement(fieldMock, componentMock);

        assert injectedElementWithField.getElementId() == null;

        InjectedElement injectedElementWithTag = new InjectedElement(tagMock, componentMock);

        assert injectedElementWithTag.getElementId() == null;
    }

    @Test
    public void getParameters_with_null_values() {
        JavaFieldMock fieldMock = null;
        XmlTagMock tagMock = null;
        TapestryComponent componentMock = org.easymock.EasyMock.createMock(TapestryComponent.class);

        InjectedElement injectedElement = new InjectedElement(fieldMock, componentMock);
        assert injectedElement.getParameters().size() == 0;

        InjectedElement injectedElement2 = new InjectedElement(tagMock, componentMock);
        assert injectedElement2.getParameters().size() == 0;
    }

    @Test
    public void getParameters_without_null_values() {
        String[] values = {"id=field2"};
        JavaFieldMock fieldMock = new JavaFieldMock("field1", true).addAnnotation(new JavaAnnotationMock(TapestryConstants.COMPONENT_ANNOTATION).addParameter("parameters", values));

        XmlTagMock tagMock = new XmlTagMock("tag1").addAttribute(new XmlAttributeMock("id", "tag2"));
        TapestryComponent componentMock = org.easymock.EasyMock.createMock(TapestryComponent.class);

        InjectedElement injectedElement = new InjectedElement(fieldMock, componentMock);
        assert injectedElement.getParameters().size() == 1;

        InjectedElement injectedElement2 = new InjectedElement(tagMock, componentMock);
        assert injectedElement2.getParameters().size() == 1;
    }

    @Test
    public void compareTo() {
        JavaFieldMock fieldMock = new JavaFieldMock("field1", true);
        JavaFieldMock fieldMock2 = new JavaFieldMock("field2", true);
        TapestryComponent componentMock = org.easymock.EasyMock.createMock(TapestryComponent.class);

        assert new InjectedElement(fieldMock, componentMock).compareTo(new InjectedElement(fieldMock, componentMock)) == 0;

        assert new InjectedElement(fieldMock, componentMock).compareTo(new InjectedElement(fieldMock2, componentMock)) < 0;
    }
}
