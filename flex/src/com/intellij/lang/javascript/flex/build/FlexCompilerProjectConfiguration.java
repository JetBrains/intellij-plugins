package com.intellij.lang.javascript.flex.build;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;

@State(
  name = "FlexCompilerConfiguration", // do not rename it for compatibility
  storages = {
    @Storage(id = "default", file = "$WORKSPACE_FILE$"),
    @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/flexCompiler.xml", scheme = StorageScheme.DIRECTORY_BASED)
  }
)
public class FlexCompilerProjectConfiguration implements PersistentStateComponent<FlexCompilerProjectConfiguration> {
  static final int PARALLEL_COMPILATIONS_LIMIT = 10;

  public boolean USE_BUILT_IN_COMPILER = true;
  public boolean USE_FCSH = false;
  public boolean USE_MXMLC_COMPC = false;
  public int MAX_PARALLEL_COMPILATIONS = 4;
  public int HEAP_SIZE_MB = 512;
  public String VM_OPTIONS = "";
  public boolean SWF_DEBUG_ENABLED = true;
  public boolean SWC_DEBUG_ENABLED = true;

  public static FlexCompilerProjectConfiguration getInstance(final Project project) {
    return ServiceManager.getService(project, FlexCompilerProjectConfiguration.class);
  }

  public FlexCompilerProjectConfiguration getState() {
    return this;
  }

  public void loadState(final FlexCompilerProjectConfiguration state) {
    USE_BUILT_IN_COMPILER = state.USE_BUILT_IN_COMPILER;
    USE_FCSH = state.USE_FCSH;
    USE_MXMLC_COMPC = state.USE_MXMLC_COMPC;

    //compatibility
    if (USE_FCSH && USE_BUILT_IN_COMPILER) {
      USE_FCSH = false;
    }

    MAX_PARALLEL_COMPILATIONS = state.MAX_PARALLEL_COMPILATIONS;
    HEAP_SIZE_MB = state.HEAP_SIZE_MB;
    VM_OPTIONS = state.VM_OPTIONS;
    SWF_DEBUG_ENABLED = state.SWF_DEBUG_ENABLED;
    SWC_DEBUG_ENABLED = state.SWC_DEBUG_ENABLED;
  }
}
