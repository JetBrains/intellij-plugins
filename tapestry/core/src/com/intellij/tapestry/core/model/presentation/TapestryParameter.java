package com.intellij.tapestry.core.model.presentation;

import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.java.IJavaAnnotation;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.java.IJavaMethod;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

import static com.intellij.openapi.util.text.StringUtil.capitalize;
import static com.intellij.openapi.util.text.StringUtil.notNullize;

/**
 * A Tapestry parameter.
 */
public class TapestryParameter implements Comparable {

    private static final String PARAMETER_NAME = "name";

    private final IJavaClassType _elementClass;
    private final IJavaField _parameterField;

    public TapestryParameter(IJavaClassType elementClass, IJavaField parameterField) {
        _elementClass = elementClass;
        _parameterField = parameterField;
    }

    /**
     * Returns the parameter name.
     * This can be defined either in the parameter annotation or from the field name itself.
     *
     * @return the parameter name.
     */
    public String getName() {
      if (!_parameterField.isValid()) return "";

      String name = _parameterField.getName();
      IJavaAnnotation annotation = getParamAnnotation();
      if (annotation != null) {
        final Map<String, String[]> params = annotation.getParameters();

        if (params.containsKey(PARAMETER_NAME)) {
          name = params.get(PARAMETER_NAME)[0];
        }
      }

      return name.startsWith("$") || name.startsWith("_") ? name.substring(1) : name;
    }

  /**
     * Returns the parameter description.
     *
     * @return the parameter description.
     */
    public String getDescription() {
    if (!_parameterField.isValid()) return "";

    return _parameterField.getDocumentation();
  }

    public IJavaField getParameterField() {
        return _parameterField;
    }

    /**
     * Checks if the parameter is required.
     *
     * @return {@code true} if the parameter is required, {@code false} otherwise.
     */
    public boolean isRequired() {
      if (!_parameterField.isValid()) return false;

      IJavaAnnotation annotation = getParamAnnotation();
      if (annotation == null) return false;
      String[] parameterValue = annotation.getParameters().get("required");

      boolean required = parameterValue != null && parameterValue[0].equals(Boolean.TRUE.toString());
      return required && !hasMethod(_elementClass, getName());
    }

  @Nullable
  private IJavaAnnotation getParamAnnotation() {
    return _parameterField.getAnnotations().get(PresentationLibraryElement.PARAMETER_ANNOTATION);
  }

  /**
     * Figures out the default prefix of the parameter value.
     *
     * @return the default prefix of the parameter value.
     */
    public String getDefaultPrefix() {
      if (!_parameterField.isValid()) return "";

      IJavaAnnotation annotation = getParamAnnotation();

      if (annotation != null) {
        String[] parameterValue = annotation.getParameters().get("defaultPrefix");
        if (parameterValue != null) return parameterValue[0];
      }

      return "prop";
    }

    /**
     * Figures out the default value of the parameter.
     *
     * @return the default value of the parameter.
     */
    public String getDefaultValue() {
        if (!_parameterField.isValid())
            return "";

        IJavaAnnotation annotation = getParamAnnotation();

        if (annotation != null) {
            String[] parameterValue = annotation.getParameters().get("value");

            if (parameterValue != null) {
                return parameterValue[0];
            }
        }

        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Object o) {
        return getName().compareTo(((TapestryParameter) o).getName());
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        return obj instanceof TapestryParameter && getName().equals(((TapestryParameter) obj).getName());
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return getName().hashCode();
    }

    private static boolean hasMethod(IJavaClassType clazz, String methodName) {
        String methodOfParameter = TapestryConstants.DEFAULT_PARAMETER_METHOD_PREFIX + capitalize(notNullize(methodName));

        Collection<IJavaMethod> methods = (clazz).getAllMethods(true);
        for (IJavaMethod method : methods) {
            if (method.getName().equals(methodOfParameter) && method.getParameters().isEmpty())
                return true;
        }

        return false;
    }
}
