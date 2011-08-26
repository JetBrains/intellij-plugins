package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ksafonov
 */
public class ChooseBuildConfigurationDialog extends DialogWrapper {
  private final Project myProject;
  private SimpleTree myTree;
  private SimpleTreeBuilder myTreeBuilder;
  private final Map<Module, List<FlexIdeBCConfigurable>> myTreeItems;
  private Set<BCNode> mySelection;

  public ChooseBuildConfigurationDialog(Project project, Map<Module, List<FlexIdeBCConfigurable>> treeItems) {
    super(project, true);
    myProject = project;
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
    SimpleNode root = new RootNode(myProject);
    final DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
    myTree = new SimpleTree(treeModel);
    myTree.setRootVisible(false);
    myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    SimpleTreeStructure s = new SimpleTreeStructure.Impl(root);
    myTreeBuilder = new SimpleTreeBuilder(myTree, treeModel, s, null);
    Disposer.register(getDisposable(), myTreeBuilder);
    myTree.setModel(treeModel);
    myTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        mySelection = myTreeBuilder.getSelectedElements(BCNode.class);
        setOKActionEnabled(!mySelection.isEmpty());
      }
    });

    JPanel p = new JPanel(new BorderLayout());
    p.add(ScrollPaneFactory.createScrollPane(myTree), BorderLayout.CENTER);
    myTreeBuilder.expandAll(null);
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

  public FlexIdeBCConfigurable[] getSelectedConfigurables() {
    return ContainerUtil.map2Array(mySelection, FlexIdeBCConfigurable.class, new Function<BCNode, FlexIdeBCConfigurable>() {
      @Override
      public FlexIdeBCConfigurable fun(BCNode bcNode) {
        return bcNode.configurable;
      }
    });
  }

  private class RootNode extends SimpleNode {
    public RootNode(Project project) {
      super(project);
    }

    @Override
    public SimpleNode[] getChildren() {
      return ContainerUtil.map2Array(myTreeItems.keySet(), SimpleNode.class, new Function<Module, SimpleNode>() {
        @Override
        public SimpleNode fun(Module module) {
          return new ModuleNode(module);
        }
      });
    }
  }

  private class ModuleNode extends SimpleNode {

    private final Module module;

    public ModuleNode(Module module) {
      this.module = module;
      myName = module.getName();
      myOpenIcon = ModuleType.get(module).getNodeIcon(true);
      myClosedIcon = ModuleType.get(module).getNodeIcon(false);
    }

    @Override
    public boolean isAutoExpandNode() {
      return true;
    }

    @Override
    public SimpleNode[] getChildren() {
      return ContainerUtil
        .map2Array(myTreeItems.get(module), BCNode.class, new Function<FlexIdeBCConfigurable, BCNode>() {
          @Override
          public BCNode fun(FlexIdeBCConfigurable configurable) {
            return new BCNode(configurable);
          }
        });
    }
  }

  private static class BCNode extends SimpleNode {

    private final FlexIdeBCConfigurable configurable;

    public BCNode(FlexIdeBCConfigurable bc) {
      this.configurable = bc;
      myName = configurable.getTreeNodeText();
      myOpenIcon = myClosedIcon = configurable.getIcon();
    }

    @Override
    public SimpleNode[] getChildren() {
      return NO_CHILDREN;
    }

    @Override
    public boolean isAlwaysLeaf() {
      return true;
    }
  }
}
