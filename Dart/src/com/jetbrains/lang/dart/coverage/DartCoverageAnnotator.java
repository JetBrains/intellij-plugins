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

import com.intellij.coverage.CoverageSuitesBundle;
import com.intellij.coverage.SimpleCoverageAnnotator;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartCoverageAnnotator extends SimpleCoverageAnnotator {
  public DartCoverageAnnotator(@NotNull Project project) {
    super(project);
  }

  @NotNull
  public static DartCoverageAnnotator getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, DartCoverageAnnotator.class);
  }

  @Override
  @Nullable
  protected DirCoverageInfo getDirCoverageInfo(@NotNull final PsiDirectory directory, @NotNull final CoverageSuitesBundle currentSuite) {
    DirCoverageInfo dirCoverageInfo = super.getDirCoverageInfo(directory, currentSuite);
    if (dirCoverageInfo == null) {
      return null;
    }

    dirCoverageInfo.totalFilesCount = getDartFilesCount(directory);
    return dirCoverageInfo;
  }

  private static int getDartFilesCount(@NotNull PsiDirectory directory) {
    int filesCount = 0;
    for (PsiFile f : directory.getFiles()) {
      if (f.getFileType() instanceof DartFileType) {
        filesCount++;
      }
    }

    for (PsiDirectory d : directory.getSubdirectories()) {
      if (!d.getName().equals("packages")) {
        // Only count files out of "packages" folder.
        filesCount += getDartFilesCount(d);
      }
    }

    return filesCount;
  }
}
