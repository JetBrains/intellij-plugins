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
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBList;
import com.intellij.ui.treeStructure.Tree;
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
import javax.swing.tree.DefaultTreeModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    DefaultListModel<Resource> requiredResourcesModel = new DefaultListModel<>();
    resolveResult.keySet().stream()
      .sorted()
      .forEach(requiredResourcesModel::addElement);

    DefaultTreeModel reasonModel = new DefaultTreeModel(new DefaultMutableTreeNode());
    myReason.setModel(reasonModel);

    myRequiredResources.setModel(requiredResourcesModel);

    myRequiredResources.addListSelectionListener(event -> {
      Resource selectedResource = myRequiredResources.getSelectedValue();
      updateReasonModel(resolveResult, reasonModel, selectedResource);
    });

    myRequiredResources.setCellRenderer(new ColoredListCellRenderer<Resource>() {
      @Override
      protected void customizeCellRenderer(@NotNull JList list, Resource resource, int index, boolean selected, boolean hasFocus) {
        setIcon(OSGI_BUNDLE_ICON);

        List<Capability> capabilities = resource.getCapabilities(IdentityNamespace.IDENTITY_NAMESPACE);

        if (capabilities.size() == 1) {
          Capability capability = capabilities.get(0);
          String identity = Objects.toString(capability.getAttributes().get(IdentityNamespace.IDENTITY_NAMESPACE));
          Object version = capability.getAttributes().get(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);

          append(identity, SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
          if (version != null) {
            append(", version ", SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
            append(version.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
          }
        }
        else {
          append(resource.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
        }
      }
    });

    myReason.setCellRenderer(new ColoredTreeCellRenderer() {
      @Override
      public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

        Object userObject = node.getUserObject();

        if (userObject instanceof Capability) {
          Capability capability = (Capability)userObject;

          String identity = Objects.toString(capability.getAttributes().get(capability.getNamespace()));
          Object version = capability.getAttributes().get(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);

          append(((Capability)userObject).getNamespace(), SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
          append("=", SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
          append(identity, SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
          if (version != null) {
            append(", version ", SimpleTextAttributes.GRAYED_ATTRIBUTES , true);
            append(version.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
          }

          switch (capability.getNamespace()) {
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
        else if (userObject instanceof Resource) {
          Resource resource = (Resource)userObject;

          setIcon(null);
          List<Capability> list = resource.getCapabilities(IdentityNamespace.IDENTITY_NAMESPACE);

          if (list.size() == 1) {
            Capability capability = list.get(0);
            String identity = Objects.toString(capability.getAttributes().get(IdentityNamespace.IDENTITY_NAMESPACE));
            Object version = capability.getAttributes().get(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);


            append("REQUIRED BY: ", SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
            append(identity, SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
            if (version != null) {
              append(", version ", SimpleTextAttributes.GRAYED_ATTRIBUTES , true);
              append(version.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
            }

          }
          else {
            append("REQUIRED BY: " + resource.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
          }
        }
        else {
          setIcon(null);
          append(Objects.toString(userObject), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
        }
      }
    });
  }

  private static void updateReasonModel(Map<Resource, List<Wire>> resolve, DefaultTreeModel reasonModel, Resource selectedResource) {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    if (selectedResource != null) {
      addRequirer(root, selectedResource, resolve);
    }
    reasonModel.setRoot(root);
  }

  private static void addRequirer(DefaultMutableTreeNode root, Resource resource, Map<Resource, List<Wire>> resolve) {
    List<Wire> wires = resolve.get(resource);
    if (wires == null) return;

    Map<Capability, DefaultMutableTreeNode> map = new HashMap<>();

    wires.forEach(wire -> {
      DefaultMutableTreeNode requirement = map.computeIfAbsent(wire.getCapability(), DefaultMutableTreeNode::new);
      DefaultMutableTreeNode child = new DefaultMutableTreeNode(wire.getRequirer());
      requirement.add(child);

      addRequirer(child, wire.getRequirer(), resolve);
    });
    map.values().forEach(root::add);
  }

  public JPanel getContentPane() {
    return myContentPane;
  }
}