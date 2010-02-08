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
package org.osmorc.frameworkintegration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.SelectedBundle;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * This interface encapsulates framework-specific runtime configuration.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 * @version $Id$
 */
public interface FrameworkRunner extends Disposable {

  /**
     * Initializes the framework runner for the next execution
     *
     * @param project          The project for which a run configuration is executed
     * @param runConfiguration The configuration of the run configuration
     */
    void init(Project project, OsgiRunConfiguration runConfiguration);

    /**
     * Returns the virtual files for all library jars and directories that need to be placed into the classpath in order
     * to start the framework.
     *
     * @return a list containing all needed library virtual files.
     */
    @NotNull
    List<VirtualFile> getFrameworkStarterLibraries();

  /**
     * Returns a map with system properties that should be set on the launched java VM.
     *
     * @param bundlesToInstall the list of bundles to install.
     * @return a map with system properties.
     */
    @NotNull
    Map<String, String> getSystemProperties(@NotNull SelectedBundle[] bundlesToInstall);

  /**
   * Runs any custom installation steps (like preparing directories etc, prior to launching the framework).
   * @param bundlesToInstall  the list of bundles to install
   * @throws ExecutionException in case preparation fails.
   */
    void runCustomInstallationSteps(@NotNull SelectedBundle[] bundlesToInstall) throws ExecutionException;

    /**
     * @return the main class of the framework to run.
     */
    @NotNull
    String getMainClass();

    /**
     * Returns the directory that is used as the working directory for the process started to run the framework.
     *
     * @return the working directory
     */
    @NotNull
    File getWorkingDir();
}
