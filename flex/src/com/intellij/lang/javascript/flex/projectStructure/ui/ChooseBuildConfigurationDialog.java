package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Condition;
import com.intellij.ui.*;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.Convertor;
import com.intellij.util.containers.HashMap;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * @author ksafonov
 */
public class ChooseBuildConfigurationDialog extends DialogWrapper {
  private final Map<Module, List<FlexIdeBCConfigurable>> myTreeItems;
  private Tree myTree;
  private DefaultMutableTreeNode[] mySelection;
  private JLabel myLabel;
  private JPanel myContentPane;
  private final boolean myAllowEmptySelection;

  /**
   * @param project
   * @param filter
   * @return <code>null</code> if there's no applicable BC configurables according to the filter provided
   */
  @Nullable
  public static ChooseBuildConfigurationDialog createForApplicableBCs(String title,
                                                                      @Nullable String labelText,
                                                                      Project project,
                                                                      boolean allowEmptySelection,
                                                                      Condition<FlexIdeBCConfigurable> filter) {
    Map<Module, List<FlexIdeBCConfigurable>> treeItems = new HashMap<Module, List<FlexIdeBCConfigurable>>();
    FlexIdeBCConfigurator configurator = FlexBuildConfigurationsExtension.getInstance().getConfigurator();
    for (Module module : ModuleStructureConfigurable.getInstance(project).getModules()) {
      if (ModuleType.get(module) != FlexModuleType.getInstance()) {
        continue;
      }
      for (CompositeConfigurable configurable : configurator.getBCConfigurables(module)) {
        FlexIdeBCConfigurable flexIdeBCConfigurable = FlexIdeBCConfigurable.unwrap(configurable);
        if (!filter.value(flexIdeBCConfigurable)) {
          continue;
        }

        List<FlexIdeBCConfigurable> list = treeItems.get(module);
        if (list == null) {
          list = new ArrayList<FlexIdeBCConfigurable>();
          treeItems.put(module, list);
        }
        list.add(flexIdeBCConfigurable);
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
                                         Map<Module, List<FlexIdeBCConfigurable>> treeItems) {
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
    List<Module> modules = new ArrayList<Module>(myTreeItems.keySet());
    Collections.sort(modules, new Comparator<Module>() {
      @Override
      public int compare(final Module o1, final Module o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
      }
    });

    for (Module module : modules) {
      DefaultMutableTreeNode moduleNode = new DefaultMutableTreeNode(module, true);
      root.add(moduleNode);
      List<FlexIdeBCConfigurable> bcs = myTreeItems.get(module);
      Collections.sort(bcs, new Comparator<FlexIdeBCConfigurable>() {
        @Override
        public int compare(final FlexIdeBCConfigurable o1, final FlexIdeBCConfigurable o2) {
          return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
        }
      });
      for (FlexIdeBCConfigurable bc : bcs) {
        DefaultMutableTreeNode bcNode = new DefaultMutableTreeNode(bc, false);
        moduleNode.add(bcNode);
      }
    }
    myTree.setModel(new DefaultTreeModel(root));
    myTree.setRootVisible(false);
    new TreeSpeedSearch(myTree, new Convertor<TreePath, String>() {
      @Override
      public String convert(TreePath o) {
        Object lastPathComponent = o.getLastPathComponent();
        return getText((DefaultMutableTreeNode)lastPathComponent);
      }
    }, true).setComparator(new SpeedSearchComparator(false));
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
      public void customizeCellRenderer(JTree tree,
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
          setIcon(ModuleType.get(module).getNodeIcon(expanded));
          append(module.getName());
        }
        else {
          FlexIdeBCConfigurable configurable = (FlexIdeBCConfigurable)object;
          setIcon(configurable.getIcon());
          BCUtils.renderBuildConfiguration(configurable.getEditableObject(), null).appendToComponent(this);
        }
      }
    });

    TreeUtil.expandAll(myTree);
    new DoubleClickListener() {
      @Override
      protected boolean onDoubleClick(MouseEvent e) {
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
    mySelection = myTree.getSelectedNodes(DefaultMutableTreeNode.class, new Tree.NodeFilter<DefaultMutableTreeNode>() {
      @Override
      public boolean accept(DefaultMutableTreeNode node) {
        return node.getUserObject() instanceof FlexIdeBCConfigurable;
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
      FlexIdeBCConfigurable configurable = (FlexIdeBCConfigurable)object;
      return configurable.getTreeNodeText();
    }
  }

  public FlexIdeBCConfigurable[] getSelectedConfigurables() {
    if (mySelection == null) {
      return new FlexIdeBCConfigurable[0];
    }

    return ContainerUtil.map2Array(mySelection, FlexIdeBCConfigurable.class, new Function<DefaultMutableTreeNode, FlexIdeBCConfigurable>() {
      @Override
      public FlexIdeBCConfigurable fun(DefaultMutableTreeNode node) {
        return (FlexIdeBCConfigurable)node.getUserObject();
      }
    });
  }
}
