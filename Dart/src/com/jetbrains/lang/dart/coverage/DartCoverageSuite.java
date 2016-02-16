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

import com.intellij.coverage.BaseCoverageSuite;
import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageFileProvider;
import com.intellij.coverage.CoverageRunner;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartCoverageSuite extends BaseCoverageSuite {
  @NotNull
  private final DartCoverageEngine myCoverageEngine;

  @Nullable
  private final VirtualFile myContextFile;

  public DartCoverageSuite(@NotNull DartCoverageEngine coverageEngine) {
    myCoverageEngine = coverageEngine;
    myContextFile = null;
  }

  public DartCoverageSuite(CoverageRunner coverageRunner,
                           String name,
                           @Nullable final CoverageFileProvider fileProvider,
                           long lastCoverageTimeStamp,
                           boolean coverageByTestEnabled,
                           boolean tracingEnabled,
                           boolean trackTestFolders,
                           final Project project,
                           @NotNull DartCoverageEngine dartCoverageEngine,
                           @Nullable VirtualFile contextFile) {
    super(name, fileProvider, lastCoverageTimeStamp, coverageByTestEnabled, tracingEnabled, trackTestFolders, coverageRunner, project);
    myCoverageEngine = dartCoverageEngine;
    myContextFile = contextFile;
  }

  @NotNull
  @Override
  public CoverageEngine getCoverageEngine() {
    return myCoverageEngine;
  }

  @Nullable
  public VirtualFile getContextFile() {
    return myContextFile;
  }
}
