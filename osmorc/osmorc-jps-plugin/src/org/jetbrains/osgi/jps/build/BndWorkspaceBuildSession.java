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
package org.jetbrains.osgi.jps.build;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.logging.ProjectBuilderLogger;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.incremental.messages.DoneSomethingNotification;
import org.jetbrains.jps.incremental.messages.ProgressMessage;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;

import aQute.bnd.build.Project;
import aQute.bnd.osgi.Builder;
import aQute.bnd.osgi.Jar;

public class BndWorkspaceBuildSession implements Reporter {
  private static final Logger LOG = Logger.getInstance(BndWorkspaceBuildSession.class);

  private String myMessagePrefix;
  private String mySourceToReport = null;
  private Project myProject;
  private CompileContext myContext;

  public void build(@NotNull OsmorcBuildTarget target, @NotNull CompileContext context) throws IOException {
    myContext = context;
    myProject = target.getProject();

    myMessagePrefix = "[" + target.getModule().getName() + "] ";

    progress("Building OSGi bundle");

    try {
      prepare();
      doBuild();
    }
    catch (OsgiBuildException e) {
      error(e.getMessage(), e.getCause(), e.getSourcePath());
      return;
    }

    try {
      Set<File> outputFiles = new HashSet<File>();
      Collection<? extends Builder> subBuilders = myProject.getSubBuilders();
      for (Builder subBuilder : subBuilders) {
        File outputFile = myProject.getOutputFile(subBuilder.getBsn());
        if (!outputFile.exists()) {
          error("Bundle was not built", null, null);
          return;
        }
      }

      ProjectBuilderLogger logger = context.getLoggingManager().getProjectBuilderLogger();
      if (logger.isEnabled()) {
        logger.logCompiledFiles(outputFiles, OsmorcBuilder.ID, "Built OSGi bundles:");
      }
    } catch (Exception e) {
      error(e.getMessage(), e.getCause(), null);
      return;
    }

    context.processMessage(DoneSomethingNotification.INSTANCE);
  }

  private void prepare() throws OsgiBuildException {
    try {
      for (Builder subBuilder : myProject.getSubBuilders()) {
        File outputRoot = myProject.getOutputFile(subBuilder.getBsn());
        if (!FileUtil.delete(outputRoot)) {
          throw new OsgiBuildException("Can't delete bundle file '" + outputRoot + "'.");
        }
        if (!FileUtil.createParentDirs(outputRoot)) {
          throw new OsgiBuildException("Cannot create directory for bundle file '" + outputRoot + "'.");
        }
      }
    } catch (Exception e) {
      throw new OsgiBuildException("Unexpected build error " + e.getMessage(), e, null);
    }
  }

  private void doBuild() throws OsgiBuildException {
    progress("Running Bnd to build the bundle");

    try {

      ReportingProjectBuilder builder = new ReportingProjectBuilder(this, myProject);
      builder.setBase(myProject.getBase());
      List<Builder> subBuilders = builder.getSubBuilders();
      for (Builder subBuilder : subBuilders) {
        File propertiesFile = subBuilder == builder ? builder.getParent().getPropertiesFile() : subBuilder.getPropertiesFile();
        mySourceToReport = propertiesFile.getAbsolutePath();
        Jar jar = subBuilder.build();
        jar.write(myProject.getOutputFile(subBuilder.getBsn()));
        subBuilder.close();
      }
    } catch (Exception e) {
      throw new OsgiBuildException("Unexpected build error " + e.getMessage(), e, null);
    }
    mySourceToReport = null;

  }

  @Override
  public void progress(@NotNull String message) {
    myContext.processMessage(new ProgressMessage(myMessagePrefix + message));
  }

  @Override
  public void warning(@NotNull String message, @Nullable Throwable t, @Nullable String sourcePath) {
    LOG.warn(message, t);
    if (sourcePath == null) sourcePath = mySourceToReport;
    myContext.processMessage(new CompilerMessage(OsmorcBuilder.ID, BuildMessage.Kind.WARNING, myMessagePrefix + message, sourcePath));
  }

  @Override
  public void error(@NotNull String message, @Nullable Throwable t, @Nullable String sourcePath) {
    LOG.warn(message, t);
    if (sourcePath == null) sourcePath = mySourceToReport;
    myContext.processMessage(new CompilerMessage(OsmorcBuilder.ID, BuildMessage.Kind.ERROR, myMessagePrefix + message, sourcePath));
  }

  @Override
  public boolean isDebugEnabled() {
    return LOG.isDebugEnabled();
  }

  @Override
  public void debug(@NotNull String message) {
    LOG.debug(message);
  }
}