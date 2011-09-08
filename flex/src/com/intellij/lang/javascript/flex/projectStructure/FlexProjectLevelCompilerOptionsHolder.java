package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableCompilerOptions;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

/**
 * User: ksafonov
 */
public abstract class FlexProjectLevelCompilerOptionsHolder {
  // TODO should be getModifiableModel()!
  public abstract ModifiableCompilerOptions getProjectLevelCompilerOptions();

  public static FlexProjectLevelCompilerOptionsHolder getInstance(final Project project) {
    return ServiceManager.getService(project, FlexProjectLevelCompilerOptionsHolder.class);
  }
}
