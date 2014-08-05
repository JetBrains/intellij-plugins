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
package org.osmorc.make;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.osmorc.build.BndWrapper;
import org.jetbrains.jps.osmorc.build.CachingBundleInfoProvider;
import org.jetbrains.jps.osmorc.build.OsgiBuildException;
import org.jetbrains.jps.osmorc.build.Reporter;

import java.io.File;
import java.util.List;

/**
 * This is a compiler step that builds up a bundle. Depending on user settings the compiler either uses a user-edited
 * manifest or builds up a manifest using bnd.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class BundleCompiler implements Reporter {
  private static final Logger LOG = Logger.getInstance(BundleCompiler.class);

  /**
   * Condition which matches order entries that are not representing a framework library.
   */
  public static final Condition<OrderEntry> NOT_FRAMEWORK_LIBRARY_CONDITION = new Condition<OrderEntry>() {
    @Override
    public boolean value(OrderEntry entry) {
      if ((entry instanceof LibraryOrderEntry)) {
        LibraryOrderEntry libEntry = (LibraryOrderEntry)entry;
        if (LibraryTablesRegistrar.PROJECT_LEVEL.equals(libEntry.getLibraryLevel())) {
          String libraryName = libEntry.getLibraryName();
          if (libraryName != null && libraryName.startsWith("Osmorc:")) {
            return false;
          }
        }
      }
      return true;
    }
  };

  /**
   * Bundlifies all libraries that belong to the given module and that are not bundles.
   * The bundles are cached, so if the source library does not change, it will not be bundlified again.
   * Returns a string array containing paths of the bundlified libraries.
   */
  @NotNull
  public List<String> bundlifyLibraries(@NotNull Module module, @NotNull ProgressIndicator indicator) throws OsgiBuildException {
    indicator.setText("Bundling non-OSGi libraries for module '" + module.getName() + "'");

    File outputDir = BndWrapper.getOutputDir(getModuleOutputDir(module));

    List<String> paths = OrderEnumerator.orderEntries(module)
      .withoutSdk()
      .withoutModuleSourceEntries()
      .withoutDepModules()
      .productionOnly()
      .runtimeOnly()
      .recursively()
      .exportedOnly()
      .satisfying(NOT_FRAMEWORK_LIBRARY_CONDITION)
      .classes()
      .getPathsList().getPathList();

    BndWrapper wrapper = new BndWrapper(this);
    List<String> result = ContainerUtil.newArrayList();
    for (String path : paths) {
      if (CachingBundleInfoProvider.canBeBundlified(path)) {
        indicator.setText2(path);
        try {
          File bundledDependency = wrapper.wrapLibrary(new File(path), outputDir);
          if (bundledDependency != null) {
            result.add(bundledDependency.getPath());
          }
        }
        catch (OsgiBuildException e) {
          warning(e.getMessage(), e.getCause(), e.getSourcePath());
        }
      }
      else if (CachingBundleInfoProvider.isBundle(path)) {
        result.add(path);
      }
    }
    return result;
  }

  private static File getModuleOutputDir(@NotNull Module module) throws OsgiBuildException {
    CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
    if (extension != null) {
      String url = extension.getCompilerOutputUrl();
      if (url != null) {
        return new File(VfsUtilCore.urlToPath(url));
      }
    }
    throw new OsgiBuildException("Unable to determine the compiler output path for the module '" + module.getName() + "'");
  }

  @Override
  public void progress(@NotNull String message) {
    LOG.debug(message);
  }

  @Override
  public void warning(@NotNull String message, @Nullable Throwable t, @Nullable String sourcePath) {
    LOG.warn(message, t);
  }

  @Override
  public void error(@NotNull String message, @Nullable Throwable t, @Nullable String sourcePath) {
    LOG.warn(message, t);
  }
}
