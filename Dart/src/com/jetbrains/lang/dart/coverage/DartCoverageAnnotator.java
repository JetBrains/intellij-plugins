// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.coverage;

import com.intellij.coverage.CoverageBundle;
import com.intellij.coverage.SimpleCoverageAnnotator;
import com.intellij.openapi.project.Project;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class DartCoverageAnnotator extends SimpleCoverageAnnotator {
  public DartCoverageAnnotator(@NotNull Project project) {
    super(project);
  }

  public static @NotNull DartCoverageAnnotator getInstance(@NotNull Project project) {
    return project.getService(DartCoverageAnnotator.class);
  }

  @Override
  protected @Nullable FileCoverageInfo fillInfoForUncoveredFile(final @NotNull File file) {
    return new FileCoverageInfo();
  }

  @Override
  protected @Nullable String getLinesCoverageInformationString(final @NotNull FileCoverageInfo info) {
    if (info.totalLineCount == 0) return null;
    if (info.coveredLineCount == 0) return info instanceof DirCoverageInfo ? null :
                                           CoverageBundle.message("lines.covered.info.no.lines.covered");
    if (info.coveredLineCount * 100 < info.totalLineCount) return CoverageBundle.message("lines.covered.info.less.than.one.percent");
    return (int)((double)info.coveredLineCount * 100. / (double)info.totalLineCount) +
           CoverageBundle.message("lines.covered.info.percent.lines.covered");
    //return super.getLinesCoverageInformationString(info); // "15% lines covered"
  }

  @Override
  protected @Nullable String getFilesCoverageInformationString(final @NotNull DirCoverageInfo info) {
    if (info.totalFilesCount == 0) return null;
    if (info.coveredFilesCount == 0) {
      return DartBundle.message("coverage.string.0.of.1.files.covered", info.coveredFilesCount, info.totalFilesCount);
    }
    return DartBundle.message("coverage.string.0.of.1.files", info.coveredFilesCount, info.totalFilesCount);
    //return super.getFilesCoverageInformationString(info); // "15% files"
  }
}
