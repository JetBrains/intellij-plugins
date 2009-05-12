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

import com.intellij.openapi.application.Application;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkInstanceManager;
import org.osmorc.frameworkintegration.LibraryHandler;
import org.osmorc.i18n.OsmorcBundle;

import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class KnopflerfishFrameworkInstanceManager implements FrameworkInstanceManager
{
  public KnopflerfishFrameworkInstanceManager(LibraryHandler libraryHandler, LocalFileSystem fileSystem,
                                              Application application)
  {
    _libraryHandler = libraryHandler;
    _fileSystem = fileSystem;
    _application = application;
  }

  public void createLibraries(final FrameworkInstanceDefinition frameworkInstanceDefinition)
  {
    VirtualFile installFolder = _fileSystem.findFileByPath(frameworkInstanceDefinition.getBaseFolder());

    if (installFolder == null)
    {
      throw new RuntimeException("The folder " + frameworkInstanceDefinition.getBaseFolder() + " does not exist.");
    }
    if (!installFolder.isDirectory())
    {
      throw new RuntimeException(frameworkInstanceDefinition.getBaseFolder() + " is not a folder");
    }

    VirtualFile knopflerfishOrgFolder = installFolder.findChild("knopflerfish.org");
    if (knopflerfishOrgFolder == null)
    {
      throw new RuntimeException(installFolder.getPath() + " does not contain a folder \"knopflerfish.org\"");
    }

    final VirtualFile osgiFolder = knopflerfishOrgFolder.findChild("osgi");
    if (osgiFolder == null)
    {
      throw new RuntimeException(knopflerfishOrgFolder.getPath() + " does not contain a folder \"osgi\"");
    }


    _application.runWriteAction(new Runnable()
    {
      public void run()
      {
        osgiFolder.refresh(false, true);

        KnopflerfishSourceFinder sourceFinder = new KnopflerfishSourceFinder(osgiFolder);

        _libraryHandler.startLibraryChanges();
        _libraryHandler.createLibrariesFromBundles(osgiFolder, frameworkInstanceDefinition.getName(), sourceFinder);

        VirtualFile jarsFolder = osgiFolder.findChild("jars");
        if (jarsFolder != null)
        {
          if (!jarsFolder.isDirectory())
          {
            throw new RuntimeException(jarsFolder.getPath() + " is not a folder");
          }
          VirtualFile[] files = jarsFolder.getChildren();
          for (VirtualFile file : files)
          {
            if (file.isDirectory())
            {
              _libraryHandler.createLibrariesFromBundles(file, frameworkInstanceDefinition.getName(), sourceFinder);
            }
          }
        }
        _libraryHandler.commitLibraryChanges();
      }
    });

  }

  public void removeLibraries(final FrameworkInstanceDefinition frameworkInstanceDefinition)
  {
    _application.runWriteAction(new Runnable()
    {
      public void run()
      {
        _libraryHandler.startLibraryChanges();
        _libraryHandler.deleteLibraries(frameworkInstanceDefinition.getName());
        _libraryHandler.commitLibraryChanges();
      }
    });
  }

  public List<Library> getLibraries(FrameworkInstanceDefinition frameworkInstanceDefinition)
  {
    return _libraryHandler.getLibraries(frameworkInstanceDefinition.getName());
  }

  public String checkValidity(@NotNull FrameworkInstanceDefinition frameworkInstanceDefinition)
  {
    if (frameworkInstanceDefinition.getName() == null || frameworkInstanceDefinition.getName().trim().length() == 0)
    {
      return "A name for the framework instance needs to be given.";
    }

    VirtualFile installFolder = _fileSystem.findFileByPath(frameworkInstanceDefinition.getBaseFolder());

    if (installFolder == null || !installFolder.isDirectory())
    {
      return OsmorcBundle
          .getTranslation("knopflerfish.folder.does.not.exist", frameworkInstanceDefinition.getBaseFolder());
    }

    VirtualFile knopflerfishOrgFolder = installFolder.findChild("knopflerfish.org");
    if (knopflerfishOrgFolder == null || !knopflerfishOrgFolder.isDirectory())
    {
      return OsmorcBundle
          .getTranslation("knopflerfish.folder.knopflerfish.org.missing", frameworkInstanceDefinition.getBaseFolder());
    }

    VirtualFile osgiFolder = knopflerfishOrgFolder.findChild("osgi");
    if (osgiFolder == null || !osgiFolder.isDirectory())
    {
      return OsmorcBundle.getTranslation("knopflerfish.folder.osgi.missing", knopflerfishOrgFolder.getPath());
    }

    VirtualFile jarsFolder = osgiFolder.findChild("jars");
    if (jarsFolder == null || !jarsFolder.isDirectory())
    {
      return OsmorcBundle.getTranslation("knopflerfish.folder.jars.missing", osgiFolder.getPath());
    }

    return null;
  }


  private final LibraryHandler _libraryHandler;
  private final LocalFileSystem _fileSystem;
  private final Application _application;
}
