package com.intellij.javascript.karma.coverage;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public interface KarmaCoverageSession {

  void onCoverageSessionFinished(@NotNull File lcovFile);

}
