package com.intellij.tapestry.core.model.presentation;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.model.Library;
import com.intellij.tapestry.core.model.presentation.components.DummyTapestryParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import gnu.trove.THashMap;

import java.util.Collections;
import java.util.Map;

public abstract class ParameterReceiverElement extends PresentationLibraryElement {

  private Map<String, TapestryParameter> _parametersCache;
  private long _parametersCacheTimestamp;

  ParameterReceiverElement(@Nullable Library library, @NotNull IJavaClassType elementClass, @NotNull TapestryProject project) {
    super(library, elementClass, project);
  }

  /**
   * Finds Tapestry parameters.
   *
   * @return the declared Tapestry parameters.
   */
  @NotNull
  public Map<String, TapestryParameter> getParameters() {
    if (_parametersCache != null && getElementClass().getFile().getFile().lastModified() <= _parametersCacheTimestamp) {
      return _parametersCache;
    }

    Map<String, TapestryParameter> _parameters = new THashMap<>();
    _parameters.put("mixins", new DummyTapestryParameter(getProject(), "mixins", false));
    _parametersCacheTimestamp = getElementClass().getFile().getFile().lastModified();

    Map<String, IJavaField> fields = getElementClass().getFields(true);

    for (IJavaField field : fields.values()) {
      if (field.isPrivate() && field.getAnnotations().containsKey(PARAMETER_ANNOTATION) && field.isValid()) {
        TapestryParameter parameter = new TapestryParameter(getElementClass(), field);
        _parameters.put(parameter.getName(), parameter);
      }
    }
    _parametersCache = Collections.unmodifiableMap(_parameters);
    return _parametersCache;
  }

  /**
   * Finds Tapestry required parameters.
   *
   * @return the declared Tapestry required parameters.
   */
  @NotNull
  public Map<String, TapestryParameter> getRequiredParameters() {
    Map<String, TapestryParameter> parameters = getParameters();
    Map<String, TapestryParameter> requiredParameters = new THashMap<>();

    for (TapestryParameter parameter : parameters.values()) {
      if (parameter.isRequired()) {
        requiredParameters.put(parameter.getName(), parameter);
      }
    }

    return requiredParameters;
  }

  /**
   * Finds Tapestry not required parameters.
   *
   * @return the declared Tapestry not required parameters.
   */
  public Map<String, TapestryParameter> getOptionalParameters() {
    Map<String, TapestryParameter> parameters = getParameters();
    Map<String, TapestryParameter> optionalParameters = new THashMap<>();

    for (TapestryParameter parameter : parameters.values()) {
      if (!parameter.isRequired()) {
        optionalParameters.put(parameter.getName(), parameter);
      }
    }

    return optionalParameters;
  }
}
