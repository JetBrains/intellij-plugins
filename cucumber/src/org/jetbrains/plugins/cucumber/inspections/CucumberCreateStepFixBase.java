package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
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
import com.intellij.util.containers.ContainerUtilRt;
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
import java.util.*;

/**
 * User: Andrey.Vokin
 * Date: 10/8/2014.
 */
public abstract class CucumberCreateStepFixBase implements LocalQuickFix {
  private static final FileFrameworkComparator FILE_FRAMEWORK_COMPARATOR = new FileFrameworkComparator();

  protected abstract void createStepOrSteps(GherkinStep step, PsiFile file);

  @NotNull
  public String getFamilyName() {
    return getName();
  }

  public void applyFix(@NotNull final Project project, @NotNull ProblemDescriptor descriptor) {
    final GherkinStep step = (GherkinStep)descriptor.getPsiElement();
    final GherkinFile featureFile = (GherkinFile)step.getContainingFile();
    // TODO + step defs pairs from other content roots
    //Tree is used to prevent duplicates (if several frameworks take care about one file)
    final SortedSet<Pair<PsiFile, BDDFrameworkType>> pairSortedSet = ContainerUtilRt.newTreeSet(FILE_FRAMEWORK_COMPARATOR);
    pairSortedSet.addAll(getStepDefinitionContainers(featureFile));
    final List<Pair<PsiFile, BDDFrameworkType>> pairs = ContainerUtil.newArrayList(pairSortedSet);
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
            return doFinalStep(new Runnable() {
              public void run() {
                createStepOrSteps(step, selectedValue.first);
              }
            });
          }
        });

      if (!ApplicationManager.getApplication().isUnitTestMode()) {
        popupStep.showCenteredInCurrentWindow(step.getProject());
      } else {
        new WriteCommandAction.Simple(step.getProject()) {
          @Override
          protected void run() throws Throwable {
            createStepOrSteps(step, pairs.get(1).getFirst());
          }
        }.execute();
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

  private void createStepDefinitionFile(final GherkinStep step) {
    final PsiFile featureFile = step.getContainingFile();
    assert featureFile != null;

    final CreateStepDefinitionFileModel model = askUserForFilePath(step);
    if (model == null) {
      return;
    }
    String filePath = FileUtil.toSystemDependentName(model.getFilePath());
    final BDDFrameworkType frameworkType = model.getSelectedFileType();

    // show error if file already exists
    if (LocalFileSystem.getInstance().findFileByPath(filePath) == null) {
      final String parentDirPath = model.getDirectory().getVirtualFile().getPath();

      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          new WriteCommandAction.Simple(step.getProject(), CucumberBundle.message("cucumber.quick.fix.create.step.command.name.create")) {
            @Override
            protected void run() throws Throwable {
              final VirtualFile parentDir = VfsUtil.createDirectories(parentDirPath);
              final Project project = getProject();
              assert project != null;
              final PsiDirectory parentPsiDir = PsiManager.getInstance(project).findDirectory(parentDir);
              assert parentPsiDir != null;
              PsiFile newFile = CucumberStepsIndex.getInstance(step.getProject())
                .createStepDefinitionFile(model.getDirectory(), model.getFileName(), frameworkType);
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

  protected void createFileOrStepDefinition(final GherkinStep step, @Nullable final PsiFile psiFile) {
    if (psiFile == null) {
      createStepDefinitionFile(step);
    }
    else {
      new WriteCommandAction.Simple(step.getProject()) {
        @Override
        protected void run() throws Throwable {
          createStepDefinition(step, psiFile);
        }
      }.execute();
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

    Map<BDDFrameworkType, String> supportedFileTypesAndDefaultFileNames = new HashMap<BDDFrameworkType, String>();
    Map<BDDFrameworkType, PsiDirectory> fileTypeToDefaultDirectoryMap = new HashMap<BDDFrameworkType, PsiDirectory>();
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
    createStepDefinitionFileDialog.show();

    if (createStepDefinitionFileDialog.isOK()) {
      return model;
    }
    else {
      return null;
    }
  }

  protected void createStepDefinition(GherkinStep step, PsiFile file) {
    if (!FileModificationService.getInstance().prepareFileForWrite(file)) {
      return;
    }

    CucumberJvmExtensionPoint[] epList = CucumberJvmExtensionPoint.EP_NAME.getExtensions();
    for (CucumberJvmExtensionPoint ep : epList) {
      if (ep.getStepDefinitionCreator().createStepDefinition(step, file)) {
        return;
      }
    }
  }

  /**
   * Compares two paris of file-frameworkType using file name as key
   */
  private static class FileFrameworkComparator implements Comparator<Pair<PsiFile, BDDFrameworkType>> {
    @Override
    public int compare(Pair<PsiFile, BDDFrameworkType> pair1, Pair<PsiFile, BDDFrameworkType> pair2) {
      if (pair1 == null && pair2 == null) {
        return 0;
      } else if (pair1 == null) {
        return -1;
      } else if (pair2 == null) {
        return 1;
      }

      final VirtualFile virtualFile1 = pair1.getFirst().getVirtualFile();
      final VirtualFile virtualFile2 = pair2.getFirst().getVirtualFile();
      if (virtualFile1 == null && virtualFile2 == null) {
        return 0;
      }
      else if (virtualFile1 == null) {
        return -1;
      }
      else if (virtualFile2 == null) {
        return 1;
      }
      return virtualFile1.getPath().compareTo(virtualFile2.getPath());
    }
  }
}
