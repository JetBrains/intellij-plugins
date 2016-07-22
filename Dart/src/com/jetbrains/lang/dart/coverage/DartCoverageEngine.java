/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.coverage;

import com.intellij.coverage.*;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunConfiguration;
import com.jetbrains.lang.dart.psi.DartFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DartCoverageEngine extends CoverageEngine {

  public static DartCoverageEngine getInstance() {
    return Extensions.findExtension(CoverageEngine.EP_NAME, DartCoverageEngine.class);
  }

  @Override
  public boolean isApplicableTo(@Nullable RunConfigurationBase conf) {
    return conf instanceof DartCommandLineRunConfiguration;
  }

  @Override
  public boolean canHavePerTestCoverage(@Nullable RunConfigurationBase conf) {
    return false;
  }

  @NotNull
  @Override
  public CoverageEnabledConfiguration createCoverageEnabledConfiguration(@Nullable RunConfigurationBase conf) {
    return new DartCoverageEnabledConfiguration(conf);
  }

  @Nullable
  @Override
  public CoverageSuite createCoverageSuite(@NotNull CoverageRunner covRunner,
                                           @NotNull String name,
                                           @NotNull CoverageFileProvider coverageDataFileProvider,
                                           @Nullable String[] filters,
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
  public CoverageAnnotator getCoverageAnnotator(Project project) {
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
  public List<PsiElement> findTestsByNames(@NotNull String[] testNames, @NotNull Project project) {
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
  public String getPresentableText() {
    return "Dart Coverage";
  }
}
