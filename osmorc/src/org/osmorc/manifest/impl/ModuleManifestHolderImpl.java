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
package org.osmorc.manifest.impl;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.ManifestHolderDisposedException;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ModuleManifestHolderImpl extends AbstractManifestHolderImpl {
  private final Module myModule;
  private final Application myApplication;
  private BundleManifest myBundleManifest;

  public ModuleManifestHolderImpl(Module module, Application application) {
    myModule = module;
    myApplication = application;
  }

  @Nullable
  public BundleManifest getBundleManifest() throws ManifestHolderDisposedException {
    if (isDisposed()) {
      throw new ManifestHolderDisposedException();
    }

    if (myBundleManifest != null) {
      VirtualFile cachedFile = myBundleManifest.getManifestFile().getVirtualFile();
      VirtualFile fileFromSettings = getManifestFile();
      if (!Comparing.equal(cachedFile, fileFromSettings)) {
        myBundleManifest = null;  // manifest file changed, need to start from scratch.
      }
    }

    // only try to load the manifest if we have an osmorc facet for that module
    if (myBundleManifest == null) {
      OsmorcFacet facet = OsmorcFacet.getInstance(myModule);
      if (facet != null) {
        myBundleManifest = loadManifest();
      }
    }

    return myBundleManifest;
  }

  public boolean isDisposed() {
    return myModule.isDisposed();
  }

  private BundleManifest loadManifest() {
    final VirtualFile manifestFile = getManifestFile();
    if (manifestFile != null) {
      return myApplication.runReadAction(new Computable<BundleManifest>() {
        public BundleManifest compute() {
          PsiFile psiFile = PsiManager.getInstance(myModule.getProject()).findFile(manifestFile);
          return psiFile instanceof ManifestFile ? new BundleManifestImpl((ManifestFile)psiFile) : null;
        }
      });
    }
    return null;
  }

  @Nullable
  private VirtualFile getManifestFile() {
    OsmorcFacet facet = OsmorcFacet.getInstance(myModule);
    return facet != null ? facet.getManifestFile() : null;
  }

  @Override
  public Object getBoundObject() throws ManifestHolderDisposedException {
    if (isDisposed()) {
      throw new ManifestHolderDisposedException();
    }
    return myModule;
  }
}
