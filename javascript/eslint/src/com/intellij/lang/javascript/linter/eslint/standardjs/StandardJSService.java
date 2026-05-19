package com.intellij.lang.javascript.linter.eslint.standardjs;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.linter.JSLinterConfiguration;
import com.intellij.lang.javascript.linter.eslint.service.ESLintBasedLanguageService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class StandardJSService extends ESLintBasedLanguageService<StandardJSState> {
  public StandardJSService(@NotNull Project project,
                           @NotNull NodePackage eslintPackage,
                           @NotNull VirtualFile workingDirectory) {
    super(project, eslintPackage, workingDirectory);
    assert eslintPackage.getName().equals(StandardJSUtil.PACKAGE_NAME) :
      "expected " + StandardJSUtil.PACKAGE_NAME + " package, got " + eslintPackage.getName();
  }

  @Override
  protected @NotNull Class<? extends JSLinterConfiguration<StandardJSState>> getConfigurationClass() {
    return StandardJSConfiguration.class;
  }
}
