package com.google.jstestdriver.idea.execution;

import org.jetbrains.annotations.NotNull;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.TestType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import org.jetbrains.annotations.Nullable;

public class JstdRunConfigurationRefactoringHandler {

  private final JstdRunConfiguration myRunConfiguration;

  public JstdRunConfigurationRefactoringHandler(@NotNull JstdRunConfiguration runConfiguration) {
    myRunConfiguration = runConfiguration;
  }

  @Nullable
  public RefactoringElementListener getRefactoringElementListener(@Nullable PsiElement element) {
    if (element == null) {
      return null;
    }
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

  @Nullable
  private RefactoringElementListener provideRefactoringDirectoryListener(@NotNull PsiElement element) {
    final JstdRunSettings currentRunSettings = myRunConfiguration.getRunSettings();
    VirtualFile directory = toVirtualFile(element);
    if (directory != null && currentRunSettings.getDirectory().equals(directory.getPath())) {
      return new RefactoringElementListener() {

        @Override
        public void elementMoved(@NotNull PsiElement newElement) {
          refactorIt(newElement);
        }

        @Override
        public void elementRenamed(@NotNull PsiElement newElement) {
          refactorIt(newElement);
        }

        private void refactorIt(@NotNull PsiElement newElement) {
          VirtualFile newDirectory = toVirtualFile(newElement);
          if (newDirectory != null) {
            JstdRunSettings newRunSettings = new JstdRunSettings.Builder(currentRunSettings)
                .setDirectory(newDirectory.getPath())
                .build();
            updateRunSettings(newRunSettings);
          }
        }

      };
    }
    return null;
  }

  @Nullable
  private RefactoringElementListener provideRefactoringConfigFileListener(PsiElement element) {
    final JstdRunSettings currentRunSettings = myRunConfiguration.getRunSettings();
    VirtualFile configFile = toVirtualFile(element);
    if (configFile != null && currentRunSettings.getConfigFile().equals(configFile.getPath())) {
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
          VirtualFile newConfigFile = toVirtualFile(newElement);
          if (newConfigFile != null) {
            JstdRunSettings newRunSettings = new JstdRunSettings.Builder(currentRunSettings)
                .setConfigFile(newConfigFile.getPath())
                .build();
            updateRunSettings(newRunSettings);
          }
        }

      };
    }
    return null;
  }

  @Nullable
  private RefactoringElementListener provideRefactoringJsFileListener(PsiElement element) {
    final JstdRunSettings currentRunSettings = myRunConfiguration.getRunSettings();
    VirtualFile jsFile = toVirtualFile(element);
    if (jsFile != null && currentRunSettings.getJsFilePath().equals(jsFile.getPath())) {
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
          VirtualFile newJsFile = toVirtualFile(newElement);
          if (newJsFile != null) {
            JstdRunSettings newRunSettings = new JstdRunSettings.Builder(currentRunSettings)
                .setJSFilePath(newJsFile.getPath())
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

  @Nullable
  private static VirtualFile toVirtualFile(@NotNull PsiElement element) {
    if (element instanceof PsiFileSystemItem) {
      PsiFileSystemItem psiFileSystemItem = (PsiFileSystemItem) element;
      return psiFileSystemItem.getVirtualFile();
    }
    return null;
  }

}
