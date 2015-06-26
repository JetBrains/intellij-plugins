package com.intellij.javascript.karma.coverage;

import com.intellij.coverage.SimpleCoverageAnnotator;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoverageAnnotator extends SimpleCoverageAnnotator {

  public KarmaCoverageAnnotator(Project project) {
    super(project);
  }

  public static KarmaCoverageAnnotator getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, KarmaCoverageAnnotator.class);
  }

  @Override
  protected boolean shouldCollectCoverageInsideLibraryDirs() {
    return false;
  }

  @Override
  @Nullable
  protected FileCoverageInfo fillInfoForUncoveredFile(@NotNull File file) {
    return null;
  }

  @Override
  protected String getLinesCoverageInformationString(@NotNull FileCoverageInfo info) {
    if (info.totalLineCount == 0) {
      return null;
    }
    if (info.coveredLineCount == 0) {
      return "not covered";
    }
    else {
      return calcCoveragePercentage(info) + "% lines covered";
    }
  }

}
