package com.intellij.javascript.karma.coverage;

import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface KarmaCoverageSession {
  void onCoverageSessionFinished(@Nullable File lcovFile);
}
