package com.intellij.tapestry.core.model.presentation.components;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.IJavaAnnotation;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.java.IJavaType;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * A dummy parameter.
 * This is used to create builtin components.
 */
public class DummyTapestryParameter extends TapestryParameter {

    private String _name;
    private boolean _required;

    DummyTapestryParameter(TapestryProject tapestryProject, String name, boolean required) {
        super(null, new DummyJavaField(name, tapestryProject.getJavaTypeFinder().findType("java.lang.String", true)));

        _name = name;
        _required = required;
    }

    public String getName() {
        return _name;
    }

    public boolean isRequired() {
        return _required;
    }

    public String getDefaultPrefix() {
        return "literal";
    }

    public IJavaField getParameterField() {
        return super.getParameterField();
    }

    /**
     * A dummy java field.
     */
    static class DummyJavaField implements IJavaField {

        private String _name;
        private IJavaClassType _type;

        public DummyJavaField(String name, IJavaClassType type) {
            _name = name;
            _type = type;
        }

        public String getName() {
            return _name;
        }

        public IJavaType getType() {
            return _type;
        }

        public boolean isPrivate() {
            return true;
        }

        public Map<String, IJavaAnnotation> getAnnotations() {
            return new HashMap<String, IJavaAnnotation>();
        }

        public String getDocumentation() {
            return "";
        }

        public String getStringRepresentation() {
            return "";
        }

        public boolean isValid() {
            return true;
        }
    }
}
