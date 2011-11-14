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

package org.osmorc.frameworkintegration.impl.concierge;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.AbstractFrameworkInstanceManager;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkLibraryCollector;
import org.osmorc.frameworkintegration.LibraryHandler;
import org.osmorc.i18n.OsmorcBundle;

import java.util.ArrayList;

/**
 * @author <a href="mailto:al@chilibi.org">Alain Greppin</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ConciergeFrameworkInstanceManager extends AbstractFrameworkInstanceManager {
  private static final Logger LOG = Logger.getInstance("#org.osmorc.frameworkintegration.impl.concierge.ConciergeFrameworkInstanceManager");

  private final LibraryHandler myLibraryHandler;
  private final Application myApplication;


  public ConciergeFrameworkInstanceManager(LibraryHandler libraryHandler, LocalFileSystem fileSystem,
                                           Application application) {
    super(fileSystem);
    myLibraryHandler = libraryHandler;
    myApplication = application;
  }

  @Override
  public void collectLibraries(@NotNull final FrameworkInstanceDefinition frameworkInstanceDefinition,
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

    VirtualFile bundlesFolder = installFolder.findChild("bundles");
    if (bundlesFolder != null) {
      if (!bundlesFolder.isDirectory()) {
        LOG.warn(bundlesFolder.getPath() + " is not a folder");
      }
      else {
        directoriesToAdd.add(bundlesFolder);
      }
    }

    myApplication.runWriteAction(new Runnable() {
      public void run() {
        installFolder.refresh(false, true);

        ConciergeSourceFinder sourceFinder = new ConciergeSourceFinder(installFolder);
          collector.collectFrameworkLibraries(sourceFinder, directoriesToAdd);
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
      return OsmorcBundle
        .getTranslation("concierge.folder.does.not.exist", frameworkInstanceDefinition.getBaseFolder());
    }

    VirtualFile bundlesFolder = installFolder.findChild("bundles");
    if (bundlesFolder == null || !bundlesFolder.isDirectory()) {
      return OsmorcBundle.getTranslation("concierge.folder.bundles.missing", installFolder.getPath());
    }

    return null;
  }
}
