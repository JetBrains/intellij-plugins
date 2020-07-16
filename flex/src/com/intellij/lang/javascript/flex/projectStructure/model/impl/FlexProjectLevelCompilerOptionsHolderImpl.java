// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.FlexProjectLevelCompilerOptionsHolder;
import com.intellij.lang.javascript.flex.projectStructure.model.ModuleOrProjectCompilerOptions;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

@State(name = "FlexIdeProjectLevelCompilerOptionsHolder", storages = @Storage("flexCompiler.xml"))
public class FlexProjectLevelCompilerOptionsHolderImpl extends FlexProjectLevelCompilerOptionsHolder
  implements PersistentStateComponent<FlexProjectLevelCompilerOptionsHolderImpl.State> {

  private final CompilerOptionsImpl myModel;
  private final Project myProject;

  public FlexProjectLevelCompilerOptionsHolderImpl(final Project project) {
    myProject = project;
    myModel = new CompilerOptionsImpl(project, true);
  }

  @Override
  public FlexProjectLevelCompilerOptionsHolderImpl.State getState() {
    FlexProjectLevelCompilerOptionsHolderImpl.State state = new State();
    state.compilerOptions = myModel.getState(myProject);
    return state;
  }

  @Override
  public ModuleOrProjectCompilerOptions getProjectLevelCompilerOptions() {
    return myModel;
  }

  @Override
  public void loadState(@NotNull final FlexProjectLevelCompilerOptionsHolderImpl.State state) {
    myModel.loadState(state.compilerOptions);
  }

  public static class State {
    @Property(surroundWithTag = false)
    public CompilerOptionsImpl.State compilerOptions = new CompilerOptionsImpl.State();
  }
}
