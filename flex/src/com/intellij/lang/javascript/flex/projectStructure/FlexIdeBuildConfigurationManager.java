package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.options.CompilerOptions;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.util.xmlb.annotations.AbstractCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@State(name = "FlexIdeBuildConfigurationManager", storages = {@Storage(file = "$MODULE_FILE$")})
public class FlexIdeBuildConfigurationManager implements PersistentStateComponent<FlexIdeBuildConfigurationManager.State> {

  private final Module myModule;
  private FlexIdeBuildConfiguration[] myConfigurations = new FlexIdeBuildConfiguration[]{new FlexIdeBuildConfiguration()};
  private CompilerOptions myModuleLevelCompilerOptions = new CompilerOptions();

  public FlexIdeBuildConfigurationManager(final Module module) {
    myModule = module;
  }

  public static FlexIdeBuildConfigurationManager getInstance(final Module module) {
    assert ModuleType.get(module) == FlexModuleType.getInstance();
    return (FlexIdeBuildConfigurationManager)module.getPicoContainer()
      .getComponentInstance(FlexIdeBuildConfigurationManager.class.getName());
  }

  public FlexIdeBuildConfiguration[] getBuildConfigurations() {
    return Arrays.copyOf(myConfigurations, myConfigurations.length);
  }

  public CompilerOptions getModuleLevelCompilerOptions() {
    return myModuleLevelCompilerOptions;
  }

  void setBuildConfigurations(final FlexIdeBuildConfiguration[] configurations) {
    assert configurations.length > 0;
    ApplicationManager.getApplication().assertWriteAccessAllowed();
    myConfigurations = Arrays.copyOf(configurations, configurations.length);
  }

  public State getState() {
    final State state = new State();
    Collections.addAll(state.myConfigurations, myConfigurations);
    state.myModuleLevelCompilerOptions = myModuleLevelCompilerOptions.clone();
    return state;
  }

  public void loadState(final State state) {
    if (state.myConfigurations.isEmpty()) {
      myConfigurations = new FlexIdeBuildConfiguration[]{new FlexIdeBuildConfiguration()};
    }
    else {
      myConfigurations = state.myConfigurations.toArray(new FlexIdeBuildConfiguration[state.myConfigurations.size()]);
    }

    myModuleLevelCompilerOptions = state.myModuleLevelCompilerOptions.clone();
  }

  public static class State {
    @AbstractCollection(elementTypes = FlexIdeBuildConfiguration.class)
    public Collection<FlexIdeBuildConfiguration> myConfigurations = new ArrayList<FlexIdeBuildConfiguration>();

    public CompilerOptions myModuleLevelCompilerOptions = new CompilerOptions();
  }
}
