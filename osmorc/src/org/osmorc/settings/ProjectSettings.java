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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.util.EventDispatcher;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EventListener;

/**
 * This class stores Osmorc's project settings.
 *
 * @author Robert F. Beeger (robert@beeger.net)
 * @author Jan Thom&auml; (janthomae@janthomae.de)
 */
@State(
  name = "Osmorc",
  storages = {@Storage(
    file = "$PROJECT_FILE$")})
public class ProjectSettings implements PersistentStateComponent<ProjectSettings> {

  private EventDispatcher<ProjectSettingsListener> dispatcher = EventDispatcher.create(ProjectSettingsListener.class);
  private @Nullable String _frameworkInstanceName;
  private boolean _createFrameworkInstanceModule;
  private @NotNull String _defaultManifestFileLocation = "META-INF/MANIFEST.MF";
  private @Nullable String _bundlesOutputPath;
  private @NotNull ManifestSynchronizationType myManifestSynchronizationType = ManifestSynchronizationType.ManuallySynchronize;


  /**
   * Returns the default output path for bundles. This is the compiler output path plus "/bundles" (e.g. $PROJECT_ROOT/out/bundles).
   *
   * @param project the project for wich the output path should be returned
   * @return the output path.
   */
  @NotNull
  public static String getDefaultBundlesOutputPath(Project project) {
    CompilerProjectExtension instance = CompilerProjectExtension.getInstance(project);
    if (instance != null) {
      final VirtualFilePointer compilerOutput = instance.getCompilerOutputPointer();
      if (compilerOutput != null) {
        return VfsUtil.urlToPath(compilerOutput.getUrl()) + "/bundles";
      }
    }
    // this actually should never happen (only in tests)
    return FileUtil.getTempDirectory();
  }

  /**
   * Returns the project settings for the given project.
   *
   * @param project the project
   * @return an instance of the project settings for the given prject.
   */
  public static ProjectSettings getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, ProjectSettings.class);
  }


  /**
   * The synchronization type for manually edited manifests.
   *
   * @return the synchronization type.
   */
  @NotNull
  public ManifestSynchronizationType getManifestSynchronizationType() {
    return myManifestSynchronizationType;
  }

  public void setManifestSynchronizationType(@NotNull ManifestSynchronizationType manifestSynchronizationType) {
    myManifestSynchronizationType = manifestSynchronizationType;
  }

  /**
   * The project wide bundle output path. All compiled bundles will be put in this path. This can be overriden by facet settings.
   *
   * @return the output path.
   */
  @Nullable
  public String getBundlesOutputPath() {
    return _bundlesOutputPath;
  }

  public void setBundlesOutputPath(@Nullable String _bundlesOutputPath) {
    this._bundlesOutputPath = _bundlesOutputPath;
  }


  /**
   * The name of the framework instance that should be used for this project.
   *
   * @return the framework instance name.
   */
  @Nullable
  public String getFrameworkInstanceName() {
    return _frameworkInstanceName;
  }

  public void setFrameworkInstanceName(@Nullable String frameworkInstanceName) {
    _frameworkInstanceName = frameworkInstanceName;
    dispatcher.getMulticaster().projectSettingsChanged();
  }

  @NotNull
  public ProjectSettings getState() {
    return this;
  }

  public void loadState(@NotNull ProjectSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public void setCreateFrameworkInstanceModule(boolean selected) {
    _createFrameworkInstanceModule = selected;
    dispatcher.getMulticaster().projectSettingsChanged();
  }

  /**
   * If Osmorc shall create and maintain a library containing the jars of the selected framework.
   *
   * @return true if Osmorc should do this, false otherwise
   */
  public boolean isCreateFrameworkInstanceModule() {
    return _createFrameworkInstanceModule;
  }

  public void setDefaultManifestFileLocation(@NotNull String defaultManifestFileLocation) {
    _defaultManifestFileLocation = defaultManifestFileLocation;
    if (_defaultManifestFileLocation.equals("META-INF")) {
      // we specify full names, so to work with older projects, we have to convert this
      _defaultManifestFileLocation = "META-INF/MANIFEST.MF";
    }
    dispatcher.getMulticaster().projectSettingsChanged();
  }

  /**
   * Allows adding a listener that will be notified if project settings change.
   *
   * @param listener the listener to be added
   */
  public void addProjectSettingsListener(@NotNull ProjectSettingsListener listener) {
    dispatcher.addListener(listener);
  }

  /**
   * Allows removing a listener.
   *
   * @param listener the listener to be removed.
   */
  public void removeProjectSettingsListener(@NotNull ProjectSettingsListener listener) {
    dispatcher.removeListener(listener);
  }

  @NotNull
  public String getDefaultManifestFileLocation() {
    return _defaultManifestFileLocation;
  }


  /**
   * Interface for project settings listeners.
   */
  public interface ProjectSettingsListener extends EventListener {
    void projectSettingsChanged();
  }

  public static enum ManifestSynchronizationType {
    /**
     * Modules with manually edited manifests won't be synchronized at all.
     */
    DoNotSynchronize,
    /**
     * Modules with manually edited manifests will be synchronized manually by clicking the notification bar.
     */
    ManuallySynchronize,
    /**
     * Modules with manually edited manifests will be automatically synchronized when they change.
     */
    AutomaticallySynchronize
  }
}
