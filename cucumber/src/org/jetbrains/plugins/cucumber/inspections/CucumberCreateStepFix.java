package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashMap;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.inspections.model.CreateStepDefinitionFileModel;
import org.jetbrains.plugins.cucumber.inspections.ui.CreateStepDefinitionFileDialog;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;

import javax.swing.*;
import java.util.*;

/**
 * @author yole
 */
public class CucumberCreateStepFix implements LocalQuickFix {

  @NotNull
  public String getName() {
    return "Create Step Definition";
  }

  @NotNull
  public String getFamilyName() {
    return getName();
  }

  public static Set<PsiFile> getStepDefinitionContainers(final PsiFile featureFile) {
    final List<PsiDirectory> stepDefsRoots = new ArrayList<PsiDirectory>();
    final Module module = ObjectUtils.assertNotNull(ModuleUtil.findModuleForPsiElement(featureFile));
    CucumberStepsIndex.getInstance(featureFile.getProject())
      .findRelatedStepDefsRoots(featureFile, module, stepDefsRoots, new HashSet<String>());

    final Set<PsiFile> stepDefs = ContainerUtil.newHashSet();
    for (PsiDirectory root : stepDefsRoots) {
      stepDefs.addAll(CucumberStepsIndex.getInstance(featureFile.getProject()).gatherStepDefinitionsFilesFromDirectory(root, true));
    }
    return stepDefs.isEmpty() ? Collections.<PsiFile>emptySet() : stepDefs;
  }

  public void applyFix(@NotNull final Project project, @NotNull ProblemDescriptor descriptor) {
    final GherkinStep step = (GherkinStep)descriptor.getPsiElement();
    final PsiFile featureFile = step.getContainingFile();
    // TODO + step defs files from other content roots
    final List<PsiFile> files = ContainerUtil.newArrayList(getStepDefinitionContainers(featureFile));
    if (files.size() > 0) {
      files.add(null);

      Collections.sort(files, new Comparator<PsiFile>() {
        @Override
        public int compare(PsiFile file, PsiFile file2) {
          if (file == null && file2 == null) {
            return 0;
          } else if (file == null && file2 != null) {
            return -1;
          } else if (file != null && file2 == null) {
            return 1;
          }

          return file.getName().compareTo(file2.getName());
        }
      });

      final JBPopupFactory popupFactory = JBPopupFactory.getInstance();

      final ListPopup popupStep =
        popupFactory.createListPopup(new BaseListPopupStep<PsiFile>(
          CucumberBundle.message("choose.step.definition.file"), files) {

          @Override
          public boolean isSpeedSearchEnabled() {
            return true;
          }

          @NotNull
          @Override
          public String getTextFor(PsiFile value) {
            if (value == null) {
              return CucumberBundle.message("create.new.file");
            }

            final VirtualFile file = value.getVirtualFile();
            assert file != null;

            CucumberStepsIndex stepsIndex = CucumberStepsIndex.getInstance(value.getProject());
            StepDefinitionCreator stepDefinitionCreator = stepsIndex.getExtensionMap().get(file.getFileType()).getStepDefinitionCreator();
            return stepDefinitionCreator.getStepDefinitionFilePath(value);
          }

          @Override
          public Icon getIconFor(PsiFile value) {
            return value == null ? AllIcons.Actions.CreateFromUsage : value.getIcon(0);
          }

          @Override
          public PopupStep onChosen(final PsiFile selectedValue, boolean finalChoice) {
            return doFinalStep(new Runnable() {
              public void run() {
                createFileOrStepDefinition(step, selectedValue);
              }
            });
          }
        });

      popupStep.showCenteredInCurrentWindow(step.getProject());
    }
    else {
      createFileOrStepDefinition(step, null);
    }
  }

  private static void createStepDefinitionFile(final GherkinStep step) {
    final PsiFile featureFile = step.getContainingFile();
    assert featureFile != null;

    final CreateStepDefinitionFileModel model = askUserForFilePath(step);
    if (model == null) {
      return;
    }
    String filePath = FileUtil.toSystemDependentName(model.getFilePath());
    final FileType fileType = model.getSelectedFileType();

    // show error if file already exists
    if (LocalFileSystem.getInstance().findFileByPath(filePath) == null) {
      final String parentDirPath = model.getDirectory().getVirtualFile().getPath();

      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          new WriteCommandAction.Simple(step.getProject(), CucumberBundle.message("cucumber.quick.fix.create.step.command.name.create")) {
            @Override
            protected void run() throws Throwable {
              final VirtualFile parentDir = VfsUtil.createDirectories(parentDirPath);
              final PsiDirectory parentPsiDir = PsiManager.getInstance(getProject()).findDirectory(parentDir);
              assert parentPsiDir != null;
              PsiFile newFile = CucumberStepsIndex.getInstance(step.getProject())
                .createStepDefinitionFile(model.getDirectory(), model.getFileName(), fileType);
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

  private static void createFileOrStepDefinition(final GherkinStep step, @Nullable final PsiFile file) {
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
  private static CreateStepDefinitionFileModel askUserForFilePath(@NotNull final GherkinStep step) {
    final InputValidator validator = new InputValidator() {
      public boolean checkInput(final String filePath) {
        return !StringUtil.isEmpty(filePath);
      }

      public boolean canClose(final String fileName) {
        return true;
      }
    };

    Map<FileType, String> supportedFileTypesAndDefaultFileNames = new HashMap<FileType, String>();
    Map<FileType, PsiDirectory> fileTypeToDefaultDirectoryMap = new HashMap<FileType, PsiDirectory>();
    for (CucumberJvmExtensionPoint e : Extensions.getExtensions(CucumberJvmExtensionPoint.EP_NAME)) {
      supportedFileTypesAndDefaultFileNames.put(e.getStepFileType(), e.getDefaultStepFileName());
      fileTypeToDefaultDirectoryMap.put(e.getStepFileType(), e.getStepDefinitionCreator().getDefaultStepDefinitionFolder(step));
    }

    CreateStepDefinitionFileModel model =
      new CreateStepDefinitionFileModel(supportedFileTypesAndDefaultFileNames, fileTypeToDefaultDirectoryMap);
    CreateStepDefinitionFileDialog createStepDefinitionFileDialog = new CreateStepDefinitionFileDialog(step.getProject(), model, validator);
    createStepDefinitionFileDialog.show();

    if (createStepDefinitionFileDialog.isOK()) {
      return model;
    }
    else {
      return null;
    }
  }

  private static void createStepDefinition(GherkinStep step, PsiFile file) {
    if (!CodeInsightUtilBase.prepareFileForWrite(file)) {
      return;
    }

    CucumberJvmExtensionPoint[] epList = CucumberJvmExtensionPoint.EP_NAME.getExtensions();
    for (CucumberJvmExtensionPoint ep : epList) {
      if (ep.getStepDefinitionCreator().createStepDefinition(step, file)) {
        return;
      }
    }
  }
}
