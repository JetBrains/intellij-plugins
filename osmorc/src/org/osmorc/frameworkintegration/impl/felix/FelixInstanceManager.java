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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.impl.AbstractFrameworkInstanceManager;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkLibraryCollector;
import org.osmorc.run.ui.SelectedBundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class FelixInstanceManager extends AbstractFrameworkInstanceManager {
  private static final Logger LOG = Logger.getInstance(FelixInstanceManager.class);

  private static final String[] BUNDLE_DIRS = {"bin", "bundle"};
  private static final Pattern SYSTEM_BUNDLE = Pattern.compile("felix.*\\.jar");
  private static final Pattern SHELL_BUNDLES = Pattern.compile(".*\\.gogo\\.(command|runtime|shell).*\\.jar");

  @Override
  public void collectLibraries(@NotNull FrameworkInstanceDefinition frameworkInstanceDefinition,
                               @NotNull final FrameworkLibraryCollector collector) {
    final VirtualFile installFolder = LocalFileSystem.getInstance().findFileByPath(frameworkInstanceDefinition.getBaseFolder());
    if (installFolder == null || !installFolder.isDirectory()) {
      LOG.warn(frameworkInstanceDefinition.getBaseFolder() + " is not a folder");
      return;
    }

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        installFolder.refresh(false, true);

        List<VirtualFile> directoriesToAdd = new ArrayList<VirtualFile>();
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

        collector.collectFrameworkLibraries(new FelixSourceFinder(), directoriesToAdd);
      }
    });
  }

  @NotNull
  @Override
  public Collection<SelectedBundle> getFrameworkBundles(@NotNull FrameworkInstanceDefinition instance, @NotNull FrameworkBundleType type) {
    return collectBundles(instance, type, BUNDLE_DIRS, SYSTEM_BUNDLE, FelixRunner.MAIN_CLASS, 1, SHELL_BUNDLES, null, 3);
  }
}
