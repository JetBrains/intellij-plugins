package com.intellij.tapestry.core.model.presentation.components;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.presentation.TapestryComponent;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.HashMap;

/**
 * The built-in Block element.
 */
public final class BlockComponent extends TapestryComponent {

  private final Map<String, TapestryParameter> myParameters = new HashMap<>();

  private BlockComponent(IJavaClassType componentClass, TapestryProject project) throws NotTapestryElementException {
    super(componentClass, project);
    myParameters.put("id", new DummyTapestryParameter(project, "id", false));
  }

  /**
   * Returns an instance of the Block component.
   *
   * @param tapestryProject the current Tapestry project.
   * @return an instance of the Block component.
   */
  public synchronized static BlockComponent getInstance(TapestryProject tapestryProject) {
    final IJavaClassType classType = tapestryProject.getJavaTypeFinder().findType("org.apache.tapestry5.internal.parser.BlockToken", true);
    return classType == null ? null : new BlockComponent(classType, tapestryProject);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return "block";
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
