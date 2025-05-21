// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.coverage;

import com.intellij.coverage.*;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
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
import java.util.Set;

public final class DartCoverageEngine extends CoverageEngine {

  public static DartCoverageEngine getInstance() {
    return EP_NAME.findExtensionOrFail(DartCoverageEngine.class);
  }

  @Override
  public boolean isApplicableTo(@NotNull RunConfigurationBase conf) {
    return conf instanceof DartCommandLineRunConfiguration;
  }

  @Override
  public @NotNull CoverageEnabledConfiguration createCoverageEnabledConfiguration(@NotNull RunConfigurationBase conf) {
    return new DartCoverageEnabledConfiguration(conf);
  }

  @Override
  public @Nullable CoverageSuite createCoverageSuite(@NotNull String name,
                                                     @NotNull Project project,
                                                     @NotNull CoverageRunner runner,
                                                     @NotNull CoverageFileProvider fileProvider,
                                                     long timestamp) {
    return null;
  }

  @Override
  public @Nullable CoverageSuite createCoverageSuite(@NotNull String name,
                                                     @NotNull Project project,
                                                     @NotNull CoverageRunner runner,
                                                     @NotNull CoverageFileProvider fileProvider,
                                                     long timestamp,
                                                     @NotNull CoverageEnabledConfiguration config) {
    if (config instanceof DartCoverageEnabledConfiguration dartConfig) {
      final String contextFilePath = ((DartCommandLineRunConfiguration)dartConfig.getConfiguration()).getRunnerParameters().getFilePath();
      return new DartCoverageSuite(project, name, fileProvider, runner, config.createTimestamp(),
                                   contextFilePath, dartConfig.getCoverageProcess());
    }

    return null;
  }

  @Override
  public @Nullable CoverageSuite createEmptyCoverageSuite(@NotNull CoverageRunner coverageRunner) {
    return new DartCoverageSuite();
  }

  @Override
  public @NotNull CoverageAnnotator getCoverageAnnotator(@NotNull Project project) {
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
  public @NotNull Set<String> getQualifiedNames(@NotNull PsiFile sourceFile) {
    Set<String> qualifiedNames = new HashSet<>();
    qualifiedNames.add(getQName(sourceFile));
    return qualifiedNames;
  }

  @Override
  protected @NotNull String getQualifiedName(@NotNull File outputFile, @NotNull PsiFile sourceFile) {
    return getQName(sourceFile);
  }

  private static @NotNull String getQName(@NotNull PsiFile sourceFile) {
    return sourceFile.getVirtualFile().getPath();
  }

  @Override
  public boolean coverageProjectViewStatisticsApplicableTo(final VirtualFile fileOrDir) {
    return !(fileOrDir.isDirectory()) && fileOrDir.getFileType() instanceof DartFileType;
  }

  @Override
  public @Nls String getPresentableText() {
    return DartBundle.message("dart.coverage.presentable.text");
  }
}
