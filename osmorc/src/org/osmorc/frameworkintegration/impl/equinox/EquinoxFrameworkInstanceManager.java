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

package org.osmorc.frameworkintegration.impl.equinox;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.Version;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkInstanceManager;
import org.osmorc.frameworkintegration.LibraryHandler;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class EquinoxFrameworkInstanceManager implements FrameworkInstanceManager
{
  public EquinoxFrameworkInstanceManager(LibraryHandler libraryHandler, LocalFileSystem fileSystem,
                                         Application application)
  {
    _libraryHandler = libraryHandler;
    _fileSystem = fileSystem;
    _application = application;
    _equinoxSourceFinder = new EquinoxSourceFinder();
  }

  public void createLibraries(final FrameworkInstanceDefinition frameworkInstanceDefinition)
  {
    VirtualFile equinoxFolder = _fileSystem.findFileByPath(frameworkInstanceDefinition.getBaseFolder());
    assert equinoxFolder != null;
    final VirtualFile pluginsFolder = equinoxFolder.findChild("plugins");

    if (pluginsFolder != null)
    {
      _application.runWriteAction(new Runnable()
      {
        public void run()
        {
          pluginsFolder.refresh(false, true);
          _libraryHandler.startLibraryChanges();
          _libraryHandler.createLibrariesFromBundles(pluginsFolder, frameworkInstanceDefinition.getName(),
              _equinoxSourceFinder);
          _libraryHandler.commitLibraryChanges();

        }
      });
    }
    else
    {
      throw new RuntimeException("the folder " + equinoxFolder.getName() + " does not contain a plugins folder.");
    }
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

    VirtualFile equinoxFolder = _fileSystem.findFileByPath(frameworkInstanceDefinition.getBaseFolder());

    if (equinoxFolder == null || !equinoxFolder.isDirectory())
    {
      return MessageFormat.format(FOLDER_DOES_NOT_EXIST, frameworkInstanceDefinition.getBaseFolder());
    }
    VirtualFile pluginsFolder = equinoxFolder.findChild("plugins");
    if (pluginsFolder == null || !pluginsFolder.isDirectory())
    {
      return MessageFormat.format(NO_PLUGINS_FOLDER, frameworkInstanceDefinition.getBaseFolder());
    }

    return null;
  }

  @Nullable
  public Version getEclipseVersion(@NotNull FrameworkInstanceDefinition frameworkInstanceDefinition)
  {
    Version result = null;
    VirtualFile equinoxFolder = _fileSystem.findFileByPath(frameworkInstanceDefinition.getBaseFolder());

    if (equinoxFolder != null && equinoxFolder.exists() && equinoxFolder.isDirectory())
    {
      VirtualFile eclipseProductFile = equinoxFolder.findChild(".eclipseproduct");
      if (eclipseProductFile != null && eclipseProductFile.exists() && !eclipseProductFile.isDirectory())
      {
        try
        {
          Properties eclipseProductProperties = new Properties();
          eclipseProductProperties.load(eclipseProductFile.getInputStream());
          String versionString = eclipseProductProperties.getProperty("version");
          if (versionString != null)
          {
            result = Version.parseVersion(versionString);
          }
        }
        catch (IOException e)
        {
          LOG.warn("Error on reading properties from eclipse product file", e);
        }
      }
    }

    return result;
  }

  private final LibraryHandler _libraryHandler;
  private final VirtualFileSystem _fileSystem;
  private Application _application;
  private final EquinoxSourceFinder _equinoxSourceFinder;
  protected static final String FOLDER_DOES_NOT_EXIST =
      "The folder <strong>{0}</strong> does not exist or is not a folder. The folder should be an existing folder containing a <strong>plugins</strong> folder which contains the bundles of your Equinox installation. "
      ;
  protected static final String NO_PLUGINS_FOLDER =
      "The folder  <strong>{0}</strong> does not contain a <strong>plugins</strong> folder or <strong>plugins</strong> is not a folder. The base folder needs to be " +
          "the folder of your Equinox installation which contains a <strong>plugins</strong> folder containing all bundle" +
          " JARs of your Equinox installation.";

  private static final Logger LOG =
      Logger.getInstance("org.osmorc.frameworkintegration.impl.equinox.EquinoxFrameworkInstanceManager");

}
