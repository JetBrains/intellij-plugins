package com.intellij.tapestry.core.model.presentation;

import com.intellij.tapestry.core.mocks.JavaAnnotationMock;
import com.intellij.tapestry.core.mocks.JavaFieldMock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class ComponentParameterTest {

    private JavaFieldMock _fieldMock;

    @BeforeMethod
    public void initMocks() {
        _fieldMock = new JavaFieldMock().setPrivate(true);
        _fieldMock.addAnnotation(new JavaAnnotationMock("org.apache.tapestry5.annotations.Parameter"));
    }

    @Test
    public void getName() {
        _fieldMock.setName("_field1");

        assert new TapestryParameter(null, _fieldMock).getName().equals("field1");

        initMocks();
        _fieldMock.setName("$field1");

        assert new TapestryParameter(null, _fieldMock).getName().equals("field1");

        initMocks();
        _fieldMock.setName("field1");

        assert new TapestryParameter(null, _fieldMock).getName().equals("field1");

        initMocks();
        _fieldMock.setName("field1");
        ((JavaAnnotationMock) _fieldMock.getAnnotations().values().iterator().next()).addParameter("name", "field2");

        assert new TapestryParameter(null, _fieldMock).getName().equals("field2");
    }

    @Test
    public void getDescription() {
        _fieldMock.setDocumentation("docs");

        assert new TapestryParameter(null, _fieldMock).getDescription().equals("docs");
    }

    @Test
    public void isRequired() {
        assert !new TapestryParameter(null, _fieldMock).isRequired();
    }

    @Test
    public void getDefaultPrefix_default() {
        assert new TapestryParameter(null, _fieldMock).getDefaultPrefix().equals("prop");
    }

    @Test
    public void getDefaultPrefix_configured() {
        ((JavaAnnotationMock) _fieldMock.getAnnotations().values().iterator().next()).addParameter("defaultPrefix", "myprefix");

        assert new TapestryParameter(null, _fieldMock).getDefaultPrefix().equals("myprefix");
    }

    @Test
    public void getDefaultValue_default() {
        assert new TapestryParameter(null, _fieldMock).getDefaultValue().isEmpty();
    }

    @Test
    public void getDefaultValue_configured() {
        ((JavaAnnotationMock) _fieldMock.getAnnotations().values().iterator().next()).addParameter("value", "myvalue");

        assert new TapestryParameter(null, _fieldMock).getDefaultValue().equals("myvalue");
    }

    @Test
    public void compareTo() {
        _fieldMock.setName("name1");

        JavaFieldMock field2Mock = new JavaFieldMock("name2", true);
        field2Mock.addAnnotation(new JavaAnnotationMock("org.apache.tapestry5.annotations.Parameter"));

        assert new TapestryParameter(null, _fieldMock).compareTo(new TapestryParameter(null, _fieldMock)) == 0;

        assert new TapestryParameter(null, _fieldMock).compareTo(new TapestryParameter(null, field2Mock)) < 0;
    }
}
