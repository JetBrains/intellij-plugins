package com.jetbrains.lang.dart.ide.runner.test;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRuntimeConfigurationProducer;
import com.jetbrains.lang.dart.ide.runner.util.Scope;
import com.jetbrains.lang.dart.ide.runner.util.TestUtil;
import com.jetbrains.lang.dart.projectWizard.DartProjectTemplate;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartTestRunConfigurationProducer extends RunConfigurationProducer<DartTestRunConfiguration> {
  public DartTestRunConfigurationProducer() {
    super(DartTestRunConfigurationType.getInstance());
  }

  @Override
  protected boolean setupConfigurationFromContext(final @NotNull DartTestRunConfiguration configuration,
                                                  final @NotNull ConfigurationContext context,
                                                  final @NotNull Ref<PsiElement> sourceElement) {
    final VirtualFile dartFile = DartCommandLineRuntimeConfigurationProducer.getRunnableDartFileFromContext(context);
    if (dartFile == null) return false;

    final DartUrlResolver urlResolver = DartUrlResolver.getInstance(context.getProject(), dartFile);
    final VirtualFile dartUnitLib = urlResolver.findFileByDartUrl("package:test/test.dart");
    if (dartUnitLib == null) return false;

    final VirtualFile yamlFile = urlResolver.getPubspecYamlFile();
    if (yamlFile != null) {
      final VirtualFile parent = yamlFile.getParent();
      final VirtualFile testFolder = parent == null ? null : parent.findChild("test");
      if (testFolder == null || !testFolder.isDirectory() || !VfsUtilCore.isAncestor(testFolder, dartFile, true)) {
        return false;
      }
    }

    final PsiElement testElement = TestUtil.findTestElement(context.getPsiLocation());
    if (testElement == null || !setupRunConfiguration(configuration.getRunnerParameters(), testElement)) {
      return false;
    }

    configuration.setGeneratedName();

    sourceElement.set(testElement);
    return true;
  }

  @Override
  public boolean isConfigurationFromContext(final @NotNull DartTestRunConfiguration configuration,
                                            final @NotNull ConfigurationContext context) {
    final PsiElement testElement = TestUtil.findTestElement(context.getPsiLocation());
    if (testElement == null) return false;

    final DartTestRunnerParameters paramsFromContext = new DartTestRunnerParameters();
    if (!setupRunConfiguration(paramsFromContext, testElement)) return false;

    final DartTestRunnerParameters existingParams = configuration.getRunnerParameters();

    return Comparing.equal(existingParams.getFilePath(), paramsFromContext.getFilePath()) &&
           Comparing.equal(existingParams.getScope(), paramsFromContext.getScope()) &&
           (existingParams.getScope() == Scope.ALL || Comparing.equal(existingParams.getTestName(), paramsFromContext.getTestName()));
  }

  private static boolean setupRunConfiguration(final @NotNull DartTestRunnerParameters runnerParams, final @NotNull PsiElement psiElement) {
    if (psiElement instanceof DartCallExpression) {
      final String testName = TestUtil.findTestName((DartCallExpression)psiElement);
      final List<VirtualFile> virtualFiles = DartResolveUtil.findLibrary(psiElement.getContainingFile());
      if (testName == null || virtualFiles.isEmpty()) {
        return false;
      }

      runnerParams.setTestName(testName);
      runnerParams.setScope(TestUtil.isTest((DartCallExpression)psiElement) ? Scope.METHOD : Scope.GROUP);
      final VirtualFile dartFile = virtualFiles.iterator().next();
      final String dartFilePath = dartFile.getPath();
      runnerParams.setFilePath(dartFilePath);
      runnerParams.setWorkingDirectory(DartProjectTemplate.getWorkingDirForDartScript(psiElement.getProject(), dartFile));
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
        runnerParams.setScope(Scope.ALL);
        final String dartFilePath = FileUtil.toSystemIndependentName(virtualFile.getPath());
        runnerParams.setFilePath(dartFilePath);
        runnerParams.setWorkingDirectory(DartProjectTemplate.getWorkingDirForDartScript(psiElement.getProject(), virtualFile));
        return true;
      }
    }
    return false;
  }
}
