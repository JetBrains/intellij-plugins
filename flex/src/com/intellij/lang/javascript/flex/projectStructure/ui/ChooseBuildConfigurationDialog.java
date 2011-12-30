package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.*;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

/**
 * @author ksafonov
 */
public class ChooseBuildConfigurationDialog extends DialogWrapper {
  private final Map<Module, List<FlexIdeBCConfigurable>> myTreeItems;
  private Tree myTree;
  private DefaultMutableTreeNode[] mySelection;

  public ChooseBuildConfigurationDialog(Project project, Map<Module, List<FlexIdeBCConfigurable>> treeItems) {
    super(project, true);
    myTreeItems = treeItems;
    setTitle("Add Dependency");
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
    for (Module module : myTreeItems.keySet()) {
      DefaultMutableTreeNode moduleNode = new DefaultMutableTreeNode(module, true);
      root.add(moduleNode);
      for (FlexIdeBCConfigurable bc : myTreeItems.get(module)) {
        DefaultMutableTreeNode bcNode = new DefaultMutableTreeNode(bc, false);
        moduleNode.add(bcNode);
      }
    }
    myTree = new Tree(new DefaultTreeModel(root));
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
        mySelection = myTree.getSelectedNodes(DefaultMutableTreeNode.class, new Tree.NodeFilter<DefaultMutableTreeNode>() {
          @Override
          public boolean accept(DefaultMutableTreeNode node) {
            return node.getUserObject() instanceof FlexIdeBCConfigurable;
          }
        });
        setOKActionEnabled(mySelection.length > 0);
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
        append(getText(treeNode));
        Object object = treeNode.getUserObject();
        if (object instanceof Module) {
          setIcon(ModuleType.get((Module)object).getNodeIcon(expanded));
        }
        else {
          setIcon(((FlexIdeBCConfigurable)object).getIcon());
        }
      }
    });

    JPanel p = new JPanel(new BorderLayout());
    p.setPreferredSize(new Dimension(400, 430));
    p.add(ScrollPaneFactory.createScrollPane(myTree), BorderLayout.CENTER);
    TreeUtil.expandAll(myTree);
    myTree.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          doOKAction();
        }
      }
    });
    myTree.addKeyListener(new KeyAdapter() {
      @Override
      public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_ENTER) {
          doOKAction();
        }
      }
    });
    return p;
  }

  private static String getText(DefaultMutableTreeNode node) {
    Object object = node.getUserObject();
    if (object instanceof Module) {
      Module module = (Module)object;
      return module.getName();
    }
    else if (object instanceof FlexIdeBCConfigurable) {
      FlexIdeBCConfigurable configurable = (FlexIdeBCConfigurable)object;
      return configurable.getTreeNodeText();
    }
    else {
      return null;
    }
  }

  public FlexIdeBCConfigurable[] getSelectedConfigurables() {
    return ContainerUtil.map2Array(mySelection, FlexIdeBCConfigurable.class, new Function<DefaultMutableTreeNode, FlexIdeBCConfigurable>() {
      @Override
      public FlexIdeBCConfigurable fun(DefaultMutableTreeNode node) {
        return (FlexIdeBCConfigurable)node.getUserObject();
      }
    });
  }
}
