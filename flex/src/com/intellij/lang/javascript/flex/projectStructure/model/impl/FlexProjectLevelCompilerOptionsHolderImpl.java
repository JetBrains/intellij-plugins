package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.FlexProjectLevelCompilerOptionsHolder;
import com.intellij.lang.javascript.flex.projectStructure.model.ModuleOrProjectCompilerOptions;
import com.intellij.openapi.components.*;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Property;

@State(
  name = "FlexIdeProjectLevelCompilerOptionsHolder",
  storages = {
    @Storage(file = StoragePathMacros.WORKSPACE_FILE),
    @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/flexCompiler.xml", scheme = StorageScheme.DIRECTORY_BASED)
  }
)
public class FlexProjectLevelCompilerOptionsHolderImpl extends FlexProjectLevelCompilerOptionsHolder
  implements PersistentStateComponent<FlexProjectLevelCompilerOptionsHolderImpl.State> {

  private final CompilerOptionsImpl myModel;
  private final Project myProject;

  public FlexProjectLevelCompilerOptionsHolderImpl(final Project project) {
    myProject = project;
    myModel = new CompilerOptionsImpl(project, true);
  }

  public FlexProjectLevelCompilerOptionsHolderImpl.State getState() {
    FlexProjectLevelCompilerOptionsHolderImpl.State state = new State();
    state.compilerOptions = myModel.getState(myProject);
    return state;
  }

  @Override
  public ModuleOrProjectCompilerOptions getProjectLevelCompilerOptions() {
    return myModel;
  }

  public void loadState(final FlexProjectLevelCompilerOptionsHolderImpl.State state) {
    myModel.loadState(state.compilerOptions);
  }

  public static class State {
    @Property(surroundWithTag = false)
    public CompilerOptionsImpl.State compilerOptions = new CompilerOptionsImpl.State();
  }
}
