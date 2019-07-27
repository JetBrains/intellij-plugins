package com.intellij.javascript.karma.coverage;

import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageRunner;
import com.intellij.coverage.CoverageSuite;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.testing.CoverageProjectDataLoader;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.rt.coverage.data.ProjectData;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class KarmaCoverageRunner extends CoverageRunner {

  private static final Logger LOG = Logger.getInstance(KarmaCoverageRunner.class);
  private KarmaServer myKarmaServer;

  @NotNull
  public static KarmaCoverageRunner getInstance() {
    return ObjectUtils.assertNotNull(CoverageRunner.getInstance(KarmaCoverageRunner.class));
  }

  @Override
  public ProjectData loadCoverageData(@NotNull File sessionDataFile, @Nullable CoverageSuite baseCoverageSuite) {
    KarmaConfig karmaConfig = null;
    if (myKarmaServer != null) {
      karmaConfig = myKarmaServer.getKarmaConfig();
    }
    String basePath = null;
    if (karmaConfig != null) {
      basePath = karmaConfig.getBasePath();
    }
    if (basePath != null) {
      File basePathDir = new File(basePath);
      if (basePathDir.isAbsolute() && basePathDir.isDirectory()) {
        try {
          return CoverageProjectDataLoader.readProjectData(sessionDataFile, basePathDir, myKarmaServer.getServerSettings().getNodeInterpreter());
        }
        catch (Exception e) {
          LOG.warn("Can't read coverage data", e);
        }
      }
    }
    return null;
  }

  public void setKarmaServer(@NotNull KarmaServer karmaServer) {
    myKarmaServer = karmaServer;
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
