package com.google.jstestdriver.idea.coverage;

import com.intellij.coverage.BaseCoverageSuite;
import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageFileProvider;
import com.intellij.coverage.CoverageRunner;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class JstdCoverageSuite extends BaseCoverageSuite {

  private final JstdCoverageEngine myJstdCoverageEngine;

  public JstdCoverageSuite(JstdCoverageEngine jstdCoverageEngine) {
    myJstdCoverageEngine = jstdCoverageEngine;
  }

  public JstdCoverageSuite(CoverageRunner coverageRunner,
                           String name,
                           @Nullable final CoverageFileProvider fileProvider,
                           long lastCoverageTimeStamp,
                           boolean coverageByTestEnabled,
                           boolean tracingEnabled,
                           boolean trackTestFolders,
                           final Project project,
                           JstdCoverageEngine jstdCoverageEngine) {
    super(name, fileProvider, lastCoverageTimeStamp, coverageByTestEnabled,
          tracingEnabled, trackTestFolders, coverageRunner, project);
    myJstdCoverageEngine = jstdCoverageEngine;
  }

  @NotNull
  @Override
  public CoverageEngine getCoverageEngine() {
    return myJstdCoverageEngine;
  }
}
