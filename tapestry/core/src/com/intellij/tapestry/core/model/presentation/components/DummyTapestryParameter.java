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

  private final String _name;
  private final boolean _required;

  public DummyTapestryParameter(TapestryProject tapestryProject, String name, boolean required) {
    super(null, new DummyJavaField(name, tapestryProject.getJavaTypeFinder().findType("java.lang.String", true)));
    _name = name;
    _required = required;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public boolean isRequired() {
    return _required;
  }

  @Override
  public String getDefaultPrefix() {
    return "literal";
  }

  @Override
  public IJavaField getParameterField() {
    return super.getParameterField();
  }

  /**
   * A dummy java field.
   */
  static class DummyJavaField implements IJavaField {

    private final String _name;
    private final IJavaClassType _type;

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
      return new HashMap<>();
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
