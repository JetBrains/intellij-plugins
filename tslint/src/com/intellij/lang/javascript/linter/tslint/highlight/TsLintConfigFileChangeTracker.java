package com.intellij.lang.javascript.linter.tslint.highlight;

import com.intellij.json.JsonFileType;
import com.intellij.lang.javascript.linter.JSLinterConfigChangeTracker;
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration.TSLINT_JSON;

/**
 * @author Irina.Chernushina on 6/4/2015.
 */
public final class TsLintConfigFileChangeTracker extends JSLinterConfigChangeTracker {

  public TsLintConfigFileChangeTracker(@NotNull Project project) {
    super(project, JsonFileType.INSTANCE);
  }

  public static TsLintConfigFileChangeTracker getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, TsLintConfigFileChangeTracker.class);
  }

  @Override
  protected boolean isAnalyzerRestartNeeded(@NotNull Project project, @NotNull VirtualFile changedFile) {
    final TsLintConfiguration configuration = TsLintConfiguration.getInstance(project);
    final TsLintState state = configuration.getExtendedState().getState();
    if (state.isCustomConfigFileUsed() && state.getCustomConfigFilePath() != null) {
      final VirtualFile configVirtualFile = JSLinterConfigFileUtil.findLocalFileByPath(state.getCustomConfigFilePath());
      return changedFile.equals(configVirtualFile);
    }
    else if (TSLINT_JSON.equals(changedFile.getName())) {
      return true;
    }
    return false;
  }
}
