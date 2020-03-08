/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.coverage;

import com.intellij.coverage.CoverageBundle;
import com.intellij.coverage.SimpleCoverageAnnotator;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class DartCoverageAnnotator extends SimpleCoverageAnnotator {
  public DartCoverageAnnotator(@NotNull Project project) {
    super(project);
  }

  @NotNull
  public static DartCoverageAnnotator getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, DartCoverageAnnotator.class);
  }

  @Nullable
  @Override
  protected FileCoverageInfo fillInfoForUncoveredFile(@NotNull final File file) {
    return new FileCoverageInfo();
  }

  @Nullable
  @Override
  protected String getLinesCoverageInformationString(@NotNull final FileCoverageInfo info) {
    if (info.totalLineCount == 0) return null;
    if (info.coveredLineCount == 0) return info instanceof DirCoverageInfo ? null :
                                           CoverageBundle.message("lines.covered.info.no.lines.covered");
    if (info.coveredLineCount * 100 < info.totalLineCount) return CoverageBundle.message("lines.covered.info.less.than.one.percent");
    return (int)((double)info.coveredLineCount * 100. / (double)info.totalLineCount) +
           CoverageBundle.message("lines.covered.info.percent.lines.covered");
    //return super.getLinesCoverageInformationString(info); // "15% lines covered"
  }

  @Nullable
  @Override
  protected String getFilesCoverageInformationString(@NotNull final DirCoverageInfo info) {
    if (info.totalFilesCount == 0) return null;
    if (info.coveredFilesCount == 0) {
      return DartBundle.message("coverage.string.0.of.1.files.covered", info.coveredFilesCount, info.totalFilesCount);
    }
    return DartBundle.message("coverage.string.0.of.1.files", info.coveredFilesCount, info.totalFilesCount);
    //return super.getFilesCoverageInformationString(info); // "15% files"
  }
}
