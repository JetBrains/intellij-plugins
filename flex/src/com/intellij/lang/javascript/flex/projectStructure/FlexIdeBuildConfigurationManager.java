package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.ProjectTopics;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationEntry;
import com.intellij.lang.javascript.flex.projectStructure.options.CompilerOptions;
import com.intellij.lang.javascript.flex.projectStructure.options.DependencyEntry;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@State(name = "FlexIdeBuildConfigurationManager", storages = {@Storage(file = "$MODULE_FILE$")})
public class FlexIdeBuildConfigurationManager implements PersistentStateComponent<FlexIdeBuildConfigurationManager.State> {

  private static final Logger LOG = Logger.getInstance(FlexIdeBuildConfigurationManager.class.getName());

  private final Module myModule;
  private FlexIdeBuildConfiguration[] myConfigurations = new FlexIdeBuildConfiguration[]{new FlexIdeBuildConfiguration()};
  private CompilerOptions myModuleLevelCompilerOptions = new CompilerOptions();
  private FlexIdeBuildConfiguration myActiveConfiguration;

  public FlexIdeBuildConfigurationManager(final Module module) {
    myModule = module;

    myModule.getProject().getMessageBus().connect(myModule).subscribe(ProjectTopics.MODULES, new ModuleAdapter() {
      @Override
      public void beforeModuleRemoved(Project project, Module module) {
        if (module != myModule) {
          removeDependenciesOn(module);
        }
      }
    });
  }

  private void removeDependenciesOn(Module module) {
    for (FlexIdeBuildConfiguration configuration : myConfigurations) {
      // TODO remove 'optimize for' links
      for (Iterator<DependencyEntry> i = configuration.DEPENDENCIES.getEntries().iterator(); i.hasNext(); ) {
        DependencyEntry entry = i.next();
        if (entry instanceof BuildConfigurationEntry && ((BuildConfigurationEntry)entry).findModule() == module) {
          i.remove();
        }
      }
    }
  }

  @Nullable
  public FlexIdeBuildConfiguration findConfigurationByName(final String name) {
    for (final FlexIdeBuildConfiguration configuration : myConfigurations) {
      if (configuration.NAME.equals(name)) {
        return configuration;
      }
    }
    return null;
  }

  public FlexIdeBuildConfiguration getActiveConfiguration() {
    return myActiveConfiguration;
  }

  public void setActiveBuildConfiguration(final FlexIdeBuildConfiguration buildConfiguration) {
    if (!ArrayUtil.contains(buildConfiguration, myConfigurations)) {
      throw new IllegalArgumentException(
        "Build configuration " + buildConfiguration.NAME + " does not belong to module " + myModule.getName());
    }
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        myActiveConfiguration = buildConfiguration;
        ProjectRootManagerEx.getInstanceEx(myModule.getProject()).makeRootsChange(EmptyRunnable.getInstance(), false, true);
        ((PsiModificationTrackerImpl)PsiManager.getInstance(myModule.getProject()).getModificationTracker()).incCounter();
      }
    });
    DaemonCodeAnalyzer.getInstance(myModule.getProject()).restart();
  }

  public static FlexIdeBuildConfigurationManager getInstance(final @NotNull Module module) {
    assert ModuleType.get(module) == FlexModuleType.getInstance() : ModuleType.get(module).getName() + ", " + module.toString();
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
    final String activeName = myActiveConfiguration != null ? myActiveConfiguration.NAME : null;
    ApplicationManager.getApplication().assertWriteAccessAllowed();
    myConfigurations = getValidatedConfigurations(Arrays.asList(configurations));
    updateActiveConfiguration(activeName);
  }

  private void updateActiveConfiguration(@Nullable final String activeName) {
    if (myConfigurations.length > 0) {
      myActiveConfiguration = activeName != null ? ContainerUtil.find(myConfigurations, new Condition<FlexIdeBuildConfiguration>() {
        @Override
        public boolean value(FlexIdeBuildConfiguration flexIdeBuildConfiguration) {
          return flexIdeBuildConfiguration.NAME.equals(activeName);
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
    Collections.addAll(state.myConfigurations, myConfigurations);
    state.myModuleLevelCompilerOptions = myModuleLevelCompilerOptions.clone();
    state.myActiveConfigurationName = myActiveConfiguration != null ? myActiveConfiguration.NAME : null;
    return state;
  }

  public void loadState(final State state) {
    myConfigurations = getValidatedConfigurations(state.myConfigurations);
    for (FlexIdeBuildConfiguration configuration : myConfigurations) {
      configuration.initialize(myModule.getProject());
    }
    updateActiveConfiguration(state.myActiveConfigurationName);
    myModuleLevelCompilerOptions = state.myModuleLevelCompilerOptions.clone();
  }

  private static FlexIdeBuildConfiguration[] getValidatedConfigurations(Collection<FlexIdeBuildConfiguration> configurations) {
    LinkedHashMap<String, FlexIdeBuildConfiguration> name2configuration =
      new LinkedHashMap<String, FlexIdeBuildConfiguration>(configurations.size());
    for (FlexIdeBuildConfiguration configuration : configurations) {
      if (StringUtil.isEmpty(configuration.NAME)) {
        LOG.error("Empty build configuration name");
      }
      if (name2configuration.put(configuration.NAME, configuration) != null) {
        LOG.error("Duplicate build configuration name: " + configuration.NAME);
      }
    }

    if (configurations.isEmpty()) {
      LOG.error("No configurations found");
      return new FlexIdeBuildConfiguration[]{new FlexIdeBuildConfiguration()};
    }
    return name2configuration.values().toArray(new FlexIdeBuildConfiguration[name2configuration.size()]);
  }

  public static class State {
    @AbstractCollection(elementTypes = FlexIdeBuildConfiguration.class)
    public Collection<FlexIdeBuildConfiguration> myConfigurations = new ArrayList<FlexIdeBuildConfiguration>();

    public CompilerOptions myModuleLevelCompilerOptions = new CompilerOptions();

    @Attribute("active")
    public String myActiveConfigurationName;
  }
}
