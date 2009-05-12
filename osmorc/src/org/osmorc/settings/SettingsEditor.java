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

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.*;

/**
 * TODO: migrate all to JGoodies Binding.
 *
 * @author Robert F. Beeger (robert@beeger.net)
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class SettingsEditor implements ProjectComponent, Configurable, Configurable.Composite
{
  public SettingsEditor(ApplicationSettings applicationSettings, ProjectSettings projectSettings,
                        ProjectSettingsEditor projectSettingsEditor, IDESettingsEditor ideSettingsEditor)
  {
    _projectSettingsEditor = projectSettingsEditor;
    _ideSettingsEditor = ideSettingsEditor;

    ApplicationSettings applicationSettingsWorkingCopy = applicationSettings.createCopy();
    ProjectSettings projectSettingsWorkingCopy = projectSettings.createCopy();

    _projectSettingsEditor.setProjectSettings(projectSettings, projectSettingsWorkingCopy);
    _projectSettingsEditor.setApplicationSettings(applicationSettings, applicationSettingsWorkingCopy);

    _ideSettingsEditor.setProjectSettings(projectSettings, projectSettingsWorkingCopy);
    _ideSettingsEditor.setApplicationSettings(applicationSettings, applicationSettingsWorkingCopy);
  }

  public Configurable[] getConfigurables()
  {
    return new Configurable[]{_projectSettingsEditor, _ideSettingsEditor};
  }

  public void projectOpened()
  {

  }

  public void projectClosed()
  {

  }

  @NonNls
  @NotNull
  public String getComponentName()
  {
    return "SettingsEditor";
  }

  public void initComponent()
  {
  }

  public void disposeComponent()
  {

  }

  @Nls
  public String getDisplayName()
  {
    return "Osmorc";
  }

  @Nullable
  public Icon getIcon()
  {
    return OsmorcBundle.getBigIcon();
  }

  @Nullable
  @NonNls
  public String getHelpTopic()
  {
    return null;
  }

  public JComponent createComponent()
  {
    return _mainPanel;
  }

  public boolean isModified()
  {
    return false;
  }

  public void apply() throws ConfigurationException
  {
  }

  public void reset()
  {
  }

  public void disposeUIResources()
  {
  }

  private JPanel _mainPanel;
  private ProjectSettingsEditor _projectSettingsEditor;
  private IDESettingsEditor _ideSettingsEditor;
}
