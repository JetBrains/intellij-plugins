// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.intellij.javascript.karma.server;

import com.intellij.execution.ExecutionException;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public final class KarmaJsSourcesLocator {
  private static final KarmaJsSourcesLocator INSTANCE = new KarmaJsSourcesLocator();
  private static final String KARMA_INTELLIJ_NAME = "karma-intellij";
  private static final String JS_REPORTER_NAME = "js_reporter";

  private final File myKarmaIntellijPackageDir;

  private KarmaJsSourcesLocator() {
    myKarmaIntellijPackageDir = findKarmaIntellijPackageDir();
  }

  @NotNull
  public static KarmaJsSourcesLocator getInstance() {
    return INSTANCE;
  }

  /**
   * @return Bundled 'karma-intellij' directory
   */
  @NotNull
  private static File findKarmaIntellijPackageDir() {
    File jsReporterDir = getBundledJsReporterDir();
    if (jsReporterDir.isDirectory()) {
      return new File(jsReporterDir, KARMA_INTELLIJ_NAME);
    }
    throw new RuntimeException("Cannot find bundled karma-intellij in " + jsReporterDir.getAbsolutePath());
  }

  private static File getBundledJsReporterDir() {
    String jarPath = PathUtil.getJarPathForClass(KarmaJsSourcesLocator.class);
    if (jarPath.endsWith(".jar")) {
      File jarFile = new File(jarPath);
      if (!jarFile.isFile()) {
        throw new RuntimeException("jar file cannot be null");
      }
      File pluginBaseDir = jarFile.getParentFile().getParentFile();
      return new File(pluginBaseDir, JS_REPORTER_NAME);
    }
    if (ApplicationManager.getApplication().isInternal()) {
      String srcDir = jarPath.replace('\\', '/').replace("/out/classes/production/intellij.karma", "/contrib/js-karma/resources");
      if (new File(srcDir).isDirectory()) {
        jarPath = srcDir;
      }
    }
    return new File(jarPath, JS_REPORTER_NAME);
  }

  private File getAppFile(@NotNull String baseName) throws IOException {
    File file = new File(myKarmaIntellijPackageDir, "lib" + File.separatorChar + baseName);
    if (!file.isFile()) {
      throw new IOException("Cannot locate " + file.getAbsolutePath());
    }
    return file;
  }

  @NotNull
  public File getIntellijConfigFile() throws IOException {
    return getAppFile("intellij.conf.js");
  }

  @NotNull
  public File getClientAppFile() throws ExecutionException {
    try {
      return getAppFile("intellijRunner.js");
    }
    catch (IOException e) {
      throw new ExecutionException(KarmaBundle.message("execution.cannot_find_intellijRunner.dialog.message"), e);
    }
  }

  @NotNull
  public File getKarmaIntellijPackageDir() {
    return myKarmaIntellijPackageDir;
  }
}
