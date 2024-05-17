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

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManagerAdapter;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectCloseListener;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.facet.OsmorcFacetType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Manages watch requests for the files that are part of the additional JAR contents and which
 * may reside outside the project folder. Files outside the project folder are normally not watched
 * by IDEA and so changes to them normally aren't noticed by IDEA. This manager makes sure that
 * changes to those files in the additional JAR contents are watched.
 *
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public final class AdditionalJARContentsWatcherManager {
  public static AdditionalJARContentsWatcherManager getInstance(@NotNull Module module) {
    return module.getService(AdditionalJARContentsWatcherManager.class);
  }

  private final Module myModule;
  private final List<VirtualFile> myAdditionalBundleJARContents;
  private final List<LocalFileSystem.WatchRequest> myWatchRequests;

  public AdditionalJARContentsWatcherManager(@NotNull Module module) {
    myModule = module;
    myAdditionalBundleJARContents = new ArrayList<>();
    myWatchRequests = new ArrayList<>();
    updateWatcherSetup();
  }

  void updateWatcherSetup() {
    OsmorcFacet osmorcFacet = OsmorcFacet.getInstance(myModule);
    LocalFileSystem fileSystem = LocalFileSystem.getInstance();
    if (osmorcFacet != null) {
      List<VirtualFile> newAdditionalJARContents = new ArrayList<>();

      OsmorcFacetConfiguration osmorcFacetConfiguration = osmorcFacet.getConfiguration();
      List<Pair<String, String>> jarContents = osmorcFacetConfiguration.getAdditionalJARContents();
      for (Pair<String, String> jarContent : jarContents) {
        VirtualFile file = fileSystem.findFileByPath(jarContent.getFirst());
        if (file != null) {
          newAdditionalJARContents.add(file);
        }
      }

      List<LocalFileSystem.WatchRequest> toRemove = new ArrayList<>();
      for (Iterator<VirtualFile> jarContentsIterator = myAdditionalBundleJARContents.iterator(); jarContentsIterator.hasNext(); ) {
        VirtualFile file = jarContentsIterator.next();
        if (!newAdditionalJARContents.contains(file)) {
          jarContentsIterator.remove();
          for (Iterator<LocalFileSystem.WatchRequest> watchIterator = myWatchRequests.iterator(); watchIterator.hasNext(); ) {
            LocalFileSystem.WatchRequest watchRequest = watchIterator.next();
            if (Comparing.strEqual(file.getPath(), watchRequest.getRootPath())) {
              toRemove.add(watchRequest);
              watchIterator.remove();
            }
          }
        }
      }

      List<String> toAdd = new ArrayList<>();
      for (VirtualFile newAdditionalJARContent : newAdditionalJARContents) {
        if (!myAdditionalBundleJARContents.contains(newAdditionalJARContent)) {
          toAdd.add(newAdditionalJARContent.getPath());
          myAdditionalBundleJARContents.add(newAdditionalJARContent);
        }
      }

      Set<LocalFileSystem.WatchRequest> requests = fileSystem.replaceWatchedRoots(toRemove, toAdd, null);
      myWatchRequests.addAll(requests);
    }
    else {
      cleanup();
    }
  }

  void cleanup() {
    LocalFileSystem.getInstance().removeWatchedRoots(myWatchRequests);
    myWatchRequests.clear();
    myAdditionalBundleJARContents.clear();
  }

  public static class FacetListener extends FacetManagerAdapter {
    @Override
    public void facetAdded(@NotNull Facet facet) {
      handleFacetChange(facet);
    }

    @Override
    public void facetConfigurationChanged(@NotNull Facet facet) {
      handleFacetChange(facet);
    }

    private static void handleFacetChange(Facet<?> facet) {
      if (facet.getTypeId() == OsmorcFacetType.ID) {
        getInstance(facet.getModule()).updateWatcherSetup();
      }
    }
  }

  public static class ProjectListener implements ProjectCloseListener {
    @Override
    public void projectClosing(@NotNull Project project) {
      for (Module module : ModuleManager.getInstance(project).getModules()) {
        AdditionalJARContentsWatcherManager manager = module.getServiceIfCreated(AdditionalJARContentsWatcherManager.class);
        if (manager != null) {
          manager.cleanup();
        }
      }
    }
  }
}