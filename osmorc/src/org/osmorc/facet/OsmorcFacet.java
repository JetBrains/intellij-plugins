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
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.settings.ProjectSettings;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public final class OsmorcFacet extends Facet<OsmorcFacetConfiguration> {
  public OsmorcFacet(@NotNull Module module) {
    this(FacetTypeRegistry.getInstance().findFacetType(OsmorcFacetType.ID), module,
         new OsmorcFacetConfiguration(),
         null, "OSGi");
  }

  public OsmorcFacet(@NotNull FacetType facetType,
                     @NotNull Module module,
                     @NotNull OsmorcFacetConfiguration configuration,
                     @Nullable Facet underlyingFacet,
                     final String name) {
    super(facetType, module, name, configuration, underlyingFacet);
    configuration.setFacet(this);
  }

  /**
   * Returns the Osmorc facet for the given module.
   *
   * @param module the module
   * @return the Osmorc facet of this module or null if the module doesn't have an Osmorc facet.
   */
  public static @Nullable OsmorcFacet getInstance(@NotNull Module module) {
    return FacetManager.getInstance(module).getFacetByType(OsmorcFacetType.ID);
  }

  /**
   * Determines the module to which the given element belongs and returns the Osmorc facet for this module.
   *
   * @param element the element
   * @return the Osmorc facet of the module to which the element belongs or null if this module doesn't have an Osmorc
   *         facet or if the belonging module could not be determined.
   */
  public static @Nullable OsmorcFacet getInstance(@NotNull PsiElement element) {
    Module module = ModuleUtilCore.findModuleForPsiElement(element);
    if (module != null) {
      return getInstance(module);
    }
    return null;
  }

  /**
   * @param module the module to check
   * @return true if there is an Osmorc facet for the given module, false otherwise.
   */
  public static boolean hasOsmorcFacet(@NotNull Module module) {
    return getInstance(module) != null;
  }

  /**
   * @param element the element to check
   * @return true if the module of the element could be determined and this module has an Osmorc facet, false
   *         otherwise.
   */
  public static boolean hasOsmorcFacet(@NotNull PsiElement element) {
    Module module = ModuleUtilCore.findModuleForPsiElement(element);
    return module != null && hasOsmorcFacet(module);
  }

  public @NotNull String getManifestLocation() {
    if (getConfiguration().isUseProjectDefaultManifestFileLocation()) {
      return ProjectSettings.getInstance(getModule().getProject()).getDefaultManifestFileLocation();
    }
    else {
      return getConfiguration().getManifestLocation();
    }
  }

  /**
   * Returns the manifest file for this facet.
   *
   * @return the manifest file. If the manifest is automatically generated, returns null.
   */
  public @Nullable VirtualFile getManifestFile() {
    if (getConfiguration().isOsmorcControlsManifest()) {
      String pathToJar = getConfiguration().getJarFileLocation();
      VirtualFile jarFile = LocalFileSystem.getInstance().findFileByPath(pathToJar);
      if (jarFile == null) {
        return null;
      }

      VirtualFile jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(jarFile);
      if (jarRoot != null) {
        return jarRoot.findFileByRelativePath("META-INF/MANIFEST.MF");
      }

      return null;
    }
    String path = getManifestLocation();
    path = path.replace('\\', '/');

    VirtualFile[] contentRoots = ModuleRootManager.getInstance(getModule()).getContentRoots();
    for (VirtualFile contentRoot : contentRoots) {
      VirtualFile manifestFile = contentRoot.findFileByRelativePath(path);
      if (manifestFile != null) {
        return manifestFile;
      }
    }

    return null;
  }
}
