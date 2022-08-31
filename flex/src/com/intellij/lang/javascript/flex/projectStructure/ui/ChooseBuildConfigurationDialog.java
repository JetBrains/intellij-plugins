// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Condition;
import com.intellij.ui.*;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ksafonov
 */
public final class ChooseBuildConfigurationDialog extends DialogWrapper {
  private final Map<Module, List<FlexBCConfigurable>> myTreeItems;
  private Tree myTree;
  private DefaultMutableTreeNode[] mySelection;
  private JLabel myLabel;
  private JPanel myContentPane;
  private final boolean myAllowEmptySelection;

  /**
   * @return {@code null} if there's no applicable BC configurables according to the filter provided
   */
  @Nullable
  public static ChooseBuildConfigurationDialog createForApplicableBCs(String title,
                                                                      @Nullable String labelText,
                                                                      Project project,
                                                                      boolean allowEmptySelection,
                                                                      Condition<? super FlexBCConfigurable> filter) {
    Map<Module, List<FlexBCConfigurable>> treeItems = new HashMap<>();
    FlexBCConfigurator configurator = FlexBuildConfigurationsExtension.getInstance().getConfigurator();
    for (Module module : ProjectStructureConfigurable.getInstance(project).getModulesConfig().getModules()) {
      if (ModuleType.get(module) != FlexModuleType.getInstance()) {
        continue;
      }
      for (CompositeConfigurable configurable : configurator.getBCConfigurables(module)) {
        FlexBCConfigurable flexBCConfigurable = FlexBCConfigurable.unwrap(configurable);
        if (!filter.value(flexBCConfigurable)) {
          continue;
        }

        List<FlexBCConfigurable> list = treeItems.get(module);
        if (list == null) {
          list = new ArrayList<>();
          treeItems.put(module, list);
        }
        list.add(flexBCConfigurable);
      }
    }

    if (treeItems.isEmpty()) {
      return null;
    }

    return new ChooseBuildConfigurationDialog(title, labelText, project, allowEmptySelection, treeItems);
  }

  private ChooseBuildConfigurationDialog(String title,
                                         @Nullable final String labelText,
                                         Project project,
                                         final boolean allowEmptySelection,
                                         Map<Module, List<FlexBCConfigurable>> treeItems) {
    super(project, true);
    myAllowEmptySelection = allowEmptySelection;
    if (labelText != null) {
      myLabel.setText(labelText);
    }
    else {
      myLabel.setVisible(false);
    }
    myTreeItems = treeItems;
    setTitle(title);
    init();
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myTree;
  }

  @Override
  protected String getDimensionServiceKey() {
    return getClass().getName();
  }

  @Override
  protected JComponent createCenterPanel() {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    List<Module> modules = new ArrayList<>(myTreeItems.keySet());
    modules.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));

    for (Module module : modules) {
      DefaultMutableTreeNode moduleNode = new DefaultMutableTreeNode(module, true);
      root.add(moduleNode);
      List<FlexBCConfigurable> bcs = myTreeItems.get(module);
      bcs.sort((o1, o2) -> o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName()));
      for (FlexBCConfigurable bc : bcs) {
        DefaultMutableTreeNode bcNode = new DefaultMutableTreeNode(bc, false);
        moduleNode.add(bcNode);
      }
    }
    myTree.setModel(new DefaultTreeModel(root));
    myTree.setRootVisible(false);
    new TreeSpeedSearch(myTree, true, o -> {
      Object lastPathComponent = o.getLastPathComponent();
      return getText((DefaultMutableTreeNode)lastPathComponent);
    }).setComparator(new SpeedSearchComparator(false));
    TreeUIHelper.getInstance().installTreeSpeedSearch(myTree);
    myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    myTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        updateOnSelectionChange();
      }
    });
    myTree.setCellRenderer(new ColoredTreeCellRenderer() {
      @Override
      public void customizeCellRenderer(@NotNull JTree tree,
                                        Object value,
                                        boolean selected,
                                        boolean expanded,
                                        boolean leaf,
                                        int row,
                                        boolean hasFocus) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;
        Object object = treeNode.getUserObject();
        if (object instanceof Module) {
          Module module = (Module)object;
          setIcon(ModuleType.get(module).getIcon());
          append(module.getName());
        }
        else if (object instanceof FlexBCConfigurable) {
          FlexBCConfigurable configurable = (FlexBCConfigurable)object;
          setIcon(configurable.getIcon());
          BCUtils.renderBuildConfiguration(configurable.getEditableObject(), null).appendToComponent(this);
        }
      }
    });

    TreeUtil.expandAll(myTree);
    new DoubleClickListener() {
      @Override
      protected boolean onDoubleClick(@NotNull MouseEvent e) {
        if (mySelection != null) {
          doOKAction();
          return true;
        }
        return false;
      }
    }.installOn(myTree);

    myTree.addKeyListener(new KeyAdapter() {
      @Override
      public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_ENTER) {
          doOKAction();
        }
      }
    });

    updateOnSelectionChange();
    return myContentPane;
  }

  private void updateOnSelectionChange() {
    mySelection = myTree.getSelectedNodes(DefaultMutableTreeNode.class, new Tree.NodeFilter<>() {
      @Override
      public boolean accept(DefaultMutableTreeNode node) {
        return node.getUserObject() instanceof FlexBCConfigurable;
      }
    });
    setOKActionEnabled(myAllowEmptySelection || mySelection.length > 0);
  }

  private static String getText(DefaultMutableTreeNode node) {
    Object object = node.getUserObject();
    if (object instanceof Module) {
      Module module = (Module)object;
      return module.getName();
    }
    else {
      FlexBCConfigurable configurable = (FlexBCConfigurable)object;
      return configurable.getTreeNodeText();
    }
  }

  public FlexBCConfigurable[] getSelectedConfigurables() {
    if (mySelection == null) {
      return new FlexBCConfigurable[0];
    }

    return ContainerUtil.map2Array(mySelection, FlexBCConfigurable.class, node -> (FlexBCConfigurable)node.getUserObject());
  }
}
