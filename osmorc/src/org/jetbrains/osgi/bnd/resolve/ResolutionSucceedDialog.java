// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.bnd.resolve;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.tree.AbstractTreeModel;
import icons.OsmorcIdeaIcons;
import org.jetbrains.annotations.NotNull;
import org.osgi.namespace.contract.ContractNamespace;
import org.osgi.namespace.extender.ExtenderNamespace;
import org.osgi.namespace.implementation.ImplementationNamespace;
import org.osgi.namespace.service.ServiceNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static org.osmorc.i18n.OsmorcBundle.message;

class ResolutionSucceedDialog extends DialogWrapper {
  private static final String IDENTITY_NAMESPACE = "osgi.identity";
  private static final String PACKAGE_NAMESPACE = "osgi.wiring.package";
  private static final String CAPABILITY_VERSION_ATTRIBUTE = "version";

  private final Map<Resource, List<Wire>> myResolveResult;
  private Tree myTree;

  ResolutionSucceedDialog(Project project, Map<Resource, List<Wire>> resolveResult) {
    super(project);
    myResolveResult = resolveResult;
    init();
    setTitle(message("bnd.resolve.succeed.title"));
  }

  @Override
  protected String getDimensionServiceKey() {
    return "bnd.resolution.succeeded";
  }

  @Override
  protected JComponent createCenterPanel() {
    myTree = new Tree();
    myTree.setRootVisible(false);
    myTree.setModel(new MyTreeModel(myResolveResult));
    myTree.setCellRenderer(new MyTreeCellRenderer());
    return new JBScrollPane(myTree, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myTree;
  }

  private static class MyTreeModel extends AbstractTreeModel {
    private final Map<Resource, List<Wire>> myResolveResult;
    private final List<Resource> myResolveRoots;
    private final DefaultMutableTreeNode myRoot;

    MyTreeModel(Map<Resource, List<Wire>> resolveResult) {
      super();
      myResolveResult = resolveResult;
      myResolveRoots = resolveResult.keySet().stream().sorted().collect(Collectors.toList());
      myRoot = new DefaultMutableTreeNode(null);
    }

    @Override
    public Object getRoot() {
      return myRoot;
    }

    @Override
    public int getChildCount(Object parent) {
      return children(parent).size();
    }

    @Override
    public Object getChild(Object parent, int index) {
      return new DefaultMutableTreeNode(children(parent).get(index));
    }

    @Override
    public boolean isLeaf(Object node) {
      return children(node).isEmpty();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) { }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
      if (child instanceof DefaultMutableTreeNode) {
        Object object = ((DefaultMutableTreeNode)child).getUserObject();
        if (object instanceof Resource || object instanceof Wire) {
          return children(parent).indexOf(object);
        }
      }
      return -1;
    }

    private List<?> children(Object node) {
      List<?> result = null;
      if (node instanceof DefaultMutableTreeNode) {
        Object object = ((DefaultMutableTreeNode)node).getUserObject();
        if (object == null) {
          result = myResolveRoots;
        }
        else if (object instanceof Resource) {
          result = myResolveResult.get(object);
        }
        else if (object instanceof Wire) {
          result = myResolveResult.get(((Wire)object).getRequirer());
        }
      }
      return ObjectUtils.notNull(result, Collections.emptyList());
    }
  }

  private static class MyTreeCellRenderer extends ColoredTreeCellRenderer {
    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      Object userObject = ((DefaultMutableTreeNode)value).getUserObject();

      if (userObject instanceof Resource) {
        renderResource((Resource)userObject, this);
        setIcon(AllIcons.Nodes.PpLib);
      }
      else if (userObject instanceof Wire) {
        append("REQUIRED BY: ", SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
        renderResource(((Wire)userObject).getRequirer(), this);

        Capability capability = ((Wire)userObject).getCapability();
        if (capability != null) {
          String namespace = capability.getNamespace();

          append(" VIA: ", SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
          append(namespace, SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
          Map<String, Object> attributes = capability.getAttributes();
          append("=", SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
          append(String.valueOf(attributes.get(namespace)), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
          renderVersion(attributes, this);

          switch (namespace) {
            case PACKAGE_NAMESPACE:
              setIcon(AllIcons.Nodes.Package);
              break;
            case IDENTITY_NAMESPACE:
              setIcon(AllIcons.Nodes.PpLib);
              break;
            case ContractNamespace.CONTRACT_NAMESPACE:
            case ImplementationNamespace.IMPLEMENTATION_NAMESPACE:
            case ExtenderNamespace.EXTENDER_NAMESPACE:
            case ServiceNamespace.SERVICE_NAMESPACE:
              setIcon(OsmorcIdeaIcons.Osgi);
              break;
          }
        }
      }
      else {
        append(String.valueOf(userObject), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
        setIcon(null);
      }
    }

    private static void renderResource(Resource resource, SimpleColoredComponent renderer) {
      List<Capability> capabilities = resource.getCapabilities(IDENTITY_NAMESPACE);
      if (capabilities.size() == 1) {
        Map<String, Object> attributes = capabilities.get(0).getAttributes();
        Object identity = attributes.get(IDENTITY_NAMESPACE);
        renderer.append(String.valueOf(identity), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
        renderVersion(attributes, renderer);
      }
      else {
        renderer.append(resource.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
      }
    }

    private static void renderVersion(Map<String, Object> attributes, SimpleColoredComponent renderer) {
      Object version = attributes.get(CAPABILITY_VERSION_ATTRIBUTE);
      if (version != null) {
        renderer.append(", version ", SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
        renderer.append(version.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
      }
    }
  }
}