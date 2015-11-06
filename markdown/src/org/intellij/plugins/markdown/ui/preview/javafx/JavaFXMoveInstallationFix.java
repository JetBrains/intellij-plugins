package org.intellij.plugins.markdown.ui.preview.javafx;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.BuildNumber;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

enum JavaFXMoveInstallationFix {
  INSTANCE;

  private final Logger LOG = Logger.getInstance(JavaFXMoveInstallationFix.class);

  public static final List<String> INSTALLATION_LIST = Arrays.asList(
    "javafx-sdk-overlay.zip",
    "bin/javafxpackager",
    "bin/javapackager",
    "jre/lib/javafx.properties",
    "jre/lib/jfxswt.jar",
    "jre/lib/libdecora_sse.dylib",
    "jre/lib/libglass.dylib",
    "jre/lib/libjavafx_font.dylib",
    "jre/lib/libjavafx_iio.dylib",
    "jre/lib/libjfxwebkit.dylib",
    "jre/lib/libprism_common.dylib",
    "jre/lib/libprism_es2.dylib",
    "jre/lib/libprism_sw.dylib",
    "jre/lib/ext/jfxrt.jar",
    "lib/ant-javafx.jar",
    "lib/javafx-mx.jar",
    "lib/packager.jar",
    "man/man1/javafxpackager.1",
    "man/man1/javapackager.1",
    "man/man1",
    "man"
  );

  public boolean runFixIfCan() {
    final BuildNumber build = ApplicationInfo.getInstance().getBuild();
    if (build.getBaselineVersion() != 143 || build.getBuildNumber() != 381) {
      return false;
    }

    final String oldInstallationPath = getOldInstallationPath();

    final String testFile = oldInstallationPath + "/" + INSTALLATION_LIST.get(0);
    if (!new File(testFile).exists()) {
      return false;
    }

    //noinspection SynchronizeOnThis
    synchronized (this) {
      if (!new File(testFile).exists()) {
        return false;
      }

      LOG.warn("Found old OpenJFX installation. Moving...");
      try {
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new ThrowableComputable<Boolean, IOException>() {
          @Override
          public Boolean compute() throws IOException {
            return doMove(oldInstallationPath);
          }
        }, "Moving OpenJFX Bundle to the Right Place...", false, null);
      }
      catch (IOException e) {
        LOG.error(e);
        return false;
      }


      LOG.warn("Move completed.");
      return true;
    }
  }

  private boolean doMove(String oldInstallationPath) throws IOException {
    final String newInstallationPath = JavaFXInstallator.INSTANCE.getInstallationPath();
    if (new File(newInstallationPath).mkdir()) {
      LOG.info("Created new installation dir " + newInstallationPath);
    }
    if (!new File(newInstallationPath).exists()) {
      LOG.warn("Could not find/create new installation path: " + newInstallationPath);
      return false;
    }

    int i = 0;
    for (String s : ContainerUtil.reverse(INSTALLATION_LIST)) {
      i++;
      ProgressManager.getInstance().getProgressIndicator().setFraction(1. * i / INSTALLATION_LIST.size());

      final File oldFile = new File(oldInstallationPath + "/" + s);
      if (!oldFile.exists()) {
        LOG.warn("File " + s + " did not exist, strange.");
      }
      final File newFile = new File(newInstallationPath + "/" + s);

      if (oldFile.isFile()) {
        //noinspection ResultOfMethodCallIgnored
        newFile.getParentFile().mkdirs();
        FileUtil.copy(oldFile, newFile);
        //noinspection SSBasedInspection
        oldFile.deleteOnExit();
      }
      else if (oldFile.isDirectory()) {
        //noinspection SSBasedInspection
        oldFile.deleteOnExit();
      }
      else {
        LOG.warn("Strange entry type: " + oldFile);
      }
    }

    return true;
  }

  private static String getOldInstallationPath() {
    return SystemProperties.getJavaHome() + "/..";
  }

}
