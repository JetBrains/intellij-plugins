package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.ProjectTopics;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiModificationTrackerImpl;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;

@State(name = FlexBuildConfigurationManagerImpl.COMPONENT_NAME, storages = {@Storage(file = "$MODULE_FILE$")})
public class FlexBuildConfigurationManagerImpl extends FlexBuildConfigurationManager
  implements PersistentStateComponent<FlexBuildConfigurationManagerImpl.State> {

  private static final Logger LOG = Logger.getInstance(FlexBuildConfigurationManagerImpl.class.getName());

  public static final String COMPONENT_NAME = "FlexBuildConfigurationManager";

  @Nullable
  private final Module myModule;
  private FlexIdeBuildConfigurationImpl[] myConfigurations = new FlexIdeBuildConfigurationImpl[]{new FlexIdeBuildConfigurationImpl()};

  private final CompilerOptionsImpl myModuleLevelCompilerOptions;
  private FlexIdeBuildConfigurationImpl myActiveConfiguration = myConfigurations[0];

  public FlexBuildConfigurationManagerImpl(@Nullable final Module module) {
    myModule = module;
    myModuleLevelCompilerOptions = module == null ? new CompilerOptionsImpl() : new CompilerOptionsImpl(module.getProject(), true);

    if (myModule != null) {
      myModule.getProject().getMessageBus().connect(myModule).subscribe(ProjectTopics.MODULES, new ModuleAdapter() {
        @Override
        public void beforeModuleRemoved(Project project, Module module) {
          if (module != myModule) {
            removeDependenciesOn(module);
          }
        }
      });
    }
  }

  private void removeDependenciesOn(Module module) {
    for (ModifiableFlexIdeBuildConfiguration configuration : myConfigurations) {
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
  public FlexIdeBuildConfiguration findConfigurationByName(final String name) {
    for (ModifiableFlexIdeBuildConfiguration configuration : myConfigurations) {
      if (configuration.getName().equals(name)) {
        return configuration;
      }
    }
    return null;
  }

  @Override
  public FlexIdeBuildConfiguration getActiveConfiguration() {
    return myActiveConfiguration;
  }

  @Override
  public void setActiveBuildConfiguration(final FlexIdeBuildConfiguration buildConfiguration) {
    if (myActiveConfiguration == buildConfiguration) {
      return;
    }

    if (!ArrayUtil.contains(buildConfiguration, myConfigurations)) {
      throw new IllegalArgumentException(
        "Build configuration " + buildConfiguration.getName() + " does not belong to module " +
        (myModule != null ? myModule.getName() : "(dummy)"));
    }

    if (myModule != null) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override
        public void run() {
          myActiveConfiguration = (FlexIdeBuildConfigurationImpl)buildConfiguration;
          resetHighlighting(myModule.getProject());
        }
      });
    }
    else {
      myActiveConfiguration = (FlexIdeBuildConfigurationImpl)buildConfiguration;
    }
  }

  public FlexIdeBuildConfiguration[] getBuildConfigurations() {
    return Arrays.copyOf(myConfigurations, myConfigurations.length);
  }

  FlexIdeBuildConfigurationImpl[] doGetBuildConfigurations() {
    return myConfigurations;
  }

  // TODO should be getModifiableModel()!
  @Override
  public ModuleOrProjectCompilerOptions getModuleLevelCompilerOptions() {
    return myModuleLevelCompilerOptions;
  }

  void setBuildConfigurations(FlexIdeBuildConfigurationImpl[] configurations) {
    final String activeName = myActiveConfiguration != null ? myActiveConfiguration.getName() : null;
    ApplicationManager.getApplication().assertWriteAccessAllowed();
    FlexIdeBuildConfigurationImpl[] validatedConfigurations = getValidatedConfigurations(Arrays.asList(configurations));
    doSetBuildConfigurations(validatedConfigurations);
    updateActiveConfiguration(activeName);
  }

  void doSetBuildConfigurations(FlexIdeBuildConfigurationImpl[] configurations) {
    myConfigurations = configurations;
  }

  private void updateActiveConfiguration(@Nullable final String activeName) {
    if (myConfigurations.length > 0) {
      myActiveConfiguration =
        activeName != null ? ContainerUtil.find(myConfigurations, new Condition<FlexIdeBuildConfigurationImpl>() {
          @Override
          public boolean value(FlexIdeBuildConfigurationImpl flexIdeBuildConfiguration) {
            return flexIdeBuildConfiguration.getName().equals(activeName);
          }
        }) : null;
      if (myActiveConfiguration == null) {
        myActiveConfiguration = myConfigurations[0];
      }
    }
    else {
      myActiveConfiguration = null;
    }
  }

  public State getState() {
    final State state = new State();
    for (FlexIdeBuildConfigurationImpl configuration : myConfigurations) {
      state.CONFIGURATIONS.add(configuration.getState());
    }
    state.myModuleLevelCompilerOptions = myModuleLevelCompilerOptions.getState();
    state.myActiveConfigurationName = myActiveConfiguration != null ? myActiveConfiguration.getName() : null;
    return state;
  }

  public void loadState(final State state) {
    if (myModule == null) {
      throw new IllegalStateException("Cannot load state of a dummy config manager instance");
    }
    Collection<FlexIdeBuildConfigurationImpl> configurations = new ArrayList<FlexIdeBuildConfigurationImpl>(state.CONFIGURATIONS.size());
    for (FlexBuildConfigurationState configurationState : state.CONFIGURATIONS) {
      FlexIdeBuildConfigurationImpl configuration = new FlexIdeBuildConfigurationImpl();
      configuration.loadState(configurationState, myModule.getProject());
      configurations.add(configuration);
    }
    doSetBuildConfigurations(getValidatedConfigurations(configurations));
    updateActiveConfiguration(state.myActiveConfigurationName);
    myModuleLevelCompilerOptions.loadState(state.myModuleLevelCompilerOptions);
  }

  static void resetHighlighting(Project project) {
    ProjectRootManagerEx.getInstanceEx(project).makeRootsChange(EmptyRunnable.getInstance(), false, true);
    ((PsiModificationTrackerImpl)PsiManager.getInstance(project).getModificationTracker()).incCounter();
    DaemonCodeAnalyzer.getInstance(project).restart();
  }

  private FlexIdeBuildConfigurationImpl[] getValidatedConfigurations(Collection<? extends FlexIdeBuildConfigurationImpl> configurations) {
    if (configurations.isEmpty()) {
      LOG.warn("No Flash build configurations found");
      return new FlexIdeBuildConfigurationImpl[]{new FlexIdeBuildConfigurationImpl()};
    }

    List<FlexIdeBuildConfigurationImpl> configList = new ArrayList<FlexIdeBuildConfigurationImpl>(configurations);
    for (FlexIdeBuildConfigurationImpl configuration : configList) {
      if (StringUtil.isEmpty(configuration.getName())) {
        LOG.warn("Empty build configuration name");
        configuration.setName(myModule.getName());
      }
    }

    Set<String> names = new HashSet<String>();
    String duplicateName = null;
    for (FlexIdeBuildConfiguration c : configList) {
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
        generateUniqueNames(ContainerUtil.map2List(configList, new Function<FlexIdeBuildConfigurationImpl, String>() {
          @Override
          public String fun(FlexIdeBuildConfigurationImpl flexIdeBuildConfiguration) {
            return flexIdeBuildConfiguration.getName();
          }
        }));
      for (int i = 0; i < configList.size(); i++) {
        configList.get(i).setName(uniqueNames.get(i));
      }
    }
    
    return configList.toArray(new FlexIdeBuildConfigurationImpl[configList.size()]);
  }

  public static List<String> generateUniqueNames(List<String> names) {
    List<String> result = new ArrayList<String>(names.size());
    Set<String> namesBefore = new HashSet<String>();
    for (int i = 0; i < names.size(); i++) {
      String name = names.get(i);
      String newName = name;
      if (namesBefore.contains(newName)) {
        Set<String> otherNames = new HashSet<String>(namesBefore);
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
    @Tag("configurations")
    @AbstractCollection(surroundWithTag = false, elementTag = "configuration")
    public List<FlexBuildConfigurationState> CONFIGURATIONS = new ArrayList<FlexBuildConfigurationState>();

    @Property(surroundWithTag = false)
    public CompilerOptionsImpl.State myModuleLevelCompilerOptions = new CompilerOptionsImpl.State();

    @Attribute("active")
    public String myActiveConfigurationName;
  }
}
