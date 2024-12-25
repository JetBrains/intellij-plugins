// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.bnd.run;

import aQute.bnd.build.ProjectLauncher;
import aQute.bnd.build.Run;
import aQute.bnd.build.Workspace;
import aQute.bnd.osgi.Constants;
import aQute.bnd.osgi.Processor;
import com.intellij.execution.CantRunException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.osgi.bnd.imp.BndProjectImporter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class BndLaunchUtil {
  public static @NotNull Run getRun(@NotNull File runFile) throws Exception {
    Workspace ws = Workspace.getWorkspaceWithoutException(runFile.getParentFile().getParentFile());
    return Run.createRun(ws, runFile);
  }

  public static @NotNull JavaParameters createJavaParameters(@NotNull BndRunConfigurationBase configuration,
                                                             @NotNull ProjectLauncher launcher) throws CantRunException {
    Project project = configuration.getProject();

    JavaParameters parameters = new JavaParameters();

    File launcherDir = launcher.getCwd();
    parameters.setWorkingDirectory(launcherDir != null ? launcherDir.getPath() : project.getBasePath());

    String jreHome = configuration.getOptions().getUseAlternativeJre() ? configuration.getOptions().getAlternativeJrePath() : null;
    JavaParametersUtil.configureProject(project, parameters, JavaParameters.JDK_ONLY, jreHome);

    parameters.getEnv().putAll(launcher.getRunEnv());
    parameters.getVMParametersList().addAll(asList(launcher.getRunVM()));
    parameters.getClassPath().addAll(asList(launcher.getClasspath()));
    parameters.setMainClass(launcher.getMainTypeName());
    parameters.getProgramParametersList().addAll(asList(launcher.getRunProgramArgs()));

    return parameters;
  }

  private static List<String> asList(Collection<String> c) {
    return c instanceof List ? (List<String>)c : new ArrayList<>(c);
  }

  public static @Nullable Boolean hasTestCases(@NotNull String path) {
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