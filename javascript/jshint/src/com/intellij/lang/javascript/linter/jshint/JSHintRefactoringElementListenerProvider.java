package com.intellij.lang.javascript.linter.jshint;

import com.intellij.lang.javascript.linter.ExtendedLinterState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.RefactoringElementListenerProvider;
import com.intellij.refactoring.listeners.UndoRefactoringElementAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JSHintRefactoringElementListenerProvider implements RefactoringElementListenerProvider {
  @Override
  public @Nullable RefactoringElementListener getListener(PsiElement element) {
    if (element == null) {
      return null;
    }
    VirtualFile fileAtElement = PsiUtilBase.asVirtualFile(element);
    if (fileAtElement == null) {
      return null;
    }
    Project project = element.getProject();
    JSHintConfiguration configuration = JSHintConfiguration.getInstance(project);
    JSHintState state = configuration.getExtendedState().getState();
    String path = fileAtElement.getPath();
    String configPath = FileUtil.toSystemIndependentName(state.getCustomConfigFilePath());
    if (!configPath.equals(path)) {
      return null;
    }
    return new MyRefactoringElementListener(configuration);
  }

  private static class MyRefactoringElementListener extends UndoRefactoringElementAdapter {
    private final JSHintConfiguration myConfiguration;

    MyRefactoringElementListener(@NotNull JSHintConfiguration configuration) {
      myConfiguration = configuration;
    }

    @Override
    protected void refactored(@NotNull PsiElement element, @Nullable String oldQualifiedName) {
      VirtualFile newFile = PsiUtilBase.asVirtualFile(element);
      if (newFile != null) {
        ExtendedLinterState<JSHintState> extendedState = myConfiguration.getExtendedState();
        JSHintState.Builder builder = new JSHintState.Builder(extendedState.getState());
        builder.setCustomConfigFilePath(FileUtil.toSystemDependentName(newFile.getPath()));
        myConfiguration.setExtendedState(extendedState.isEnabled(), builder.build());
      }
    }
  }
}
