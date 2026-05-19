package com.intellij.lang.javascript.linter.eslint;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.jsonSchema.extension.JsonSchemaEnabler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EsLintJsJsonSchemaEnabler implements JsonSchemaEnabler {
  @Override
  public boolean isEnabledForFile(@NotNull VirtualFile file, @Nullable Project project) {
    return EsLintJSJsonSchemaProvider.ESLINT_JS_CONFIG.contains(file.getName());
  }

  @Override
  public boolean shouldShowSwitcherWidget(VirtualFile file) {
    return false;
  }
}
