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

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkRunner;
import org.osmorc.frameworkintegration.util.PropertiesWrapper;
import org.osmorc.run.OsgiRunConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * This class provides a default implementation for a part of the FrameworkRunner interface useful to any kind of
 * FrameworkRunner.
 *
 * @author Robert F. Beeger (robert@beeger.net)
 */
public abstract class AbstractFrameworkRunner<P extends PropertiesWrapper> implements FrameworkRunner {
    private Project project;
    private OsgiRunConfiguration runConfiguration;
    private P additionalProperties;
    private File workingDir;
    private File frameworkDir;


    public void init(@NotNull final Project project, @NotNull final OsgiRunConfiguration runConfiguration) {
        this.project = project;
        this.runConfiguration = runConfiguration;
        additionalProperties = convertProperties(this.runConfiguration.getAdditionalProperties());
    }

    protected P getAdditionalProperties() {
        return additionalProperties;
    }

    @NotNull
    public File getWorkingDir() {
        if (workingDir == null) {
            String path;
            if (getRunConfiguration().getWorkingDir().length() == 0) {
                path = PathManager.getSystemPath() + File.separator + "osmorc" + File.separator + "runtmp" +
                        System.currentTimeMillis();
            } else {
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

    protected File getFrameworkDir() {
        if (frameworkDir == null) {
            if (getRunConfiguration().getFrameworkDir().length() > 0) {
                String path = getRunConfiguration().getFrameworkDir();
                File dir = new File(path);
                if (!dir.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    dir.mkdirs();
                }
                frameworkDir = dir;
            } else {
                frameworkDir = getWorkingDir();
            }
        }
        return frameworkDir;
    }

    protected String getFrameworkDirCanonicalPath() {
        try {
            return getFrameworkDir().getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void dispose() {
        if (getRunConfiguration().isRuntimeDirsOsmorcControlled()) {
            FileUtil.asyncDelete(getWorkingDir());
        }
    }


    @NotNull
    protected abstract P convertProperties(final Map<String, String> properties);

    protected Project getProject() {
        return project;
    }

    protected OsgiRunConfiguration getRunConfiguration() {
        return runConfiguration;
    }


}
