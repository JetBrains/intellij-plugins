package com.intellij.lang.javascript.linter.eslint;

import com.intellij.lang.javascript.linter.LinterUnsavedConfigFileManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.PROJECT)
public final class EslintUnsavedConfigManager extends LinterUnsavedConfigFileManager {
  public static @NotNull EslintUnsavedConfigManager getInstance(@NotNull Project project) {
    return project.getService(EslintUnsavedConfigManager.class);
  }

  public EslintUnsavedConfigManager(@NotNull Project project) {
    super(project);
  }
}
