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

import com.intellij.openapi.application.Application;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.AbstractFrameworkInstanceManager;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkLibraryCollector;
import org.osmorc.frameworkintegration.LibraryHandler;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class FelixFrameworkInstanceManager extends AbstractFrameworkInstanceManager {
  private static final Logger LOG = Logger.getInstance("#org.osmorc.frameworkintegration.impl.felix.FelixFrameworkInstanceManager");
  private final LibraryHandler myLibraryHandler;
  private final Application myApplication;
  private final FelixSourceFinder myFelixsourcefinder;
  protected static final String FOLDER_DOES_NOT_EXIST =
    "The folder <strong>{0}</strong> does not exist or is not a folder. Please choose an existing Felix installation folder. It should contain the folders \"bin\" and \"bundle\"";
  protected static final String NO_BIN_FOLDER =
    "The base folder <strong>{0}</strong> does not contain a folder <strong>bin</strong>. The bin folder in a Felix installation contains the core bundle.";
  protected static final String NO_BUNDLE_FOLDER =
    "The base folder <strong>{0}</strong> does not contain a folder <strong>bundle</strong>. The bundle folder in a Felix installation contains additional bundles of the Felix framework.";

  public FelixFrameworkInstanceManager(LibraryHandler libraryHandler, LocalFileSystem fileSystem,
                                       Application application) {
    super(fileSystem);
    myLibraryHandler = libraryHandler;
    myApplication = application;
    myFelixsourcefinder = new FelixSourceFinder();
  }


  @Override
  public void collectLibraries(@NotNull FrameworkInstanceDefinition frameworkInstanceDefinition,
                               @NotNull final FrameworkLibraryCollector collector) {
    final VirtualFile installFolder = getLocalFileSystem().findFileByPath(frameworkInstanceDefinition.getBaseFolder());

    final ArrayList<VirtualFile> directoriesToAdd = new ArrayList<VirtualFile>();
    if (installFolder == null) {
      LOG.warn("The folder " + frameworkInstanceDefinition.getBaseFolder() + " does not exist.");
      return;
    }
    if (!installFolder.isDirectory()) {
      LOG.warn(frameworkInstanceDefinition.getBaseFolder() + " is not a folder");
      return;
    }

    directoriesToAdd.add(installFolder);

    VirtualFile binFolder = installFolder.findChild("bin");
    if (binFolder != null) {
      if (!binFolder.isDirectory()) {
        LOG.warn("The Felix bin-Folder is not a directory.");
      }
      else {
        directoriesToAdd.add(binFolder);
      }
    }

    VirtualFile bundleFolder = installFolder.findChild("bundle");
    if (bundleFolder != null) {
      if (!bundleFolder.isDirectory()) {
        LOG.warn("The Felix bundle-folder is not a directory");
      }
      else {
        directoriesToAdd.add(bundleFolder);
      }
    }


    myApplication.runWriteAction(new Runnable() {
      public void run() {
        installFolder.refresh(false, true);
        collector.collectFrameworkLibraries(myFelixsourcefinder, directoriesToAdd);
      }
    });
  }

  public String checkValidity(@NotNull FrameworkInstanceDefinition frameworkInstanceDefinition) {

    if (frameworkInstanceDefinition.getName() == null || frameworkInstanceDefinition.getName().trim().length() == 0) {
      return "A name for the framework instance needs to be given.";
    }

    if (frameworkInstanceDefinition.isDownloadedByPaxRunner()) {
      return checkDownloadedFolderStructure(frameworkInstanceDefinition);
    }

    VirtualFile installFolder = getLocalFileSystem().findFileByPath(frameworkInstanceDefinition.getBaseFolder());

    if (installFolder == null || !installFolder.isDirectory()) {
      return MessageFormat.format(FOLDER_DOES_NOT_EXIST, frameworkInstanceDefinition.getBaseFolder());
    }

    VirtualFile binFolder = installFolder.findChild("bin");
    if (binFolder == null || !binFolder.isDirectory()) {
      return MessageFormat.format(NO_BIN_FOLDER, frameworkInstanceDefinition.getBaseFolder());
    }

    VirtualFile bundleFolder = installFolder.findChild("bundle");
    if (bundleFolder == null || !bundleFolder.isDirectory()) {
      return MessageFormat.format(NO_BUNDLE_FOLDER, frameworkInstanceDefinition.getBaseFolder());
    }

    return null;
  }
}
