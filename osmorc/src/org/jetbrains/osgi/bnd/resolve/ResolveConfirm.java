/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.osgi.bnd.resolve;

import com.intellij.icons.AllIcons;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.tree.AbstractTreeModel;
import icons.OsmorcIdeaIcons;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.framework.namespace.PackageNamespace;
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

public class ResolveConfirm {
  private JPanel myContentPane;
  private JBList<Resource> myRequiredResources;
  private Tree myReason;

  private static final Icon OSGI_BUNDLE_ICON;
  static {
    LayeredIcon icon = new LayeredIcon(2);
    icon.setIcon(AllIcons.Nodes.PpLib, 0);
    Icon osgiSmall = new LayeredIcon(OsmorcIdeaIcons.Osgi).scale(0.5f);
    icon.setIcon(osgiSmall, 1, SwingConstants.SOUTH_EAST);
    OSGI_BUNDLE_ICON = icon;
  }

  public ResolveConfirm(Map<Resource, List<Wire>> resolveResult) {
    MyTreeModel reasonModel = new MyTreeModel(resolveResult);
    myReason.setModel(reasonModel);
    myReason.setCellRenderer(new MyTreeCellRenderer());

    List<Resource> resources = resolveResult.keySet().stream().sorted().collect(Collectors.toList());
    myRequiredResources.setModel(new CollectionListModel<>(resources));
    myRequiredResources.addListSelectionListener(event -> reasonModel.setRoot(myRequiredResources.getSelectedValue()));
    myRequiredResources.setCellRenderer(new MyListCellRenderer());
  }

  public JPanel getContentPane() {
    return myContentPane;
  }

  private static class MyTreeModel extends AbstractTreeModel {
    private final Map<Resource, List<Wire>> myResolveResult;
    private DefaultMutableTreeNode myRoot;

    public MyTreeModel(Map<Resource, List<Wire>> resolveResult) {
      super();
      myResolveResult = resolveResult;
      myRoot = new DefaultMutableTreeNode(null);
    }

    @Override
    public Object getRoot() {
      return myRoot;
    }

    public void setRoot(Resource resource) {
      myRoot = new DefaultMutableTreeNode(resource);
      treeStructureChanged(new TreePath(myRoot), ArrayUtil.EMPTY_INT_ARRAY, ArrayUtil.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public int getChildCount(Object parent) {
      return wires(parent).size();
    }

    @Override
    public Object getChild(Object parent, int index) {
      return new DefaultMutableTreeNode(wires(parent).get(index));
    }

    @Override
    public boolean isLeaf(Object node) {
      return wires(node).isEmpty();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) { }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
      if (child instanceof DefaultMutableTreeNode) {
        Object object = ((DefaultMutableTreeNode)child).getUserObject();
        if (object instanceof Wire) {
          return wires(parent).indexOf(object);
        }
      }
      return -1;
    }

    private List<Wire> wires(Object node) {
      List<Wire> wires = null;
      if (node instanceof DefaultMutableTreeNode) {
        Object object = ((DefaultMutableTreeNode)node).getUserObject();
        if (object instanceof Resource) {
          wires = myResolveResult.get(object);
        }
        else if (object instanceof Wire) {
          wires = myResolveResult.get(((Wire)object).getRequirer());
        }
      }
      return ObjectUtils.notNull(wires, Collections.emptyList());
    }
  }

  private static class MyListCellRenderer extends ColoredListCellRenderer<Resource> {
    @Override
    protected void customizeCellRenderer(@NotNull JList list, Resource resource, int index, boolean selected, boolean hasFocus) {
      renderResource(resource, this);
      setIcon(OSGI_BUNDLE_ICON);
    }
  }

  private static class MyTreeCellRenderer extends ColoredTreeCellRenderer {
    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      Object userObject = ((DefaultMutableTreeNode)value).getUserObject();

      Resource resource = null;
      Capability capability = null;
      if (userObject instanceof Resource) {
        resource = (Resource)userObject;
      }
      else if (userObject instanceof Wire) {
        resource = ((Wire)userObject).getRequirer();
        capability = ((Wire)userObject).getCapability();
      }

      if (resource != null) {
        append("REQUIRED BY: ", SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
        renderResource(resource, this);

        if (capability != null) {
          String namespace = capability.getNamespace();

          append(" VIA: ", SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
          append(namespace, SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
          Map<String, Object> attributes = capability.getAttributes();
          append("=", SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
          append(String.valueOf(attributes.get(namespace)), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
          renderVersion(attributes, this);

          switch (namespace) {
            case PackageNamespace.PACKAGE_NAMESPACE:
              setIcon(AllIcons.Nodes.Package);
              break;
            case IdentityNamespace.IDENTITY_NAMESPACE:
              setIcon(OSGI_BUNDLE_ICON);
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
  }

  private static void renderResource(Resource resource, SimpleColoredComponent renderer) {
    List<Capability> capabilities = resource.getCapabilities(IdentityNamespace.IDENTITY_NAMESPACE);
    if (capabilities.size() == 1) {
      Map<String, Object> attributes = capabilities.get(0).getAttributes();
      Object identity = attributes.get(IdentityNamespace.IDENTITY_NAMESPACE);
      renderer.append(String.valueOf(identity), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
      renderVersion(attributes, renderer);
    }
    else {
      renderer.append(resource.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
    }
  }

  private static void renderVersion(Map<String, Object> attributes, SimpleColoredComponent renderer) {
    Object version = attributes.get(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);
    if (version != null) {
      renderer.append(", version ", SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
      renderer.append(version.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
    }
  }
}