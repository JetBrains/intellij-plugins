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

package org.osmorc.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.BundleManager;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.ManifestHolderRegistry;
import org.osmorc.settings.ApplicationSettings;
import org.osmorc.settings.ProjectSettings;

import java.util.Collections;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class BundleManagerImpl implements BundleManager {

  private final ModuleManager myModuleManager;
  private final ManifestHolderRegistry myManifestHolderRegistry;
  private final FrameworkIntegratorRegistry myFrameworkIntegratorRegistry;
  private final ProjectSettings myProjectSettings;
  private final ApplicationSettings myApplicationSettings;
  private long myHighestBundleId = 0;

  private String _currentFrameworkInstanceName;
  
  public BundleManagerImpl(ModuleManager moduleManager, ManifestHolderRegistry manifestHolderRegistry,
                           FrameworkIntegratorRegistry frameworkIntegratorRegistry,
                           ProjectSettings projectSettings, ApplicationSettings applicationSettings) {
    this.myModuleManager = moduleManager;
    this.myManifestHolderRegistry = manifestHolderRegistry;
    this.myFrameworkIntegratorRegistry = frameworkIntegratorRegistry;
    this.myProjectSettings = projectSettings;
    this.myApplicationSettings = applicationSettings;
  }

  @Nullable
  private Object findBundle(String bundleSymbolicName) {
    Module[] modules = myModuleManager.getModules();
    for (Module module : modules) {
      BundleManifest bundleManifest = getBundleManifest(module);
      if (bundleManifest != null && bundleSymbolicName.equals(bundleManifest.getBundleSymbolicName())) {
        return module;
      }
    }

    List<Library> libraries = getFrameworkInstanceLibraries();
    for (Library library : libraries) {
      BundleManifest bundleManifest = getBundleManifest(library);
      if (bundleManifest != null && bundleSymbolicName.equals(bundleManifest.getBundleSymbolicName())) {
        return library;
      }
    }

    return null;
  }

  @Nullable
  public BundleManifest getBundleManifest(String bundleSymbolicName) {
    Object bundle = findBundle(bundleSymbolicName);

    if (bundle != null) {
      return getBundleManifest(bundle);
    }
    return null;
  }

  public BundleManifest getBundleManifest(@NotNull Object bundle) {
    return getManifestHolder(bundle).getBundleManifest();
  }

  public void addOrUpdateBundle(@NotNull Object bundle) {
    createInitialState();
    addOrUpdateBundleInternal(bundle);
  }

  private void addOrUpdateBundleInternal(Object bundle) {
    BundleManifest bundleManifest = getBundleManifest(bundle);
    if (bundleManifest != null) {
      ManifestHolder manifestHolder = getManifestHolder(bundle);
      if (manifestHolder.getBundleID() == -1) {
        manifestHolder.setBundleID(++myHighestBundleId);
      }
    }
  }

  public boolean reloadFrameworkInstanceLibraries(boolean onlyIfFrameworkInstanceSelectionChanged) {
    String frameworkInstanceName = myProjectSettings.getFrameworkInstanceName();
    if (!onlyIfFrameworkInstanceSelectionChanged ||
        (frameworkInstanceName != null && !frameworkInstanceName.equals(_currentFrameworkInstanceName))) {
      myManifestHolderRegistry.clearLibraryManifestHolders();
      loadFrameworkInstanceLibraryBundles();
      return true;
    }
    return false;
  }

  private synchronized void createInitialState() {

    if (myManifestHolderRegistry.isEmpty()) {
      Module[] modules = myModuleManager.getModules();
      for (Module module : modules) {
        addOrUpdateBundleInternal(module);
      }

      loadFrameworkInstanceLibraryBundles();
    }
  }

  private void loadFrameworkInstanceLibraryBundles() {
    _currentFrameworkInstanceName = myProjectSettings.getFrameworkInstanceName();
    if (_currentFrameworkInstanceName != null) {
      List<Library> libraries = getFrameworkInstanceLibraries();
      for (Library library : libraries) {
        addOrUpdateBundleInternal(library);
      }
    }
  }

  private List<Library> getFrameworkInstanceLibraries() {
    FrameworkInstanceDefinition frameworkInstanceDefinition =
      myApplicationSettings.getFrameworkInstance(myProjectSettings.getFrameworkInstanceName());
    List<Library> libraries = null;
    if (frameworkInstanceDefinition != null) {
      FrameworkIntegrator frameworkIntegrator =
        myFrameworkIntegratorRegistry.findIntegratorByInstanceDefinition(frameworkInstanceDefinition);
      libraries = frameworkIntegrator.getFrameworkInstanceManager().getLibraries(frameworkInstanceDefinition);
    }
    if (libraries == null) {
      libraries = Collections.emptyList();
    }
    return libraries;
  }

  protected ManifestHolder getManifestHolder(Object bundle) {
    return myManifestHolderRegistry.getManifestHolder(bundle);
  }

}
