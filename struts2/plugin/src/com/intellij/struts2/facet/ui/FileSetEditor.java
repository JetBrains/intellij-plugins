/*
 * Copyright 2010 The authors
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

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.struts2.StrutsBundle;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.EditorTextField;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.ui.tree.TreeModelAdapter;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.List;
import java.util.Set;

public class FileSetEditor extends DialogWrapper {

  private JPanel myMainPanel;
  private EditorTextField mySetName;
  private StrutsFilesTree myFilesTree;

  private final StrutsFileSet myFileSet;
  private final StrutsFileSet myOriginalSet;

  protected FileSetEditor(final Component parent,
                          final StrutsFileSet fileSet,
                          final FacetEditorContext context,
                          final StrutsConfigsSearcher searcher) {

    super(parent, true);

    setTitle(StrutsBundle.message("facet.fileset.editor.title"));
    myOriginalSet = fileSet;
    myFileSet = new StrutsFileSet(fileSet);

    final CheckedTreeNode myRoot = new CheckedTreeNode(null);
    myFilesTree.setModel(new DefaultTreeModel(myRoot));
    searcher.search();
    final MultiMap<Module,PsiFile> files = searcher.getFilesByModules();
    final MultiMap<VirtualFile, PsiFile> jars = searcher.getJars();
    final Set<PsiFile> psiFiles = myFilesTree.buildModuleNodes(files, jars, fileSet);

    final Project project = context.getProject();
    final PsiManager psiManager = PsiManager.getInstance(project);
    final List<VirtualFilePointer> list = fileSet.getFiles();
    for (final VirtualFilePointer pointer : list) {
      final VirtualFile file = pointer.getFile();
      if (file != null) {
        final PsiFile psiFile = psiManager.findFile(file);
        if (psiFile != null && psiFiles.contains(psiFile)) {
          continue;
        }
        myFilesTree.addFile(file);
      }
    }

    TreeUtil.expandAll(myFilesTree);
    myFilesTree.getModel().addTreeModelListener(new TreeModelAdapter() {
      public void treeNodesChanged(final TreeModelEvent e) {
        updateFileSet();
      }
    });

    mySetName.setText(fileSet.getName());
    mySetName.addDocumentListener(new DocumentAdapter() {
      public void documentChanged(final DocumentEvent e) {
        updateFileSet();
      }
    });

    init();

    getOKAction().setEnabled(fileSet.isNew());
  }

  @Nullable
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @NonNls
  protected String getDimensionServiceKey() {
    return "struts2 file set editor";
  }

  public boolean isOKActionEnabled() {
    if (myOriginalSet.isNew()) {
      return true;
    }

    final int selectedFilesCount = myFileSet.getFiles().size();
    if (selectedFilesCount == 0) {
      return false;
    }

    if (selectedFilesCount != myOriginalSet.getFiles().size()) {
      return true;
    }

    final List<VirtualFilePointer> pointers = myFileSet.getFiles();
    for (int i = 0; i < pointers.size(); i++) {
      if (!Comparing.equal(pointers.get(i).getUrl(), myOriginalSet.getFiles().get(i).getUrl())) {
        return true;
      }
    }

    return !Comparing.equal(myFileSet.getName(), myOriginalSet.getName());
  }

  protected void doOKAction() {
    updateFileSet();
    super.doOKAction();
  }

  private void updateFileSet() {
    myFileSet.setName(mySetName.getText());
    myFilesTree.updateFileSet(myFileSet);
    getOKAction().setEnabled(isOKActionEnabled());
  }

  public StrutsFileSet getEditedFileSet() {
    return myFileSet;
  }
}