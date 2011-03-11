package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.CommonBundle;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectFormatPanel;
import com.intellij.projectImport.ProjectImportWizardStep;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SelectDirWithFlashBuilderProjectsStep extends ProjectImportWizardStep {
  private JPanel myMainPanel;
  private LabeledComponent<TextFieldWithBrowseButton> myWorkspaceOrDirWithProjectsComponent;
  private ProjectFormatPanel myProjectFormatPanel;

  public SelectDirWithFlashBuilderProjectsStep(final WizardContext context) {
    super(context);
    setupProjectsFileChooser();
    myProjectFormatPanel.getPanel().setVisible(context.isCreatingNewProject());
  }

  private void createUIComponents() {
    myProjectFormatPanel = new ProjectFormatPanel();
  }

  private void setupProjectsFileChooser() {
    final String title = FlexBundle.message("select.flash.builder.workspace.or.project.dir");

    final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, true, false, false, false, false) {
      public boolean isFileVisible(final VirtualFile file, final boolean showHiddenFiles) {
        return (super.isFileVisible(file, showHiddenFiles) && (file.isDirectory() || FlashBuilderProjectFinder.isFlashBuilderProject(file)));
      }

      public Icon getOpenIcon(final VirtualFile virtualFile) {
        if (FlashBuilderProjectFinder.isFlashBuilderProject(virtualFile)) {
          return getBuilder().getIcon();
        }
        return super.getOpenIcon(virtualFile);
      }

      public Icon getClosedIcon(final VirtualFile virtualFile) {
        if (FlashBuilderProjectFinder.isFlashBuilderProject(virtualFile)) {
          return getBuilder().getIcon();
        }
        return super.getClosedIcon(virtualFile);
      }
    };

    myWorkspaceOrDirWithProjectsComponent.getComponent()
      .addBrowseFolderListener(title, null, getWizardContext().getProject(), descriptor, new TextComponentAccessor<JTextField>() {
        public String getText(final JTextField component) {
          return component.getText();
        }

        public void setText(final JTextField component, String text) {
          if (FileUtil.toSystemIndependentName(text).endsWith("/" + FlashBuilderImporter.DOT_PROJECT)) {
            text = text.substring(0, text.length() - ("/" + FlashBuilderImporter.DOT_PROJECT).length());
          }
          component.setText(text);
        }
      });
  }

  public JComponent getComponent() {
    return myMainPanel;
  }

  public JComponent getPreferredFocusedComponent() {
    return myWorkspaceOrDirWithProjectsComponent.getComponent().getTextField();
  }

  public void updateDataModel() {
    final String dirPath = myWorkspaceOrDirWithProjectsComponent.getComponent().getText().trim();
    final FlashBuilderImporter importer = (FlashBuilderImporter)getBuilder();

    importer.setInitiallySelectedDirPath(dirPath);

    suggestProjectNameAndPath(null, dirPath);
    myProjectFormatPanel.updateData(getWizardContext());
  }

  public boolean validate() throws ConfigurationException {
    final String dirPath = myWorkspaceOrDirWithProjectsComponent.getComponent().getText().trim();
    if (dirPath.length() == 0) {
      throw new ConfigurationException(FlexBundle.message("specify.flash.builder.workspace.or.project.dir"), CommonBundle.getErrorTitle());
    }
    final VirtualFile dir = LocalFileSystem.getInstance().refreshAndFindFileByPath(dirPath);
    if (dir == null || !dir.isDirectory()) {
      throw new ConfigurationException(FlexBundle.message("folder.does.not.exist", dirPath), CommonBundle.getErrorTitle());
    }

    final List<String> projectPaths = new ArrayList<String>();
    // false if user cancelled
    final boolean ok = FlashBuilderProjectFinder.collectAllProjectPaths(getWizardContext().getProject(), projectPaths, dirPath);
    ((FlashBuilderImporter)getBuilder()).setList(projectPaths);
    return ok;
  }

  public String getHelpId() {
    return "reference.dialogs.new.project.import.flex.page1";
  }
}
