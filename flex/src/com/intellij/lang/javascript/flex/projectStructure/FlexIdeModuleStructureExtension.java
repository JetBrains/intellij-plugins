package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexIdeBCConfigurable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureExtension;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.Computable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FlexIdeModuleStructureExtension extends ModuleStructureExtension {

  final FlexIdeBCConfigurator myConfigurator;

  public FlexIdeModuleStructureExtension() {
    myConfigurator = new FlexIdeBCConfigurator();
  }

  public void reset() {
    myConfigurator.reset();
  }

  public boolean addModuleNodeChildren(final Module module,
                                       final MasterDetailsComponent.MyNode moduleNode,
                                       ModifiableRootModel modifiableRootModel,
                                       final Runnable treeNodeNameUpdater) {
    if (!(ModuleType.get(module) instanceof FlexModuleType)) {
      return false;
    }

    final List<FlexIdeBCConfigurable> configurables =
      myConfigurator.getOrCreateConfigurables(module, treeNodeNameUpdater, modifiableRootModel);

    for (final FlexIdeBCConfigurable configurable : configurables) {
      if (MasterDetailsComponent.findNodeByObject(moduleNode, configurable.getEditableObject()) == null) {
        final MasterDetailsComponent.MyNode configurationNode = new MasterDetailsComponent.MyNode(configurable);
        addConfigurationChildNodes(module.getProject(), configurable, configurationNode);
        moduleNode.add(configurationNode);
      }
    }

    return configurables.size() > 0;
  }

  static void addConfigurationChildNodes(final Project project,
                                         final FlexIdeBCConfigurable configurable,
                                         final MasterDetailsComponent.MyNode configurationNode) {
    configurationNode.add(new MasterDetailsComponent.MyNode(configurable.getDependenciesConfigurable()));
    configurationNode.add(new MasterDetailsComponent.MyNode(configurable.getCompilerOptionsConfigurable()));

    final FlexIdeBuildConfiguration configuration = configurable.getEditableObject();

    switch (configuration.TARGET_PLATFORM) {
      case Web:
        configurationNode.add(new MasterDetailsComponent.MyNode(configurable.getHtmlWrapperConfigurable()));
        break;
      case Desktop:
        configurationNode.add(new MasterDetailsComponent.MyNode(configurable.getAirDescriptorConfigurable()));
        configurationNode.add(new MasterDetailsComponent.MyNode(configurable.getAirDesktopPackagingConfigurable()));
        break;
      case Mobile:
        configurationNode.add(new MasterDetailsComponent.MyNode(configurable.getAirDescriptorConfigurable()));
        configurationNode.add(new MasterDetailsComponent.MyNode(configurable.getAndroidPackagingConfigurable()));
        configurationNode.add(new MasterDetailsComponent.MyNode(configurable.getIOSPackagingConfigurable()));
        break;
    }
  }

  public void moduleRemoved(final Module module) {
    myConfigurator.moduleRemoved(module);
  }

  public boolean isModified() {
    return myConfigurator.isModified();
  }

  @Override
  public boolean isModulesConfiguratorModified() {
    return myConfigurator.isModulesConfiguratorModified();
  }

  public void apply() throws ConfigurationException {
    myConfigurator.apply();
  }

  public void disposeUIResources() {
    myConfigurator.clearMaps();
  }

  public boolean canBeRemoved(final Object editableObject) {
    return editableObject instanceof FlexIdeBuildConfiguration &&
           myConfigurator.getBCCount(((FlexIdeBuildConfiguration)editableObject)) > 1;
  }

  public boolean removeObject(final Object editableObject) {
    if (editableObject instanceof FlexIdeBuildConfiguration) {
      myConfigurator.removeConfiguration(((FlexIdeBuildConfiguration)editableObject));
      return true;
    }
    return false;
  }

  public boolean canBeCopied(final NamedConfigurable configurable) {
    return configurable instanceof FlexIdeBCConfigurable;
  }

  public void copy(final NamedConfigurable configurable, final Runnable treeNodeNameUpdater) {
    if (configurable instanceof FlexIdeBCConfigurable) {
      myConfigurator.copy((FlexIdeBCConfigurable)configurable, treeNodeNameUpdater);
    }
  }

  public Collection<AnAction> createAddActions(final Computable<Object> selectedObjectRetriever,
                                               final Runnable treeNodeNameUpdater,
                                               final ModulesConfigurator modulesConfigurator) {
    final Collection<AnAction> actions = new ArrayList<AnAction>(2);
    actions.add(new Separator());
    actions.add(new DumbAwareAction("Build Configuration") {
      public void update(final AnActionEvent e) {
        final Object selectedObject = selectedObjectRetriever.compute();
        e.getPresentation().setVisible(selectedObject instanceof FlexIdeBuildConfiguration ||
                                       (selectedObject instanceof Module &&
                                        ModuleType.get((Module)selectedObject) instanceof FlexModuleType));
      }

      public void actionPerformed(final AnActionEvent e) {
        myConfigurator.addConfiguration(selectedObjectRetriever.compute(), treeNodeNameUpdater, modulesConfigurator);
      }
    });
    return actions;
  }
}
