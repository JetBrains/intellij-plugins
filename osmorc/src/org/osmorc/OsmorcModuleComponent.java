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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetType;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcModuleComponent implements ModuleComponent
{
  public OsmorcModuleComponent(Module module, ModuleDependencySynchronizer moduleDependencySynchronizer,
                               AdditionalJARContentsWatcherManager additionalJARContentsWatcherManager, Application application)
  {
    _module = module;
    _moduleDependencySynchronizer = moduleDependencySynchronizer;
    _additionalJARContentsWatcherManager = additionalJARContentsWatcherManager;
    _application = application;
    _disposed = false;
  }

  public void projectOpened()
  {
    _application.invokeLater(new Runnable()
    {
      public void run()
      {
        if (!_disposed && OsmorcFacet.hasOsmorcFacet(_module))
        {
          _moduleDependencySynchronizer.syncDependenciesFromManifest();
        }
      }
    });
  }

  public void projectClosed()
  {
    _additionalJARContentsWatcherManager.dispose();
  }

  public void moduleAdded()
  {
  }

  @NonNls
  @NotNull
  public String getComponentName()
  {
    return "OsmorcModuleComponent";
  }

  public void initComponent()
  {
    _disposed = false;
    MessageBusConnection connection = _module.getMessageBus().connect();
    connection.subscribe(FacetManager.FACETS_TOPIC, new FacetManagerAdapter()
    {
      public void facetAdded(@NotNull Facet facet)
      {
        if (!_disposed && facet.getTypeId() == OsmorcFacetType.ID)
        {
          _moduleDependencySynchronizer.syncDependenciesFromManifest();
          syncAllModuleDependencies();
          _additionalJARContentsWatcherManager.updateWatcherSetup();
        }
      }

      public void facetConfigurationChanged(@NotNull Facet facet)
      {
        if (!_disposed && facet.getTypeId() == OsmorcFacetType.ID)
        {
          _moduleDependencySynchronizer.syncDependenciesFromManifest();
          syncAllModuleDependencies();
          _additionalJARContentsWatcherManager.updateWatcherSetup();
        }
      }
    });
  }

  public void disposeComponent()
  {
    _disposed = true;
  }

  private void syncAllModuleDependencies()
  {
    Module[] modules = ModuleManager.getInstance(_module.getProject()).getModules();
    for (Module module : modules)
    {
      if (OsmorcFacet.hasOsmorcFacet(module))
      {
        ModuleDependencySynchronizer.getInstance(module).syncDependenciesFromManifest();
      }
    }
  }


  private final Module _module;
  private final ModuleDependencySynchronizer _moduleDependencySynchronizer;
  private AdditionalJARContentsWatcherManager _additionalJARContentsWatcherManager;
  private final Application _application;
  private boolean _disposed;
}
