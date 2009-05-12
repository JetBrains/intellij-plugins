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

package org.osmorc.frameworkintegration.impl.knopflerfish;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkInstanceLibrarySourceFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: Robert F. Beeger (robert@beeger.net)
 */
public class KnopflerfishSourceFinder implements FrameworkInstanceLibrarySourceFinder
{
  public KnopflerfishSourceFinder(@NotNull VirtualFile osgiFolder)
  {
    _sourceMapping = new HashMap<String, VirtualFile>();

    VirtualFile bundlesFolder = osgiFolder.findChild("bundles");
    if (bundlesFolder != null)
    {
      findSources(bundlesFolder);
    }
    VirtualFile frameworkFolder = osgiFolder.findFileByRelativePath("framework/src");
    if (frameworkFolder != null)
    {
      _sourceMapping.put("framework", frameworkFolder);
    }
  }

  private void findSources(VirtualFile folder)
  {
    VirtualFile sourceFolder = folder.findChild("src");
    if (sourceFolder != null)
    {
      _sourceMapping.put(folder.getName(), sourceFolder);
    }
    else
    {
      VirtualFile[] files = folder.getChildren();
      for (VirtualFile file : files)
      {
        if (file.isDirectory())
        {
          findSources(file);
        }
      }
    }

  }

  public List<VirtualFile> getSourceForLibraryClasses(@NotNull VirtualFile libraryClasses)
  {
    List<VirtualFile> result = new ArrayList<VirtualFile>();
    if (libraryClasses.getNameWithoutExtension().equals("framework"))
    {
      VirtualFile frameworkSources = _sourceMapping.get("framework");
      if (frameworkSources != null)
      {
        result.add(frameworkSources);
      }
    }
    else
    {
      String parentFolderName = libraryClasses.getParent().getName();
      VirtualFile sources = _sourceMapping.get(parentFolderName);
      if (sources != null)
      {
        result.add(sources);
      }
    }
    return result;
  }

  public boolean containsOnlySources(@NotNull VirtualFile libraryClassesCondidate)
  {
    return false;    
  }

  @NonNls
  private final Map<String, VirtualFile> _sourceMapping;
}
