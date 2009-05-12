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
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
@State(
    name = "Osmorc",
    storages = {
    @Storage(
        id = "other",
        file = "$PROJECT_FILE$"
    )}
)
public class ProjectSettings implements PersistentStateComponent<ProjectSettings>
{
  @Nullable
  public String getFrameworkInstanceName()
  {
    return _frameworkInstanceName;
  }

  public void setFrameworkInstanceName(@Nullable String frameworkInstanceName)
  {
    _frameworkInstanceName = frameworkInstanceName;
  }

  @NotNull
  public ProjectSettings getState()
  {
    return this;
  }

  public void loadState(@NotNull ProjectSettings state)
  {
    XmlSerializerUtil.copyBean(state, this);
  }

  @NotNull
  public ProjectSettings createCopy()
  {
    ProjectSettings copy = new ProjectSettings();
    XmlSerializerUtil.copyBean(this, copy);
    return copy;
  }

  public void setCreateFrameworkInstanceModule(boolean selected)
  {
    _createFrameworkInstanceModule = selected;
  }

  public boolean isCreateFrameworkInstanceModule()
  {
    return _createFrameworkInstanceModule;
  }

  public void setDefaultManifestFileLocation(@NotNull String defaultManifestFileLocation)
  {
    _defaultManifestFileLocation = defaultManifestFileLocation;
  }

  @NotNull
  public String getDefaultManifestFileLocation()
  {
    return _defaultManifestFileLocation;
  }

  private @Nullable String _frameworkInstanceName;
  private boolean _createFrameworkInstanceModule;
  private @NotNull String _defaultManifestFileLocation = "META-INF";
}
