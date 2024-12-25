// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.tslint.config;

import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.lang.javascript.linter.JSLinterConfiguration;
import com.intellij.lang.javascript.linter.JSLinterDescriptor;
import com.intellij.lang.javascript.linter.JSLinterGuesser;
import com.intellij.lang.javascript.linter.tslint.TsLintBundle;
import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.javascript.linter.tslint.codestyle.TsLintCodeStyleImporter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.concurrency.ThreadingAssertions;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

import static com.intellij.lang.javascript.linter.JSLinterConfigFileUtil.findDistinctConfigInContentRoots;

public final class TsLintDescriptor extends JSLinterDescriptor {
  public static final String PACKAGE_NAME = "tslint";

  @Override
  public @NotNull String getDisplayName() {
    return TsLintBundle.message("settings.javascript.linters.tslint.configurable.name");
  }

  @Override
  public String packageName() {
    return PACKAGE_NAME;
  }

  @Override
  public boolean supportsMultipleRoots() {
    return true;
  }

  @Override
  public boolean hasConfigFiles(@NotNull Project project) {
    return TslintUtil.hasConfigFiles(project);
  }

  @Override
  public boolean enable(@NotNull Project project, Collection<PackageJsonData> packageJsonFiles) {
    // skip if there is typescript-tslint-plugin
    if (ContainerUtil.or(packageJsonFiles, data ->
      data.isDependencyOfAnyType(TslintUtil.TYPESCRIPT_PLUGIN_OLD_PACKAGE_NAME) ||
      data.isDependencyOfAnyType(TslintUtil.TYPESCRIPT_PLUGIN_PACKAGE_NAME))) {
      return false;
    }
    return super.enable(project, packageJsonFiles);
  }

  @Override
  public void importSettings(@NotNull Project project, @NotNull JSLinterGuesser.EnableCase enableCase) {
    ThreadingAssertions.assertEventDispatchThread();
    VirtualFile config = findDistinctConfigInContentRoots(project, Arrays.asList(TslintUtil.CONFIG_FILE_NAMES));
    if (config == null) return;

    PsiFile file = PsiManager.getInstance(project).findFile(config);
    if (file == null) return;

    new TsLintCodeStyleImporter(true).importConfigFileWhenToolInstalled(file);
  }

  @Override
  public @NotNull Class<? extends JSLinterConfiguration> getConfigurationClass() {
    return TsLintConfiguration.class;
  }
}
