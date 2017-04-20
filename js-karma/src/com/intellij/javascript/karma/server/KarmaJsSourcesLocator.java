package com.intellij.javascript.karma.server;

import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

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
    if (true) {
      return new File("/home/segrey/work/idea-master/contrib/js-karma/src/js_reporter/karma-intellij");
    }
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
    String jarPath = PathUtil.getJarPathForClass(KarmaServer.class);
    if (!jarPath.endsWith(".jar")) {
      return new File(jarPath, "js_reporter");
    }
    File jarFile = new File(jarPath);
    if (!jarFile.isFile()) {
      throw new RuntimeException("jar file cannot be null");
    }
    File pluginBaseDir = jarFile.getParentFile().getParentFile();
    return new File(pluginBaseDir, "js_reporter");
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
