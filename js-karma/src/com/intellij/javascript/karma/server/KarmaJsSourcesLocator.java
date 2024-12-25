// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.javascript.karma.server;

import com.intellij.execution.ExecutionException;
import com.intellij.idea.AppMode;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.lang.javascript.psi.util.JSPluginPathManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class KarmaJsSourcesLocator {
  private static final KarmaJsSourcesLocator INSTANCE = new KarmaJsSourcesLocator();
  private static final String KARMA_INTELLIJ_NAME = "karma-intellij";
  private static final String JS_REPORTER_NAME = "js_reporter";

  private final Path myKarmaIntellijPackageDir;

  private KarmaJsSourcesLocator() {
    myKarmaIntellijPackageDir = findKarmaIntellijPackageDir();
  }

  public static @NotNull KarmaJsSourcesLocator getInstance() {
    return INSTANCE;
  }

  /**
   * @return Bundled 'karma-intellij' directory
   */
  private static @NotNull Path findKarmaIntellijPackageDir() {
    Path jsReporterDir = getBundledJsReporterDir();
    if (Files.isDirectory(jsReporterDir)) {
      return jsReporterDir.resolve(KARMA_INTELLIJ_NAME);
    }
    throw new RuntimeException("Cannot find bundled karma-intellij in " + jsReporterDir);
  }

  private static @NotNull Path getBundledJsReporterDir() {
    String relativePathToResources;
    if (AppMode.isDevServer()) {
      relativePathToResources = "karma";
    }
    else {
      relativePathToResources = "js-karma/resources";
    }
    try {
      return JSPluginPathManager.getPluginResource(
        KarmaJsSourcesLocator.class,
        JS_REPORTER_NAME,
        relativePathToResources
      );
    }
    catch (IOException e) {
      throw new RuntimeException("Cannot find bundled karma-intellij in " + relativePathToResources);
    }
  }

  private @NotNull Path getAppFile(@NotNull String baseName) throws IOException {
    Path file = myKarmaIntellijPackageDir.resolve("lib").resolve(baseName);
    if (!Files.isRegularFile(file)) {
      throw new IOException("Cannot locate " + file);
    }
    return file;
  }

  public @NotNull Path getIntellijConfigFile() throws IOException {
    return getAppFile("intellij.conf.js");
  }

  public @NotNull Path getClientAppFile() throws ExecutionException {
    try {
      return getAppFile("intellijRunner.js");
    }
    catch (IOException e) {
      throw new ExecutionException(KarmaBundle.message("execution.cannot_find_intellijRunner.dialog.message"), e);
    }
  }

  public @NotNull Path getKarmaIntellijPackageDir() {
    return myKarmaIntellijPackageDir;
  }
}
