// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.build;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

// do not rename it for compatibility
@State(name = "FlexCompilerConfiguration", storages = @Storage("flexCompiler.xml"))
public class FlexCompilerProjectConfiguration implements PersistentStateComponent<FlexCompilerProjectConfiguration> {

  public boolean GENERATE_FLEXMOJOS_CONFIGS = true;

  public boolean USE_BUILT_IN_COMPILER = true;
  public boolean USE_FCSH = false;
  public boolean USE_MXMLC_COMPC = false;
  public boolean PREFER_ASC_20 = true;
  public int MAX_PARALLEL_COMPILATIONS = 4;
  public int HEAP_SIZE_MB = 512;
  public String VM_OPTIONS = "";

  public static FlexCompilerProjectConfiguration getInstance(final Project project) {
    return project.getService(FlexCompilerProjectConfiguration.class);
  }

  @Override
  public FlexCompilerProjectConfiguration getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull final FlexCompilerProjectConfiguration state) {
    GENERATE_FLEXMOJOS_CONFIGS = state.GENERATE_FLEXMOJOS_CONFIGS;

    USE_BUILT_IN_COMPILER = state.USE_BUILT_IN_COMPILER;
    USE_FCSH = state.USE_FCSH;
    USE_MXMLC_COMPC = state.USE_MXMLC_COMPC;

    PREFER_ASC_20 = state.PREFER_ASC_20;

    //compatibility
    if (USE_FCSH /*&& USE_BUILT_IN_COMPILER*/) {
      USE_FCSH = false;
      USE_BUILT_IN_COMPILER = true;
    }

    //  MAX_PARALLEL_COMPILATIONS = state.MAX_PARALLEL_COMPILATIONS;
    HEAP_SIZE_MB = state.HEAP_SIZE_MB;
    VM_OPTIONS = state.VM_OPTIONS;
  }
}
