package com.intellij.tapestry.core.model.presentation.components;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.presentation.TapestryComponent;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

/**
 * The built-in Body element.
 */
public final class BodyComponent extends TapestryComponent {

  private BodyComponent(IJavaClassType componentClass, TapestryProject project) throws NotTapestryElementException {
    super(componentClass, project);
  }

  /**
   * Returns an instance of the Body component.
   *
   * @param tapestryProject the current Tapestry project.
   * @return an instance of the Body component.
   */
  public synchronized static BodyComponent getInstance(TapestryProject tapestryProject) {
    final IJavaClassType classType = tapestryProject.getJavaTypeFinder().findType("org.apache.tapestry5.internal.parser.BodyToken", true);
    return classType == null ? null : new BodyComponent(classType, tapestryProject);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return "body";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @NotNull
  public Map<String, TapestryParameter> getParameters() {
    return Collections.emptyMap();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getElementNameFromClass(String rootPackage) throws NotTapestryElementException {
    return getName();
  }
}
