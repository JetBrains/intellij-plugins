package com.intellij.tapestry.core.model.presentation;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.model.Library;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import gnu.trove.THashMap;

import java.util.Map;

public abstract class ParameterReceiverElement extends PresentationLibraryElement {

  private Map<String, TapestryParameter> _parametersCache;
  private long _parametersCacheTimestamp;

  ParameterReceiverElement(@Nullable Library library, @NonNull IJavaClassType elementClass, @NonNull TapestryProject project) {
    super(library, elementClass, project);
  }

  /**
   * Finds Tapestry parameters.
   *
   * @return the declared Tapestry parameters.
   */
  public Map<String, TapestryParameter> getParameters() {
    if (_parametersCache != null && getElementClass().getFile().getFile().lastModified() <= _parametersCacheTimestamp) {
      return _parametersCache;
    }

    _parametersCache = new THashMap<String, TapestryParameter>();
    _parametersCacheTimestamp = getElementClass().getFile().getFile().lastModified();

    Map<String, IJavaField> fields = getElementClass().getFields(true);

    for (IJavaField field : fields.values()) {
      if (field.isPrivate() && field.getAnnotations().containsKey(PARAMETER_ANNOTATION) && field.isValid()) {
        TapestryParameter parameter = new TapestryParameter(getElementClass(), field);
        _parametersCache.put(parameter.getName(), parameter);
      }
    }

    return _parametersCache;
  }

  /**
   * Finds Tapestry required parameters.
   *
   * @return the declared Tapestry required parameters.
   */
  public Map<String, TapestryParameter> getRequiredParameters() {
    Map<String, TapestryParameter> parameters = getParameters();
    Map<String, TapestryParameter> requiredParameters = new THashMap<String, TapestryParameter>();

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
    Map<String, TapestryParameter> optionalParameters = new THashMap<String, TapestryParameter>();

    for (TapestryParameter parameter : parameters.values()) {
      if (!parameter.isRequired()) {
        optionalParameters.put(parameter.getName(), parameter);
      }
    }

    return optionalParameters;
  }
}
