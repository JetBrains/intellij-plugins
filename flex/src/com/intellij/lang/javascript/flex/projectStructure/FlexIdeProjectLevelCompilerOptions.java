package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.projectStructure.options.CompilerOptions;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;

@State(
  name = "FlexIdeProjectLevelCompilerOptions",
  storages = {
    @Storage(file = "$WORKSPACE_FILE$"),
    @Storage(file = "$PROJECT_CONFIG_DIR$/flexCompiler.xml", scheme = StorageScheme.DIRECTORY_BASED)
  }
)
public class FlexIdeProjectLevelCompilerOptions implements PersistentStateComponent<FlexIdeProjectLevelCompilerOptions> {

  public CompilerOptions myProjectLevelCompilerOptions = new CompilerOptions();

  public FlexIdeProjectLevelCompilerOptions getState() {
    return this;
  }

  public void loadState(final FlexIdeProjectLevelCompilerOptions state) {
    myProjectLevelCompilerOptions = state.myProjectLevelCompilerOptions.clone();
  }

  public static FlexIdeProjectLevelCompilerOptions getInstance(final Project project) {
    return ServiceManager.getService(project, FlexIdeProjectLevelCompilerOptions.class);
  }

  public CompilerOptions getProjectLevelCompilerOptions() {
    return myProjectLevelCompilerOptions;
  }
}
