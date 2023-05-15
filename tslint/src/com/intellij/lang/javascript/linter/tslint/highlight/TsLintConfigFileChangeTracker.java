// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.highlight;

import com.intellij.json.JsonFileType;
import com.intellij.lang.javascript.linter.JSLinterConfigChangeTracker;
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public final class TsLintConfigFileChangeTracker extends JSLinterConfigChangeTracker {

  public TsLintConfigFileChangeTracker(@NotNull Project project) {
    super(project, JsonFileType.INSTANCE);
  }

  public static TsLintConfigFileChangeTracker getInstance(@NotNull Project project) {
    return project.getService(TsLintConfigFileChangeTracker.class);
  }

  @Override
  protected boolean isAnalyzerRestartNeeded(@NotNull Project project, @NotNull VirtualFile changedFile) {
    final TsLintConfiguration configuration = TsLintConfiguration.getInstance(project);
    final TsLintState state = configuration.getExtendedState().getState();
    if (state.isCustomConfigFileUsed() && state.getCustomConfigFilePath() != null) {
      final VirtualFile configVirtualFile = JSLinterConfigFileUtil.findLocalFileByPath(state.getCustomConfigFilePath());
      return changedFile.equals(configVirtualFile);
    }
    return TslintUtil.isConfigFile(changedFile);
  }
}
