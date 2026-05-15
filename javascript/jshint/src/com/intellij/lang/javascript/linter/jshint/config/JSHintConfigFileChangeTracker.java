package com.intellij.lang.javascript.linter.jshint.config;

import com.intellij.lang.javascript.linter.ExtendedLinterState;
import com.intellij.lang.javascript.linter.JSLinterConfigChangeTracker;
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.lang.javascript.linter.jshint.JSHintConfiguration;
import com.intellij.lang.javascript.linter.jshint.JSHintState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Ensures daemon code analyzer restart if JSHint config file is edited/copied/moved/deleted.
 *
 * @author Sergey Simonchik
 */
public class JSHintConfigFileChangeTracker extends JSLinterConfigChangeTracker {
  public JSHintConfigFileChangeTracker(@NotNull Project project) {
    super(project, JSHintConfigFileType.INSTANCE);
  }

  public static @NotNull JSHintConfigFileChangeTracker getInstance(@NotNull Project project) {
    return project.getService(JSHintConfigFileChangeTracker.class);
  }

  @Override
  protected boolean isAnalyzerRestartNeeded(@NotNull Project project, @NotNull VirtualFile changedFile) {
    ExtendedLinterState<JSHintState> extendedState = JSHintConfiguration.getInstance(project).getExtendedState();
    JSHintState state = extendedState.getState();
    if (!extendedState.isEnabled() || !state.isConfigFileUsed()) {
      return false;
    }
    if (state.isCustomConfigFileUsed()) {
      return changedFile.equals(JSLinterConfigFileUtil.findLocalFileByPath(state.getCustomConfigFilePath()));
    }
    return true;
  }
}
