// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.test;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRuntimeConfigurationProducer;
import com.jetbrains.lang.dart.ide.runner.util.TestUtil;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public final class DartTestRunConfigurationProducer extends LazyRunConfigurationProducer<DartTestRunConfiguration> {
  @Override
  public @NotNull ConfigurationFactory getConfigurationFactory() {
    return DartTestRunConfigurationType.getInstance().getConfigurationFactories()[0];
  }

  @Override
  protected boolean setupConfigurationFromContext(final @NotNull DartTestRunConfiguration configuration,
                                                  final @NotNull ConfigurationContext context,
                                                  final @NotNull Ref<PsiElement> sourceElement) {
    final boolean ok;
    final PsiElement location = context.getPsiLocation();
    if (location instanceof PsiDirectory) {
      ok = setupRunnerParametersForFolderIfApplicable(configuration.getProject(), configuration.getRunnerParameters(),
                                                      ((PsiDirectory)location).getVirtualFile());
    }
    else {
      ok = setupRunnerParametersForFileIfApplicable(configuration.getRunnerParameters(), context, sourceElement);
    }

    if (ok) {
      configuration.setGeneratedName();
    }

    return ok;
  }

  @Override
  public boolean isConfigurationFromContext(final @NotNull DartTestRunConfiguration configuration,
                                            final @NotNull ConfigurationContext context) {
    final DartTestRunnerParameters existingParams = configuration.getRunnerParameters();
    final DartTestRunnerParameters paramsFromContext = new DartTestRunnerParameters();

    final PsiElement location = context.getPsiLocation();
    if (location instanceof PsiDirectory) {
      if (!setupRunnerParametersForFolder(paramsFromContext, ((PsiDirectory)location).getVirtualFile())) return false;
    }
    else {
      final PsiElement testElement = TestUtil.findTestElement(location);
      if (testElement == null || !setupRunnerParametersForFile(paramsFromContext, testElement)) return false;
    }

    return Objects.equals(existingParams.getFilePath(), paramsFromContext.getFilePath()) &&
           Comparing.equal(existingParams.getScope(), paramsFromContext.getScope()) &&
           (existingParams.getScope() != DartTestRunnerParameters.Scope.GROUP_OR_TEST_BY_NAME ||
            Objects.equals(existingParams.getTestName(), paramsFromContext.getTestName()));
  }

  private static boolean setupRunnerParametersForFolderIfApplicable(final @NotNull Project project,
                                                                    final @NotNull DartTestRunnerParameters params,
                                                                    final @NotNull VirtualFile dir) {
    final DartUrlResolver urlResolver = DartUrlResolver.getInstance(project, dir);

    final VirtualFile dartTestLib = urlResolver.findFileByDartUrl("package:test/test.dart");
    if (dartTestLib == null) return false;

    final VirtualFile pubspec = urlResolver.getPubspecYamlFile();
    final VirtualFile rootDir = pubspec == null ? null : pubspec.getParent();
    final VirtualFile testDir = rootDir == null ? null : rootDir.findChild("test");
    if (testDir == null || !testDir.isDirectory()) return false;

    if (!rootDir.equals(dir) && !VfsUtilCore.isAncestor(testDir, dir, false)) return false;

    return setupRunnerParametersForFolder(params, dir);
  }

  private static boolean setupRunnerParametersForFolder(final @NotNull DartTestRunnerParameters params, @NotNull VirtualFile dir) {
    if ("test".equals(dir.getName()) &&
        dir.findChild(PubspecYamlUtil.PUBSPEC_YAML) == null &&
        dir.getParent().findChild(PubspecYamlUtil.PUBSPEC_YAML) != null) {
      dir = dir.getParent(); // anyway test engine looks for tests in 'test' subfolder only
    }

    params.setScope(DartTestRunnerParameters.Scope.FOLDER);
    params.setFilePath(dir.getPath());
    return true;
  }

  private static boolean setupRunnerParametersForFileIfApplicable(final @NotNull DartTestRunnerParameters params,
                                                                  final @NotNull ConfigurationContext context,
                                                                  final @NotNull Ref<PsiElement> sourceElement) {
    final VirtualFile dartFile = DartCommandLineRuntimeConfigurationProducer.getRunnableDartFileFromContext(context, false);
    if (dartFile == null || !isFileInTestDirAndTestPackageExists(context.getProject(), dartFile)) return false;

    final PsiElement testElement = TestUtil.findTestElement(context.getPsiLocation());
    if (testElement == null || !setupRunnerParametersForFile(params, testElement)) {
      return false;
    }

    sourceElement.set(testElement);
    return true;
  }

  public static boolean isFileInTestDirAndTestPackageExists(final @NotNull Project project, final @NotNull VirtualFile file) {
    final DartUrlResolver urlResolver = DartUrlResolver.getInstance(project, file);
    final VirtualFile dartTestLib = urlResolver.findFileByDartUrl("package:test/test.dart");
    if (dartTestLib == null) return false;

    final VirtualFile pubspec = urlResolver.getPubspecYamlFile();
    final VirtualFile rootDir = pubspec == null ? null : pubspec.getParent();
    final VirtualFile testDir = rootDir == null ? null : rootDir.findChild("test");
    return testDir != null && testDir.isDirectory() && VfsUtilCore.isAncestor(testDir, file, true);
  }

  private static boolean setupRunnerParametersForFile(final @NotNull DartTestRunnerParameters runnerParams,
                                                      final @NotNull PsiElement psiElement) {
    if (psiElement instanceof DartCallExpression) {
      final String testName = TestUtil.findGroupOrTestName((DartCallExpression)psiElement);
      final List<VirtualFile> virtualFiles = DartResolveUtil.findLibrary(psiElement.getContainingFile());
      if (testName == null || virtualFiles.isEmpty()) {
        return false;
      }

      runnerParams.setTestName(testName);
      runnerParams.setScope(DartTestRunnerParameters.Scope.GROUP_OR_TEST_BY_NAME);
      final VirtualFile dartFile = virtualFiles.iterator().next();
      final String dartFilePath = dartFile.getPath();
      runnerParams.setFilePath(dartFilePath);
      return true;
    }
    else {
      final PsiFile psiFile = psiElement.getContainingFile();
      if (psiFile instanceof DartFile) {
        final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile((DartFile)psiElement);
        if (virtualFile == null || !DartResolveUtil.isLibraryRoot((DartFile)psiElement)) {
          return false;
        }

        runnerParams.setTestName(DartResolveUtil.getLibraryName((DartFile)psiElement));
        runnerParams.setScope(DartTestRunnerParameters.Scope.FILE);
        final String dartFilePath = FileUtil.toSystemIndependentName(virtualFile.getPath());
        runnerParams.setFilePath(dartFilePath);
        return true;
      }
    }
    return false;
  }
}
