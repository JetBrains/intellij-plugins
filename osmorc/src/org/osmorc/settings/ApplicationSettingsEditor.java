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
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.JComponent;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public final class ApplicationSettingsEditor implements SearchableConfigurable {
  private FrameworkDefinitionsEditorComponent myComponent;

  @Override
  public @Nls String getDisplayName() {
    return OsmorcBundle.message("settings.application");
  }

  @Override
  public String getHelpTopic() {
    return "reference.settings.project.osgi.framework.definitions";
  }

  @Override
  public @NotNull String getId() {
    return "osmorc.ide.settings";
  }

  @Override
  public JComponent createComponent() {
    myComponent = new FrameworkDefinitionsEditorComponent();
    return myComponent.getMainPanel();
  }

  @Override
  public void disposeUIResources() {
    myComponent = null;
  }

  @Override
  public boolean isModified() {
    return myComponent.isModified();
  }

  @Override
  public void apply() {
    myComponent.applyTo(ApplicationSettings.getInstance());
  }

  @Override
  public void reset() {
    myComponent.resetTo(ApplicationSettings.getInstance());
  }
}
