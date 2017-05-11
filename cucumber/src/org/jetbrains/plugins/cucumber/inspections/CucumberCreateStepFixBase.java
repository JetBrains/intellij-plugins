package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashMap;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CucumberCreateStepFixBase implements LocalQuickFix {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.cucumber.inspections.CucumberCreateStepFixBase");
  protected abstract void createStepOrSteps(GherkinStep step, @Nullable final Pair<PsiFile, BDDFrameworkType> fileAndFrameworkType);

  @Override
  public boolean startInWriteAction() {
    return false;
  }

  @NotNull
  public String getFamilyName() {
    return getName();
  }

  public void applyFix(@NotNull final Project project, @NotNull ProblemDescriptor descriptor) {
    final GherkinStep step = (GherkinStep)descriptor.getPsiElement();
    final GherkinFile featureFile = (GherkinFile)step.getContainingFile();
    // TODO + step defs pairs from other content roots
    final List<Pair<PsiFile, BDDFrameworkType>> pairs = ContainerUtil.newArrayList(getStepDefinitionContainers(featureFile));
    if (!pairs.isEmpty()) {
      pairs.add(0, null);

      final JBPopupFactory popupFactory = JBPopupFactory.getInstance();
      final ListPopup popupStep =
        popupFactory.createListPopup(new BaseListPopupStep<Pair<PsiFile, BDDFrameworkType>>(
          CucumberBundle.message("choose.step.definition.file"), ContainerUtil.newArrayList(pairs)) {
          @Override
          public boolean isSpeedSearchEnabled() {
            return true;
          }

          @NotNull
          @Override
          public String getTextFor(Pair<PsiFile, BDDFrameworkType> value) {
            if (value == null) {
              return CucumberBundle.message("create.new.file");
            }

            final VirtualFile file = value.getFirst().getVirtualFile();
            assert file != null;

            CucumberStepsIndex stepsIndex = CucumberStepsIndex.getInstance(value.getFirst().getProject());
            StepDefinitionCreator stepDefinitionCreator = stepsIndex.getExtensionMap().get(value.getSecond()).getStepDefinitionCreator();
            return stepDefinitionCreator.getStepDefinitionFilePath(value.getFirst());
          }

          @Override
          public Icon getIconFor(Pair<PsiFile, BDDFrameworkType> value) {
            return value == null ? AllIcons.Actions.CreateFromUsage : value.getFirst().getIcon(0);
          }

          @Override
          public PopupStep onChosen(final Pair<PsiFile, BDDFrameworkType> selectedValue, boolean finalChoice) {
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
      createFileOrStepDefinition(step, null);
    }
  }

  public static Set<Pair<PsiFile, BDDFrameworkType>> getStepDefinitionContainers(@NotNull final GherkinFile featureFile) {
    final Set<Pair<PsiFile, BDDFrameworkType>> result =
      CucumberStepsIndex.getInstance(featureFile.getProject()).getStepDefinitionContainers(featureFile);

    CucumberStepsIndex stepsIndex = CucumberStepsIndex.getInstance(featureFile.getProject());
    for (Pair<PsiFile, BDDFrameworkType> item : result) {
      if (stepsIndex.getExtensionMap().get(item.getSecond()) == null) {
        result.remove(item);
      }
    }

    return result;
  }

  private static void createStepDefinitionFile(final GherkinStep step) {
    final PsiFile featureFile = step.getContainingFile();
    assert featureFile != null;

    final CreateStepDefinitionFileModel model = askUserForFilePath(step);
    if (model == null) {
      return;
    }
    String filePath = FileUtil.toSystemDependentName(model.getFilePath());
    final BDDFrameworkType frameworkType = model.getSelectedFileType();

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
            Pair<PsiFile, BDDFrameworkType> pair = Pair.create(newFile, frameworkType);
            createStepDefinition(step, pair);
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

  protected void createFileOrStepDefinition(final GherkinStep step, @Nullable final Pair<PsiFile, BDDFrameworkType> fileAndFrameworkType) {
    if (fileAndFrameworkType == null) {
      createStepDefinitionFile(step);
    }
    else {
      createStepDefinition(step, fileAndFrameworkType);
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

    Map<BDDFrameworkType, String> supportedFileTypesAndDefaultFileNames = new HashMap<>();
    Map<BDDFrameworkType, PsiDirectory> fileTypeToDefaultDirectoryMap = new HashMap<>();
    for (CucumberJvmExtensionPoint e : Extensions.getExtensions(CucumberJvmExtensionPoint.EP_NAME)) {
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

  private static void createStepDefinition(GherkinStep step, @NotNull final Pair<PsiFile, BDDFrameworkType> fileAndFrameworkType) {
    CucumberStepsIndex stepsIndex = CucumberStepsIndex.getInstance(step.getProject());
    StepDefinitionCreator stepDefCreator = stepsIndex.getExtensionMap().get(fileAndFrameworkType.getSecond()).getStepDefinitionCreator();
    PsiFile file = fileAndFrameworkType.first;
    WriteCommandAction.runWriteCommandAction(step.getProject(), null, null, () -> stepDefCreator.createStepDefinition(step, file), file);
  }
}
