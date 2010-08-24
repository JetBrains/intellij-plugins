/*
 * Copyright 2007 The authors
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
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.dom.struts.model.StrutsManager;
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
import java.awt.event.ActionEvent;
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

    setTitle(StrutsBundle.message("facet.fileseteditor.title"));
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

  protected Action[] createLeftSideActions() {
    final AbstractAction locateAction = new AbstractAction(StrutsBundle.message("facet.fileseteditor.button.locate.browse")) {
      public void actionPerformed(final ActionEvent e) {
        final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, true, true) {

          /**
           * Only show JARs, directories and valid struts.xml files.
           *
           * @param file File to check for visibility.
           * @param showHiddenFiles Flag from dialog.
           * @return true if above condition matches.
           */
          public boolean isFileVisible(final VirtualFile file, final boolean showHiddenFiles) {
            if (file.isDirectory() || file.getFileType() == StdFileTypes.ARCHIVE) {
              return true;
            }

            if (StdFileTypes.XML != file.getFileType()) {
              return false;
            }

            final Project project = DataKeys.PROJECT.getData(DataManager.getInstance().getDataContext());
            assert project != null;
            final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            return !(!(psiFile instanceof XmlFile) || !StrutsManager.getInstance(project).isStruts2ConfigFile((XmlFile) psiFile));
          }

          /**
           * Check selected file(s) for validity.
           *
           * @param files Selected files
           * @throws Exception If selected files contains at least one file which is not a valid struts.xml.
           */
          public void validateSelectedFiles(final VirtualFile[] files) throws Exception {
            final Project project = DataKeys.PROJECT.getData(DataManager.getInstance().getDataContext());
            assert project != null;
            final PsiManager psiManager = PsiManager.getInstance(project);
            for (final VirtualFile file : files) {
              final PsiFile psiFile = psiManager.findFile(file);
              if (!(psiFile instanceof XmlFile) || !StrutsManager.getInstance(project).isStruts2ConfigFile((XmlFile) psiFile)) {
                throw new Exception(file.getPresentableUrl() + " is not a valid struts.xml file");
              }
            }
          }
        };
        descriptor.setTitle(StrutsBundle.message("facet.fileseteditor.locate"));
        descriptor.setDescription(StrutsBundle.message("facet.fileseteditor.choose.files"));

        final VirtualFile[] files = FileChooser.chooseFiles(myMainPanel, descriptor);
        if (files.length > 0) {
          for (final VirtualFile file : files) {
            myFilesTree.addFile(file);
          }
          updateFileSet();
          TreeUtil.expandAll(myFilesTree);
        }
      }
    };
    locateAction.putValue(AbstractAction.SMALL_ICON, IconLoader.getIcon("/general/toolWindowFind.png"));
    return new Action[]{locateAction};
  }

  public StrutsFileSet getEditedFileSet() {
    return myFileSet;
  }
}