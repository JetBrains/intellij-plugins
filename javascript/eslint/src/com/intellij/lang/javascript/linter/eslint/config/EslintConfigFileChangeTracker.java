package com.intellij.lang.javascript.linter.eslint.config;

import com.intellij.lang.javascript.linter.ExtendedLinterState;
import com.intellij.lang.javascript.linter.JSLinterConfigChangeTracker;
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.lang.javascript.linter.eslint.EslintConfiguration;
import com.intellij.lang.javascript.linter.eslint.EslintState;
import com.intellij.lang.javascript.linter.eslint.EslintUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class EslintConfigFileChangeTracker extends JSLinterConfigChangeTracker {
  public EslintConfigFileChangeTracker(@NotNull Project project) {
    super(project, file -> EslintUtil.isFlatOrLegacyConfigFile(file));
  }

  public static EslintConfigFileChangeTracker getInstance(@NotNull Project project) {
    return project.getService(EslintConfigFileChangeTracker.class);
  }

  @Override
  protected boolean isAnalyzerRestartNeeded(@NotNull Project project, @NotNull VirtualFile changedFile) {
    ExtendedLinterState<EslintState> extendedState = EslintConfiguration.getInstance(project).getExtendedState();
    EslintState state = extendedState.getState();
    if (!extendedState.isEnabled()) {
      return false;
    }
    if (state.isCustomConfigFileUsed()) {
      VirtualFile configVirtualFile = JSLinterConfigFileUtil.findLocalFileByPath(state.getCustomConfigFilePath());
      return changedFile.equals(configVirtualFile);
    }
    return true;
  }
}
