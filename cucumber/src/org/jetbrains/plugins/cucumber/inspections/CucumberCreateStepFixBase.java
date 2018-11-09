// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
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
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.*;
import org.jetbrains.plugins.cucumber.inspections.model.CreateStepDefinitionFileModel;
import org.jetbrains.plugins.cucumber.inspections.ui.CreateStepDefinitionFileDialog;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CucumberCreateStepFixBase implements LocalQuickFix {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.cucumber.inspections.CucumberCreateStepFixBase");
  protected abstract void createStepOrSteps(GherkinStep step, @NotNull final CucumberStepDefinitionCreationContext fileAndFrameworkType);

  @Override
  public boolean startInWriteAction() {
    return false;
  }

  @Override
  @NotNull
  public String getFamilyName() {
    return getName();
  }

  @Override
  public void applyFix(@NotNull final Project project, @NotNull ProblemDescriptor descriptor) {
    final GherkinStep step = (GherkinStep)descriptor.getPsiElement();
    final GherkinFile featureFile = (GherkinFile)step.getContainingFile();
    // TODO + step defs pairs from other content roots
    final List<CucumberStepDefinitionCreationContext> pairs = ContainerUtil.newArrayList(getStepDefinitionContainers(featureFile));
    if (!pairs.isEmpty()) {
      pairs.add(0, new CucumberStepDefinitionCreationContext());

      final JBPopupFactory popupFactory = JBPopupFactory.getInstance();
      final ListPopup popupStep =
        popupFactory.createListPopup(new BaseListPopupStep<CucumberStepDefinitionCreationContext>(
          CucumberBundle.message("choose.step.definition.file"), ContainerUtil.newArrayList(pairs)) {
          @Override
          public boolean isSpeedSearchEnabled() {
            return true;
          }

          @NotNull
          @Override
          public String getTextFor(CucumberStepDefinitionCreationContext value) {
            if (value.getPsiFile() == null) {
              return CucumberBundle.message("create.new.file");
            }

            PsiFile psiFile = value.getPsiFile();
            final VirtualFile file = value.getPsiFile().getVirtualFile();
            assert file != null;

            CucumberStepsIndex stepsIndex = CucumberStepsIndex.getInstance(psiFile.getProject());
            StepDefinitionCreator stepDefinitionCreator = stepsIndex.getExtensionMap().get(value.getFrameworkType()).getStepDefinitionCreator();
            return stepDefinitionCreator.getStepDefinitionFilePath(psiFile);
          }

          @Override
          public Icon getIconFor(CucumberStepDefinitionCreationContext value) {
            PsiFile psiFile = value.getPsiFile();
            return psiFile == null ? AllIcons.Actions.IntentionBulb : psiFile.getIcon(0);
          }

          @Override
          public PopupStep onChosen(final CucumberStepDefinitionCreationContext selectedValue, boolean finalChoice) {
            return doFinalStep(() -> createStepOrSteps(step, selectedValue));
          }
        });

      if (!ApplicationManager.getApplication().isUnitTestMode()) {
        popupStep.showCenteredInCurrentWindow(step.getProject());
      } else {
        createStepOrSteps(step, pairs.get(1));
      }
    }
    else {
      createFileOrStepDefinition(step, new CucumberStepDefinitionCreationContext());
    }
  }

  public static Set<CucumberStepDefinitionCreationContext> getStepDefinitionContainers(@NotNull final GherkinFile featureFile) {
    CucumberStepsIndex index = CucumberStepsIndex.getInstance(featureFile.getProject());
    final Set<CucumberStepDefinitionCreationContext> result = index.getStepDefinitionContainers(featureFile);
    for (CucumberStepDefinitionCreationContext item : result) {
      if (index.getExtensionMap().get(item.getFrameworkType()) == null) {
        result.remove(item);
      }
    }

    return result;
  }

  private static void createStepDefinitionFile(final GherkinStep step, @NotNull final CucumberStepDefinitionCreationContext context) {
    final PsiFile featureFile = step.getContainingFile();
    assert featureFile != null;

    final CreateStepDefinitionFileModel model = askUserForFilePath(step);
    if (model == null) {
      return;
    }
    String filePath = FileUtil.toSystemDependentName(model.getFilePath());
    final BDDFrameworkType frameworkType = model.getSelectedFileType();
    context.setFrameworkType(frameworkType);

    // show error if file already exists
    Project project = step.getProject();
    if (LocalFileSystem.getInstance().findFileByPath(filePath) == null) {
      final String parentDirPath = model.getDirectory().getVirtualFile().getPath();

      ApplicationManager.getApplication().invokeLater(
        () -> CommandProcessor.getInstance().executeCommand(project, () -> {
          try {
            VirtualFile parentDir = VfsUtil.createDirectories(parentDirPath);
            PsiDirectory parentPsiDir = PsiManager.getInstance(project).findDirectory(parentDir);
            assert parentPsiDir != null;
            PsiFile newFile = CucumberStepsIndex.getInstance(project)
              .createStepDefinitionFile(model.getDirectory(), model.getFileName(), frameworkType);
            createStepDefinition(step, new CucumberStepDefinitionCreationContext(newFile, frameworkType));
            context.setPsiFile(newFile);
          }
          catch (IOException e) {
            LOG.error(e);
          }
        }, CucumberBundle.message("cucumber.quick.fix.create.step.command.name.create"), null));
    }
    else {
      Messages.showErrorDialog(project,
                               CucumberBundle.message("cucumber.quick.fix.create.step.error.already.exist.msg", filePath),
                               CucumberBundle.message("cucumber.quick.fix.create.step.file.name.title"));
    }
  }

  protected void createFileOrStepDefinition(final GherkinStep step, @NotNull final CucumberStepDefinitionCreationContext context) {
    if (context.getFrameworkType() == null) {
      createStepDefinitionFile(step, context);
    }
    else {
      createStepDefinition(step, context);
    }
  }

  @Nullable
  private static CreateStepDefinitionFileModel askUserForFilePath(@NotNull final GherkinStep step) {
    final InputValidator validator = new InputValidator() {
      @Override
      public boolean checkInput(final String filePath) {
        return !StringUtil.isEmpty(filePath);
      }

      @Override
      public boolean canClose(final String fileName) {
        return true;
      }
    };

    Map<BDDFrameworkType, String> supportedFileTypesAndDefaultFileNames = new HashMap<>();
    Map<BDDFrameworkType, PsiDirectory> fileTypeToDefaultDirectoryMap = new HashMap<>();
    for (CucumberJvmExtensionPoint e : CucumberJvmExtensionPoint.EP_NAME.getExtensionList()) {
      if (e instanceof OptionalStepDefinitionExtensionPoint) {
        // Skip if framework file creation support is optional
        if (!((OptionalStepDefinitionExtensionPoint)e).participateInStepDefinitionCreation(step)) {
          continue;
        }
      }
      supportedFileTypesAndDefaultFileNames.put(e.getStepFileType(), e.getStepDefinitionCreator().getDefaultStepFileName(step));
      fileTypeToDefaultDirectoryMap.put(e.getStepFileType(), e.getStepDefinitionCreator().getDefaultStepDefinitionFolder(step));
    }

    CreateStepDefinitionFileModel model =
      new CreateStepDefinitionFileModel(step.getProject(), supportedFileTypesAndDefaultFileNames, fileTypeToDefaultDirectoryMap);
    CreateStepDefinitionFileDialog createStepDefinitionFileDialog = new CreateStepDefinitionFileDialog(step.getProject(), model, validator);
    if (createStepDefinitionFileDialog.showAndGet()) {
      return model;
    }
    else {
      return null;
    }
  }

  private static void createStepDefinition(GherkinStep step, @NotNull final CucumberStepDefinitionCreationContext context) {
    CucumberStepsIndex stepsIndex = CucumberStepsIndex.getInstance(step.getProject());
    StepDefinitionCreator stepDefCreator = stepsIndex.getExtensionMap().get(context.getFrameworkType()).getStepDefinitionCreator();
    PsiFile file = context.getPsiFile();
    WriteCommandAction.runWriteCommandAction(step.getProject(), null, null, () -> stepDefCreator.createStepDefinition(step, file), file);
  }
}
