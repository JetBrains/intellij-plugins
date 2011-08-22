package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.ui.AddBuildConfigurationDialog;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexIdeBCConfigurable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.ui.navigation.Place;
import gnu.trove.THashMap;

import java.util.*;

public class FlexIdeBCConfigurator {

  // keep these maps in sync!
  private Map<Module, List<FlexIdeBCConfigurable>> myModuleToConfigurablesMap = new THashMap<Module, List<FlexIdeBCConfigurable>>();
  private Map<FlexIdeBuildConfiguration, Module> myConfigurationsToModuleMap = new THashMap<FlexIdeBuildConfiguration, Module>();
  private boolean myModified;

  public void reset() {
    for (final Map.Entry<Module, List<FlexIdeBCConfigurable>> entry : myModuleToConfigurablesMap.entrySet()) {
      final List<FlexIdeBCConfigurable> configurables = entry.getValue();

      for (final FlexIdeBCConfigurable configurable : configurables) {
        configurable.reset();
      }
    }
    myModified = false;
  }

  public List<FlexIdeBCConfigurable> getOrCreateConfigurables(final Module module, final Runnable treeNodeNameUpdater) {
    List<FlexIdeBCConfigurable> configurables = myModuleToConfigurablesMap.get(module);

    if (configurables == null) {
      final FlexIdeBuildConfiguration[] configurations = FlexIdeBuildConfigurationManager.getInstance(module).getBuildConfigurations();
      configurables = new LinkedList<FlexIdeBCConfigurable>();

      for (final FlexIdeBuildConfiguration configuration : configurations) {
        final FlexIdeBuildConfiguration clonedConfiguration = configuration.clone();
        final FlexIdeBCConfigurable configurable = new FlexIdeBCConfigurable(module, clonedConfiguration, treeNodeNameUpdater);
        configurables.add(configurable);
        myConfigurationsToModuleMap.put(clonedConfiguration, module);
      }

      myModuleToConfigurablesMap.put(module, configurables);
    }

    return configurables;
  }

  public void moduleRemoved(final Module module) {
    for (final FlexIdeBCConfigurable configurable : myModuleToConfigurablesMap.get(module)) {
      myConfigurationsToModuleMap.remove(configurable.getEditableObject());
    }

    myModuleToConfigurablesMap.remove(module);
  }

  public boolean isModified() {
    if (myModified) return true;

    for (final List<FlexIdeBCConfigurable> configurables : myModuleToConfigurablesMap.values()) {
      for (final FlexIdeBCConfigurable configurable : configurables) {
        if (configurable.isModified()) {
          return true;
        }
      }
    }
    return false;
  }


  public void apply() throws ConfigurationException {
    for (final Map.Entry<Module, List<FlexIdeBCConfigurable>> entry : myModuleToConfigurablesMap.entrySet()) {
      final Module module = entry.getKey();

      final List<FlexIdeBuildConfiguration> configurations = new LinkedList<FlexIdeBuildConfiguration>();
      final List<FlexIdeBCConfigurable> configurables = entry.getValue();

      for (final FlexIdeBCConfigurable configurable : configurables) {
        configurable.apply();
        configurations.add(configurable.getEditableObject().clone());
      }

      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          FlexIdeBuildConfigurationManager.getInstance(module)
            .setBuildConfigurations(configurations.toArray(new FlexIdeBuildConfiguration[configurations.size()]));
        }
      });
    }

    myModified = false;
  }

  public void clearMaps() {
    myModuleToConfigurablesMap.clear();
    myConfigurationsToModuleMap.clear();
  }

  public int getBCCount(final FlexIdeBuildConfiguration configuration) {
    final Module module = myConfigurationsToModuleMap.get(configuration);
    return myModuleToConfigurablesMap.get(module).size();
  }

  public void removeConfiguration(final FlexIdeBuildConfiguration configuration) {
    myModified = true;
    final Module module = myConfigurationsToModuleMap.get(configuration);
    myConfigurationsToModuleMap.remove(configuration);
    final Iterator<FlexIdeBCConfigurable> configurablesIterator = myModuleToConfigurablesMap.get(module).iterator();
    while (configurablesIterator.hasNext()) {
      final FlexIdeBCConfigurable configurable = configurablesIterator.next();
      if (configuration == configurable.getEditableObject()) {
        configurablesIterator.remove();
        break;
      }
    }
  }

  public void addConfiguration(final Object selectedObject, final Runnable treeNodeNameUpdater) {
    final Module module = selectedObject instanceof Module
                          ? (Module)selectedObject
                          : selectedObject instanceof FlexIdeBuildConfiguration
                            ? myConfigurationsToModuleMap.get(((FlexIdeBuildConfiguration)selectedObject))
                            : null;
    if (module != null) {
      final FlexIdeBuildConfiguration configuration = new FlexIdeBuildConfiguration();
      addConfiguration(module, configuration, "Add Build Configuration", treeNodeNameUpdater);
    }
  }

  public void copy(final FlexIdeBCConfigurable configurable, final Runnable treeNodeNameUpdater) {
    final FlexIdeBuildConfiguration configuration = configurable.getCurrentConfiguration();
    final FlexIdeBuildConfiguration newConfiguration = configuration.clone();
    final Module module = myConfigurationsToModuleMap.get(configurable.getEditableObject());
    addConfiguration(module, newConfiguration, "Copy Build Configuration", treeNodeNameUpdater);
  }

  private void addConfiguration(final Module module,
                                final FlexIdeBuildConfiguration configuration,
                                final String dialogTitle,
                                final Runnable treeNodeNameUpdater) {
    final Project project = module.getProject();
    final AddBuildConfigurationDialog dialog =
      new AddBuildConfigurationDialog(project, dialogTitle, getUsedNames(module), configuration.TARGET_PLATFORM,
                                      configuration.PURE_ACTION_SCRIPT, configuration.OUTPUT_TYPE);
    dialog.show();
    if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
      myModified = true;

      configuration.NAME = dialog.getName();
      configuration.TARGET_PLATFORM = dialog.getTargetPlatform();
      configuration.PURE_ACTION_SCRIPT = dialog.isPureActionScript();
      configuration.OUTPUT_TYPE = dialog.getOutputType();

      final FlexIdeBCConfigurable configurable = new FlexIdeBCConfigurable(module, configuration, treeNodeNameUpdater);

      final List<FlexIdeBCConfigurable> configurables = myModuleToConfigurablesMap.get(module);
      configurables.add(configurable);
      myConfigurationsToModuleMap.put(configuration, module);

      final MasterDetailsComponent.MyNode node = new MasterDetailsComponent.MyNode(configurable);
      FlexIdeModuleStructureExtension.addConfigurationChildNodes(project, configurable, node);

      final ModuleStructureConfigurable moduleStructureConfigurable = ModuleStructureConfigurable.getInstance(project);
      moduleStructureConfigurable.addNode(node, moduleStructureConfigurable.findModuleNode(module));

      final Place place = new Place().putPath(ProjectStructureConfigurable.CATEGORY, moduleStructureConfigurable)
        .putPath(MasterDetailsComponent.TREE_OBJECT, configuration);
      ProjectStructureConfigurable.getInstance(project).navigateTo(place, true);
    }
  }

  private Collection<String> getUsedNames(final Module module) {
    final Collection<String> result = new LinkedList<String>();
    for (final FlexIdeBCConfigurable configurable : myModuleToConfigurablesMap.get(module)) {
      result.add(configurable.getDisplayName());
    }
    return result;
  }
}
