/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

import aQute.bnd.build.Run;
import aQute.bnd.build.Workspace;
import aQute.bnd.header.OSGiHeader;
import aQute.bnd.osgi.Constants;
import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.application.PathManager;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.osmorc.i18n.OsmorcBundle;

import java.io.File;
import java.util.Collection;

public class BndRunState extends JavaCommandLineState {
  private final BndRunConfiguration myConfiguration;

  public BndRunState(@NotNull ExecutionEnvironment environment, @NotNull BndRunConfiguration configuration) {
    super(environment);
    myConfiguration = configuration;
  }

  @Override
  protected JavaParameters createJavaParameters() throws ExecutionException {
    File runFile = new File(myConfiguration.bndRunFile);
    if (!runFile.isFile()) {
      throw new CantRunException(OsmorcBundle.message("bnd.run.configuration.invalid", runFile));
    }

    JavaParameters parameters = new JavaParameters();
    parameters.setWorkingDirectory(myConfiguration.getProject().getBasePath());

    String jreHome = myConfiguration.useAlternativeJre ? myConfiguration.alternativeJrePath : null;
    JavaParametersUtil.configureProject(myConfiguration.getProject(), parameters, JavaParameters.JDK_ONLY, jreHome);

    try {
      Run run = Workspace.getRun(runFile);

      String env = run.getProperty(Constants.RUNENV);
      if (env != null) {
        parameters.getEnv().putAll(OSGiHeader.parseProperties(env));
      }

      Collection<String> vmOptions = run.getRunVM();
      if (vmOptions != null) {
        parameters.getVMParametersList().addAll(ContainerUtil.newArrayList(vmOptions));
      }
    }
    catch (Exception e) {
      throw new CantRunException(OsmorcBundle.message("bnd.run.configuration.invalid", runFile), e);
    }

    parameters.getClassPath().add(PathManager.getJarPathForClass(Workspace.class));
    parameters.getClassPath().add(PathManager.getJarPathForClass(Runner.class));
    parameters.setMainClass(Runner.class.getName());
    parameters.getProgramParametersList().add(myConfiguration.bndRunFile);

    return parameters;
  }
}
