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
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.osgi.jps.build.BndWrapper;
import org.jetbrains.osgi.jps.build.OsgiBuildException;
import org.jetbrains.osgi.jps.build.Reporter;
import org.jetbrains.osgi.jps.model.LibraryBundlificationRule;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.settings.ApplicationSettings;

import java.io.File;
import java.util.List;

/**
 * This is a compiler step that builds up a bundle. Depending on user settings the compiler either uses a user-edited
 * manifest or builds up a manifest using bnd.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public class BundleCompiler implements Reporter {
  private static final Logger LOG = Logger.getInstance(BundleCompiler.class);

  private final ProgressIndicator myIndicator;

  public BundleCompiler(@NotNull ProgressIndicator indicator) {
    myIndicator = indicator;
  }

  /**
   * Bundlifies all libraries that belong to the given module and that are not bundles.
   * The bundles are cached, so if the source library does not change, it will not be bundlified again.
   * Returns a string array containing paths of the bundlified libraries.
   */
  public @NotNull List<String> bundlifyLibraries(@NotNull Module module) throws OsgiBuildException {
    myIndicator.setText(OsmorcBundle.message("compiler.progress.bundling.libraries", module.getName()));

    File outputDir = getOutputDir(module);
    List<LibraryBundlificationRule> libRules = ApplicationSettings.getInstance().getLibraryBundlificationRules();

    List<String> paths = OrderEnumerator.orderEntries(module)
      .withoutSdk()
      .withoutModuleSourceEntries()
      .withoutDepModules()
      .productionOnly()
      .runtimeOnly()
      .recursively()
      .exportedOnly()
      .classes()
      .getPathsList().getPathList();

    List<File> files = ContainerUtil.map(paths, path -> new File(path));

    return new BndWrapper(this).bundlifyLibraries(files, outputDir, libRules);
  }

  private static File getOutputDir(Module module) throws OsgiBuildException {
    CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
    if (extension != null) {
      String url = extension.getCompilerOutputUrl();
      if (url != null) {
        File moduleOutputDir = new File(VfsUtilCore.urlToPath(url));
        File outputDir = new File(moduleOutputDir.getParent(), "bundles");
        if (!outputDir.exists() && !outputDir.mkdirs()) {
          throw new OsgiBuildException(OsmorcBundle.message("compiler.error.cannot.create.output", outputDir));
        }
        return outputDir;
      }
    }
    throw new OsgiBuildException(OsmorcBundle.message("compiler.error.cannot.locate.output", module.getName()));
  }

  @Override
  public void progress(@NotNull String message) {
    LOG.debug(message);
    myIndicator.setText2(message);
  }

  @Override
  public void warning(@NotNull String message, @Nullable Throwable t, @Nullable String sourcePath, int lineNum) {
    LOG.warn(message, t);
  }

  @Override
  public void error(@NotNull String message, @Nullable Throwable t, @Nullable String sourcePath, int lineNum) {
    LOG.warn(message, t);
  }

  @Override
  public boolean isDebugEnabled() {
    return LOG.isDebugEnabled();
  }

  @Override
  public void debug(@NotNull String message) {
    LOG.debug(message);
  }

  @Override
  public String setReportSource(String source) {
    return null;
  }
}
