// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.ProjectTopics;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.RootsChangeRescanningInfo;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.XCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;

@State(name = FlexBuildConfigurationManagerImpl.COMPONENT_NAME)
public final class FlexBuildConfigurationManagerImpl extends FlexBuildConfigurationManager
  implements PersistentStateComponent<FlexBuildConfigurationManagerImpl.State> {

  private static final Logger LOG = Logger.getInstance(FlexBuildConfigurationManagerImpl.class.getName());

  public static final String COMPONENT_NAME = "FlexBuildConfigurationManager";

  @Nullable
  private final Module myModule;
  private FlexBuildConfigurationImpl[] myConfigurations = new FlexBuildConfigurationImpl[]{new FlexBuildConfigurationImpl()};

  private final CompilerOptionsImpl myModuleLevelCompilerOptions;
  private FlexBuildConfigurationImpl myActiveConfiguration = myConfigurations[0];

  public FlexBuildConfigurationManagerImpl(@Nullable final Module module) {
    myModule = module;
    myModuleLevelCompilerOptions = module == null ? new CompilerOptionsImpl() : new CompilerOptionsImpl(module.getProject(), true);

    if (myModule != null) {
      myModule.getProject().getMessageBus().connect(myModule).subscribe(ProjectTopics.MODULES, new ModuleListener() {
        @Override
        public void beforeModuleRemoved(@NotNull Project project, @NotNull Module module) {
          if (module != myModule) {
            removeDependenciesOn(module);
          }
        }
      });
    }
  }

  private void removeDependenciesOn(Module module) {
    for (ModifiableFlexBuildConfiguration configuration : myConfigurations) {
      // TODO remove 'optimize for' links
      for (Iterator<ModifiableDependencyEntry> i = configuration.getDependencies().getModifiableEntries().iterator(); i.hasNext(); ) {
        DependencyEntry entry = i.next();
        if (entry instanceof BuildConfigurationEntry && ((BuildConfigurationEntry)entry).findModule() == module) {
          i.remove();
        }
      }
    }
  }

  @Override
  @Nullable
  public FlexBuildConfiguration findConfigurationByName(final String name) {
    for (ModifiableFlexBuildConfiguration configuration : myConfigurations) {
      if (configuration.getName().equals(name)) {
        return configuration;
      }
    }
    return null;
  }

  @Override
  public FlexBuildConfiguration getActiveConfiguration() {
    return myActiveConfiguration;
  }

  @Override
  public void setActiveBuildConfiguration(final FlexBuildConfiguration buildConfiguration) {
    if (myActiveConfiguration == buildConfiguration) {
      return;
    }

    if (!ArrayUtil.contains(buildConfiguration, myConfigurations)) {
      throw new IllegalArgumentException(
        "Build configuration " + buildConfiguration.getName() + " does not belong to module " +
        (myModule != null ? myModule.getName() : "(dummy)"));
    }

    if (myModule != null) {
      ApplicationManager.getApplication().runWriteAction(() -> {
        myActiveConfiguration = (FlexBuildConfigurationImpl)buildConfiguration;
        resetHighlighting(myModule.getProject());
      });
    }
    else {
      myActiveConfiguration = (FlexBuildConfigurationImpl)buildConfiguration;
    }
  }

  @Override
  public FlexBuildConfiguration[] getBuildConfigurations() {
    return myConfigurations.clone();
  }

  FlexBuildConfigurationImpl[] doGetBuildConfigurations() {
    return myConfigurations;
  }

  // TODO should be getModifiableModel()!
  @Override
  public ModuleOrProjectCompilerOptions getModuleLevelCompilerOptions() {
    return myModuleLevelCompilerOptions;
  }

  void setBuildConfigurations(FlexBuildConfigurationImpl[] configurations) {
    final String activeName = myActiveConfiguration != null ? myActiveConfiguration.getName() : null;
    ApplicationManager.getApplication().assertWriteAccessAllowed();
    FlexBuildConfigurationImpl[] validatedConfigurations = getValidatedConfigurations(Arrays.asList(configurations));
    doSetBuildConfigurations(validatedConfigurations);
    updateActiveConfiguration(activeName);
  }

  void doSetBuildConfigurations(FlexBuildConfigurationImpl[] configurations) {
    myConfigurations = configurations;
  }

  private void updateActiveConfiguration(@Nullable final String activeName) {
    if (myConfigurations.length > 0) {
      myActiveConfiguration =
        activeName != null ? ContainerUtil.find(myConfigurations, bc -> bc.getName().equals(activeName)) : null;
      if (myActiveConfiguration == null) {
        myActiveConfiguration = myConfigurations[0];
      }
    }
    else {
      myActiveConfiguration = null;
    }
  }

  @Override
  public State getState() {
    final State state = new State();
    for (FlexBuildConfigurationImpl configuration : myConfigurations) {
      state.CONFIGURATIONS.add(configuration.getState(myModule));
    }
    state.myModuleLevelCompilerOptions = myModuleLevelCompilerOptions.getState(myModule);
    state.myActiveConfigurationName = myActiveConfiguration != null ? myActiveConfiguration.getName() : null;
    return state;
  }

  @Override
  public void loadState(@NotNull final State state) {
    if (myModule == null) {
      throw new IllegalStateException("Cannot load state of a dummy config manager instance");
    }
    Collection<FlexBuildConfigurationImpl> configurations = new ArrayList<>(state.CONFIGURATIONS.size());
    for (FlexBuildConfigurationState configurationState : state.CONFIGURATIONS) {
      FlexBuildConfigurationImpl configuration = new FlexBuildConfigurationImpl();
      configuration.loadState(configurationState, myModule.getProject());
      configurations.add(configuration);
    }
    doSetBuildConfigurations(getValidatedConfigurations(configurations));
    updateActiveConfiguration(state.myActiveConfigurationName);
    myModuleLevelCompilerOptions.loadState(state.myModuleLevelCompilerOptions);
  }

  static void resetHighlighting(Project project) {
    ProjectRootManagerEx.getInstanceEx(project).makeRootsChange(EmptyRunnable.getInstance(),
                                                                RootsChangeRescanningInfo.NO_RESCAN_NEEDED);
  }

  private FlexBuildConfigurationImpl[] getValidatedConfigurations(Collection<? extends FlexBuildConfigurationImpl> configurations) {
    if (configurations.isEmpty()) {
      LOG.warn("No Flash build configurations found");
      return new FlexBuildConfigurationImpl[]{new FlexBuildConfigurationImpl()};
    }

    List<FlexBuildConfigurationImpl> configList = new ArrayList<>(configurations);
    for (FlexBuildConfigurationImpl configuration : configList) {
      if (StringUtil.isEmpty(configuration.getName())) {
        LOG.warn("Empty build configuration name");
        configuration.setName(myModule.getName());
      }
    }

    Set<String> names = new HashSet<>();
    String duplicateName = null;
    for (FlexBuildConfiguration c : configList) {
      if (StringUtil.isEmpty(c.getName())) {
        LOG.warn("Empty build configuration name");
        continue;
      }
      if (!names.add(c.getName())) {
        duplicateName = c.getName();
        break;
      }
    }

    if (duplicateName != null) {
      LOG.warn("Duplicate build configuration name: " + duplicateName);
      List<String> uniqueNames =
        generateUniqueNames(ContainerUtil.map(configList, bc -> bc.getName()));
      for (int i = 0; i < configList.size(); i++) {
        configList.get(i).setName(uniqueNames.get(i));
      }
    }

    return configList.toArray(new FlexBuildConfigurationImpl[0]);
  }

  private static List<String> generateUniqueNames(List<String> names) {
    List<String> result = new ArrayList<>(names.size());
    Set<String> namesBefore = new HashSet<>();
    for (int i = 0; i < names.size(); i++) {
      String name = names.get(i);
      String newName = name;
      if (namesBefore.contains(newName)) {
        Set<String> otherNames = new HashSet<>(namesBefore);
        otherNames.addAll(names.subList(i + 1, names.size()));
        int index = 1;
        while (true) {
          newName = MessageFormat.format("{0} ({1})", name, index++);
          if (!otherNames.contains(newName)) break;
        }
      }
      result.add(newName);
      namesBefore.add(newName);
    }
    return result;
  }

  public static class State {
    @XCollection(propertyElementName = "configurations", elementName = "configuration")
    public List<FlexBuildConfigurationState> CONFIGURATIONS = new ArrayList<>();

    @Property(surroundWithTag = false)
    public CompilerOptionsImpl.State myModuleLevelCompilerOptions = new CompilerOptionsImpl.State();

    @Attribute("active")
    public String myActiveConfigurationName;
  }
}
