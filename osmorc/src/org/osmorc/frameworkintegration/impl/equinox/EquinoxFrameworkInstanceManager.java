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
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.AbstractFrameworkInstanceManager;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkLibraryCollector;

import java.text.MessageFormat;
import java.util.Collections;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class EquinoxFrameworkInstanceManager extends AbstractFrameworkInstanceManager {
  private static final Logger LOG = Logger.getInstance("#org.osmorc.frameworkintegration.impl.equinox.EquinoxFrameworkInstanceManager");
  private final Application myApplication;
  private final EquinoxSourceFinder myEquinoxSourceFinder;
  protected static final String FOLDER_DOES_NOT_EXIST =
    "The folder <strong>{0}</strong> does not exist or is not a folder. The folder should be an existing folder containing a <strong>plugins</strong> folder which contains the bundles of your Equinox installation. ";
  protected static final String NO_PLUGINS_FOLDER =
    "The folder  <strong>{0}</strong> does not contain a <strong>plugins</strong> folder or <strong>plugins</strong> is not a folder. The base folder needs to be " +
    "the folder of your Equinox installation which contains a <strong>plugins</strong> folder containing all bundle" +
    " JARs of your Equinox installation.";


  public EquinoxFrameworkInstanceManager(LocalFileSystem fileSystem, Application application) {
    super(fileSystem);
    this.myApplication = application;
    myEquinoxSourceFinder = new EquinoxSourceFinder();
  }

  @Override
  public void collectLibraries(@NotNull final FrameworkInstanceDefinition frameworkInstanceDefinition,
                               @NotNull final FrameworkLibraryCollector collector) {

    VirtualFile equinoxFolder = getLocalFileSystem().findFileByPath(frameworkInstanceDefinition.getBaseFolder());
    if (equinoxFolder == null) {
      // mhh someone deleted it?
      LOG.warn("Looks like the Equinox root folder is missing. Did someone delete it?");
      return;
    }
    final VirtualFile pluginsFolder = equinoxFolder.findChild("plugins");

    if (pluginsFolder != null && pluginsFolder.isDirectory()) {
      myApplication.runWriteAction(new Runnable() {
        public void run() {
          pluginsFolder.refresh(false, true);
          collector.collectFrameworkLibraries(myEquinoxSourceFinder, Collections.singletonList(pluginsFolder));
        }
      });
    }
    else {
      LOG.warn("Looks like there is no plugins folder below the Equinox root folder. Ignoring this.");
    }
  }

  @Nullable
  public String checkValidity(@NotNull FrameworkInstanceDefinition frameworkInstanceDefinition) {
    if (frameworkInstanceDefinition.getName() == null || frameworkInstanceDefinition.getName().trim().length() == 0) {
      return "A name for the framework instance needs to be given.";
    }

    if (frameworkInstanceDefinition.isDownloadedByPaxRunner()) {
      return checkDownloadedFolderStructure(frameworkInstanceDefinition);
    }

    VirtualFile equinoxFolder = getLocalFileSystem().findFileByPath(frameworkInstanceDefinition.getBaseFolder());

    if (equinoxFolder == null || !equinoxFolder.isDirectory()) {
      return MessageFormat.format(FOLDER_DOES_NOT_EXIST, frameworkInstanceDefinition.getBaseFolder());
    }
    VirtualFile pluginsFolder = equinoxFolder.findChild("plugins");
    if (pluginsFolder == null || !pluginsFolder.isDirectory()) {
      return MessageFormat.format(NO_PLUGINS_FOLDER, frameworkInstanceDefinition.getBaseFolder());
    }

    return null;
  }
}
