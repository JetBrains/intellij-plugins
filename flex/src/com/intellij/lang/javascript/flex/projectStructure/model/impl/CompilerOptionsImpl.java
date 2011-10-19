package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableCompilerOptions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

class CompilerOptionsImpl implements ModifiableCompilerOptions {

  private final Project myProject;
  private final boolean myResetHighlightingOnCommit;

  private final Map<String, String> myOptions = new THashMap<String, String>();

  CompilerOptionsImpl() {
    this(null, false);
  }

  CompilerOptionsImpl(final Project project, final boolean resetHighlightingOnCommit) {
    myProject = project;
    myResetHighlightingOnCommit = resetHighlightingOnCommit;
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
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          FlexBuildConfigurationManagerImpl.resetHighlighting(myProject);
        }
      });
    }
  }

  public CompilerOptionsImpl getCopy() {
    CompilerOptionsImpl copy = new CompilerOptionsImpl();
    applyTo(copy);
    return copy;
  }

  void applyTo(ModifiableCompilerOptions copy) {
    copy.setAllOptions(myOptions);
  }

  public State getState() {
    State state = new State();
    state.options.putAll(myOptions);
    return state;
  }

  public void loadState(State state) {
    myOptions.clear();
    myOptions.putAll(state.options);
  }

  public boolean isEqual(CompilerOptionsImpl other) {
    return myOptions.equals(other.myOptions);
  }

  @Tag("compiler-options")
  public static class State {
    @Property(surroundWithTag = false)
    @MapAnnotation(surroundKeyWithTag = false, surroundValueWithTag = false)
    public Map<String, String> options = new THashMap<String, String>();
  }
}
