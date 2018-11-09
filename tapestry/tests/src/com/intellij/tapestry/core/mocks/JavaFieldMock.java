package com.intellij.tapestry.core.mocks;

import com.intellij.tapestry.core.java.IJavaAnnotation;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.java.IJavaType;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for easy creation of IJavaField mocks.
 *
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class JavaFieldMock implements IJavaField {

    private String _name;
    private IJavaType _type;
    private boolean _private;
    private final Map<String, IJavaAnnotation> _annotations = new HashMap<>();
    private String _documentation;
    private String _stringRepresentation;

    public JavaFieldMock() {
    }

    public JavaFieldMock(String name, boolean aPrivate) {
        _name = name;
        _private = aPrivate;
    }

    @Override
    public String getName() {
        return _name;
    }

    public JavaFieldMock setName(String name) {
        _name = name;

        return this;
    }

    @Override
    public IJavaType getType() {
        return _type;
    }

    public JavaFieldMock setType(IJavaType type) {
        _type = type;

        return this;
    }

    @Override
    public boolean isPrivate() {
        return _private;
    }

    public JavaFieldMock setPrivate(boolean aPrivate) {
        _private = aPrivate;

        return this;
    }

    @Override
    public Map<String, IJavaAnnotation> getAnnotations() {
        return _annotations;
    }

    public JavaFieldMock addAnnotation(IJavaAnnotation annotation) {
        _annotations.put(annotation.getFullyQualifiedName(), annotation);

        return this;
    }

    @Override
    public String getDocumentation() {
        return _documentation;
    }

    public JavaFieldMock setDocumentation(String documentation) {
        _documentation = documentation;

        return this;
    }

    @Override
    public String getStringRepresentation() {
        return _stringRepresentation;
    }

    public JavaFieldMock setStringRepresentation(String stringRepresentation) {
        _stringRepresentation = stringRepresentation;

        return this;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
