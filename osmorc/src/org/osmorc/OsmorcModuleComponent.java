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
package org.osmorc;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetManagerAdapter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetType;
import org.osmorc.impl.AdditionalJARContentsWatcherManager;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcModuleComponent implements ModuleComponent {
  private final Module myModule;
  private final BundleManager myBundleManager;
  private final AdditionalJARContentsWatcherManager myWatcherManager;

  public OsmorcModuleComponent(Module module, BundleManager bundleManager, AdditionalJARContentsWatcherManager watcherManager) {
    myModule = module;
    myBundleManager = bundleManager;
    myWatcherManager = watcherManager;
  }

  @NonNls
  @NotNull
  @Override
  public String getComponentName() {
    return "OsmorcModuleComponent";
  }

  @Override
  public void initComponent() {
    myModule.getMessageBus().connect(myModule).subscribe(FacetManager.FACETS_TOPIC, new FacetManagerAdapter() {
      @Override
      public void facetAdded(@NotNull Facet facet) {
        handleFacetChange(facet);
      }

      @Override
      public void facetConfigurationChanged(@NotNull Facet facet) {
        handleFacetChange(facet);
      }
    });
  }

  @Override
  public void disposeComponent() { }

  @Override
  public void projectOpened() {
    // the project component will rebuild indices
  }

  @Override
  public void projectClosed() {
    myWatcherManager.cleanup();
  }

  @Override
  public void moduleAdded() { }

  private void handleFacetChange(Facet facet) {
    if (facet.getTypeId() == OsmorcFacetType.ID) {
      if (facet.getModule().getProject().isInitialized()) {
        // reindex the module itself
        buildManifestIndex();
      }
      myWatcherManager.updateWatcherSetup();
    }
  }

  /**
   * Runs over the module and refreshes it's information in the bundle manager.
   */
  private void buildManifestIndex() {
    OsmorcFacet facet = OsmorcFacet.getInstance(myModule);
    if (facet != null) {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        @Override
        public void run() {
          if (myModule.isDisposed()) return;
          myBundleManager.reindex(myModule);
        }
      }, myModule.getDisposed());
    }
  }
}
