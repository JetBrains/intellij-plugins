package com.intellij.flex.compiler;

import flex2.tools.oem.PathResolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SdkFilesResolver implements PathResolver {

  private static final String FLEXLIB_PATH;

  static {
    // See #flex2.tools.Compiler.processConfiguration(): System.getProperty("application.home") + File.separator + "frameworks"
    final String appHome = System.getProperty("application.home");
    FLEXLIB_PATH = appHome == null ? "." : appHome + File.separator + "frameworks";
  }

  // slashes are messed up, but that's how it works.
  // see DefaultsConfigurator.java: "${flexlib}/${configname}-config.xml"
  private static final String FLEX_CONFIG_PATH = FLEXLIB_PATH + "/flex-config.xml";
  private static final String AIR_CONFIG_PATH = FLEXLIB_PATH + "/air-config.xml";

  private static File fakeConfigFile;

  public static SdkFilesResolver INSTANCE = new SdkFilesResolver();

  private SdkFilesResolver() {
  }

  public File resolve(final String path) {
    /*
    if (FLEX_CONFIG_PATH.equals(path) || AIR_CONFIG_PATH.equals(path)) {
      // We need not to show real flex-config.xml file, otherwise stupid compiler reads options from there and overrides actual options

      //final File configFile = new File(path);
      //if (configFile.exists()) {
      //  return configFile;
      //}

      synchronized (SdkFilesResolver.class) {
        if (fakeConfigFile == null) {
          fakeConfigFile = createFakeConfigFile();
        }
        return fakeConfigFile;
      }
    }
    */

    final File file = new File(FLEXLIB_PATH, path);
    if (file.exists()) {
      return file;
    }

    return null;
  }

  private static File createFakeConfigFile() {
    try {
      final File file = File.createTempFile("fake-config", ".xml");
      file.deleteOnExit();
      final FileOutputStream fileOutputStream = new FileOutputStream(file);
      fileOutputStream.write("<flex-config/>".getBytes());
      fileOutputStream.flush();
      fileOutputStream.close();
      return file;
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void removeTempFile() {
    if (fakeConfigFile != null) {
      fakeConfigFile.delete();
    }
  }
}
