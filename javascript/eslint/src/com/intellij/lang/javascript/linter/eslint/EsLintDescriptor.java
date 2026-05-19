package com.intellij.lang.javascript.linter.eslint;

import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import com.intellij.lang.javascript.linter.JSLinterConfiguration;
import com.intellij.lang.javascript.linter.JSLinterDescriptor;
import com.intellij.lang.javascript.linter.JSLinterGuesser;
import com.intellij.lang.javascript.linter.eslint.importer.EslintCodeStyleImporter;
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class EsLintDescriptor extends JSLinterDescriptor {

  @Override
  public @Nullable String packageJsonSectionName() {
    return EslintUtil.CONFIG_SECTION_NAME;
  }

  @Override
  public @NotNull String getDisplayName() {
    return EslintBundle.message("settings.javascript.linters.eslint.configurable.name");
  }

  @Override
  public @NotNull String packageName() {
    return EslintUtil.PACKAGE_NAME;
  }

  @Override
  public boolean supportsMultipleRoots() {
    return true;
  }

  @Override
  public @NotNull Class<? extends JSLinterConfiguration> getConfigurationClass() {
    return EslintConfiguration.class;
  }

  @Override
  public boolean hasConfigFiles(@NotNull Project project) {
    return EslintUtil.hasConfigFiles(project);
  }

  @Override
  public boolean isGuesserLogEnabled() {
    return false;
  }

  @Override
  public boolean enable(@NotNull Project project, Collection<PackageJsonData> packageJsonFiles) {
    if (StandardJSConfiguration.getInstance(project).isEnabled()) {
      JSLinterGuesser.LOG.info("Not enabling ESLint because StandardJS detected");
      return false;
    }
    return super.enable(project, packageJsonFiles);
  }

  @Override
  public void importSettings(@NotNull Project project, @NotNull JSLinterGuesser.EnableCase enableCase) {
    VirtualFile config = EslintUtil.findDistinctConfigInContentRoots(project);
    if (config == null) return;
    final PsiFile psiFile = PsiManager.getInstance(project).findFile(config);
    if (psiFile == null) return;

    new EslintCodeStyleImporter(true).importConfigFileWhenToolInstalled(psiFile);
  }
}
