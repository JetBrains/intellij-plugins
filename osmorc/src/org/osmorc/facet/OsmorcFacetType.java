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
package org.osmorc.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.text.StringUtil;
import icons.OsmorcIdeaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.osgi.jps.model.OutputPathType;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.settings.ProjectSettings;

import javax.swing.Icon;

/**
 * The facet type of Osmorc.
 *
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public final class OsmorcFacetType extends FacetType<OsmorcFacet, OsmorcFacetConfiguration> {
  public static final FacetTypeId<OsmorcFacet> ID = new FacetTypeId<>("Osmorc");

  public static OsmorcFacetType getInstance() {
    return findInstance(OsmorcFacetType.class);
  }

  private OsmorcFacetType() {
    super(ID, "Osmorc", OsmorcBundle.message("facet.type.name"));
  }

  @Override
  public OsmorcFacetConfiguration createDefaultConfiguration() {
    return new OsmorcFacetConfiguration();
  }

  @Override
  public OsmorcFacet createFacet(@NotNull Module module, String name, @NotNull OsmorcFacetConfiguration configuration, @Nullable Facet facet) {
    completeDefaultConfiguration(configuration, module);
    return new OsmorcFacet(this, module, configuration, facet, name);
  }

  @Override
  public boolean isSuitableModuleType(ModuleType moduleType) {
    return moduleType instanceof JavaModuleType;
  }

  private static void completeDefaultConfiguration(OsmorcFacetConfiguration configuration, Module module) {
    if (StringUtil.isEmpty(configuration.getJarFileLocation())) {
      String jarFileName = module.getName().replaceAll("[\\s]", "_") + ".jar";

      // by default put stuff into the compiler output path.
      OutputPathType outputPathType = OutputPathType.CompilerOutputPath;
      ProjectSettings projectSettings = ProjectSettings.getInstance(module.getProject());
      if (projectSettings != null) {
        String bundlesOutputPath = projectSettings.getBundlesOutputPath();
        if (StringUtil.isNotEmpty(bundlesOutputPath)) {
          outputPathType = OutputPathType.OsgiOutputPath;
        }
      }

      configuration.setJarFileLocation(jarFileName, outputPathType);
    }
  }

  @Override
  public Icon getIcon() {
    return OsmorcIdeaIcons.Osgi;
  }
}
