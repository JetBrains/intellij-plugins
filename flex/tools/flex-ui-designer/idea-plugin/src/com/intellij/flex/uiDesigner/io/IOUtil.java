package com.intellij.flex.uiDesigner.io;

import com.intellij.openapi.util.io.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class IOUtil {
  public static void saveStream(InputStream input, File output) throws IOException {
    FileOutputStream os = new FileOutputStream(output);
    try {
      FileUtil.copy(input, os);
    }
    finally {
      os.close();
    }
  }
  
  public static int sizeOf(int counter) {
    return counter < 0x80 ? 1 : 2;
  }
}
