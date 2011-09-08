package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.ui.CompositeConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexIdeBCConfigurable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureExtension;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.NullableComputable;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FlexIdeModuleStructureExtension extends ModuleStructureExtension {

  final FlexIdeBCConfigurator myConfigurator;

  public FlexIdeModuleStructureExtension() {
    myConfigurator = new FlexIdeBCConfigurator();
  }

  public static FlexIdeModuleStructureExtension getInstance() {
    return ModuleStructureExtension.EP_NAME.findExtension(FlexIdeModuleStructureExtension.class);
  }

  public FlexIdeBCConfigurator getConfigurator() {
    return myConfigurator;
  }

  public void reset() {
    myConfigurator.reset();
  }

  public boolean addModuleNodeChildren(final Module module,
                                       final MasterDetailsComponent.MyNode moduleNode,
                                       final Runnable treeNodeNameUpdater) {
    if (!(ModuleType.get(module) instanceof FlexModuleType) || !FlexIdeUtils.isNewUI()) {
      return false;
    }

    final List<NamedConfigurable<FlexIdeBuildConfiguration>> configurables =
      myConfigurator.getOrCreateConfigurables(module, treeNodeNameUpdater);

    for (final NamedConfigurable<FlexIdeBuildConfiguration> configurable : configurables) {
      if (MasterDetailsComponent.findNodeByObject(moduleNode, configurable.getEditableObject()) == null) {
        final MasterDetailsComponent.MyNode configurationNode = new BuildConfigurationNode(configurable);
        addConfigurationChildNodes(FlexIdeBCConfigurable.unwrapIfNeeded(configurable), configurationNode);
        moduleNode.add(configurationNode);
      }
    }

    return configurables.size() > 0;
  }

  static void addConfigurationChildNodes(final FlexIdeBCConfigurable configurable, final MasterDetailsComponent.MyNode configurationNode) {
    if (!FlexIdeUtils.isFlatUi()) {
      List<NamedConfigurable> children = configurable.getChildren();
      for (NamedConfigurable child : children) {
        configurationNode.add(new MasterDetailsComponent.MyNode(child));
      }
    }
  }

  public void moduleRemoved(final Module module) {
    myConfigurator.moduleRemoved(module);
  }

  public boolean isModified() {
    if (!FlexIdeUtils.isNewUI()) return false;
    return myConfigurator.isModified();
  }

  public void apply() throws ConfigurationException {
    myConfigurator.apply();
  }

  public void disposeUIResources() {
    myConfigurator.dispose();
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
    if (FlexIdeUtils.isFlatUi()) {
      return configurable instanceof CompositeConfigurable;
    }
    else {
      return configurable instanceof FlexIdeBCConfigurable;
    }
  }

  public void copy(final NamedConfigurable configurable, final Runnable treeNodeNameUpdater) {
    FlexIdeBCConfigurable bcConfigurable = FlexIdeBCConfigurable.unwrapIfNeeded(configurable);
    myConfigurator.copy(bcConfigurable, treeNodeNameUpdater);
  }

  public Collection<AnAction> createAddActions(final NullableComputable<MasterDetailsComponent.MyNode> selectedNodeRetriever,
                                               final Runnable treeNodeNameUpdater) {
    final Collection<AnAction> actions = new ArrayList<AnAction>(2);
    actions.add(new Separator());
    actions.add(new DumbAwareAction("Build Configuration") {
      public void update(final AnActionEvent e) {
        e.getPresentation().setVisible(getModuleForNode(selectedNodeRetriever.compute()) != null);
      }

      public void actionPerformed(final AnActionEvent e) {
        final Module module = getModuleForNode(selectedNodeRetriever.compute());
        myConfigurator.addConfiguration(module, treeNodeNameUpdater);
      }
    });
    return actions;
  }

  @Nullable
  private static Module getModuleForNode(@Nullable MasterDetailsComponent.MyNode node) {
    while (node != null) {
      final NamedConfigurable configurable = node.getConfigurable();
      final Object editableObject = configurable == null ? null : configurable.getEditableObject();
      if (editableObject instanceof Module) {
        return (Module)editableObject;
      }
      final TreeNode parent = node.getParent();
      node = parent instanceof MasterDetailsComponent.MyNode ? (MasterDetailsComponent.MyNode)parent : null;
    }
    return null;
  }
}
