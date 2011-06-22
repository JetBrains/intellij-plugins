package com.google.jstestdriver.idea.execution;

import org.jetbrains.annotations.NotNull;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.TestType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.refactoring.listeners.RefactoringElementListener;

public class JstdRunConfigurationRefactoringHandler {

  private final JstdRunConfiguration myRunConfiguration;

  public JstdRunConfigurationRefactoringHandler(JstdRunConfiguration runConfiguration) {
    myRunConfiguration = runConfiguration;
  }

  public RefactoringElementListener getRefactoringElementListener(PsiElement element) {
    final TestType testType = myRunConfiguration.getRunSettings().getTestType();
    if (testType == TestType.ALL_CONFIGS_IN_DIRECTORY) {
      return provideRefactoringDirectoryListener(element);
    } else if (testType == TestType.CONFIG_FILE) {
      return provideRefactoringConfigFileListener(element);
    } else if (testType == TestType.JS_FILE) {
      return provideRefactoringJsFileListener(element);
    }
    return null;
  }

  private RefactoringElementListener provideRefactoringDirectoryListener(PsiElement element) {
    final JstdRunSettings currentRunSettings = myRunConfiguration.getRunSettings();
    String directoryPath = asFileSystemItem(element);
    if (currentRunSettings.getDirectory().equals(directoryPath)) {
      return new RefactoringElementListener() {

        @Override
        public void elementMoved(@NotNull PsiElement newElement) {
          refactorIt(newElement);
        }

        @Override
        public void elementRenamed(@NotNull PsiElement newElement) {
          refactorIt(newElement);
        }

        private void refactorIt(PsiElement newElement) {
          System.out.println("Running provideRefactoringDirectoryListener ...");
          String newDirectoryPath = asFileSystemItem(newElement);
          if (newDirectoryPath != null) {
            JstdRunSettings newRunSettings = new JstdRunSettings.Builder(currentRunSettings)
                .setDirectory(newDirectoryPath)
                .build();
            updateRunSettings(newRunSettings);
          }
        }

      };
    }
    return null;
  }

  private RefactoringElementListener provideRefactoringConfigFileListener(PsiElement element) {
    final JstdRunSettings currentRunSettings = myRunConfiguration.getRunSettings();
    String configFilePath = asFileSystemItem(element);
    if (currentRunSettings.getConfigFile().equals(configFilePath)) {
      return new RefactoringElementListener() {

        @Override
        public void elementMoved(@NotNull PsiElement newElement) {
          refactorIt(newElement);
        }

        @Override
        public void elementRenamed(@NotNull PsiElement newElement) {
          refactorIt(newElement);
        }

        private void refactorIt(PsiElement newElement) {
          String newConfigFilePath = asFileSystemItem(newElement);
          if (newConfigFilePath != null) {
            JstdRunSettings newRunSettings = new JstdRunSettings.Builder(currentRunSettings)
                .setConfigFile(newConfigFilePath)
                .build();
            updateRunSettings(newRunSettings);
          }
        }

      };
    }
    return null;
  }

  private RefactoringElementListener provideRefactoringJsFileListener(PsiElement element) {
    final JstdRunSettings currentRunSettings = myRunConfiguration.getRunSettings();
    String jsFilePath = asFileSystemItem(element);
    if (currentRunSettings.getJsFilePath().equals(jsFilePath)) {
      return new RefactoringElementListener() {

        @Override
        public void elementMoved(@NotNull PsiElement newElement) {
          refactorIt(newElement);
        }

        @Override
        public void elementRenamed(@NotNull PsiElement newElement) {
          refactorIt(newElement);
        }

        private void refactorIt(PsiElement newElement) {
          String newJsFilePath = asFileSystemItem(newElement);
          if (newJsFilePath != null) {
            JstdRunSettings newRunSettings = new JstdRunSettings.Builder(currentRunSettings)
                .setJSFilePath(newJsFilePath)
                .build();
            updateRunSettings(newRunSettings);
          }
        }

      };
    }
    return null;
  }

  private void updateRunSettings(JstdRunSettings newRunSettings) {
    boolean generatedName = myRunConfiguration.isGeneratedName();
    myRunConfiguration.setRunSettings(newRunSettings);
    if (generatedName) {
      myRunConfiguration.setName(myRunConfiguration.suggestedName());
    }
  }

  private String asFileSystemItem(PsiElement element) {
    if (element instanceof PsiFileSystemItem) {
      PsiFileSystemItem psiFileSystemItem = (PsiFileSystemItem) element;
      VirtualFile virtualFile = psiFileSystemItem.getVirtualFile();
      if (virtualFile != null) {
        return virtualFile.getPath();
      }
    }
    return null;
  }

}
