// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.coverage;

import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageLoadErrorReporter;
import com.intellij.coverage.CoverageRunner;
import com.intellij.coverage.CoverageSuite;
import com.intellij.coverage.FailedCoverageLoadingResult;
import com.intellij.coverage.CoverageLoadingResult;
import com.intellij.javascript.nodejs.execution.NodeTargetRun;
import com.intellij.javascript.testing.CoverageProjectDataLoader;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public final class KarmaCoverageRunner extends CoverageRunner {
  private static final Logger LOG = Logger.getInstance(KarmaCoverageRunner.class);
  private NodeTargetRun myTargetRun;
  private Path myLocalProjectRoot;

  public static @NotNull KarmaCoverageRunner getInstance() {
    return Objects.requireNonNull(CoverageRunner.getInstance(KarmaCoverageRunner.class));
  }

  @Override
  public @NotNull CoverageLoadingResult loadCoverageData(
    @NotNull File sessionDataFile,
    @Nullable CoverageSuite baseCoverageSuite,
    @NotNull CoverageLoadErrorReporter reporter
  ) {
    Path localProjectRoot = myLocalProjectRoot;
    if (localProjectRoot != null) {
      try {
        return CoverageProjectDataLoader.readProjectData(sessionDataFile.toPath(), localProjectRoot, myTargetRun, reporter);
      }
      catch (Exception e) {
        LOG.warn("Can't read coverage data", e);
        return new FailedCoverageLoadingResult(e, true);
      }
    }
    String message = "The localProjectRoot is not set";
    LOG.warn(message);
    return new FailedCoverageLoadingResult(message);
  }

  public void setTargetRun(@NotNull NodeTargetRun targetRun) {
    myTargetRun = targetRun;
  }

  public void setProjectRoot(@NotNull Path localProjectRoot) {
    myLocalProjectRoot = localProjectRoot;
  }

  @Override
  public @NotNull String getPresentableName() {
    return "KarmaPresentableName";
  }

  @Override
  public @NotNull String getId() {
    return KarmaCoverageEngine.ID;
  }

  @Override
  public @NotNull String getDataFileExtension() {
    return "dat";
  }

  @Override
  public boolean acceptsCoverageEngine(@NotNull CoverageEngine engine) {
    return engine instanceof KarmaCoverageEngine;
  }
}
