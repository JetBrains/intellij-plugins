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
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.Nullable;

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
    myModuleLevelCompilerOptions = module == null ? null : new CompilerOptionsImpl(module.getProject(), true);

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
  public ModifiableCompilerOptions getModuleLevelCompilerOptions() {
    return myModuleLevelCompilerOptions;
  }

  void setBuildConfigurations(FlexIdeBuildConfiguration[] configurations) {
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
    for (FlexIdeBuildConfigurationImpl.State configurationState : state.CONFIGURATIONS) {
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

  private static FlexIdeBuildConfigurationImpl[] getValidatedConfigurations(Collection<? extends FlexIdeBuildConfiguration> configurations) {
    LinkedHashMap<String, FlexIdeBuildConfigurationImpl> name2configuration =
      new LinkedHashMap<String, FlexIdeBuildConfigurationImpl>(configurations.size());
    for (FlexIdeBuildConfiguration configuration : configurations) {
      if (StringUtil.isEmpty(configuration.getName())) {
        LOG.error("Empty build configuration name");
      }
      if (name2configuration.put(configuration.getName(), (FlexIdeBuildConfigurationImpl)configuration) != null) {
        LOG.error("Duplicate build configuration name: " + configuration.getName());
      }
    }

    if (configurations.isEmpty()) {
      // TODO this project is opened with new UI the first time -> convert
      LOG.warn("No configurations found");
      return new FlexIdeBuildConfigurationImpl[]{new FlexIdeBuildConfigurationImpl()};
    }
    return name2configuration.values().toArray(new FlexIdeBuildConfigurationImpl[name2configuration.size()]);
  }

  public static class State {
    @Tag("configurations")
    @AbstractCollection(surroundWithTag = false, elementTag = "configuration")
    public List<FlexIdeBuildConfigurationImpl.State> CONFIGURATIONS = new ArrayList<FlexIdeBuildConfigurationImpl.State>();

    @Property(surroundWithTag = false)
    public CompilerOptionsImpl.State myModuleLevelCompilerOptions = new CompilerOptionsImpl.State();

    @Attribute("active")
    public String myActiveConfigurationName;
  }
}
