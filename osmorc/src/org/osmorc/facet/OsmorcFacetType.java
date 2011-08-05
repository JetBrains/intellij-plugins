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
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.settings.ProjectSettings;

import javax.swing.*;
import java.io.IOException;

/**
 * The facet type of Osmorc.
 *
 * @author Robert F. Beeger (robert@beeger.net)
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class OsmorcFacetType extends FacetType<OsmorcFacet, OsmorcFacetConfiguration> {
  public static final FacetTypeId<OsmorcFacet> ID = new FacetTypeId<OsmorcFacet>("Osmorc");

  public static OsmorcFacetType getInstance() {
    return findInstance(OsmorcFacetType.class);
  }

  protected OsmorcFacetType() {
    super(ID, "Osmorc", "OSGi");
  }

  public OsmorcFacetConfiguration createDefaultConfiguration() {
    return new OsmorcFacetConfiguration();
  }

  public OsmorcFacet createFacet(
    @NotNull Module module, String name,
    @NotNull OsmorcFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
    completeDefaultConfiguration(configuration, module);
    return new OsmorcFacet(this, module, configuration, underlyingFacet, name);
  }

  public boolean isSuitableModuleType(ModuleType moduleType) {
    return moduleType instanceof JavaModuleType;
  }

  private void completeDefaultConfiguration(final OsmorcFacetConfiguration configuration, final Module module) {
    if (configuration.getJarFileLocation().length() == 0) {
      final String outputPathUrl = CompilerModuleExtension.getInstance(module).getCompilerOutputUrl();
      StartupManager.getInstance(module.getProject()).runWhenProjectIsInitialized(new Runnable() {
        public void run() {
          try {
            // Must be run in event dispatch thread therefore... we put all there..
            VfsUtil.createDirectories(VfsUtil.urlToPath(outputPathUrl));
          }
          catch (IOException e) {
            return;
          }
          VirtualFile moduleCompilerOutputPath = CompilerModuleExtension.getInstance(module).getCompilerOutputPath();
          if (moduleCompilerOutputPath == null) {
            return;
          }

          String jarFileName = module.getName();
          jarFileName = jarFileName.replaceAll("[\\s]", "_") + ".jar";
          // by default put stuff into the compiler output path.
          OsmorcFacetConfiguration.OutputPathType outputPathType = OsmorcFacetConfiguration.OutputPathType.CompilerOutputPath;
          final ProjectSettings projectSettings = ModuleServiceManager.getService(module, ProjectSettings.class);
          if (projectSettings != null) {
            String bundlesOutputPath = projectSettings.getBundlesOutputPath();
            if (bundlesOutputPath != null && bundlesOutputPath.length() > 0) {
              outputPathType = OsmorcFacetConfiguration.OutputPathType.OsgiOutputPath;
            }
          }
          configuration.setJarFileLocation(jarFileName, outputPathType);
        }
      });
    }
  }


  public Icon getIcon() {
    return OsmorcBundle.getSmallIcon();
  }
}
