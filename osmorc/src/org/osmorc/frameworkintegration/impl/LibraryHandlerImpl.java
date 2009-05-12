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

package org.osmorc.frameworkintegration.impl;

import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkInstanceLibrarySourceFinder;
import org.osmorc.frameworkintegration.LibraryHandler;
import org.osmorc.frameworkintegration.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class LibraryHandlerImpl implements LibraryHandler
{
  public void startLibraryChanges()
  {
    _modifiableApplicationLibraryTableModel =
        LibraryTablesRegistrar.getInstance().getLibraryTable().getModifiableModel();
    _modifiableLibraryModels = new ArrayList<Library.ModifiableModel>();
    _isChangingLibraries = true;
  }

  public void commitLibraryChanges()
  {
    for (Library.ModifiableModel model : _modifiableLibraryModels)
    {
      model.commit();
    }
    _modifiableLibraryModels.clear();
    _modifiableApplicationLibraryTableModel.commit();
  }

  public void endLibraryChanges()
  {
    _modifiableApplicationLibraryTableModel = null;
    _modifiableLibraryModels = null;
    _isChangingLibraries = false;
  }

  public boolean hasLibraryChanges()
  {
    return _modifiableApplicationLibraryTableModel.isChanged();
  }

  public boolean isChangingLibraries()
  {
    return _isChangingLibraries;
  }

  public void createLibrariesFromBundles(
      @NotNull VirtualFile bundleFolder,
      @NotNull String instanceName, @NotNull FrameworkInstanceLibrarySourceFinder sourceFinder)
  {
    assert isChangingLibraries();

    if (!bundleFolder.isDirectory())
    {
      throw new IllegalArgumentException(bundleFolder.getPath() + " is not a folder");
    }

    VirtualFile[] files = bundleFolder.getChildren();
    for (VirtualFile file : files)
    {
      VirtualFile dir = FileUtil.getFolder(file);

      if (dir != null)
      {
        VirtualFile manifest = dir.findFileByRelativePath("META-INF/MANIFEST.MF");
        if (manifest != null && !sourceFinder.containsOnlySources(file))
        {
          List<VirtualFile> sources = sourceFinder.getSourceForLibraryClasses(file);
          _modifiableLibraryModels.add(createLibrary(instanceName, dir, sources));
        }
      }
    }
  }

  private Library.ModifiableModel createLibrary(
      @NotNull String instanceName, @NotNull VirtualFile classes, List<VirtualFile> sources)
  {
    Library library =
        _modifiableApplicationLibraryTableModel.createLibrary(getLibraryPrefix(instanceName) + classes
            .getNameWithoutExtension());
    Library.ModifiableModel modifiableLibraryModel = library.getModifiableModel();
    modifiableLibraryModel.addRoot(classes, OrderRootType.CLASSES);
    for (VirtualFile source : sources)
    {
      VirtualFile sourceDir = FileUtil.getFolder(source);
      if (sourceDir != null)
      {
        modifiableLibraryModel.addRoot(sourceDir, OrderRootType.SOURCES);
      }
      else
      {
        throw new RuntimeException(source + " is neither a directory nor a JAR.");
      }
    }
    return modifiableLibraryModel;
  }

  private String getLibraryPrefix(String instanceName)
  {
    return getFrameworkInstanceLibraryPrefix() + instanceName + FRAMEWORK_INSTANCE_LIBRARY_NAME_PREFIX_SEPARATOR;
  }

  private String getFrameworkInstanceLibraryPrefix()
  {
    return FRAMEWORK_INSTANCE_LIBRARY_NAME_OSMORC_PREFIX + FRAMEWORK_INSTANCE_LIBRARY_NAME_PREFIX_SEPARATOR;
  }

  public List<Library> getLibraries(@NotNull String instanceName)
  {
    List<Library> result = new ArrayList<Library>();
    String libraryPrefix = getLibraryPrefix(instanceName);
    LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable();
    for (Library library : libraryTable.getLibraries())
    {
      if (library.getName().startsWith(libraryPrefix))
      {
        result.add(library);
      }
    }
    return result;
  }

  public boolean isFrameworkInstanceLibrary(@NotNull Library library)
  {
    return library.getName() != null && library.getName().startsWith(getFrameworkInstanceLibraryPrefix()) &&
        LibraryTablesRegistrar.APPLICATION_LEVEL.equals(library.getTable().getTableLevel());
  }

  public boolean isFrameworkInstanceLibrary(@NotNull LibraryOrderEntry libraryOrderEntry)
  {
    return libraryOrderEntry.getLibraryName() != null &&
        libraryOrderEntry.getLibraryName().startsWith(getFrameworkInstanceLibraryPrefix()) &&
        LibraryTablesRegistrar.APPLICATION_LEVEL.equals(libraryOrderEntry.getLibraryLevel());
  }

  public void deleteLibraries(String instanceName)
  {
    List<Library> libraries = getLibraries(instanceName);
    for (Library library : libraries)
    {
      _modifiableApplicationLibraryTableModel.removeLibrary(library);
    }
  }

  public String getShortLibraryName(String instanceName, Library library)
  {
    String libraryPrefix = getLibraryPrefix(instanceName);
    String libraryName = library.getName();
    if (libraryName.startsWith(libraryPrefix))
    {
      return libraryName.substring(libraryPrefix.length());
    }
    return libraryName;
  }

  private static final String FRAMEWORK_INSTANCE_LIBRARY_NAME_PREFIX_SEPARATOR = "/";
  private static final String FRAMEWORK_INSTANCE_LIBRARY_NAME_OSMORC_PREFIX = "Osmorc";
  private boolean _isChangingLibraries;
  private LibraryTable.ModifiableModel _modifiableApplicationLibraryTableModel;
  private List<Library.ModifiableModel> _modifiableLibraryModels;
}
