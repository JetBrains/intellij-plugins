package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.StringBuilderSpinAllocator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.inspections.model.CreateStepDefinitionFileModel;
import org.jetbrains.plugins.cucumber.inspections.ui.CreateStepDefinitionFileDialog;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author yole
 */
public class CucumberCreateStepFix implements LocalQuickFix {
  private static final Icon NEW_FILE_ICON = AllIcons.Actions.CreateFromUsage;

  @NotNull
  public String getName() {
    return "Create Step Definition";
  }

  @NotNull
  public String getFamilyName() {
    return getName();
  }

  @Nullable
  public static PsiDirectory findStepDefinitionDirectory(@NotNull final PsiFile featureFile) {
    final PsiDirectory psiFeatureDir = featureFile.getContainingDirectory();
    assert psiFeatureDir != null;

    VirtualFile featureDir = psiFeatureDir.getVirtualFile();
    VirtualFile contentRoot = ProjectRootManager.getInstance(featureFile.getProject()).getFileIndex().getContentRootForFile(featureDir);
    while (featureDir != null && featureDir != contentRoot && featureDir.findChild(CucumberUtil.STEP_DEFINITIONS_DIR_NAME) == null) {
      featureDir = featureDir.getParent();
    }
    if (featureDir != null) {
      VirtualFile stepsDir = featureDir.findChild(CucumberUtil.STEP_DEFINITIONS_DIR_NAME);
      if (stepsDir != null) {
        return PsiManager.getInstance(featureFile.getProject()).findDirectory(stepsDir);
      }
    }
    return null;
  }

  public List<PsiFile> getStepDefinitionFiles(final PsiFile featureFile) {
    final List<PsiDirectory> stepDefsRoots = new ArrayList<PsiDirectory>();

    PsiDirectory dir = findStepDefinitionDirectory(featureFile);
    if (dir != null) {
      stepDefsRoots.add(dir);
    }
    CucumberStepsIndex.getInstance(featureFile.getProject()).findRelatedStepDefsRoots(featureFile, stepDefsRoots, false);

    final List<PsiFile> stepDefs = new ArrayList<PsiFile>();
    for (PsiDirectory root : stepDefsRoots) {
      stepDefs.addAll(CucumberStepsIndex.getInstance(featureFile.getProject()).gatherStepDefinitionsFilesFromDirectory(root));
    }
    return !stepDefs.isEmpty() ? stepDefs : Collections.<PsiFile>emptyList();
  }


  public void applyFix(@NotNull final Project project, @NotNull ProblemDescriptor descriptor) {
    final GherkinStep step = (GherkinStep) descriptor.getPsiElement();
    final PsiFile featureFile = step.getContainingFile();
    // TODO + step defs files from other content roots
    final List<PsiFile> files = getStepDefinitionFiles(featureFile);
    if (files.size() > 0) {
      files.add(null);
      BaseListPopupStep<PsiFile> popupStep = new BaseListPopupStep<PsiFile>("Choose step definition file", files) {
        @NotNull
        @Override
        public String getTextFor(PsiFile value) {
          if (value == null) {
            return "Create New File";
          }

          final VirtualFile file = value.getVirtualFile();
          assert file != null;

          VirtualFile parent = file.getParent();
          // if file is direct child of step definitions dir
          if (parent != null && CucumberUtil.STEP_DEFINITIONS_DIR_NAME.equals(parent.getName())) {
            return file.getName();
          }

          // in subfolder
          final List<String> dirsReversed= new ArrayList<String>();
          while (parent != null) {
            final String name = parent.getName();
            if (CucumberUtil.STEP_DEFINITIONS_DIR_NAME.equals(name)) {
              break;
            }
            dirsReversed.add(name);
            parent = parent.getParent();
          }
          final StringBuilder buf = StringBuilderSpinAllocator.alloc();
          try {
            for (int i = dirsReversed.size() - 1; i >= 0; i--) {
              buf.append(dirsReversed.get(i)).append(File.separatorChar);
            }
            buf.append(file.getName());
            return buf.toString();
          }
          finally {
            StringBuilderSpinAllocator.dispose(buf);
          }
        }

        @Override
        public Icon getIconFor(PsiFile value) {
          return value == null ? NEW_FILE_ICON : value.getIcon(0);
        }

        @Override
        public PopupStep onChosen(final PsiFile selectedValue, boolean finalChoice) {
          return doFinalStep(new Runnable() {
            public void run() {
              createFileOrStepDefinition(step, selectedValue);
            }
          });
        }
      };
      JBPopupFactory.getInstance().createListPopup(popupStep).showInBestPositionFor(DataManager.getInstance().getDataContext());
    }
    else {
      createFileOrStepDefinition(step, null);
    }
  }

