package com.google.jstestdriver.idea.coverage;

import com.intellij.coverage.SimpleCoverageAnnotator;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class JstdCoverageAnnotator extends SimpleCoverageAnnotator {

  private static final Key<JstdCoverageAnnotator> KEY = Key.create("jstd-coverage-annotator");

  public JstdCoverageAnnotator(Project project) {
    super(project);
  }

  public static JstdCoverageAnnotator getInstance(@NotNull Project project) {
    JstdCoverageAnnotator annotator = project.getUserData(KEY);
    if (annotator == null) {
      annotator = new JstdCoverageAnnotator(project);
      project.putUserData(KEY, annotator);
    }
    return annotator;
  }

  @Override
  protected FileCoverageInfo fillInfoForUncoveredFile(@NotNull VirtualFile file) {
    return new FileCoverageInfo();
  }

  @Override
  protected String getLinesCoverageInformationString(@NotNull FileCoverageInfo info) {
    if (info.totalLineCount == 0) {
      return "not covered";
    }
    else {
      return calcCoveragePercentage(info) + "% lines covered";
    }
  }
}
