package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.ui.CompositeConfigurable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureExtension;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FlexIdeBuildConfigurationsExtension extends ModuleStructureExtension {

  final FlexIdeBCConfigurator myConfigurator;
  private static final Icon ICON = IconLoader.getIcon("buildConfig.png");

  public FlexIdeBuildConfigurationsExtension() {
    myConfigurator = new FlexIdeBCConfigurator();
  }

  public static FlexIdeBuildConfigurationsExtension getInstance() {
    return ModuleStructureExtension.EP_NAME.findExtension(FlexIdeBuildConfigurationsExtension.class);
  }

  public FlexIdeBCConfigurator getConfigurator() {
    return myConfigurator;
  }

  public void reset(Project project) {
    myConfigurator.reset(project);
  }

  public boolean addModuleNodeChildren(final Module module,
                                       final MasterDetailsComponent.MyNode moduleNode,
                                       final Runnable treeNodeNameUpdater) {
    if (!(ModuleType.get(module) instanceof FlexModuleType)) {
      return false;
    }

    final List<CompositeConfigurable> configurables = myConfigurator.getOrCreateConfigurables(module, treeNodeNameUpdater);
    for (final CompositeConfigurable configurable : configurables) {
      if (MasterDetailsComponent.findNodeByObject(moduleNode, configurable.getEditableObject()) == null) {
        moduleNode.add(new BuildConfigurationNode(configurable));
      }
    }

    return configurables.size() > 0;
  }

  public void moduleRemoved(final Module module) {
    myConfigurator.moduleRemoved(module);
  }

  public boolean isModified() {
    return myConfigurator.isModified();
  }

  public void apply() throws ConfigurationException {
    myConfigurator.apply();
  }

  public void disposeUIResources() {
    myConfigurator.dispose();
  }

  public boolean canBeRemoved(final Object[] editableObjects) {
    ModifiableFlexIdeBuildConfiguration[] configurations =
      ContainerUtil.mapNotNull(editableObjects, new Function<Object, ModifiableFlexIdeBuildConfiguration>() {
        @Override
        public ModifiableFlexIdeBuildConfiguration fun(Object o) {
          return o instanceof ModifiableFlexIdeBuildConfiguration ? (ModifiableFlexIdeBuildConfiguration)o : null;
        }
      }, new ModifiableFlexIdeBuildConfiguration[0]);
    return configurations.length == editableObjects.length && myConfigurator.canBeRemoved(configurations);
  }

  public boolean removeObject(final Object editableObject) {
    if (editableObject instanceof ModifiableFlexIdeBuildConfiguration) {
      myConfigurator.removeConfiguration(((ModifiableFlexIdeBuildConfiguration)editableObject));
      return true;
    }
    return false;
  }

  public boolean canBeCopied(final NamedConfigurable configurable) {
    return configurable instanceof CompositeConfigurable;
  }

  public void copy(final NamedConfigurable configurable, final Runnable treeNodeNameUpdater) {
    myConfigurator.copy(((CompositeConfigurable)configurable), treeNodeNameUpdater);
  }

  public Collection<AnAction> createAddActions(final NullableComputable<MasterDetailsComponent.MyNode> selectedNodeRetriever,
                                               final Runnable treeNodeNameUpdater,
                                               final Project project,
                                               final MasterDetailsComponent.MyNode root) {
    final Collection<AnAction> actions = new ArrayList<AnAction>(2);
    actions.add(new DumbAwareAction("Build Configuration", "Create Build Configuration", ICON) {
      public void update(final AnActionEvent e) {
        e.getPresentation().setVisible(getFlexModuleForNode(selectedNodeRetriever.compute()) != null);
      }

      public void actionPerformed(final AnActionEvent e) {
        final Module module = getFlexModuleForNode(selectedNodeRetriever.compute());
        myConfigurator.addConfiguration(module, treeNodeNameUpdater);
      }
    });
    return actions;
  }

  @Nullable
  private static Module getFlexModuleForNode(@Nullable MasterDetailsComponent.MyNode node) {
    while (node != null) {
      final NamedConfigurable configurable = node.getConfigurable();
      final Object editableObject = configurable == null ? null : configurable.getEditableObject();
      if (editableObject instanceof Module && ModuleType.get((Module)editableObject) instanceof FlexModuleType) {
        return (Module)editableObject;
      }
      final TreeNode parent = node.getParent();
      node = parent instanceof MasterDetailsComponent.MyNode ? (MasterDetailsComponent.MyNode)parent : null;
    }
    return null;
  }
}