  private static void createStepDefinitionFile(final GherkinStep step) {
    final PsiFile featureFile = step.getContainingFile();
    assert featureFile != null;

    final PsiDirectory dir = getOrCreateStepDefinitionsDirectory(step, featureFile);

    final CreateStepDefinitionFileModel model = askUserForFilePath(step, dir);
    if (model == null) {
      return;
    }
    String filePath = FileUtil.toSystemDependentName(model.getFilePath());
    final FileType fileType = model.getSelectedFileType();

    // show error if file already exists
    if (LocalFileSystem.getInstance().findFileByPath(filePath) == null) {
      final String parendDirPath = model.getDirectory().getVirtualFile().getPath();

      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          new WriteCommandAction.Simple(step.getProject(),
                                        CucumberBundle.message("cucumber.quick.fix.create.step.command.name.create")) {
            @Override
            protected void run() throws Throwable {

              final VirtualFile parentDir = VfsUtil.createDirectories(parendDirPath);
              final PsiDirectory parentPsiDir = PsiManager.getInstance(getProject()).findDirectory(parentDir);
              assert parentPsiDir != null;
              PsiFile newFile = CucumberStepsIndex.getInstance(step.getProject()).createStepDefinitionFile(dir, model.getFileName(), fileType);
              createStepDefinition(step, newFile);
            }
          }.execute();
        }
      });
    }
    else {
      Messages.showErrorDialog(step.getProject(),
                               CucumberBundle.message("cucumber.quick.fix.create.step.error.already.exist.msg", filePath),
                               CucumberBundle.message("cucumber.quick.fix.create.step.file.name.title"));
    }
  }

  private static void createFileOrStepDefinition(final GherkinStep step, final PsiFile file) {
    if (file == null) {
      createStepDefinitionFile(step);
    }
    else {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          new WriteCommandAction.Simple(step.getProject()) {
            @Override
            protected void run() throws Throwable {
              createStepDefinition(step, file);
            }
          }.execute();
        }
      });
    }
  }

  @Nullable
  private static CreateStepDefinitionFileModel askUserForFilePath(@NotNull final GherkinStep step,
                                           @NotNull final PsiDirectory dir) {

    final InputValidator validator = new InputValidator() {
      public boolean checkInput(final String filePath) {
        return !StringUtil.isEmpty(filePath);
      }

      public boolean canClose(final String fileName) {
        return true;
      }
    };

    Map<FileType,String> supportedFileTypesAndDefaultFileNames =
      CucumberStepsIndex.getInstance(step.getProject()).getSupportedFileTypesAndDefaultFileNames();
    CreateStepDefinitionFileModel model =
      new CreateStepDefinitionFileModel(supportedFileTypesAndDefaultFileNames, dir);
    CreateStepDefinitionFileDialog createStepDefinitionFileDialog = new CreateStepDefinitionFileDialog(step.getProject(), model, validator);
    createStepDefinitionFileDialog.show();

    if (createStepDefinitionFileDialog.isOK()) {
      return model;
    } else {
      return null;
    }
  }

  private static PsiDirectory getOrCreateStepDefinitionsDirectory(@NotNull final GherkinStep step,
                                                                  final PsiFile featureFile) {
    final PsiDirectory dir = findStepDefinitionDirectory(featureFile);
    if (dir == null) {
      final PsiDirectory featureParentDir = featureFile.getParent();
      assert featureParentDir != null;

      final Ref<PsiDirectory> dirRef = new Ref<PsiDirectory>();
      new WriteCommandAction.Simple(step.getProject(),
                                    CucumberBundle.message("cucumber.quick.fix.create.step.command.name.add")) {
        @Override
        protected void run() throws Throwable {
          // create steps_definitions directory
          dirRef.set(featureParentDir.createSubdirectory(CucumberUtil.STEP_DEFINITIONS_DIR_NAME));
        }
      }.execute();

      return dirRef.get();
    }
    return dir;
  }

  private static void createStepDefinition(GherkinStep step, PsiFile file) {
    if (!CodeInsightUtilBase.prepareFileForWrite(file)) {
      return;
    }

    CucumberJvmExtensionPoint[] epList = CucumberJvmExtensionPoint.EP_NAME.getExtensions();
    for (CucumberJvmExtensionPoint ep : epList) {
      if (ep.createStepDefinition(step, file)) {
        return;
      }
    }
  }
}
