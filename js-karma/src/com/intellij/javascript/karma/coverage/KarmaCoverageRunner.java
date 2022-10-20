// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.coverage;

import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageRunner;
import com.intellij.coverage.CoverageSuite;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.nodejs.execution.NodeTargetRun;
import com.intellij.javascript.testing.CoverageProjectDataLoader;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.rt.coverage.data.ProjectData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;

public class KarmaCoverageRunner extends CoverageRunner {

  private static final Logger LOG = Logger.getInstance(KarmaCoverageRunner.class);
  private KarmaServer myKarmaServer;
  private NodeTargetRun myTargetRun;

  @NotNull
  public static KarmaCoverageRunner getInstance() {
    return Objects.requireNonNull(CoverageRunner.getInstance(KarmaCoverageRunner.class));
  }

  @Override
  public ProjectData loadCoverageData(@NotNull File sessionDataFile, @Nullable CoverageSuite baseCoverageSuite) {
    KarmaConfig karmaConfig = myKarmaServer != null ? myKarmaServer.getKarmaConfig() : null;
    File localBasePathDir = karmaConfig != null ? toLocal(karmaConfig.getBasePath()) : null;
    if (localBasePathDir != null && localBasePathDir.isAbsolute() && localBasePathDir.isDirectory()) {
      try {
        return CoverageProjectDataLoader.readProjectData(sessionDataFile, localBasePathDir,
                                                         myKarmaServer.getServerSettings().getNodeInterpreter(), myTargetRun);
      }
      catch (Exception e) {
        LOG.warn("Can't read coverage data", e);
      }
    }
    return null;
  }

  private @Nullable File toLocal(@NotNull String targetPath) {
    try {
      return new File(myTargetRun.convertTargetPathToLocalPath(targetPath));
    }
    catch (IllegalArgumentException e) {
      LOG.warn("Cannot load coverage");
      return null;
    }
  }

  public void setKarmaServer(@NotNull KarmaServer karmaServer) {
    myKarmaServer = karmaServer;
  }

  public void setTargetRun(@NotNull NodeTargetRun targetRun) {
    myTargetRun = targetRun;
  }

  @Override
  @NotNull
  public String getPresentableName() {
    return "KarmaPresentableName";
  }

  @NotNull
  @Override
  public String getId() {
    return KarmaCoverageEngine.ID;
  }

  @Override
  @NotNull
  public String getDataFileExtension() {
    return "dat";
  }

  @Override
  public boolean acceptsCoverageEngine(@NotNull CoverageEngine engine) {
    return engine instanceof KarmaCoverageEngine;
  }
}
