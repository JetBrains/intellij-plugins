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
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetType;
import org.osmorc.frameworkintegration.FrameworkInstanceModuleManager;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcModuleComponent implements ModuleComponent {
  private MessageBusConnection connection;
  private final Module myModule;
  private final BundleManager myBundleManager;
  private final FrameworkInstanceModuleManager myFrameworkInstanceModuleManager;
  private final AdditionalJARContentsWatcherManager myAdditionalJARContentsWatcherManager;
  private final Application myApplication;
  private boolean disposed;

  public OsmorcModuleComponent(Module module,
                               BundleManager bundleManager,
                               FrameworkInstanceModuleManager frameworkInstanceModuleManager,
                               AdditionalJARContentsWatcherManager additionalJARContentsWatcherManager,
                               Application application) {
    this.myModule = module;
    myBundleManager = bundleManager;
    this.myFrameworkInstanceModuleManager = frameworkInstanceModuleManager;
    this.myAdditionalJARContentsWatcherManager = additionalJARContentsWatcherManager;
    this.myApplication = application;
    disposed = false;
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return "OsmorcModuleComponent";
  }

  public void initComponent() {
    disposed = false;
    connection = myModule.getMessageBus().connect();
    connection.subscribe(FacetManager.FACETS_TOPIC, new FacetManagerAdapter() {
      public void facetAdded(@NotNull Facet facet) {
        handleFacetChange(facet);
      }

      @Override
      public void facetRemoved(@NotNull Facet facet) {
        myFrameworkInstanceModuleManager.updateFrameworkInstanceModule();
      }

      public void facetConfigurationChanged(@NotNull Facet facet) {
        handleFacetChange(facet);
      }
    });
  }

  public void disposeComponent() {
    if (connection != null) {
      connection.disconnect();
    }
    disposed = true;
  }

  public void projectOpened() {
    myApplication.invokeLater(new Runnable() {
      public void run() {
        if (!disposed && OsmorcFacet.hasOsmorcFacet(myModule)) {
          buildManuallyEditedManifestIndex();
          updateModuleDependencyIndex();
        }
      }
    });
  }

  public void projectClosed() {
    myAdditionalJARContentsWatcherManager.dispose();
  }

  public void moduleAdded() {
  }

  private void updateModuleDependencyIndex() {
    myApplication.invokeLater(new Runnable() {
      public void run() {
        // Fix for EA-23251
        if ( myModule.isDisposed() ) {
          return;
        }
        ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        if (indicator != null) {
          indicator.setText("Updating OSGi dependency index for module '" + myModule.getName() + "'");
        }
        ModifiableRootModel model = new ReadAction<ModifiableRootModel>() {
          protected void run(Result<ModifiableRootModel> result) throws Throwable {
            ModifiableRootModel model = ModuleRootManager.getInstance(myModule).getModifiableModel();
            result.setResult(model);
          }
        }.execute().getResultObject();

        OrderEntry[] entries = model.getOrderEntries();
        try {
          for (int i = 0, entriesLength = entries.length; i < entriesLength; i++) {
            if (indicator != null) {
              indicator.setFraction(i / entriesLength);
            }
            OrderEntry entry = entries[i];
            if (entry instanceof LibraryOrderEntry) {
              final Library library = ((LibraryOrderEntry)entry).getLibrary();
              if (library != null) {
                myBundleManager.addOrUpdateBundle(library);
              }
            }
          }
        }
        finally {
          model.dispose();
        }
      }
    });
  }

  /**
   * Runs over the module which has a manually edited manifest and refreshes it's information in the bundle manager.
   */
  private void buildManuallyEditedManifestIndex() {
    final OsmorcFacet facet = OsmorcFacet.getInstance(myModule);
    if (facet != null && facet.getConfiguration().isManifestManuallyEdited()) {
        myApplication.invokeLater(new Runnable() {
        public void run() {
          if (myModule.isDisposed()) return;
          ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
          if (indicator != null) {
            indicator.setIndeterminate(true);
            indicator.setText("Updating manifest indices.");
          }
          myBundleManager.addOrUpdateBundle(myModule);
        }
      });
    }
  }

  private void handleFacetChange(Facet facet) {
    if (!disposed && facet.getTypeId() == OsmorcFacetType.ID) {
      if (facet.getModule().getProject().isInitialized()) {
        myFrameworkInstanceModuleManager.updateFrameworkInstanceModule();
        buildManuallyEditedManifestIndex();
      }
      myAdditionalJARContentsWatcherManager.updateWatcherSetup();
    }
  }
}
