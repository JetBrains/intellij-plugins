// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.CommonBundle;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectFormatPanel;
import com.intellij.projectImport.ProjectImportWizardStep;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SelectDirWithFlashBuilderProjectsStep extends ProjectImportWizardStep {
  private JPanel myMainPanel;
  private LabeledComponent<TextFieldWithBrowseButton> myInitialPathComponent;
  private LabeledComponent<TextFieldWithBrowseButton> myExtractPathComponent;
  private LabeledComponent<JTextField> myProjectNameComponent;
  private LabeledComponent<TextFieldWithBrowseButton> myProjectLocationComponent;
  private JLabel myMultiProjectNote;
  private JCheckBox myCreateSubfolderCheckBox;
  private ProjectFormatPanel myProjectFormatPanel;

  public SelectDirWithFlashBuilderProjectsStep(final WizardContext context) {
    super(context);
    setupInitialPathComponent();

    myExtractPathComponent.setVisible(false);
    myExtractPathComponent.getComponent()
      .addBrowseFolderListener(null, null, context.getProject(), FileChooserDescriptorFactory.createSingleFolderDescriptor());

    final boolean creatingNewProject = context.isCreatingNewProject();
    myProjectNameComponent.setVisible(creatingNewProject);
    myProjectLocationComponent.setVisible(creatingNewProject);
    myProjectLocationComponent.getComponent()
      .addBrowseFolderListener(null, null, context.getProject(), FileChooserDescriptorFactory.createSingleFolderDescriptor());
    myProjectFormatPanel.getPanel().setVisible(creatingNewProject);

    myMultiProjectNote.setVisible(false);
    myCreateSubfolderCheckBox.setVisible(false);
  }

  private void setupInitialPathComponent() {
    myInitialPathComponent.getComponent().getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull final DocumentEvent e) {
        onInitialPathChanged();
      }
    });

    final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, true, true, true, false, false) {
      @Override
      public boolean isFileVisible(final VirtualFile file, final boolean showHiddenFiles) {
        return (super.isFileVisible(file, showHiddenFiles) &&
                (file.isDirectory() || FlashBuilderProjectFinder.isFlashBuilderProject(file)) ||
                FlashBuilderProjectFinder.hasArchiveExtension(file.getPath()));
      }

      @Override
      public Icon getIcon(final VirtualFile file) {
        // do not use Flash Builder specific icon for zip
        return !file.isDirectory() &&
               (FlashBuilderProjectFinder.hasFxpExtension(file.getPath()) || FlashBuilderProjectFinder.isFlashBuilderProject(file))
               ? dressIcon(file, getBuilder().getIcon())
               : super.getIcon(file);
      }
    };

    myInitialPathComponent.getComponent().addBrowseFolderListener(FlexBundle.message("select.flash.builder.workspace.or.project"),
                                                                  null, getWizardContext().getProject(), descriptor);
  }

  private void onInitialPathChanged() {
    final String path = myInitialPathComponent.getComponent().getText().trim();
    ((FlashBuilderImporter)getBuilder()).setInitiallySelectedPath(path);
    getWizardContext().requestWizardButtonsUpdate();

    final VirtualFile file = path.isEmpty() ? null : LocalFileSystem.getInstance().findFileByPath(path);

    final boolean isArchive = file != null && !file.isDirectory() && FlashBuilderProjectFinder.hasArchiveExtension(file.getPath());
    final boolean multiProjectArchive = isArchive && FlashBuilderProjectFinder.isMultiProjectArchive(file.getPath());

    if (getWizardContext().isCreatingNewProject()) {
      if (file != null) {
        final String suggestedProjectName = ((FlashBuilderImporter)getBuilder()).getSuggestedProjectName();
        myProjectNameComponent.getComponent().setText(suggestedProjectName);

        if (isArchive) {
          myProjectLocationComponent.setText(FlexBundle.message("project.location"));

          String dir = getWizardContext().getProjectFileDirectory();
          if (new File(dir).isFile()) {
            dir = PathUtil.getParentPath(dir);
          }
          myProjectLocationComponent.getComponent().setText(FileUtil.toSystemDependentName(dir + "/" + suggestedProjectName));
        }
        else {
          myProjectLocationComponent.setText(FlexBundle.message("project.files.location"));
          myProjectLocationComponent.getComponent()
            .setText(FileUtil.toSystemDependentName(file.isDirectory() ? file.getPath() : file.getParent().getPath()));
        }
      }

      myMultiProjectNote.setVisible(isArchive && multiProjectArchive);
      myCreateSubfolderCheckBox.setVisible(isArchive && !multiProjectArchive);
      myCreateSubfolderCheckBox
        .setText(FlexBundle.message("extract.single.to.subfolder.0", file == null ? "" : file.getNameWithoutExtension()));
    }
    else {
      myExtractPathComponent.setVisible(isArchive);
      if (isArchive) {
        myExtractPathComponent.setText(multiProjectArchive ? FlexBundle.message("folder.to.unzip.several.FB.projects")
                                                           : FlexBundle.message("folder.to.unzip.one.FB.project"));
        final String extractPath = multiProjectArchive ? getWizardContext().getProject().getBasePath()
                                                       : getWizardContext().getProject().getBasePath()
                                                         + "/" + file.getNameWithoutExtension();
        myExtractPathComponent.getComponent().setText(FileUtil.toSystemDependentName(extractPath));
      }
    }
  }

  private void createUIComponents() {
    myProjectFormatPanel = new ProjectFormatPanel();
  }

  @Override
  public JComponent getComponent() {
    return myMainPanel;
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myInitialPathComponent.getComponent().getTextField();
  }

  @Override
  public void updateStep() {
    if (getWizardContext().isProjectFileDirectorySet() && myInitialPathComponent.getComponent().getText().isEmpty()) {
      myInitialPathComponent.getComponent().setText(FileUtil.toSystemDependentName(getWizardContext().getProjectFileDirectory()));
    }
  }

  @Override
  public void updateDataModel() {
    final FlashBuilderImporter builder = (FlashBuilderImporter)getBuilder();
    builder.setExtractToSubfolder(myCreateSubfolderCheckBox.isSelected());
    builder.setExtractPath(FileUtil.toSystemIndependentName(myExtractPathComponent.getComponent().getText().trim()));

    getWizardContext().setProjectName(myProjectNameComponent.getComponent().getText().trim());
    getWizardContext()
      .setProjectFileDirectory(FileUtil.toSystemIndependentName(myProjectLocationComponent.getComponent().getText().trim()));

    myProjectFormatPanel.updateData(getWizardContext());
  }

  @Override
  public boolean validate() throws ConfigurationException {
    final String path = myInitialPathComponent.getComponent().getText().trim();
    if (path.length() == 0) {
      throw new ConfigurationException(FlexBundle.message("specify.flash.builder.workspace.or.project.dir"), CommonBundle.getErrorTitle());
    }
    final VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
    if (file == null) {
      throw new ConfigurationException(FlexBundle.message("file.or.folder.not.found", path), CommonBundle.getErrorTitle());
    }

    if (file.isDirectory()) {
      final List<String> projectPaths = new ArrayList<>();
      // false if user cancelled
      final boolean ok = FlashBuilderProjectFinder.collectAllProjectPaths(getWizardContext().getProject(), projectPaths, path);

      if (ok) {
        if (projectPaths.isEmpty()) {
          throw new ConfigurationException(FlexBundle.message("flash.builder.projects.not.found.in"), CommonBundle.getErrorTitle());
        }

        ((FlashBuilderImporter)getBuilder()).setList(projectPaths);
      }

      return ok && checkProjectNameAndPath();
    }
    else {
      if (FlashBuilderProjectFinder.hasArchiveExtension(file.getPath())) {
        FlashBuilderProjectFinder.checkArchiveContainsFBProject(file.getPath());
        final File dir = new File(getWizardContext().isCreatingNewProject()
                                  ? myProjectLocationComponent.getComponent().getText().trim()
                                  : myExtractPathComponent.getComponent().getText().trim());
        if (dir.isDirectory() && Objects.requireNonNull(dir.list()).length > 0) {
          final String title = FlexBundle.message("dialog.title.import.from.flash.builder", getWizardContext().getPresentationName());
          if (Messages.YES !=
              Messages.showYesNoDialog(myMainPanel, FlexBundle.message("folder.not.empty", dir.getPath()), title, Messages.getWarningIcon())) {
            return false;
          }
        }
      }
      else if (!FlashBuilderProjectFinder.isFlashBuilderProject(file)) {
        throw new ConfigurationException(FlexBundle.message("not.flash.builder.project"), CommonBundle.getErrorTitle());
      }

      ((FlashBuilderImporter)getBuilder()).setList(Collections.singletonList(path));

      return checkProjectNameAndPath();
    }
  }

  private boolean checkProjectNameAndPath() throws ConfigurationException {
    if (!getWizardContext().isCreatingNewProject()) return true;

    if (myProjectNameComponent.getComponent().getText().trim().isEmpty()) {
      throw new ConfigurationException(FlexBundle.message("project.name.empty"));
    }

    if (myProjectLocationComponent.getComponent().getText().trim().isEmpty()) {
      throw new ConfigurationException(FlexBundle.message("project.path.empty"));
    }

    return true;
  }

  @Override
  public String getHelpId() {
    return "reference.dialogs.new.project.import.flex.page1";
  }
}
