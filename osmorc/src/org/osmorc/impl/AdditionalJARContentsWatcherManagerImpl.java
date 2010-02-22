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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.osmorc.AdditionalJARContentsWatcherManager;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.facet.OsmorcFacetUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class AdditionalJARContentsWatcherManagerImpl implements AdditionalJARContentsWatcherManager
{
  private final Logger logger = Logger.getInstance("#org.osmorc.impl.AdditionalJARContentsWatcherManagerImpl");
  
  public AdditionalJARContentsWatcherManagerImpl(Module module, OsmorcFacetUtil osmorcFacetUtil, LocalFileSystem fileSystem)
  {
    _module = module;
    _osmorcFacetUtil = osmorcFacetUtil;
    _fileSystem = fileSystem;
    _additionalBundleJARContents = new ArrayList<VirtualFile>();
    _watchRequests = new ArrayList<LocalFileSystem.WatchRequest>();

    updateWatcherSetup();
  }

  public void updateWatcherSetup()
  {
    if (_osmorcFacetUtil.hasOsmorcFacet(_module))
    {
      OsmorcFacet osmorcFacet = _osmorcFacetUtil.getOsmorcFacet(_module);
      List<VirtualFile> newAdditionalJARContents = new ArrayList<VirtualFile>();

      OsmorcFacetConfiguration osmorcFacetConfiguration = osmorcFacet.getConfiguration();
      List<Pair<String, String>> jarContents = osmorcFacetConfiguration.getAdditionalJARContents();
      for (Pair<String, String> jarContent : jarContents)
      {
        VirtualFile file = _fileSystem.findFileByPath(jarContent.getFirst());
        if (file != null)
        {
          newAdditionalJARContents.add(file);
        }
      }

      for (Iterator<VirtualFile> jarContentsIterator = _additionalBundleJARContents.iterator();
           jarContentsIterator.hasNext();)
      {
        VirtualFile file = jarContentsIterator.next();
        if (!newAdditionalJARContents.contains(file))
        {
          jarContentsIterator.remove();
          for (Iterator<LocalFileSystem.WatchRequest> watchIterator = _watchRequests.iterator(); watchIterator.hasNext();)
          {
            LocalFileSystem.WatchRequest watchRequest = watchIterator.next();
            if (Comparing.strEqual(file.getPath(), watchRequest.getRootPath()))
            {
              _fileSystem.removeWatchedRoot(watchRequest);
              watchIterator.remove();
            }
          }
        }
      }
      for (VirtualFile newAdditionalJARContent : newAdditionalJARContents)
      {
        if (!_additionalBundleJARContents.contains(newAdditionalJARContent))
        {
          final LocalFileSystem.WatchRequest watchRequest = _fileSystem.addRootToWatch(newAdditionalJARContent.getPath(), true);
          // This Check should fix EA-18652, so no NULL-values are entered into the _watchRequests list.
          if ( watchRequest != null ) {
            _watchRequests.add(watchRequest);
          }
          else {
            logger.warn("It seems like " + newAdditionalJARContent.getPath() + " doesn't belong to the file system or the file watcher is not operational.");
          }
          _additionalBundleJARContents.add(newAdditionalJARContent);
        }
      }
    }
    else
    {
      _additionalBundleJARContents.clear();
      for (LocalFileSystem.WatchRequest watchRequest : _watchRequests)
      {
        _fileSystem.removeWatchedRoot(watchRequest);
      }
      _watchRequests.clear();
    }
  }

  public void dispose()
  {
    for (LocalFileSystem.WatchRequest watchRequest : _watchRequests)
    {
      _fileSystem.removeWatchedRoot(watchRequest);
    }
    _watchRequests.clear();
    _additionalBundleJARContents.clear();
  }

  private LocalFileSystem _fileSystem;
  private List<VirtualFile> _additionalBundleJARContents;
  private List<LocalFileSystem.WatchRequest> _watchRequests;
  private Module _module;
  private OsmorcFacetUtil _osmorcFacetUtil;
}
