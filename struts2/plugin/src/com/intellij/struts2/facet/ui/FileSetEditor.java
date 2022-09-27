/*
 * Copyright 2013 The authors
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
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.struts2.StrutsBundle;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.tree.TreeModelAdapter;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.xml.config.ConfigFilesTreeBuilder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FileSetEditor extends DialogWrapper {

  private JPanel myMainPanel;
  private JTextField mySetName;
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

    ConfigFilesTreeBuilder builder = new ConfigFilesTreeBuilder(myFilesTree) {
      @Override
      protected DefaultMutableTreeNode createFileNode(Object file) {
        CheckedTreeNode node = new CheckedTreeNode(file);
        if (file instanceof PsiFile && myFileSet.hasFile(((PsiFile)file).getVirtualFile()) ||
            file instanceof VirtualFile && myFileSet.hasFile((VirtualFile)file)) {
          node.setChecked(true);
        }
        else {
          node.setChecked(false);
        }
        return node;
      }
    };

    final CheckedTreeNode myRoot = new CheckedTreeNode(null);
    myFilesTree.setModel(new DefaultTreeModel(myRoot));

    searcher.search();
    Set<PsiFile> psiFiles = builder.buildTree(myRoot, searcher);

    final PsiManager psiManager = PsiManager.getInstance(context.getProject());
    final List<VirtualFilePointer> list = fileSet.getFiles();
    for (VirtualFilePointer pointer : list) {
      final VirtualFile file = pointer.getFile();
      if (file != null) {
        final PsiFile psiFile = psiManager.findFile(file);
        if (psiFile != null && psiFiles.contains(psiFile)) {
          continue;
        }
        builder.addFile(file);
      }
    }

    TreeUtil.expandAll(myFilesTree);
    myFilesTree.getModel().addTreeModelListener(new TreeModelAdapter() {
      @Override
      public void treeNodesChanged(final TreeModelEvent e) {
        updateFileSet();
      }
    });

    mySetName.setText(fileSet.getName());
    mySetName.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull DocumentEvent e) {
        updateFileSet();
      }
    });

    init();

    getOKAction().setEnabled(fileSet.isNew());
  }

  @Override
  @Nullable
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Override
  @NonNls
  protected String getDimensionServiceKey() {
    return "struts2 file set editor";
  }

  @Override
  public boolean isOKActionEnabled() {
    if (myOriginalSet.isNew()) {
      return true;
    }

    if (StringUtil.isEmptyOrSpaces(mySetName.getText())) {
      return false;
    }
    if (!Objects.equals(myFileSet.getName(), myOriginalSet.getName())) {
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
      if (!Objects.equals(pointers.get(i).getUrl(), myOriginalSet.getFiles().get(i).getUrl())) {
        return true;
      }
    }

    return false;
  }

  @Override
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