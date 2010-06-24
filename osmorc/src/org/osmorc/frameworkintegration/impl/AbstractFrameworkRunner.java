/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.osmorc.frameworkintegration.impl;

import com.intellij.execution.configurations.DebuggingRunnerData;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.*;
import org.osmorc.frameworkintegration.util.PropertiesWrapper;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.SelectedBundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class provides a default implementation for a part of the FrameworkRunner interface useful to any kind of
 * FrameworkRunner.
 *
 * @author Robert F. Beeger (robert@beeger.net)
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public abstract class AbstractFrameworkRunner<P extends PropertiesWrapper> implements FrameworkRunner {
  private Project myProject;
  private OsgiRunConfiguration myRunConfiguration;
  private RunnerSettings myRunnerSettings;
  private P myAdditionalProperties;
  private File workingDir;
  private File frameworkDir;


  public void init(@NotNull final Project project, @NotNull final OsgiRunConfiguration runConfiguration, RunnerSettings runnerSettings) {
    this.myProject = project;
    this.myRunConfiguration = runConfiguration;
    myRunnerSettings = runnerSettings;
    myAdditionalProperties = convertProperties(this.myRunConfiguration.getAdditionalProperties());
  }

  protected P getFrameworkProperties() {
    return myAdditionalProperties;
  }

  /**
   * Returns the start level of the framework. If the run configuration is set to automatic, this will determine the greatest start level
   *  of the given bundles. Otherwise the start level from the run configuration is returned.
   * @param bundlesToStart the list of bundles to be examined.
   * @return the framework start level.
   */
  protected int getFrameworkStartLevel(SelectedBundle[] bundlesToStart) {
    if ( myRunConfiguration.isAutoStartLevel() ) {
      int sl = 0;
      for (SelectedBundle selectedBundle : bundlesToStart) {
        sl = Math.max(selectedBundle.getStartLevel(), sl);
      }
      return sl;
    }
    else {
      return myRunConfiguration.getFrameworkStartLevel();
    }
  }

  /**
   * @return true if this run is a debug run, false otherwise.
   */
  protected boolean isDebugRun() {
    return myRunnerSettings.getData() instanceof DebuggingRunnerData;
  }

  /**
   * Returns the debug port. Use {@link #isDebugRun()} to check if this is a debug run.
   * @return the debug port that is in use, or -1 if this is not a debug run.
   */
  @NotNull
  protected String getDebugPort() {
    if (!isDebugRun()) {
      return "-1";
    }
    DebuggingRunnerData data = (DebuggingRunnerData)myRunnerSettings.getData();
    return data.getDebugPort();
  }

  @NotNull
  public File getWorkingDir() {
    if (workingDir == null) {
      String path;
      if (getRunConfiguration().isGenerateWorkingDir()) {
        path = PathManager.getSystemPath() + File.separator + "osmorc" + File.separator + "runtmp" + System.currentTimeMillis();
      }
      else {
        path = getRunConfiguration().getWorkingDir();
      }

      File dir = new File(path);
      if (!dir.exists()) {
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
      }
      workingDir = dir;
    }
    return workingDir;
  }

  public void dispose() {
    if (getRunConfiguration().isGenerateWorkingDir()) {
      FileUtil.asyncDelete(getWorkingDir());
    }
  }

  @NotNull
  protected abstract P convertProperties(final Map<String, String> properties);

  protected Project getProject() {
    return myProject;
  }

  protected OsgiRunConfiguration getRunConfiguration() {
    return myRunConfiguration;
  }


  protected RunnerSettings getRunnerSettings() {
    return myRunnerSettings;
  }

  @NotNull
  public List<VirtualFile> getFrameworkStarterLibraries() {
    List<VirtualFile> result = new ArrayList<VirtualFile>();

    FrameworkInstanceDefinition definition = getRunConfiguration().getInstanceToUse();
    FrameworkIntegratorRegistry registry = ServiceManager.getService(getProject(), FrameworkIntegratorRegistry.class);
    FrameworkIntegrator integrator = registry.findIntegratorByInstanceDefinition(definition);
    FrameworkInstanceManager frameworkInstanceManager = integrator.getFrameworkInstanceManager();

    List<Library> libs = frameworkInstanceManager.getLibraries(definition);

    final Pattern starterClasspathPattern = getFrameworkStarterClasspathPattern();
    for (Library lib : libs) {
      for (VirtualFile virtualFile : lib.getFiles(OrderRootType.CLASSES)) {
        if (starterClasspathPattern == null || starterClasspathPattern.matcher(virtualFile.getName()).matches()) {
          result.add(virtualFile);
        }
      }
    }
    return result;
  }

  /**
   * A pattern tested against all framework bundle jars to collect all jars that need to be put into the classpath in
   * order to start a framework.
   *
   * @return The pattern matching all needed jars for running of a framework instance.
   */
  @Nullable
  protected abstract Pattern getFrameworkStarterClasspathPattern();
}
