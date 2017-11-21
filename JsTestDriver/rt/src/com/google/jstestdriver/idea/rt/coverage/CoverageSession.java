package com.google.jstestdriver.idea.rt.coverage;

import com.google.common.io.Files;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Sergey Simonchik
 */
public class CoverageSession {

  private final File myIdeCoverageFile;

  public CoverageSession(@NotNull File ideCoverageFile) {
    myIdeCoverageFile = ideCoverageFile;
  }

  public void copyCoverageFile(@NotNull File coverageFile) throws IOException {
    Files.copy(coverageFile, myIdeCoverageFile);
  }
}
