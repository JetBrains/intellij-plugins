package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.TestType;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.UndoRefactoringElementAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JstdRunConfigurationRefactoringHandler {
  @Nullable
  public static RefactoringElementListener getRefactoringElementListener(@NotNull JstdRunConfiguration configuration,
                                                                         @Nullable PsiElement element) {
    VirtualFile fileAtElement = PsiUtilBase.asVirtualFile(element);
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

  private static class FilePathRefactoringElementListener extends UndoRefactoringElementAdapter {
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
    protected void refactored(@NotNull PsiElement element, @Nullable String oldQualifiedName) {
      VirtualFile newFile = PsiUtilBase.asVirtualFile(element);
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
        myConfiguration.setRunSettings(settingsBuilder.build());
      }
    }
  }
}