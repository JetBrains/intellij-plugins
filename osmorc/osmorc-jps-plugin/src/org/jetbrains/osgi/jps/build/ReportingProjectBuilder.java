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
package org.jetbrains.osgi.jps.build;

import aQute.bnd.build.Project;
import aQute.bnd.build.ProjectBuilder;
import org.jetbrains.annotations.NotNull;

public class ReportingProjectBuilder extends ProjectBuilder {
  private final Reporter myReporter;

  public ReportingProjectBuilder(@NotNull Reporter reporter, @NotNull Project project) {
    super(project);
    myReporter = reporter;
  }

  @Override
  public void progress(float progress, String format, Object... args) {
    myReporter.progress(formatArrays(format, args));
  }

  @Override
  public boolean isTrace() {
    return myReporter.isDebugEnabled();
  }

  @Override
  public void trace(String format, Object... args) {
    myReporter.debug(formatArrays(format, args));
  }
}