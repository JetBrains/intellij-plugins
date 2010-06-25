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

package org.osmorc.facet.ui;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import com.intellij.util.io.FileTypeFilter;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileView;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * The facet editor tab which is used to set up Osmorc facet settings concerning the bundle JAR created by Osmorc.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcFacetJAREditorTab extends FacetEditorTab {
  public OsmorcFacetJAREditorTab(FacetEditorContext editorContext) {
    _editorContext = editorContext;
    final Project project = editorContext.getProject();

    _ignoreFilePatternTextField = new EditorTextField("", project, FileTypes.PLAIN_TEXT);
    FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName("*.regexp");
    if (fileType == FileTypes.UNKNOWN) {
      fileType = FileTypeManager.getInstance().getFileTypeByFileName("*.txt"); // RegExp plugin is not installed
    }

    final PsiFile file =
      PsiFileFactory.getInstance(project).createFileFromText("*.regexp", fileType, _ignoreFilePatternTextField.getText(), -1, true);
    _ignoreFilePatternTextField.setNewDocumentAndFileType(fileType, PsiDocumentManager.getInstance(project).getDocument(file));
    _ignoreFilePatternPanel.add(_ignoreFilePatternTextField, BorderLayout.CENTER);

    UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.addUserActivityListener(new UserActivityListener() {
      public void stateChanged() {
        _modified = true;
      }
    });

    watcher.register(_root);
    _jarFileChooser.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onJarFileSelect();
      }
    });

    _additionalJARContentsTableModel = new AdditionalJARContentsTableModel();
    _additionalJARContentsTable.setModel(_additionalJARContentsTableModel);

    TableColumn col = _additionalJARContentsTable.getColumnModel().getColumn(0);
    final FileSelectorTableCellEditor selectorTableCellEditor = new FileSelectorTableCellEditor(project, _editorContext.getModule());
    col.setCellEditor(selectorTableCellEditor);
    selectorTableCellEditor.addCellEditorListener(new CellEditorListener() {
      public void editingCanceled(ChangeEvent e) {
      }

      public void editingStopped(ChangeEvent e) {
        // ok we finished editing the left, now get the stuff from there, calculate a destination name and edit the other cell
        int row = _additionalJARContentsTable.getSelectedRow();
        if (row > -1) {
          Pair<String, String> additionalJARContent = _additionalJARContentsTableModel.getAdditionalJARContent(row);
          VirtualFile preselectedPath = LocalFileSystem.getInstance().findFileByPath(additionalJARContent.getFirst());
          String destinationName;
          if (preselectedPath != null) {
            destinationName = determineMostLikelyLocationInJAR(preselectedPath);
          }
          else {
            destinationName = "";
          }

          _additionalJARContentsTableModel.changeAdditionalJARConent(row, additionalJARContent.first, destinationName);
          _additionalJARContentsTable.editCellAt(row, 1);
          _additionalJARContentsTable.getEditorComponent().requestFocus();

        }
      }
    });

    _additionalJARContentsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        _removeButton.setEnabled(_additionalJARContentsTable.getSelectedRowCount() > 0);
        _editButton.setEnabled(_additionalJARContentsTable.getSelectedRowCount() > 0);
      }
    });


    _addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onAddAdditionalJARContent();
      }
    });

    _removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onRemoveAdditionalJARContent();
      }
    });

    _editButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onEditAdditionalJARContent();
      }
    });

    _editButton.setEnabled(false);
    _removeButton.setEnabled(false);
  }

  private void onEditAdditionalJARContent() {
    int row = _additionalJARContentsTable.getSelectedRow();
    if (row > -1) {
      Pair<String, String> additionalJARContent = _additionalJARContentsTableModel.getAdditionalJARContent(row);
      Project project = _editorContext.getProject();
      FileChooserDescriptor descriptor = new FileChooserDescriptor(true, true, true, true, false, false);
      descriptor.setTitle("Choose source file or folder");
      FileChooserDialog fileChooserDialog = FileChooserFactory.getInstance().createFileChooser(descriptor, project);
      VirtualFile preselectedPath = LocalFileSystem.getInstance().findFileByPath(additionalJARContent.getFirst());
      if (preselectedPath == null) {
        Module module = _editorContext.getModule();
        VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
        if (contentRoots.length > 0) {
          for (VirtualFile contentRoot : contentRoots) {
            VirtualFile path = VfsUtil.findRelativeFile(additionalJARContent.getFirst(), contentRoot);
            if (path != null) {
              preselectedPath = path;
              break;
            }
          }
          if (preselectedPath == null) {
            preselectedPath = contentRoots[0];
          }
        }
        else if (project.getBaseDir() != null) {
          preselectedPath = project.getBaseDir();
        }
      }
      VirtualFile[] files = fileChooserDialog.choose(preselectedPath, project);
      if (files.length > 0) {
        String sourcePath = files[0].getPath();
        String destPath = determineMostLikelyLocationInJAR(files[0]);
        _additionalJARContentsTableModel.changeAdditionalJARConent(row, sourcePath, destPath);
        _additionalJARContentsTable.editCellAt(row, 1);
        _additionalJARContentsTable.getEditorComponent().requestFocus();
      }
    }
  }

  private void onRemoveAdditionalJARContent() {
    _additionalJARContentsTableModel.deleteAdditionalJARContent(_additionalJARContentsTable.getSelectedRow());
  }

  private void onAddAdditionalJARContent() {
    Project project = _editorContext.getProject();
    FileChooserDescriptor descriptor = new FileChooserDescriptor(true, true, true, true, false, true);
    descriptor.setTitle("Choose source file or folder");
    FileChooserDialog fileChooserDialog = FileChooserFactory.getInstance().createFileChooser(descriptor, project);
    VirtualFile rootFolder = null;
    Module module = _editorContext.getModule();
    VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
    if (contentRoots.length > 0) {
      rootFolder = contentRoots[0];
    }
    else if (project.getBaseDir() != null) {
      rootFolder = project.getBaseDir();
    }
    VirtualFile[] files = fileChooserDialog.choose(rootFolder, project);
    for (VirtualFile file : files) {
      String destFile = determineMostLikelyLocationInJAR(file);
      int row = _additionalJARContentsTableModel.addAdditionalJARContent(file.getPath(), destFile);
      _additionalJARContentsTable.editCellAt(row, 1);
      _additionalJARContentsTable.getEditorComponent().requestFocus();

    }
  }

  private String determineMostLikelyLocationInJAR(@NotNull VirtualFile file) {
    Project project = _editorContext.getProject();
    Module module = _editorContext.getModule();

    VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
    for (VirtualFile contentRoot : contentRoots) {
      if (VfsUtil.isAncestor(contentRoot, file, false)) {
        return VfsUtil.getRelativePath(file, contentRoot, '/');
      }
    }

    VirtualFile projectBaseFolder = project.getBaseDir();
    if (projectBaseFolder != null && VfsUtil.isAncestor(projectBaseFolder, file, false)) {
      return VfsUtil.getRelativePath(file, projectBaseFolder, '/');
    }
    return file.getName();
  }

  private void updateGui() {
    Boolean data = _editorContext.getUserData(OsmorcFacetGeneralEditorTab.BND_CREATION_KEY);
    boolean isBnd = data != null ? data : true;
    _jarFileChooser.setEnabled(!isBnd);
    _additionalJARContentsTable.setEnabled(!isBnd);
    _ignoreFilePatternTextField.setEnabled(!isBnd);
    _addButton.setEnabled(!isBnd);
    _removeButton.setEnabled(!isBnd);
    _editButton.setEnabled(!isBnd);
    _alwaysRebuildBundleJARCheckBox.setEnabled(!isBnd);
    _additionalJarContentsLabel.setEnabled(!isBnd);
    _jarFileToCreateLabel.setEnabled(!isBnd);
    _fileIgnorePatternLabel.setEnabled(!isBnd);
  }

  private void onJarFileSelect() {
    String currentFile = _jarFileChooser.getText();
    VirtualFile moduleCompilerOutputPath = CompilerModuleExtension.getInstance(_editorContext.getModule()).getCompilerOutputPath();

    // okay there is some strange thing going on here. The method getCompilerOutputPath() returns null
    // but getCompilerOutputPathUrl() returns something. I assume that we cannot get a VirtualFile object for a non-existing
    // path, so we need to make sure the compiler output path exists.

    if (moduleCompilerOutputPath == null) {
      // get the url
      String outputPathUrl = CompilerModuleExtension.getInstance(_editorContext.getModule()).getCompilerOutputUrl();

      // create the paths
      try {
        VfsUtil.createDirectories(VfsUtil.urlToPath(outputPathUrl));
      }
      catch (IOException e) {
        Messages.showErrorDialog(_root, OsmorcBundle.getTranslation("error"),
                                 OsmorcBundle.getTranslation("faceteditor.cannot.create.outputpath"));
        return;
      }

      // now try again to get VirtualFile object for it
      moduleCompilerOutputPath = CompilerModuleExtension.getInstance(_editorContext.getModule()).getCompilerOutputPath();
      if (moduleCompilerOutputPath == null) {
        // this should not happen
        throw new IllegalStateException("Cannot access compiler output path.");
      }
    }


    String preselectedFileName = StringUtil.isNotEmpty(currentFile) ? currentFile : moduleCompilerOutputPath.getParent().getPath();
    File file = new File(preselectedFileName);
    if (!file.exists()) {
      preselectedFileName = file.getParent();
    }
    JFileChooser jfilechooser = new JFileChooser(preselectedFileName);
    FileView fileview = new FileView() {
      public Icon getIcon(File aFile) {
        if (aFile.isDirectory()) {
          return super.getIcon(aFile);
        }
        else {
          FileType filetype = FileTypeManager.getInstance().getFileTypeByFileName(aFile.getName());
          return filetype.getIcon();
        }
      }
    };
    jfilechooser.setFileView(fileview);
    jfilechooser.setMultiSelectionEnabled(false);
    jfilechooser.setAcceptAllFileFilterUsed(false);
    jfilechooser.setDialogTitle(OsmorcBundle.getTranslation("faceteditor.selectjar.title"));
    jfilechooser.addChoosableFileFilter(new FileTypeFilter(StdFileTypes.ARCHIVE));
    if (jfilechooser.showSaveDialog(WindowManager.getInstance().suggestParentWindow(_editorContext.getProject())) != 0) {
      return;
    }
    file = jfilechooser.getSelectedFile();
    if (file != null && VfsUtil.isAncestor(new File(moduleCompilerOutputPath.getPath()), file, false)) {
      _jarFileChooser.setText("");
      Messages.showErrorDialog(_editorContext.getProject(), OsmorcBundle.getTranslation("error"),
                               OsmorcBundle.getTranslation("faceteditor.jar.cannot.be.in.output.path"));
      return;
    }

    // XXX: there is a problem in here, as the path is not system independent and there is no meaningful way
    // of transporting this setting from one system to the other as the user can choose arbitrary paths in here
    // This is quite a serious usability issue as you cannot use the IML-files on multiple systems...
    _jarFileChooser.setText(file.getPath());
  }

  @Nls
  public String getDisplayName() {
    return "Bundle JAR";
  }

  public JComponent createComponent() {
    return _root;
  }

  public boolean isModified() {
    return _modified;
  }

  public void apply() {
    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration)_editorContext.getFacet().getConfiguration();
    String fileLocation = _jarFileChooser.getText();
    fileLocation = fileLocation.replace('\\', '/');
    configuration.setJarFileLocation(fileLocation);
    configuration.setIgnoreFilePattern(_ignoreFilePatternTextField.getText());
    configuration.setAlwaysRebuildBundleJAR(_alwaysRebuildBundleJARCheckBox.isSelected());

    configuration.setAdditionalJARContents(_additionalJARContentsTableModel.getAdditionalContents());


  }

  public void reset() {
    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration)_editorContext.getFacet().getConfiguration();
    _jarFileChooser.setText(configuration.getJarFileLocation());
    _additionalJARContentsTableModel.replaceContent(configuration.getAdditionalJARContents());
    _ignoreFilePatternTextField.setText(configuration.getIgnoreFilePattern());
    _alwaysRebuildBundleJARCheckBox.setSelected(configuration.isAlwaysRebuildBundleJAR());
    updateGui();
  }

  @Override
  public void onTabEntering() {
    super.onTabEntering();
    updateGui();
  }

  public void disposeUIResources() {

  }

  @Override
  public String getHelpTopic() {
    return "reference.settings.module.facet.osgi";
  }

  private TextFieldWithBrowseButton _jarFileChooser;
  private JPanel _root;
  private JTable _additionalJARContentsTable;
  private final EditorTextField _ignoreFilePatternTextField;
  private JButton _addButton;
  private JButton _removeButton;
  private JButton _editButton;
  private JCheckBox _alwaysRebuildBundleJARCheckBox;
  private JLabel _additionalJarContentsLabel;
  private JLabel _jarFileToCreateLabel;
  private JLabel _fileIgnorePatternLabel;
  private JPanel _ignoreFilePatternPanel;
  private boolean _modified;
  private final FacetEditorContext _editorContext;
  private final AdditionalJARContentsTableModel _additionalJARContentsTableModel;

}