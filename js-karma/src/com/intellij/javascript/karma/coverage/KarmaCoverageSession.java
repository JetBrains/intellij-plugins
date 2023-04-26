package com.intellij.javascript.karma.coverage;

import org.jetbrains.annotations.Nullable;

public interface KarmaCoverageSession {
  void onCoverageSessionFinished(@Nullable KarmaCoverageResultPaths coverageResultPaths);
}
