/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.run.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.TreeUIHelper;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import icons.OsmorcIdeaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.osgi.jps.build.CachingBundleInfoProvider;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.osmorc.frameworkintegration.FrameworkInstanceManager.FrameworkBundleType;

/**
 * Dialog for selecting a bundle to be deployed.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public class BundleSelector extends DialogWrapper {
  private JPanel myContentPane;
  private SimpleTree myBundleTree;

  public BundleSelector(@NotNull Project project, @Nullable FrameworkInstanceDefinition instance, @NotNull List<SelectedBundle> selected) {
    super(project);

    setTitle(OsmorcBundle.message("bundle.selector.title"));
    setModal(true);

    myContentPane.setPreferredSize(JBUI.size(600, 400));
    myBundleTree.setModel(createModel(project, instance, selected));
    myBundleTree.setCellRenderer(new BundleTreeRenderer());
    myBundleTree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        setOKActionEnabled(myBundleTree.getSelectionCount() > 0);
      }
    });
    TreeUtil.expandAll(myBundleTree);
    TreeUIHelper.getInstance().installTreeSpeedSearch(myBundleTree);

    init();
  }

  private static TreeModel createModel(Project project, FrameworkInstanceDefinition instance, Collection<SelectedBundle> selectedList) {
    Set<SelectedBundle> selected = new HashSet<>(selectedList);
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();

    // all the modules
    DefaultMutableTreeNode moduleNode = new DefaultMutableTreeNode(OsmorcBundle.message("bundle.selector.group.modules"));
    Module[] modules = ModuleManager.getInstance(project).getModules();
    for (Module module : modules) {
      if (OsmorcFacet.hasOsmorcFacet(module)) {
        SelectedBundle bundle = new SelectedBundle(SelectedBundle.BundleType.Module, module.getName(), null);
        if (!selected.contains(bundle)) {
          moduleNode.add(new DefaultMutableTreeNode(bundle));
        }
      }
    }
    if (moduleNode.getChildCount() > 0) root.add(moduleNode);

    // all the framework bundles (if there are any)
    if (instance != null) {
      FrameworkIntegrator integrator = FrameworkIntegratorRegistry.getInstance().findIntegratorByInstanceDefinition(instance);
      if (integrator != null) {
        DefaultMutableTreeNode frameworkNode = new DefaultMutableTreeNode(OsmorcBundle.message("bundle.selector.group.framework"));
        for (SelectedBundle bundle : integrator.getFrameworkInstanceManager().getFrameworkBundles(instance, FrameworkBundleType.OTHER)) {
          if (!selected.contains(bundle)) {
            frameworkNode.add(new DefaultMutableTreeNode(bundle));
          }
        }
        if (frameworkNode.getChildCount() > 0) root.add(frameworkNode);
      }
    }

    // all the libraries that are bundles already (doesn't make much sense to start bundlified libs as they have no activator).
    DefaultMutableTreeNode libraryNode = new DefaultMutableTreeNode(OsmorcBundle.message("bundle.selector.group.libraries"));
    List<String> paths = OrderEnumerator.orderEntries(project)
      .librariesOnly()
      .productionOnly()
      .runtimeOnly()
      .classes()
      .getPathsList().getPathList();
    for (String path : paths) {
      String displayName = CachingBundleInfoProvider.getBundleSymbolicName(path);
      if (displayName != null) {
        SelectedBundle bundle = new SelectedBundle(SelectedBundle.BundleType.StartLibrary, displayName, path);
        if (!selected.contains(bundle)) {
          libraryNode.add(new DefaultMutableTreeNode(bundle));
        }
      }
    }
    if (libraryNode.getChildCount() > 0) root.add(libraryNode);

    return new DefaultTreeModel(root);
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return myContentPane;
  }

  @Override
  public @Nullable JComponent getPreferredFocusedComponent() {
    return myBundleTree;
  }

  public @NotNull List<SelectedBundle> getSelectedBundles() {
    TreePath[] paths = myBundleTree.getSelectionPaths();
    if (paths == null) return ContainerUtil.emptyList();

    List<SelectedBundle> bundles = new ArrayList<>(paths.length);
    for (TreePath path : paths) {
      Object last = path.getLastPathComponent();
      if (last instanceof DefaultMutableTreeNode) {
        Object object = ((DefaultMutableTreeNode)last).getUserObject();
        if (object instanceof SelectedBundle) {
          bundles.add((SelectedBundle)object);
        }
      }
    }
    return bundles;
  }


  private static class BundleTreeRenderer extends ColoredTreeCellRenderer {
    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      if (value instanceof DefaultMutableTreeNode) {
        Object object = ((DefaultMutableTreeNode)value).getUserObject();
        if (object instanceof SelectedBundle bundle) {
          if (bundle.isModule()) {
            setIcon(AllIcons.Nodes.Module);
          }
          else if (bundle.getBundleType() == SelectedBundle.BundleType.FrameworkBundle) {
            setIcon(OsmorcIdeaIcons.Osgi);
          }
          else {
            setIcon(AllIcons.Nodes.PpJar);
          }
        }
      }

      append(value.toString()); //NON-NLS
    }
  }
}
