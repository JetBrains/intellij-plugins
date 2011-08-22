package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.projectStructure.options.CompilerOptions;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;

@State(
  name = "FlexIdeProjectLevelCompilerOptionsHolder",
  storages = {
    @Storage(file = "$WORKSPACE_FILE$"),
    @Storage(file = "$PROJECT_CONFIG_DIR$/flexCompiler.xml", scheme = StorageScheme.DIRECTORY_BASED)
  }
)
public class FlexIdeProjectLevelCompilerOptionsHolder implements PersistentStateComponent<FlexIdeProjectLevelCompilerOptionsHolder> {

  public CompilerOptions myProjectLevelCompilerOptions = new CompilerOptions();

  public FlexIdeProjectLevelCompilerOptionsHolder getState() {
    return this;
  }

  public void loadState(final FlexIdeProjectLevelCompilerOptionsHolder state) {
    myProjectLevelCompilerOptions = state.myProjectLevelCompilerOptions.clone();
  }

  public static FlexIdeProjectLevelCompilerOptionsHolder getInstance(final Project project) {
    return ServiceManager.getService(project, FlexIdeProjectLevelCompilerOptionsHolder.class);
  }

  public CompilerOptions getProjectLevelCompilerOptions() {
    return myProjectLevelCompilerOptions;
  }
}
