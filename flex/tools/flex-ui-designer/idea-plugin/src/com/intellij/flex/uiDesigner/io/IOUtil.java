package com.intellij.flex.uiDesigner.io;

import com.intellij.openapi.util.io.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class IOUtil {
  public static void saveStream(InputStream input, File output) throws IOException {
    FileOutputStream outputStream = new FileOutputStream(output);
    try {
      FileUtil.copy(input, outputStream);
    }
    finally {
      outputStream.close();
    }
  }
  
  public static int sizeOf(int counter) {
    return counter < 0x80 ? 1 : 2;
  }

  public static byte[] getBytes(ByteProvider... byteProviders) {
    int size = 0;
    for (ByteProvider byteProvider : byteProviders) {
      size += byteProvider.size();
    }

    byte[] bytes = new byte[size];
    int offset = 0;
    for (ByteProvider byteProvider : byteProviders) {
      offset = byteProvider.writeTo(bytes, offset);
    }

    return bytes;
  }
}