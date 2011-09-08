package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.projectStructure.model.CompilerOptionsImpl;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableCompilerOptions;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Property;

@State(
  name = "FlexIdeProjectLevelCompilerOptionsHolder",
  storages = {
    @Storage(file = "$WORKSPACE_FILE$"),
    @Storage(file = "$PROJECT_CONFIG_DIR$/flexCompiler.xml", scheme = StorageScheme.DIRECTORY_BASED)
  }
)
public class FlexIdeProjectLevelCompilerOptionsHolder implements PersistentStateComponent<FlexIdeProjectLevelCompilerOptionsHolder.State> {

  private final CompilerOptionsImpl myModel = new CompilerOptionsImpl();

  public FlexIdeProjectLevelCompilerOptionsHolder.State getState() {
    FlexIdeProjectLevelCompilerOptionsHolder.State state = new State();
    state.compilerOptions = myModel.getState();
    return state;
  }

  public void loadState(final FlexIdeProjectLevelCompilerOptionsHolder.State state) {
    myModel.loadState(state.compilerOptions);
  }

  public static FlexIdeProjectLevelCompilerOptionsHolder getInstance(final Project project) {
    return ServiceManager.getService(project, FlexIdeProjectLevelCompilerOptionsHolder.class);
  }

  // TODO should be getModifiableModel()!
  public ModifiableCompilerOptions getProjectLevelCompilerOptions() {
    return myModel;
  }

  public static class State {
    @Property(surroundWithTag = false)
    public CompilerOptionsImpl.State compilerOptions = new CompilerOptionsImpl.State();
  }
}
