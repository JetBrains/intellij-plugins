package com.intellij.javascript.karma.server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.platform.loader.PlatformLoader;
import org.jetbrains.platform.loader.repository.RuntimeModuleId;

import java.io.File;
import java.io.IOException;

/**
 * @author Sergey Simonchik
 */
public class KarmaJsSourcesLocator {

  private final File myKarmaPackageDir;
  private final File myKarmaIntellijPackageDir;

  public KarmaJsSourcesLocator(@NotNull File karmaPackageDir) throws IOException {
    myKarmaPackageDir = karmaPackageDir;
    myKarmaIntellijPackageDir = findKarmaIntellijPackageDir(karmaPackageDir);
  }

  /**
   * 'karma-intellij' directory contains installed node module 'karma-intellij'
   * Source code: https://github.com/karma-runner/karma-intellij
   *
   * @param karmaPackageDir 'karma' package directory
   * @return 'karma-intellij' directory
   */
  private static File findKarmaIntellijPackageDir(@NotNull File karmaPackageDir) {
    File dir = new File(karmaPackageDir.getParentFile(), "karma-intellij");
    if (dir.isDirectory()) {
      return dir;
    }
    File jsReporterDir = getBundledJsReporterDir();
    if (!jsReporterDir.isDirectory()) {
      throw new RuntimeException("Can't find bundled version of karma-intellij node module!");
    }
    return new File(jsReporterDir, "karma-intellij");
  }

  private static File getBundledJsReporterDir() {
    return new File(PlatformLoader.getInstance().getRepository().getModuleRootPath(RuntimeModuleId.moduleResource("js-karma", "js_reporter")));
  }

  private File getAppFile(@NotNull String baseName) throws IOException {
    File file = new File(myKarmaIntellijPackageDir, "lib" + File.separatorChar + baseName);
    if (!file.isFile()) {
      throw new IOException("Can't find " + file);
    }
    return file;
  }

  @NotNull
  public File getServerAppFile() throws IOException {
    return getAppFile("intellijServer.js");
  }

  @NotNull
  public File getClientAppFile() throws IOException {
    return getAppFile("intellijRunner.js");
  }

  @NotNull
  public File getKarmaPackageDir() {
    return myKarmaPackageDir;
  }

}
