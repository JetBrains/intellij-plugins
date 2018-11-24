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
package org.jetbrains.osgi.bnd.run;

import aQute.bnd.build.ProjectLauncher;
import aQute.bnd.osgi.Constants;
import aQute.bnd.osgi.Processor;
import com.intellij.execution.CantRunException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.osgi.bnd.imp.BndProjectImporter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class BndLaunchUtil {
  @NotNull
  public static JavaParameters createJavaParameters(@NotNull BndRunConfigurationBase configuration,
                                                    @NotNull ProjectLauncher launcher) throws CantRunException {
    Project project = configuration.getProject();

    JavaParameters parameters = new JavaParameters();
    parameters.setWorkingDirectory(project.getBasePath());

    String jreHome = configuration.useAlternativeJre ? configuration.alternativeJrePath : null;
    JavaParametersUtil.configureProject(project, parameters, JavaParameters.JDK_ONLY, jreHome);

    parameters.getEnv().putAll(launcher.getRunEnv());
    parameters.getVMParametersList().addAll(asList(launcher.getRunVM()));
    parameters.getClassPath().addAll(asList(launcher.getClasspath()));
    parameters.setMainClass(launcher.getMainTypeName());
    parameters.getProgramParametersList().addAll(asList(launcher.getRunProgramArgs()));

    return parameters;
  }

  private static List<String> asList(Collection<String> c) {
    return c instanceof List ? (List<String>)c : ContainerUtil.newArrayList(c);
  }

  @Nullable
  public static Boolean hasTestCases(@NotNull String path) {
    File file = new File(FileUtil.toSystemDependentName(path));
    if (file.isFile()) {
      try (Processor processor = new Processor()) {
        processor.setProperties(file);

        if (processor.get(Constants.RUNFW) != null) {
          // "Test-Cases" header may be inherited from a project
          if (!BndProjectImporter.BND_FILE.equals(file.getName())) {
            File projectFile = new File(file.getParent(), BndProjectImporter.BND_FILE);
            if (projectFile.isFile()) {
              Processor project = new Processor();
              project.setProperties(projectFile);
              processor.setParent(project);
            }
          }

          boolean hasTestCases = processor.get(Constants.TESTCASES) != null;
          return hasTestCases ? Boolean.TRUE : Boolean.FALSE;
        }
      }
      catch (IOException ignored) { }
    }

    return null;
  }

  public static String message(Throwable t) {
    String message = t.getMessage();
    return StringUtil.isEmptyOrSpaces(message) ? t.getClass().getSimpleName() : t.getClass().getSimpleName() + ": " + message;
  }
}