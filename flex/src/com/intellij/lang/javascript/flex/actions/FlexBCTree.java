package com.intellij.lang.javascript.flex.actions;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.util.CollectConsumer;
import com.intellij.util.Consumer;
import com.intellij.util.EventDispatcher;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeModel;
import java.util.Collection;
import java.util.Enumeration;

public class FlexBCTree extends CheckboxTree {

  private final Project myProject;
  private final EventDispatcher<ChangeListener> myDispatcher = EventDispatcher.create(ChangeListener.class);

  public FlexBCTree(final Project project) {
    this(project, Conditions.alwaysTrue());
  }

  public FlexBCTree(final Project project, final Condition<FlexBuildConfiguration> bcFilter) {
    super(createRenderer(), new CheckedTreeNode(project), new CheckPolicy(true, true, true, true));
    myProject = project;

    setRootVisible(true);
    setShowsRootHandles(false);

    final CheckedTreeNode rootNode = (CheckedTreeNode)getModel().getRoot();
    addNodes(rootNode, bcFilter);
    ((DefaultTreeModel)getModel()).reload(rootNode);

    TreeUtil.expandAll(this);
  }

  public void addToggleCheckBoxListener(final ChangeListener listener) {
    myDispatcher.addListener(listener);
  }

  @Override
  protected void onNodeStateChanged(CheckedTreeNode node) {
    myDispatcher.getMulticaster().stateChanged(new ChangeEvent(node));
  }

  public Collection<Pair<Module, FlexBuildConfiguration>> getSelectedBCs() {
    return getBCs(true);
  }

  public Collection<Pair<Module, FlexBuildConfiguration>> getDeselectedBCs() {
    return getBCs(false);
  }

  private Collection<Pair<Module, FlexBuildConfiguration>> getBCs(final boolean checked) {
    final CollectConsumer<Pair<Module, FlexBuildConfiguration>> consumer =
      new CollectConsumer<>();
    iterateRecursively((CheckedTreeNode)getModel().getRoot(), checked, consumer);
    return consumer.getResult();
  }

  public void setCheckedStatusForAll(final boolean checked) {
    final CheckedTreeNode node = (CheckedTreeNode)getModel().getRoot();
    node.setChecked(checked);
    setChildrenCheckedRecursively(node, checked);
  }

  private static void setChildrenCheckedRecursively(final CheckedTreeNode node, final boolean checked) {
    final Enumeration children = node.children();
    while (children.hasMoreElements()) {
      final CheckedTreeNode childNode = (CheckedTreeNode)children.nextElement();
      childNode.setChecked(checked);
      setChildrenCheckedRecursively(childNode, checked);
    }
  }

  public void setChecked(final String moduleName, final String bcName, final boolean checked) {
    final CheckedTreeNode node = getBCNode(moduleName, bcName);
    if (node != null) {
      node.setChecked(checked);
    }
  }

  private CheckedTreeNode getBCNode(final String moduleName, final String bcName) {
    final Enumeration moduleNodes = ((CheckedTreeNode)getModel().getRoot()).children();
    while (moduleNodes.hasMoreElements()) {
      final CheckedTreeNode moduleNode = (CheckedTreeNode)moduleNodes.nextElement();
      final Object userObject = moduleNode.getUserObject();

      if (userObject instanceof Module && ((Module)userObject).getName().equals(moduleName)) {
        final Enumeration bcNodes = moduleNode.children();
        while (bcNodes.hasMoreElements()) {
          final CheckedTreeNode bcNode = (CheckedTreeNode)bcNodes.nextElement();
          final Object bcUserObject = bcNode.getUserObject();
          if (bcUserObject instanceof FlexBuildConfiguration && ((FlexBuildConfiguration)bcUserObject).getName().equals(bcName)) {
            return bcNode;
          }
        }
        return null;
      }
    }
    return null;
  }

  public void selectRow(final Module module, final FlexBuildConfiguration bc) {
    clearSelection();

    final CheckedTreeNode node = getBCNode(module.getName(), bc.getName());
    if (node != null) {
      TreeUtil.selectInTree(node, true, this);
    }
  }

  private static void iterateRecursively(final CheckedTreeNode node,
                                         final boolean iterateChecked,
                                         final Consumer<Pair<Module, FlexBuildConfiguration>> consumer) {
    if (node.isLeaf()) {
      if (node.isChecked() == iterateChecked && node.getParent() instanceof CheckedTreeNode) {
        final Object userObject = node.getUserObject();
        final Object parentUserObject = ((CheckedTreeNode)node.getParent()).getUserObject();
        if (userObject instanceof FlexBuildConfiguration && parentUserObject instanceof Module) {
          consumer.consume(Pair.create((Module)parentUserObject, (FlexBuildConfiguration)userObject));
        }
      }
    }
    else {
      // do not try to optimize asking non-leaf node about its checked status - it may give unexpected result!
      final Enumeration children = node.children();
      while (children.hasMoreElements()) {
        iterateRecursively((CheckedTreeNode)children.nextElement(), iterateChecked, consumer);
      }
    }
  }

  private static CheckboxTreeCellRenderer createRenderer() {
    return new CheckboxTree.CheckboxTreeCellRenderer() {
      @Override
      public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (!(value instanceof CheckedTreeNode node)) return;

        final Object userObject = node.getUserObject();

        if (userObject instanceof Project) {
          getTextRenderer().append(((Project)userObject).getName());
        }
        else if (userObject instanceof Module) {
          getTextRenderer().setIcon(ModuleType.get((Module)userObject).getIcon());
          getTextRenderer().append(((Module)userObject).getName());
        }
        else if (userObject instanceof FlexBuildConfiguration) {
          BCUtils.renderBuildConfiguration((FlexBuildConfiguration)userObject, null, false).appendToComponent(getTextRenderer());
          getTextRenderer().setIcon(((FlexBuildConfiguration)userObject).getIcon());
        }
      }
    };
  }

  private void addNodes(final CheckedTreeNode rootNode, final Condition<FlexBuildConfiguration> bcFilter) {
    final FlexModuleType flexModuleType = FlexModuleType.getInstance();

    for (Module module : ModuleManager.getInstance(myProject).getModules()) {
      if (ModuleType.get(module) != flexModuleType) continue;

      final CheckedTreeNode moduleNode = new CheckedTreeNode(module);
      for (FlexBuildConfiguration bc : FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
        if (bcFilter.value(bc)) {
          moduleNode.add(new CheckedTreeNode(bc));
        }
      }

      if (moduleNode.getChildCount() > 0) {
        rootNode.add(moduleNode);
      }
    }
  }

  @Override
  protected void installSpeedSearch() {
    new TreeSpeedSearch(this, false, path -> {
      final CheckedTreeNode node = (CheckedTreeNode)path.getLastPathComponent();
      final Object userObject = node.getUserObject();
      if (userObject instanceof Project) {
        return ((Project)userObject).getName();
      }
      else if (userObject instanceof Module) {
        return ((Module)userObject).getName();
      }
      else if (userObject instanceof FlexBuildConfiguration) {
        return ((FlexBuildConfiguration)userObject).getName();
      }
      return null;
    });
  }
}
