package com.intellij.tapestry.core.model.presentation.components;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.presentation.TapestryComponent;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * The built-in Parameter element.
 */
public final class ParameterComponent extends TapestryComponent {

  private final Map<String, TapestryParameter> myParameters = new HashMap<>();

  private ParameterComponent(IJavaClassType componentClass, TapestryProject project) throws NotTapestryElementException {
    super(componentClass, project);
    myParameters.put("id", new DummyTapestryParameter(project, "name", true));
  }

  /**
   * Returns an instance of the Parameter component.
   *
   * @param tapestryProject the current Tapestry project.
   * @return an instance of the Parameter component.
   */
  public synchronized static ParameterComponent getInstance(TapestryProject tapestryProject) {
    final IJavaClassType classType =
        tapestryProject.getJavaTypeFinder().findType("org.apache.tapestry5.internal.parser.ParameterToken", true);
    return classType == null ? null : new ParameterComponent(classType, tapestryProject);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return "parameter";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @NotNull
  public Map<String, TapestryParameter> getParameters() {
    return myParameters;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getElementNameFromClass(String rootPackage) throws NotTapestryElementException {
    return getName();
  }
}
