package com.intellij.javascript.karma.coverage;

import com.intellij.coverage.BaseCoverageSuite;
import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageFileProvider;
import com.intellij.coverage.CoverageRunner;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KarmaCoverageSuite extends BaseCoverageSuite {

  private final KarmaCoverageEngine myKarmaCoverageEngine;

  public KarmaCoverageSuite(KarmaCoverageEngine karmaCoverageEngine) {
    myKarmaCoverageEngine = karmaCoverageEngine;
  }

  public KarmaCoverageSuite(CoverageRunner coverageRunner,
                            String name,
                            @Nullable final CoverageFileProvider fileProvider,
                            long lastCoverageTimeStamp,
                            boolean coverageByTestEnabled,
                            boolean tracingEnabled,
                            boolean trackTestFolders,
                            final Project project,
                            KarmaCoverageEngine karmaCoverageEngine) {
    super(name, fileProvider, lastCoverageTimeStamp, coverageByTestEnabled,
          tracingEnabled, trackTestFolders, coverageRunner, project);
    myKarmaCoverageEngine = karmaCoverageEngine;
  }

  @NotNull
  @Override
  public CoverageEngine getCoverageEngine() {
    return myKarmaCoverageEngine;
  }
}
