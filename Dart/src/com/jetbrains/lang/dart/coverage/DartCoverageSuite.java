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
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartCoverageSuite extends BaseCoverageSuite {
  @NonNls private static final String CONTEXT_FILE_PATH = "CONTEXT_FILE_PATH";

  @Nullable private String myContextFilePath;
  @Nullable private final ProcessHandler myCoverageProcess;

  public DartCoverageSuite() {
    myCoverageProcess = null;
  }

  public DartCoverageSuite(@NotNull Project project,
                           @NotNull String name,
                           @NotNull CoverageFileProvider fileProvider,
                           @NotNull CoverageRunner coverageRunner,
                           long timestamp,
                           @Nullable String contextFilePath,
                           @Nullable ProcessHandler coverageProcess) {
    super(name, project, coverageRunner, fileProvider, timestamp);
    myContextFilePath = contextFilePath;
    myCoverageProcess = coverageProcess;
  }

  @NotNull
  @Override
  public CoverageEngine getCoverageEngine() {
    return DartCoverageEngine.getInstance();
  }

  @Nullable
  public String getContextFilePath() {
    return myContextFilePath;
  }

  @Nullable
  public ProcessHandler getCoverageProcess() {
    return myCoverageProcess;
  }

  @Override
  public void writeExternal(final Element element) throws WriteExternalException {
    super.writeExternal(element);

    if (myContextFilePath != null) {
      element.setAttribute(CONTEXT_FILE_PATH, myContextFilePath);
    }
  }

  @Override
  public void readExternal(final Element element) throws InvalidDataException {
    super.readExternal(element);

    final String contextFilePath = element.getAttributeValue(CONTEXT_FILE_PATH);
    if (contextFilePath != null) {
      myContextFilePath = contextFilePath;
    }
  }
}
