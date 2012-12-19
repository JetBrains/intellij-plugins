/*
 * Copyright 2011 The authors
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

package com.intellij.struts2.facet.ui;

import com.intellij.ide.presentation.VirtualFilePresentation;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.ui.*;
import com.intellij.util.containers.Convertor;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.*;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsFilesTree extends CheckboxTreeBase {

  private static final Comparator<PsiFile> FILE_COMPARATOR = new Comparator<PsiFile>() {
    public int compare(final PsiFile o1, final PsiFile o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };

  public StrutsFilesTree() {
    super(new CheckboxTreeCellRendererBase() {

      public void customizeRenderer(final JTree tree,
                                    final Object value,
                                    final boolean selected,
                                    final boolean expanded,
                                    final boolean leaf,
                                    final int row,
                                    final boolean hasFocus) {
        if (!(value instanceof DefaultMutableTreeNode)) return;
        final ColoredTreeCellRenderer renderer = getTextRenderer();
        final Object object = ((DefaultMutableTreeNode) value).getUserObject();
        if (object instanceof Module) {
          final Module module = (Module) object;
          final Icon icon = ModuleType.get(module).getIcon();
          renderer.setIcon(icon);
          final String moduleName = module.getName();
          renderer.append(moduleName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        } else if (object instanceof PsiFile) {
          final PsiFile psiFile = (PsiFile) object;
          final Icon icon = psiFile.getIcon(0);
          renderer.setIcon(icon);
          final String fileName = psiFile.getName();
          renderer.append(fileName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
          final VirtualFile virtualFile = psiFile.getVirtualFile();
          if (virtualFile != null) {
            String path = virtualFile.getPath();
            final int i = path.indexOf(JarFileSystem.JAR_SEPARATOR);
            if (i >= 0) {
              path = path.substring(i + JarFileSystem.JAR_SEPARATOR.length());
            }
            renderer.append(" (" + path + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
          }
        } else if (object instanceof VirtualFile) {
          final VirtualFile file = (VirtualFile) object;
          renderer.setIcon(VirtualFilePresentation.getIcon(file));
          renderer.append(file.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
          String path = file.getPath();
          final int i = path.indexOf(JarFileSystem.JAR_SEPARATOR);
          if (i >= 0) {
            path = path.substring(i + JarFileSystem.JAR_SEPARATOR.length());
          }
          renderer.append(" (" + path + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
        }
      }
    }, null);

    new TreeSpeedSearch(this, new Convertor<TreePath, String>() {
      public String convert(final TreePath treePath) {
        final Object object = ((CheckedTreeNode) treePath.getLastPathComponent()).getUserObject();
        if (object instanceof Module) {
          return ((Module) object).getName();
        } else if (object instanceof PsiFile) {
          return ((PsiFile) object).getName();
        } else if (object instanceof VirtualFile) {
          return ((VirtualFile) object).getName();
        } else {
          return "";
        }
      }
    });
  }

  public Set<PsiFile> buildModuleNodes(final MultiMap<Module, PsiFile> files,
                                       final MultiMap<VirtualFile, PsiFile> jars,
                                       final StrutsFileSet fileSet) {

    final CheckedTreeNode root = (CheckedTreeNode) getModel().getRoot();
    final HashSet<PsiFile> psiFiles = new HashSet<PsiFile>();
    final List<Module> modules = new ArrayList<Module>(files.keySet());
    Collections.sort(modules, new Comparator<Module>() {
      public int compare(final Module o1, final Module o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });

    for (final Module module : modules) {
      final CheckedTreeNode moduleNode = new CheckedTreeNode(module);
      moduleNode.setChecked(false);
      root.add(moduleNode);
      if (files.containsKey(module)) {
        final List<PsiFile> moduleFiles = new ArrayList<PsiFile>(files.get(module));
        Collections.sort(moduleFiles, FILE_COMPARATOR);
        for (final PsiFile file : moduleFiles) {
          final CheckedTreeNode fileNode = createFileNode(file, fileSet);
          moduleNode.add(fileNode);
          psiFiles.add(file);
        }
      }
    }

    for (final VirtualFile file : jars.keySet()) {
      final List<PsiFile> list = new ArrayList<PsiFile>(jars.get(file));
      final PsiFile jar = list.get(0).getManager().findFile(file);
      if (jar != null) {
        final CheckedTreeNode jarNode = new CheckedTreeNode(jar);
        jarNode.setChecked(false);
        root.add(jarNode);
        Collections.sort(list, FILE_COMPARATOR);
        for (final PsiFile psiFile : list) {
          final CheckedTreeNode vfNode = createFileNode(psiFile, fileSet);
          jarNode.add(vfNode);
          psiFiles.add(psiFile);
        }
      }
    }
    return psiFiles;
  }

  public void updateFileSet(final StrutsFileSet fileSet) {
    final Set<VirtualFile> configured = new HashSet<VirtualFile>();
    TreeUtil.traverse((TreeNode) getModel().getRoot(), new TreeUtil.Traverse() {
      public boolean accept(final Object node) {
        final CheckedTreeNode checkedTreeNode = (CheckedTreeNode) node;
        if (!checkedTreeNode.isChecked()) {
          return true;
        }
        final Object object = checkedTreeNode.getUserObject();
        VirtualFile virtualFile = null;
        if (object instanceof XmlFile) {
          virtualFile = ((XmlFile) object).getVirtualFile();
        } else if (object instanceof VirtualFile) {
          virtualFile = (VirtualFile) object;
        }
        if (virtualFile != null) {
          if (!fileSet.hasFile(virtualFile)) {
            fileSet.addFile(virtualFile);
          }
          configured.add(virtualFile);
        }
        return true;
      }
    });

    for (Iterator<VirtualFilePointer> i = fileSet.getFiles().iterator(); i.hasNext(); ) {
      final VirtualFilePointer pointer = i.next();
      final VirtualFile file = pointer.getFile();
      if (file == null || !configured.contains(file)) {
        i.remove();
      }
    }
  }

  private static CheckedTreeNode createFileNode(final PsiFile file, final StrutsFileSet fileSet) {
    final CheckedTreeNode fileNode = new CheckedTreeNode(file);
    fileNode.setChecked(fileSet.hasFile(file.getVirtualFile()));
    return fileNode;
  }

  public void addFile(final VirtualFile file) {
    final CheckedTreeNode root = (CheckedTreeNode) getModel().getRoot();
    final CheckedTreeNode treeNode = new CheckedTreeNode(file);
    root.add(treeNode);
    final DefaultTreeModel model = (DefaultTreeModel) getModel();
    model.nodeStructureChanged(root);
  }

}
