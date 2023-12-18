package com.intellij.javascript.karma.coverage;

import com.intellij.coverage.BaseCoverageSuite;
import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageFileProvider;
import com.intellij.coverage.CoverageRunner;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class KarmaCoverageSuite extends BaseCoverageSuite {

  private final KarmaCoverageEngine myKarmaCoverageEngine;

  public KarmaCoverageSuite(KarmaCoverageEngine karmaCoverageEngine) {
    myKarmaCoverageEngine = karmaCoverageEngine;
  }

  public KarmaCoverageSuite(String name,
                            Project project,
                            CoverageRunner coverageRunner,
                            CoverageFileProvider fileProvider,
                            long timestamp,
                            KarmaCoverageEngine karmaCoverageEngine) {
    super(name, project, coverageRunner, fileProvider, timestamp);
    myKarmaCoverageEngine = karmaCoverageEngine;
    myTrackTestFolders = true;
  }

  @NotNull
  @Override
  public CoverageEngine getCoverageEngine() {
    return myKarmaCoverageEngine;
  }
}
