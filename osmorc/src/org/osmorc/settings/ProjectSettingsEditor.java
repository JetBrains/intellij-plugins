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

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.JComponent;

/**
 * @author <a href="janthomae@janthomae.de">Jan Thomä</a>
 */
public final class ProjectSettingsEditor implements SearchableConfigurable {
  private final Project myProject;
  private ProjectSettingsEditorComponent component;

  public ProjectSettingsEditor(Project project) {
    myProject = project;
  }

  @Override
  public @Nls String getDisplayName() {
    return OsmorcBundle.message("settings.project");
  }

  @Override
  public @NotNull String getHelpTopic() {
    return "reference.settings.project.osgi.project.settings";
  }

  @Override
  public @NotNull String getId() {
    return getHelpTopic();
  }

  @Override
  public JComponent createComponent() {
    component = new ProjectSettingsEditorComponent(myProject);
    return component.getMainPanel();
  }

  @Override
  public void disposeUIResources() {
    component.dispose();
    component = null;
  }

  @Override
  public boolean isModified() {
    // Fixes:    EA-23200. This probably occurs when isModified is called after disposing the UI. should not happen but does.. :(
    return component != null && component.isModified();
  }

  @Override
  public void apply() {
    component.applyTo(ProjectSettings.getInstance(myProject));
  }

  @Override
  public void reset() {
    component.resetTo(ProjectSettings.getInstance(myProject));
  }
}
