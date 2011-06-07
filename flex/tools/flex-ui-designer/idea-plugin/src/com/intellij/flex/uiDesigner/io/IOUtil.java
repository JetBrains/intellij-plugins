package com.intellij.flex.uiDesigner.io;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public final class IOUtil {
  private static final Logger LOG = Logger.getInstance(IOUtil.class.getName());

  public static void saveStream(InputStream input, File output) throws IOException {
    //noinspection ResultOfMethodCallIgnored
    output.getParentFile().mkdirs();
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

  public static void writeInt(final int v, final byte[] bytes, int offset) {
    bytes[offset++] = (byte)((v >>> 24) & 0xFF);
    bytes[offset++] = (byte)((v >>> 16) & 0xFF);
    bytes[offset++] = (byte)((v >>> 8) & 0xFF);
    bytes[offset] = (byte)(v & 0xFF);
  }

  public static void writeShort(final int v, final byte[] bytes, int offset) {
    bytes[offset++] = (byte)((v >>> 8) & 0xFF);
    bytes[offset] = (byte)(v & 0xFF);
  }

  public static void close(Closable closable) {
    if (closable != null) {
      try {
        closable.close();
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
  }

  public static void close(Socket socket) {
    if (socket != null) {
      try {
        socket.close();
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
  }
}