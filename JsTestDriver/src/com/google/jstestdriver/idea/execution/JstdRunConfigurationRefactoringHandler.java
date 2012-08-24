package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.TestType;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JstdRunConfigurationRefactoringHandler {

  @Nullable
  public static RefactoringElementListener getRefactoringElementListener(@NotNull JstdRunConfiguration configuration,
                                                                         @Nullable PsiElement element) {
    if (element == null) {
      return null;
    }
    VirtualFile fileAtElement = toVirtualFile(element);
    if (fileAtElement == null) {
      return null;
    }
    JstdRunSettings settings = configuration.getRunSettings();
    String path = fileAtElement.getPath();
    if (settings.getTestType() == TestType.ALL_CONFIGS_IN_DIRECTORY) {
      String allInDirectory = FileUtil.toSystemIndependentName(settings.getDirectory());
      if (allInDirectory.equals(path)) {
        return new FilePathRefactoringElementListener(configuration, false, false, true);
      }
    } else {
      String jsFilePath = FileUtil.toSystemIndependentName(settings.getJsFilePath());
      if (jsFilePath.equals(path)) {
        return new FilePathRefactoringElementListener(configuration, false, true, false);
      }
      String configFilePath = FileUtil.toSystemIndependentName(settings.getConfigFile());
      if (configFilePath.equals(path)) {
        return new FilePathRefactoringElementListener(configuration, true, false, false);
      }
    }
    return null;
  }

  @Nullable
  private static VirtualFile toVirtualFile(@NotNull PsiElement element) {
    if (element instanceof PsiFileSystemItem) {
      PsiFileSystemItem psiFileSystemItem = (PsiFileSystemItem) element;
      return psiFileSystemItem.getVirtualFile();
    }
    return null;
  }

  private static class FilePathRefactoringElementListener implements RefactoringElementListener {

    private final JstdRunConfiguration myConfiguration;
    private final boolean myIsConfigFile;
    private final boolean myIsJsTestFile;
    private final boolean myIsAllInDirectory;

    private FilePathRefactoringElementListener(@NotNull JstdRunConfiguration configuration,
                                               boolean isConfigFile,
                                               boolean isJsTestFile,
                                               boolean isAllInDirectory) {
      myConfiguration = configuration;
      myIsConfigFile = isConfigFile;
      myIsJsTestFile = isJsTestFile;
      myIsAllInDirectory = isAllInDirectory;
    }

    @Override
    public void elementMoved(@NotNull PsiElement newElement) {
      refactorIt(newElement);
    }

    @Override
    public void elementRenamed(@NotNull PsiElement newElement) {
      refactorIt(newElement);
    }

    private void refactorIt(PsiElement newElement) {
      VirtualFile newFile = toVirtualFile(newElement);
      if (newFile != null) {
        String newPath = FileUtil.toSystemDependentName(newFile.getPath());
        JstdRunSettings.Builder settingsBuilder = new JstdRunSettings.Builder(myConfiguration.getRunSettings());
        if (myIsConfigFile) {
          settingsBuilder.setConfigFile(newPath);
        }
        if (myIsJsTestFile) {
          settingsBuilder.setJSFilePath(newPath);
        }
        if (myIsAllInDirectory) {
          settingsBuilder.setDirectory(newPath);
        }
        updateRunSettings(settingsBuilder.build());
      }
    }

    private void updateRunSettings(@NotNull JstdRunSettings newRunSettings) {
      boolean generatedName = myConfiguration.isGeneratedName();
      myConfiguration.setRunSettings(newRunSettings);
      if (generatedName) {
        myConfiguration.setName(myConfiguration.resetGeneratedName());
      }
    }

  }

}
