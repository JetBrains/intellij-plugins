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
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.settings.MyErrorText;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import static org.osmorc.facet.OsmorcFacetConfiguration.OutputPathType.*;

/**
 * The facet editor tab which is used to set up Osmorc facet settings concerning the bundle JAR created by Osmorc.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcFacetJAREditorTab extends FacetEditorTab {
  private JPanel myRoot;
  private JTable myAdditionalJARContentsTable;
  private final EditorTextField myIgnoreFilePatternTextField;
  private JButton myAddButton;
  private JButton myRemoveButton;
  private JButton myEditButton;
  private JCheckBox myAlwaysRebuildBundleJARCheckBox;
  private JLabel myFileIgnorePatternLabel;
  private JPanel myIgnoreFilePatternPanel;
  private JTextField myJarFileTextField;
  private JRadioButton myPlaceInCompilerOutputPathRadioButton;
  private JRadioButton myPlaceInProjectWideRadioButton;
  private JRadioButton myPlaceInThisPathRadioButton;
  private TextFieldWithBrowseButton myJarOutputPathChooser;
  private MyErrorText myErrorText;
  private JPanel myAdditionalJarContentsPanel;
  private boolean myModified;
  private final FacetEditorContext myEditorContext;
  private FacetValidatorsManager myValidatorsManager;
  private final AdditionalJARContentsTableModel myAdditionalJARContentsTableModel;

  public OsmorcFacetJAREditorTab(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
    myEditorContext = editorContext;
    myValidatorsManager = validatorsManager;
    final Project project = editorContext.getProject();

    myIgnoreFilePatternTextField = new EditorTextField("", project, FileTypes.PLAIN_TEXT);
    FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName("*.regexp");
    if (fileType == FileTypes.UNKNOWN) {
      fileType = FileTypeManager.getInstance().getFileTypeByFileName("*.txt"); // RegExp plugin is not installed
    }

    final PsiFile file =
      PsiFileFactory.getInstance(project).createFileFromText("*.regexp", fileType, myIgnoreFilePatternTextField.getText(), -1, true);
    myIgnoreFilePatternTextField.setNewDocumentAndFileType(fileType, PsiDocumentManager.getInstance(project).getDocument(file));
    myIgnoreFilePatternPanel.add(myIgnoreFilePatternTextField, BorderLayout.CENTER);

    UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.addUserActivityListener(new UserActivityListener() {
      public void stateChanged() {
        myModified = true;
        updateGui();
      }
    });

    myJarOutputPathChooser.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        onOutputPathSelect();
      }
    });

    ChangeListener listener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        updateGui();
      }
    };

    myPlaceInProjectWideRadioButton.addChangeListener(listener);
    myPlaceInThisPathRadioButton.addChangeListener(listener);
    myPlaceInCompilerOutputPathRadioButton.addChangeListener(listener);

    watcher.register(myRoot);

    myAdditionalJARContentsTableModel = new AdditionalJARContentsTableModel();
    myAdditionalJARContentsTable.setModel(myAdditionalJARContentsTableModel);

    TableColumn col = myAdditionalJARContentsTable.getColumnModel().getColumn(0);
    final FileSelectorTableCellEditor selectorTableCellEditor = new FileSelectorTableCellEditor(project, myEditorContext.getModule());
    col.setCellEditor(selectorTableCellEditor);
    selectorTableCellEditor.addCellEditorListener(new CellEditorListener() {
      public void editingCanceled(ChangeEvent e) {
      }

      public void editingStopped(ChangeEvent e) {
        // ok we finished editing the left, now get the stuff from there, calculate a destination name and edit the other cell
        int row = myAdditionalJARContentsTable.getSelectedRow();
        if (row > -1) {
          Pair<String, String> additionalJARContent = myAdditionalJARContentsTableModel.getAdditionalJARContent(row);
          VirtualFile preselectedPath = LocalFileSystem.getInstance().findFileByPath(additionalJARContent.getFirst());
          String destinationName;
          if (preselectedPath != null) {
            destinationName = determineMostLikelyLocationInJAR(preselectedPath);
          }
          else {
            destinationName = "";
          }

          myAdditionalJARContentsTableModel.changeAdditionalJARConent(row, additionalJARContent.first, destinationName);
          myAdditionalJARContentsTable.editCellAt(row, 1);
          myAdditionalJARContentsTable.getEditorComponent().requestFocus();
        }
      }
    });

    myAdditionalJARContentsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        myRemoveButton.setEnabled(myAdditionalJARContentsTable.getSelectedRowCount() > 0);
        myEditButton.setEnabled(myAdditionalJARContentsTable.getSelectedRowCount() > 0);
      }
    });


    myAddButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onAddAdditionalJARContent();
      }
    });

    myRemoveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onRemoveAdditionalJARContent();
      }
    });

    myEditButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onEditAdditionalJARContent();
      }
    });

    myEditButton.setEnabled(false);
    myRemoveButton.setEnabled(false);

    myValidatorsManager.registerValidator(new OsmorcFacetJarEditorValidator(myEditorContext, this));
  }

  private void onEditAdditionalJARContent() {
    int row = myAdditionalJARContentsTable.getSelectedRow();
    if (row > -1) {
      Pair<String, String> additionalJARContent = myAdditionalJARContentsTableModel.getAdditionalJARContent(row);
      Project project = myEditorContext.getProject();
      FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleLocalFileDescriptor();
      descriptor.setTitle("Choose source file or folder");
      FileChooserDialog fileChooserDialog = FileChooserFactory.getInstance().createFileChooser(descriptor, project);
      VirtualFile preselectedPath = LocalFileSystem.getInstance().findFileByPath(additionalJARContent.getFirst());
      if (preselectedPath == null) {
        Module module = myEditorContext.getModule();
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
        myAdditionalJARContentsTableModel.changeAdditionalJARConent(row, sourcePath, destPath);
        myAdditionalJARContentsTable.editCellAt(row, 1);
        myAdditionalJARContentsTable.getEditorComponent().requestFocus();
      }
    }
  }

  private void onRemoveAdditionalJARContent() {
    myAdditionalJARContentsTableModel.deleteAdditionalJARContent(myAdditionalJARContentsTable.getSelectedRow());
  }

  private void onAddAdditionalJARContent() {
    Project project = myEditorContext.getProject();
    FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createAllButJarContentsDescriptor();
    descriptor.setTitle("Choose source file or folder");
    FileChooserDialog fileChooserDialog = FileChooserFactory.getInstance().createFileChooser(descriptor, project);
    VirtualFile rootFolder = null;
    Module module = myEditorContext.getModule();
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
      int row = myAdditionalJARContentsTableModel.addAdditionalJARContent(file.getPath(), destFile);
      myAdditionalJARContentsTable.editCellAt(row, 1);
      myAdditionalJARContentsTable.getEditorComponent().requestFocus();
    }
  }

  private String determineMostLikelyLocationInJAR(@NotNull VirtualFile file) {
    Project project = myEditorContext.getProject();
    Module module = myEditorContext.getModule();

    VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
    for (VirtualFile contentRoot : contentRoots) {
      if (VfsUtil.isAncestor(contentRoot, file, false)) {
        return VfsUtilCore.getRelativePath(file, contentRoot, '/');
      }
    }

    VirtualFile projectBaseFolder = project.getBaseDir();
    if (projectBaseFolder != null && VfsUtil.isAncestor(projectBaseFolder, file, false)) {
      return VfsUtilCore.getRelativePath(file, projectBaseFolder, '/');
    }
    return file.getName();
  }

  private void updateGui() {
    Boolean bnd = myEditorContext.getUserData(OsmorcFacetGeneralEditorTab.BND_CREATION_KEY);
    Boolean bundlor = myEditorContext.getUserData(OsmorcFacetGeneralEditorTab.BUNDLOR_CREATION_KEY);
    boolean useExternalTool = (bnd != null && bnd) || (bundlor != null && bundlor);
    myJarOutputPathChooser.setEnabled(myPlaceInThisPathRadioButton.isSelected());
    myAdditionalJARContentsTable.setEnabled(!useExternalTool);
    myIgnoreFilePatternTextField.setEnabled(!useExternalTool);
    myAddButton.setEnabled(!useExternalTool);
    myRemoveButton.setEnabled(!useExternalTool);
    myEditButton.setEnabled(!useExternalTool);
    myAlwaysRebuildBundleJARCheckBox.setEnabled(!useExternalTool);
    myAdditionalJarContentsPanel.setEnabled(!useExternalTool);
    myFileIgnorePatternLabel.setEnabled(!useExternalTool);
    myValidatorsManager.validate();
    if (myPlaceInThisPathRadioButton.isSelected() && myJarOutputPathChooser.getText().trim().length()==0) {
     myErrorText.setError("Please select an output path");
    }
    else {
      myErrorText.setError(null);
    }
  }


  void onOutputPathSelect() {
    String currentFile = getSelectedOutputPath();
    VirtualFile moduleCompilerOutputPath = CompilerModuleExtension.getInstance(myEditorContext.getModule()).getCompilerOutputPath();

    // okay there is some strange thing going on here. The method getCompilerOutputPath() returns null
    // but getCompilerOutputPathUrl() returns something. I assume that we cannot get a VirtualFile object for a non-existing
    // path, so we need to make sure the compiler output path exists.

    if (moduleCompilerOutputPath == null) {
      // get the url
      String outputPathUrl = CompilerModuleExtension.getInstance(myEditorContext.getModule()).getCompilerOutputUrl();

      // create the paths
      try {
        VfsUtil.createDirectories(VfsUtil.urlToPath(outputPathUrl));
      }
      catch (IOException e) {
        Messages.showErrorDialog(myRoot, OsmorcBundle.getTranslation("error"),
                                 OsmorcBundle.getTranslation("faceteditor.cannot.create.outputpath"));
        return;
      }

      // now try again to get VirtualFile object for it
      moduleCompilerOutputPath = CompilerModuleExtension.getInstance(myEditorContext.getModule()).getCompilerOutputPath();
      if (moduleCompilerOutputPath == null) {
        // this should not happen
        throw new IllegalStateException("Cannot access compiler output path.");
      }
    }

    VirtualFile preselectedFile =
      StringUtil.isNotEmpty(currentFile) ? LocalFileSystem.getInstance().findFileByPath(currentFile) : moduleCompilerOutputPath;

    FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
    descriptor.setTitle("Select bundle output directory");
    VirtualFile file = FileChooser.chooseFile(myEditorContext.getProject(), descriptor, preselectedFile);
    if (file != null) {
      if (VfsUtil.isAncestor(moduleCompilerOutputPath, file, false)) {
        Messages.showErrorDialog(myEditorContext.getProject(),
                                 OsmorcBundle.getTranslation("faceteditor.jar.cannot.be.in.output.path"), OsmorcBundle.getTranslation("error"));
        myJarOutputPathChooser.setText("");
        return;
      }
      myJarOutputPathChooser.setText(file.getPath());
    }
  }

  @Nls
  public String getDisplayName() {
    return "Bundle JAR";
  }

  public JComponent createComponent() {
    return myRoot;
  }

  public boolean isModified() {
    return myModified;
  }


  public void apply() {
    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration)myEditorContext.getFacet().getConfiguration();
    String fileLocation = getSelectedOutputPath();
    String jarFileName = getJarFileName();


    OsmorcFacetConfiguration.OutputPathType pathType = getSelectedOutputPathType();
    // Build a complete path if the user wants to put the file into some specific path.
    if (pathType == SpecificOutputPath) {
      String completeOutputPath = new File(fileLocation, jarFileName).getPath();
      configuration.setJarFileLocation(completeOutputPath, pathType);
    }
    else {
      configuration.setJarFileLocation(jarFileName, pathType);
    }
    configuration.setIgnoreFilePattern(myIgnoreFilePatternTextField.getText());
    configuration.setAlwaysRebuildBundleJAR(myAlwaysRebuildBundleJARCheckBox.isSelected());

    configuration.setAdditionalJARContents(myAdditionalJARContentsTableModel.getAdditionalContents());
    myModified = false;
  }

  public String getJarFileName() {
    return myJarFileTextField.getText();
  }

  String getSelectedOutputPath() {
    return myJarOutputPathChooser.getText();
  }

  OsmorcFacetConfiguration.OutputPathType getSelectedOutputPathType() {
    return myPlaceInProjectWideRadioButton.isSelected()
                                                       ? OsgiOutputPath
                                                       : myPlaceInCompilerOutputPathRadioButton.isSelected()
                                                         ? CompilerOutputPath
                                                         : SpecificOutputPath;
  }

  public void reset() {
    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration)myEditorContext.getFacet().getConfiguration();
    OsmorcFacetConfiguration.OutputPathType outputPathType = configuration.getOutputPathType();
    myPlaceInCompilerOutputPathRadioButton.setSelected(outputPathType == CompilerOutputPath);
    myPlaceInProjectWideRadioButton.setSelected(outputPathType == OsgiOutputPath);
    myPlaceInThisPathRadioButton.setSelected(outputPathType == SpecificOutputPath);


    myJarFileTextField.setText(configuration.getJarFileName());
    if (outputPathType == SpecificOutputPath) {
      myJarOutputPathChooser.setText(configuration.getJarFilePath());
    }
    else {
      myJarOutputPathChooser.setText("");
    }

    myAdditionalJARContentsTableModel.replaceContent(configuration.getAdditionalJARContents());
    myIgnoreFilePatternTextField.setText(configuration.getIgnoreFilePattern());
    myAlwaysRebuildBundleJARCheckBox.setSelected(configuration.isAlwaysRebuildBundleJAR());
    updateGui();
    myModified = false;
  }

  @Override
  public void onTabEntering() {
    super.onTabEntering();
    updateGui();
    myValidatorsManager.validate();
  }

  public void disposeUIResources() {

  }

  @Override
  public String getHelpTopic() {
    return "reference.settings.module.facet.osgi";
  }
}
