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

package org.osmorc.frameworkintegration.impl.felix;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkInstanceLibrarySourceFinder;
import org.osmorc.frameworkintegration.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Robert F. Beeger (robert@beeger.net)
 */
public class FelixSourceFinder implements FrameworkInstanceLibrarySourceFinder
{
  public List<VirtualFile> getSourceForLibraryClasses(@NotNull VirtualFile libraryClasses)
  {
    List<VirtualFile> result = new ArrayList<VirtualFile>();

    VirtualFile bundleFolder = libraryClasses.getParent();
    assert bundleFolder != null;
    VirtualFile felixFolder = bundleFolder.getParent();
    assert felixFolder != null;
    VirtualFile srcFolder = felixFolder.findChild("src");

    if (srcFolder != null)
    {
      if (bundleFolder.getName().equals("bin"))
      {
        VirtualFile binFolder = srcFolder.findChild("bin");
        if (binFolder != null)
        {
          VirtualFile[] children = binFolder.getChildren();
          for (VirtualFile child : children)
          {
            addIfSource(child, result);
          }
        }
      }
      else
      {
        VirtualFile srcBundleFolder = srcFolder.findChild("bundle");
        if (srcBundleFolder != null)
        {
          VirtualFile[] children = srcBundleFolder.getChildren();
          for (VirtualFile child : children)
          {
            addIfSource(libraryClasses, child, result);
          }
        }
      }
    }

    return result;
  }

  public boolean containsOnlySources(@NotNull VirtualFile libraryClassesCondidate)
  {
    return false;
  }

  private void addIfSource(VirtualFile sourceZIP, List<VirtualFile> result)
  {
    VirtualFile folder = FileUtil.getFolder(sourceZIP);
    if (folder != null)
    {
      String name = FileUtil.getNameWithoutTail(FileUtil.getNameWithoutTail(sourceZIP, ".zip"), "-project");
      VirtualFile sourceFolder = folder.findFileByRelativePath(name + ZIP_SOURCE_PATH);
      if (sourceFolder != null && sourceFolder.isDirectory())
      {
        result.add(sourceFolder);
      }
    }
  }

  private void addIfSource(VirtualFile libraryClasses, VirtualFile sourceZIP, List<VirtualFile> result)
  {
    VirtualFile folder = FileUtil.getFolder(sourceZIP);
    if (folder != null)
    {
      String classesName = FileUtil.getNameWithoutTail(libraryClasses, ".jar");
      String sourceName = FileUtil.getNameWithoutTail(FileUtil.getNameWithoutTail(sourceZIP, ".zip"), "-project");

      if (classesName.contains(sourceName))
      {
        VirtualFile sourceFolder = folder.findFileByRelativePath(sourceName + ZIP_SOURCE_PATH);
        if (sourceFolder != null && sourceFolder.isDirectory())
        {
          result.add(sourceFolder);
        }
      }
    }
  }

  private static final String ZIP_SOURCE_PATH = "/src/main/java";
}
