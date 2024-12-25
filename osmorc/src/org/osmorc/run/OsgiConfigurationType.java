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
package org.osmorc.run;

import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.facet.ProjectFacetManager;
import com.intellij.openapi.project.Project;
import icons.OsmorcIdeaIcons;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacetType;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.settings.ApplicationSettings;

import static com.intellij.openapi.util.NotNullLazyValue.createValue;

/**
 * Configuration type for a bundle run configuration.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public final class OsgiConfigurationType extends SimpleConfigurationType {
  private static final String ID = "#org.osmorc.OsgiConfigurationType";

  public OsgiConfigurationType() {
    super(ID, OsmorcBundle.message("run.configuration.name"), OsmorcBundle.message("run.configuration.description"), createValue(() -> OsmorcIdeaIcons.Osgi));
  }

  @Override
  public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
    return new OsgiRunConfiguration(project, this, "");
  }

  @Override
  public boolean isApplicable(@NotNull Project project) {
    return !ApplicationSettings.getInstance().getFrameworkInstanceDefinitions().isEmpty()
           && ProjectFacetManager.getInstance(project).hasFacets(OsmorcFacetType.ID);
  }

  @Override
  public @NotNull String getTag() {
    return "osgi";
  }

  @Override
  public String getHelpTopic() {
    return "reference.dialogs.rundebug.osgi.classic";
  }
}