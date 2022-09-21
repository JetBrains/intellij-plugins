// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.flex.model.bc.CompilerOptionInfo;
import com.intellij.lang.javascript.flex.projectStructure.model.CompilerOptionsListener;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableCompilerOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.ModuleOrProjectCompilerOptions;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.EventDispatcher;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class CompilerOptionsImpl implements ModifiableCompilerOptions, ModuleOrProjectCompilerOptions {

  private final Project myProject;
  private final boolean myResetHighlightingOnCommit;

  private final EventDispatcher<CompilerOptionsListener> myDispatcher = EventDispatcher.create(CompilerOptionsListener.class);

  private final Map<String, String> myOptions = new HashMap<>();
  private @NotNull ResourceFilesMode myResourceFilesMode = ResourceFilesMode.All;
  private @NotNull String myFilesToIncludeInSWC = "";
  private @NotNull String myAdditionalConfigFilePath = "";
  private @NotNull String myAdditionalOptions = "";

  CompilerOptionsImpl() {
    this(null, false);
  }

  // todo introduce modifiable model with highlighting reset on commit instead of myResetHighlightingOnCommit, move listeners notification to committing method
  CompilerOptionsImpl(final Project project, final boolean resetHighlightingOnCommit) {
    myProject = project;
    myResetHighlightingOnCommit = resetHighlightingOnCommit;
  }

  @Override
  public void addOptionsListener(CompilerOptionsListener listener, Disposable parentDisposable) {
    myDispatcher.addListener(listener, parentDisposable);
  }

  @Override
  @Nullable
  public String getOption(@NotNull String name) {
    return myOptions.get(name);
  }

  @Override
  public Map<String, String> getAllOptions() {
    return Collections.unmodifiableMap(myOptions);
  }

  @Override
  public void setAllOptions(Map<String, String> newOptions) {
    myOptions.clear();
    myOptions.putAll(newOptions);

    if (myResetHighlightingOnCommit) {
      ApplicationManager.getApplication().runWriteAction(() -> FlexBuildConfigurationManagerImpl.resetHighlighting(myProject));
      myDispatcher.getMulticaster().optionsInTableChanged();
    }
  }

  @Override
  public void setResourceFilesMode(@NotNull final ResourceFilesMode mode) {
    myResourceFilesMode = mode;
  }

  @Override
  @NotNull
  public ResourceFilesMode getResourceFilesMode() {
    return myResourceFilesMode;
  }

  @Override
  public Collection<String> getFilesToIncludeInSWC() {
    if (myFilesToIncludeInSWC.isEmpty()) return Collections.emptyList();
    return StringUtil.split(myFilesToIncludeInSWC, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
  }

  @Override
  public void setFilesToIncludeInSWC(@NotNull Collection<String> filesToIncludeInSWC) {
    myFilesToIncludeInSWC =
      filesToIncludeInSWC.isEmpty() ? "" : StringUtil.join(filesToIncludeInSWC, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
  }

  @Override
  public void setAdditionalConfigFilePath(@NotNull final String path) {
    myAdditionalConfigFilePath = path;

    if (myResetHighlightingOnCommit) {
      // module and project level settings don't have config file field
      assert myAdditionalConfigFilePath.equals(path);
    }
  }

  @Override
  @NotNull
  public String getAdditionalConfigFilePath() {
    return myAdditionalConfigFilePath;
  }

  @Override
  public void setAdditionalOptions(@NotNull final String options) {
    myAdditionalOptions = options;

    if (myResetHighlightingOnCommit) {
      ApplicationManager.getApplication().runWriteAction(() -> FlexBuildConfigurationManagerImpl.resetHighlighting(myProject));
      myDispatcher.getMulticaster().additionalOptionsChanged();
    }
  }

  @Override
  @NotNull
  public String getAdditionalOptions() {
    return myAdditionalOptions;
  }

  public CompilerOptionsImpl getCopy() {
    CompilerOptionsImpl copy = new CompilerOptionsImpl();
    applyTo(copy);
    return copy;
  }

  void applyTo(ModifiableCompilerOptions copy) {
    copy.setAllOptions(myOptions);
    copy.setResourceFilesMode(myResourceFilesMode);
    ((CompilerOptionsImpl)copy).myFilesToIncludeInSWC = myFilesToIncludeInSWC;
    copy.setAdditionalConfigFilePath(myAdditionalConfigFilePath);
    copy.setAdditionalOptions(myAdditionalOptions);
  }

  public State getState(final @Nullable ComponentManager componentManager) {
    State state = new State();
    putOptionsCollapsingPaths(myOptions, state.options, componentManager);
    state.resourceFilesMode = myResourceFilesMode;
    state.filesToIncludeInSWC = FlexBuildConfigurationImpl.collapsePaths(componentManager, myFilesToIncludeInSWC);
    state.additionalConfigFilePath = myAdditionalConfigFilePath;
    state.additionalOptions = myAdditionalOptions;
    return state;
  }

  private static void putOptionsCollapsingPaths(final Map<String, String> fromMap,
                                                final Map<String, String> toMap,
                                                final @Nullable ComponentManager componentManager) {
    for (Map.Entry<String, String> entry : fromMap.entrySet()) {
      toMap.put(entry.getKey(), FlexBuildConfigurationImpl.collapsePaths(componentManager, entry.getValue()));
    }
  }

  public void loadState(State state) {
    myOptions.clear();
    // filter out options that are not known in current IDEA version
    for (Map.Entry<String, String> entry : state.options.entrySet()) {
      if (CompilerOptionInfo.idExists(entry.getKey())) {
        // no need in expanding paths, it is done automatically even if macros is not in the beginning of the string
        myOptions.put(entry.getKey(), entry.getValue());
      }
    }
    myResourceFilesMode = state.resourceFilesMode;
    myFilesToIncludeInSWC = state.filesToIncludeInSWC;
    myAdditionalConfigFilePath = state.additionalConfigFilePath;
    myAdditionalOptions = state.additionalOptions;
  }

  public boolean isEqual(CompilerOptionsImpl other) {
    return myOptions.equals(other.myOptions) &&
           myResourceFilesMode == other.myResourceFilesMode &&
           myFilesToIncludeInSWC.equals(other.myFilesToIncludeInSWC) &&
           myAdditionalConfigFilePath.equals(other.myAdditionalConfigFilePath) &&
           myAdditionalOptions.equals(other.myAdditionalOptions);
  }

  @Tag("compiler-options")
  public static class State {
    @Property(surroundWithTag = false)
    @MapAnnotation
    public Map<String, String> options = new HashMap<>();
    public ResourceFilesMode resourceFilesMode = ResourceFilesMode.All;
    public String filesToIncludeInSWC = "";
    public String additionalConfigFilePath = "";
    public String additionalOptions = "";
  }
}
