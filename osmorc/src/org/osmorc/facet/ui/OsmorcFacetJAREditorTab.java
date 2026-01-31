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

import com.intellij.CommonBundle;
import com.intellij.compiler.server.BuildManager;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetEditorValidator;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.UserActivityWatcher;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.jps.model.OutputPathType;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.io.File;

import static org.jetbrains.osgi.jps.model.OutputPathType.CompilerOutputPath;
import static org.jetbrains.osgi.jps.model.OutputPathType.OsgiOutputPath;
import static org.jetbrains.osgi.jps.model.OutputPathType.SpecificOutputPath;

/**
 * The facet editor tab which is used to set up Osmorc facet settings concerning the bundle JAR created by Osmorc.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class OsmorcFacetJAREditorTab extends FacetEditorTab {
  private JPanel myRoot;
  private final JTable myAdditionalJARContentsTable;
  private final EditorTextField myIgnoreFilePatternTextField;
  private JCheckBox myAlwaysRebuildBundleJARCheckBox;
  private JLabel myFileIgnorePatternLabel;
  private JPanel myIgnoreFilePatternPanel;
  private JTextField myJarFileTextField;
  private JRadioButton myPlaceInCompilerOutputPathRadioButton;
  private JRadioButton myPlaceInProjectWideRadioButton;
  private JRadioButton myPlaceInThisPathRadioButton;
  private TextFieldWithBrowseButton myJarOutputPathChooser;
  private JPanel myAdditionalJarContentsPanel;

  private final FacetEditorContext myEditorContext;
  private final FacetValidatorsManager myValidatorsManager;
  private final AdditionalJARContentsTableModel myAdditionalJARContentsTableModel;
  private boolean myModified;

  public OsmorcFacetJAREditorTab(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
    myEditorContext = editorContext;
    myValidatorsManager = validatorsManager;

    Project project = editorContext.getProject();
    myIgnoreFilePatternTextField = new EditorTextField("", project, FileTypes.PLAIN_TEXT);
    FileType type = FileTypeManager.getInstance().getFileTypeByFileName("*.regexp");
    if (type == FileTypes.UNKNOWN) {
      type = FileTypeManager.getInstance().getFileTypeByFileName("*.txt"); // RegExp plugin is not installed
    }
    PsiFile file = PsiFileFactory.getInstance(project).createFileFromText("*.regexp", type, myIgnoreFilePatternTextField.getText(), -1, true);
    myIgnoreFilePatternTextField.setNewDocumentAndFileType(type, PsiDocumentManager.getInstance(project).getDocument(file));
    myIgnoreFilePatternPanel.add(myIgnoreFilePatternTextField, BorderLayout.CENTER);

    UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.addUserActivityListener(() -> { myModified = true; updateGui(); });
    watcher.register(myRoot);

    myJarOutputPathChooser.addActionListener(actionEvent -> onOutputPathSelect());

    ChangeListener listener = e -> updateGui();
    myPlaceInProjectWideRadioButton.addChangeListener(listener);
    myPlaceInThisPathRadioButton.addChangeListener(listener);
    myPlaceInCompilerOutputPathRadioButton.addChangeListener(listener);

    myAdditionalJARContentsTableModel = new AdditionalJARContentsTableModel();
    myAdditionalJARContentsTable = new JBTable(myAdditionalJARContentsTableModel);
    TableColumn col = myAdditionalJARContentsTable.getColumnModel().getColumn(0);
    FileSelectorTableCellEditor selectorTableCellEditor = new FileSelectorTableCellEditor(project, myEditorContext.getModule());
    col.setCellEditor(selectorTableCellEditor);
    selectorTableCellEditor.addCellEditorListener(new CellEditorListener() {
      @Override
      public void editingCanceled(ChangeEvent e) { }

      @Override
      public void editingStopped(ChangeEvent e) {
        // ok we finished editing the left, now get the stuff from there, calculate a destination name and edit the other cell
        int row = myAdditionalJARContentsTable.getSelectedRow();
        if (row > -1) {
          Pair<String, String> additionalJARContent = myAdditionalJARContentsTableModel.getAdditionalJARContent(row);
          VirtualFile preselectedPath = LocalFileSystem.getInstance().findFileByPath(additionalJARContent.getFirst());
          String destinationName = preselectedPath != null ? determineMostLikelyLocationInJar(preselectedPath) : "";
          myAdditionalJARContentsTableModel.changeAdditionalJARContent(row, additionalJARContent.first, destinationName);
          myAdditionalJARContentsTable.editCellAt(row, 1);
          IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(
            () -> IdeFocusManager.getGlobalInstance().requestFocus(myAdditionalJARContentsTable.getEditorComponent(), true));
        }
      }
    });

    myAdditionalJarContentsPanel.add(
      ToolbarDecorator.createDecorator(myAdditionalJARContentsTable)
        .setAddAction((b) -> onAddAdditionalJarContent())
        .setRemoveAction((b) -> onRemoveAdditionalJarContent())
        .setEditAction((b) -> onEditAdditionalJARContent())
        .disableUpDownActions()
        .createPanel(), BorderLayout.CENTER);

    myValidatorsManager.registerValidator(new FacetEditorValidator() {
      @Override
      public @NotNull ValidationResult check() {
        if (StringUtil.isEmptyOrSpaces(myJarFileTextField.getText())) {
          return new ValidationResult(OsmorcBundle.message("facet.editor.jar.empty.jar.name"));
        }
        if (getSelectedOutputPathType() == SpecificOutputPath && StringUtil.isEmptyOrSpaces(myJarOutputPathChooser.getText())) {
          return new ValidationResult(OsmorcBundle.message("facet.editor.jar.empty.output.path"));
        }
        return ValidationResult.OK;
      }
    });
  }

  private void onEditAdditionalJARContent() {
    int row = myAdditionalJARContentsTable.getSelectedRow();
    if (row > -1) {
      Pair<String, String> additionalJARContent = myAdditionalJARContentsTableModel.getAdditionalJARContent(row);
      Project project = myEditorContext.getProject();
      FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleLocalFileDescriptor().withTitle(OsmorcBundle.message("facet.editor.select.source.title"));
      VirtualFile preselectedPath = LocalFileSystem.getInstance().findFileByPath(additionalJARContent.getFirst());
      if (preselectedPath == null) {
        Module module = myEditorContext.getModule();
        VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
        if (contentRoots.length > 0) {
          for (VirtualFile contentRoot : contentRoots) {
            VirtualFile path = contentRoot.findFileByRelativePath(additionalJARContent.getFirst());
            if (path != null) {
              preselectedPath = path;
              break;
            }
          }
          if (preselectedPath == null) {
            preselectedPath = contentRoots[0];
          }
        }
        else {
          preselectedPath = ProjectUtil.guessProjectDir(project);
        }
      }
      VirtualFile[] files = FileChooser.chooseFiles(descriptor, project, preselectedPath);
      if (files.length > 0) {
        String sourcePath = files[0].getPath();
        String destPath = determineMostLikelyLocationInJar(files[0]);
        myAdditionalJARContentsTableModel.changeAdditionalJARContent(row, sourcePath, destPath);
        myAdditionalJARContentsTable.editCellAt(row, 1);
        IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(
          () -> IdeFocusManager.getGlobalInstance().requestFocus(myAdditionalJARContentsTable.getEditorComponent(), true));
      }
    }
  }

  private void onRemoveAdditionalJarContent() {
    int row = myAdditionalJARContentsTable.convertRowIndexToModel(myAdditionalJARContentsTable.getSelectedRow());
    int editingCol = myAdditionalJARContentsTable.getEditingColumn();
    int editingRow = myAdditionalJARContentsTable.getEditingRow();
    if (editingCol != -1 && editingRow != -1) {
      TableCellEditor editor = myAdditionalJARContentsTable.getCellEditor(editingRow, editingCol);
      editor.cancelCellEditing();
    }
    myAdditionalJARContentsTableModel.deleteAdditionalJARContent(row);
  }

  private void onAddAdditionalJarContent() {
    Project project = myEditorContext.getProject();
    FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createAllButJarContentsDescriptor().withTitle(OsmorcBundle.message("facet.editor.select.source.title"));
    VirtualFile rootFolder;
    VirtualFile[] contentRoots = ModuleRootManager.getInstance(myEditorContext.getModule()).getContentRoots();
    if (contentRoots.length > 0) {
      rootFolder = contentRoots[0];
    }
    else {
      rootFolder = ProjectUtil.guessProjectDir(project);
    }
    VirtualFile[] files = FileChooser.chooseFiles(descriptor, project, rootFolder);
    for (VirtualFile file : files) {
      String destFile = determineMostLikelyLocationInJar(file);
      int row = myAdditionalJARContentsTableModel.addAdditionalJARContent(file.getPath(), destFile);
      myAdditionalJARContentsTable.editCellAt(row, 1);
      IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(
        () -> IdeFocusManager.getGlobalInstance().requestFocus(myAdditionalJARContentsTable.getEditorComponent(), true));
    }
  }

  private String determineMostLikelyLocationInJar(@NotNull VirtualFile file) {
    VirtualFile[] contentRoots = ModuleRootManager.getInstance(myEditorContext.getModule()).getContentRoots();
    for (VirtualFile contentRoot : contentRoots) {
      if (VfsUtilCore.isAncestor(contentRoot, file, false)) {
        return VfsUtilCore.getRelativePath(file, contentRoot);
      }
    }
    VirtualFile projectBaseFolder = ProjectUtil.guessProjectDir(myEditorContext.getProject());
    if (projectBaseFolder != null && VfsUtilCore.isAncestor(projectBaseFolder, file, false)) {
      return VfsUtilCore.getRelativePath(file, projectBaseFolder);
    }
    return file.getName();
  }

  private void updateGui() {
    myJarOutputPathChooser.setEnabled(myPlaceInThisPathRadioButton.isSelected());

    boolean enabled = myEditorContext.getUserData(OsmorcFacetGeneralEditorTab.EXT_TOOL_MANIFEST_CREATION_KEY) != Boolean.TRUE;
    myAdditionalJARContentsTable.setEnabled(enabled);
    myAdditionalJarContentsPanel.setEnabled(enabled);
    myIgnoreFilePatternTextField.setEnabled(enabled);
    myFileIgnorePatternLabel.setEnabled(enabled);

    myValidatorsManager.validate();
  }

  private void onOutputPathSelect() {
    String current = myJarOutputPathChooser.getText();
    final VirtualFile moduleOutputDir = CompilerPaths.getModuleOutputDirectory(myEditorContext.getModule(), false);
    VirtualFile toSelect = StringUtil.isNotEmpty(current) ? LocalFileSystem.getInstance().findFileByPath(current) : moduleOutputDir;
    FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle(OsmorcBundle.message("facet.editor.select.bundle.dir.title"));
    FileChooser.chooseFile(descriptor, myEditorContext.getProject(), toSelect, file -> {
      if (moduleOutputDir != null && VfsUtilCore.isAncestor(moduleOutputDir, file, false)) {
        Messages.showErrorDialog(myRoot, OsmorcBundle.message("facet.editor.jar.cannot.be.in.output.path"), CommonBundle.getErrorTitle());
        myJarOutputPathChooser.setText("");
      }
      else {
        myJarOutputPathChooser.setText(file.getPath());
      }
    });
  }

  @Override
  public @Nls String getDisplayName() {
    return OsmorcBundle.message("facet.tab.jar");
  }

  @Override
  public @NotNull JComponent createComponent() {
    return myRoot;
  }

  @Override
  public boolean isModified() {
    return myModified;
  }

  @Override
  public void apply() throws ConfigurationException {
    String jarFileName = myJarFileTextField.getText();
    if (StringUtil.isEmptyOrSpaces(jarFileName)) {
      throw new ConfigurationException(OsmorcBundle.message("facet.editor.jar.empty.jar.name"));
    }

    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration)myEditorContext.getFacet().getConfiguration();
    OutputPathType pathType = getSelectedOutputPathType();
    if (pathType == SpecificOutputPath) {
      String location = myJarOutputPathChooser.getText();
      if (StringUtil.isEmptyOrSpaces(location)) {
        throw new ConfigurationException(OsmorcBundle.message("facet.editor.jar.empty.output.path"));
      }
      String completeOutputPath = new File(location, jarFileName).getPath();
      configuration.setJarFileLocation(completeOutputPath, pathType);
    }
    else {
      configuration.setJarFileLocation(jarFileName, pathType);
    }
    configuration.setIgnoreFilePattern(myIgnoreFilePatternTextField.getText());
    configuration.setAlwaysRebuildBundleJAR(myAlwaysRebuildBundleJARCheckBox.isSelected());
    configuration.setAdditionalJARContents(myAdditionalJARContentsTableModel.getAdditionalContents());

    if (myModified) {
      BuildManager.getInstance().clearState(myEditorContext.getProject());
    }
    myModified = false;
  }

  @Override
  public void reset() {
    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration)myEditorContext.getFacet().getConfiguration();

    OutputPathType outputPathType = configuration.getOutputPathType();
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
    updateGui();
  }

  @Override
  public String getHelpTopic() {
    return "reference.settings.module.facet.osgi";
  }

  private OutputPathType getSelectedOutputPathType() {
    return myPlaceInProjectWideRadioButton.isSelected() ? OsgiOutputPath :
           myPlaceInCompilerOutputPathRadioButton.isSelected() ? CompilerOutputPath :
           SpecificOutputPath;
  }
}
