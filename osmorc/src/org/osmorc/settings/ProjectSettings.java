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
package org.osmorc.settings;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.EventDispatcher;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EventListener;

/**
 * This class stores Osmorc project settings.
 *
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 * @author Jan Thomä (janthomae@janthomae.de)
 */
@Service(Service.Level.PROJECT)
@State(name = "Osmorc")
public final class ProjectSettings implements PersistentStateComponent<ProjectSettings> {
  private final EventDispatcher<ProjectSettingsListener> myDispatcher = EventDispatcher.create(ProjectSettingsListener.class);

  private String myFrameworkInstanceName;
  private String myDefaultManifestFileLocation = "META-INF/MANIFEST.MF";
  private String myBundlesOutputPath;
  private boolean myBndAutoImport = false;

  /**
   * Returns the default output path for bundles. This is the compiler output path plus "/bundles" (e.g. $PROJECT_ROOT/out/bundles).
   *
   * @param project the project for which the output path should be returned
   * @return the output path.
   */
  public static @NotNull String getDefaultBundlesOutputPath(Project project) {
    CompilerProjectExtension instance = CompilerProjectExtension.getInstance(project);
    if (instance != null) {
      final String compilerOutput = instance.getCompilerOutputUrl();
      if (compilerOutput != null) {
        return VfsUtilCore.urlToPath(compilerOutput) + "/bundles";
      }
    }
    // this actually should never happen (only in tests)
    return FileUtil.getTempDirectory();
  }

  /**
   * Returns the project settings for the given project.
   *
   * @param project the project
   * @return an instance of the project settings for the given project.
   */
  public static ProjectSettings getInstance(@NotNull Project project) {
    return project.getService(ProjectSettings.class);
  }

  /**
   * The project wide bundle output path. All compiled bundles will be put in this path. This can be overridden by facet settings.
   */
  public @Nullable String getBundlesOutputPath() {
    return myBundlesOutputPath;
  }

  public void setBundlesOutputPath(@Nullable String bundlesOutputPath) {
    myBundlesOutputPath = bundlesOutputPath;
  }

  /**
   * The name of the framework instance that should be used for this project.
   */
  public @Nullable String getFrameworkInstanceName() {
    return myFrameworkInstanceName;
  }

  public void setFrameworkInstanceName(@Nullable String frameworkInstanceName) {
    myFrameworkInstanceName = frameworkInstanceName;
    myDispatcher.getMulticaster().projectSettingsChanged();
  }

  public @NotNull @NlsSafe String getDefaultManifestFileLocation() {
    return myDefaultManifestFileLocation;
  }

  public void setDefaultManifestFileLocation(@NotNull String location) {
    // we specify full names, so to work with older projects, we have to convert this
    myDefaultManifestFileLocation = location.equals("META-INF") ? "META-INF/MANIFEST.MF" : location;
    myDispatcher.getMulticaster().projectSettingsChanged();
  }

  public boolean isBndAutoImport() {
    return myBndAutoImport;
  }

  public void setBndAutoImport(boolean bndAutoImport) {
    myBndAutoImport = bndAutoImport;
  }

  @Override
  public @NotNull ProjectSettings getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull ProjectSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  /**
   * Allows adding a listener that will be notified if project settings change.
   */
  public void addProjectSettingsListener(@NotNull ProjectSettingsListener listener, @NotNull Disposable parent) {
    myDispatcher.addListener(listener);
    Disposer.register(parent, () -> removeProjectSettingsListener(listener));
  }

  /**
   * Allows removing a listener.
   */
  public void removeProjectSettingsListener(@NotNull ProjectSettingsListener listener) {
    myDispatcher.removeListener(listener);
  }

  /**
   * Interface for project settings listeners.
   */
  public interface ProjectSettingsListener extends EventListener {
    void projectSettingsChanged();
  }
}
