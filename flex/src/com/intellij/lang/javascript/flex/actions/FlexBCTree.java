package com.intellij.lang.javascript.flex.actions;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.util.CollectConsumer;
import com.intellij.util.Consumer;
import com.intellij.util.EventDispatcher;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.Collection;
import java.util.Enumeration;

public class FlexBCTree extends CheckboxTree {

  private final Project myProject;
  private final EventDispatcher<ChangeListener> myDispatcher = EventDispatcher.create(ChangeListener.class);

  public FlexBCTree(final Project project) {
    //noinspection unchecked
    this(project, Condition.TRUE);
  }

  public FlexBCTree(final Project project, final Condition<FlexIdeBuildConfiguration> bcFilter) {
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

  protected void checkNode(final CheckedTreeNode node, final boolean checked) {
    super.checkNode(node, checked);
    myDispatcher.getMulticaster().stateChanged(new ChangeEvent(node));
  }

  public boolean areAllSelected() {
    return ((CheckedTreeNode)getModel().getRoot()).isChecked();
  }

  public Collection<Pair<Module, FlexIdeBuildConfiguration>> getSelectedBCs() {
    final CollectConsumer<Pair<Module, FlexIdeBuildConfiguration>> consumer =
      new CollectConsumer<Pair<Module, FlexIdeBuildConfiguration>>();
    iterateRecursively((CheckedTreeNode)getModel().getRoot(), consumer);
    return consumer.getResult();
  }

  private static void iterateRecursively(final CheckedTreeNode node, final Consumer<Pair<Module, FlexIdeBuildConfiguration>> consumer) {
    if (node.isLeaf()) {
      if (node.isChecked() && node.getParent() instanceof CheckedTreeNode) {
        final Object userObject = node.getUserObject();
        final Object parentUserObject = ((CheckedTreeNode)node.getParent()).getUserObject();
        if (userObject instanceof FlexIdeBuildConfiguration && parentUserObject instanceof Module) {
          consumer.consume(Pair.create((Module)parentUserObject, (FlexIdeBuildConfiguration)userObject));
        }
      }
    }
    else {
      final Enumeration children = node.children();
      while (children.hasMoreElements()) {
        iterateRecursively((CheckedTreeNode)children.nextElement(), consumer);
      }
    }
  }

  private static CheckboxTreeCellRenderer createRenderer() {
    return new CheckboxTree.CheckboxTreeCellRenderer() {
      public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (!(value instanceof CheckedTreeNode)) return;

        final CheckedTreeNode node = (CheckedTreeNode)value;
        final Object userObject = node.getUserObject();

        if (userObject instanceof Project) {
          getTextRenderer().append(((Project)userObject).getName());
        }
        else if (userObject instanceof Module) {
          getTextRenderer().setIcon(ModuleType.get((Module)userObject).getNodeIcon(expanded));
          getTextRenderer().append(((Module)userObject).getName());
        }
        else if (userObject instanceof FlexIdeBuildConfiguration) {
          BCUtils.renderBuildConfiguration((FlexIdeBuildConfiguration)userObject, null, false).appendToComponent(getTextRenderer());
          getTextRenderer().setIcon(((FlexIdeBuildConfiguration)userObject).getIcon());
        }
      }
    };
  }

  private void addNodes(final CheckedTreeNode rootNode, final Condition<FlexIdeBuildConfiguration> bcFilter) {
    final FlexModuleType flexModuleType = FlexModuleType.getInstance();

    for (Module module : ModuleManager.getInstance(myProject).getModules()) {
      if (ModuleType.get(module) != flexModuleType) continue;

      final CheckedTreeNode moduleNode = new CheckedTreeNode(module);
      for (FlexIdeBuildConfiguration bc : FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
        if (bcFilter.value(bc)) {
          moduleNode.add(new CheckedTreeNode(bc));
        }
      }

      if (moduleNode.getChildCount() > 0) {
        rootNode.add(moduleNode);
      }
    }
  }

  protected void installSpeedSearch() {
    new TreeSpeedSearch(this, new Convertor<TreePath, String>() {
      public String convert(final TreePath path) {
        final CheckedTreeNode node = (CheckedTreeNode)path.getLastPathComponent();
        final Object userObject = node.getUserObject();
        if (userObject instanceof Project) {
          return ((Project)userObject).getName();
        }
        else if (userObject instanceof Module) {
          return ((Module)userObject).getName();
        }
        else if (userObject instanceof FlexIdeBuildConfiguration) {
          return ((FlexIdeBuildConfiguration)userObject).getName();
        }
        return null;
      }
    });
  }
}
