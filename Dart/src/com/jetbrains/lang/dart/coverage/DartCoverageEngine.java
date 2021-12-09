// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.coverage;

import com.intellij.coverage.*;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunConfiguration;
import com.jetbrains.lang.dart.psi.DartFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DartCoverageEngine extends CoverageEngine {

  public static DartCoverageEngine getInstance() {
    return CoverageEngine.EP_NAME.findExtensionOrFail(DartCoverageEngine.class);
  }

  @Override
  public boolean isApplicableTo(@NotNull RunConfigurationBase conf) {
    return conf instanceof DartCommandLineRunConfiguration;
  }

  @Override
  public boolean canHavePerTestCoverage(@NotNull RunConfigurationBase conf) {
    return false;
  }

  @NotNull
  @Override
  public CoverageEnabledConfiguration createCoverageEnabledConfiguration(@NotNull RunConfigurationBase conf) {
    return new DartCoverageEnabledConfiguration(conf);
  }

  @Nullable
  @Override
  public CoverageSuite createCoverageSuite(@NotNull CoverageRunner covRunner,
                                           @NotNull String name,
                                           @NotNull CoverageFileProvider coverageDataFileProvider,
                                           String @Nullable [] filters,
                                           long lastCoverageTimeStamp,
                                           @Nullable String suiteToMerge,
                                           boolean coverageByTestEnabled,
                                           boolean tracingEnabled,
                                           boolean trackTestFolders,
                                           Project project) {
    return null;
  }

  @Nullable
  @Override
  public CoverageSuite createCoverageSuite(@NotNull CoverageRunner covRunner,
                                           @NotNull String name,
                                           @NotNull CoverageFileProvider coverageDataFileProvider,
                                           @NotNull CoverageEnabledConfiguration config) {
    if (config instanceof DartCoverageEnabledConfiguration) {
      DartCoverageEnabledConfiguration dartConfig = (DartCoverageEnabledConfiguration)config;
      Project project = config.getConfiguration().getProject();
      final String contextFilePath = ((DartCommandLineRunConfiguration)dartConfig.getConfiguration()).getRunnerParameters().getFilePath();
      return new DartCoverageSuite(project, name, coverageDataFileProvider, covRunner, contextFilePath, dartConfig.getCoverageProcess());
    }

    return null;
  }

  @Nullable
  @Override
  public CoverageSuite createEmptyCoverageSuite(@NotNull CoverageRunner coverageRunner) {
    return new DartCoverageSuite();
  }

  @NotNull
  @Override
  public CoverageAnnotator getCoverageAnnotator(@NotNull Project project) {
    return DartCoverageAnnotator.getInstance(project);
  }

  @Override
  public boolean coverageEditorHighlightingApplicableTo(@NotNull PsiFile psiFile) {
    return psiFile instanceof DartFile;
  }

  @Override
  public boolean acceptedByFilters(@NotNull PsiFile psiFile, @NotNull CoverageSuitesBundle suite) {
    return psiFile instanceof DartFile;
  }

  @Override
  public boolean recompileProjectAndRerunAction(@NotNull Module module,
                                                @NotNull CoverageSuitesBundle suite,
                                                @NotNull Runnable chooseSuiteAction) {
    return false;
  }

  @NotNull
  @Override
  public Set<String> getQualifiedNames(@NotNull PsiFile sourceFile) {
    Set<String> qualifiedNames = new HashSet<>();
    qualifiedNames.add(getQName(sourceFile));
    return qualifiedNames;
  }

  @NotNull
  @Override
  public String getQualifiedName(@NotNull File outputFile, @NotNull PsiFile sourceFile) {
    return getQName(sourceFile);
  }

  @NotNull
  private static String getQName(@NotNull PsiFile sourceFile) {
    return sourceFile.getVirtualFile().getPath();
  }

  @Nullable
  @Override
  public List<PsiElement> findTestsByNames(String @NotNull [] testNames, @NotNull Project project) {
    return null;
  }

  @Override
  public boolean coverageProjectViewStatisticsApplicableTo(final VirtualFile fileOrDir) {
    return !(fileOrDir.isDirectory()) && fileOrDir.getFileType() instanceof DartFileType;
  }

  @Nullable
  @Override
  public String getTestMethodName(@NotNull PsiElement element, @NotNull AbstractTestProxy testProxy) {
    return null;
  }

  @Override
  public @Nls String getPresentableText() {
    return DartBundle.message("dart.coverage.presentable.text");
  }
}
