package com.intellij.flex.uiDesigner.io;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

public final class IOUtil {
  private static final Logger LOG = Logger.getInstance(IOUtil.class.getName());

  public static void saveStream(URL source, File target) throws IOException {
    final URLConnection sourceConnection = source.openConnection();
    long sourceLastModified = sourceConnection.getLastModified();
    if (target.isFile() && target.lastModified() == sourceLastModified) {
      // target file is already up to date
      return;
    }

    //noinspection ResultOfMethodCallIgnored
    target.getParentFile().mkdirs();
    FileOutputStream outputStream = new FileOutputStream(target);
    try {
      FileUtil.copy(sourceConnection.getInputStream(), outputStream);
    }
    finally {
      outputStream.close();
    }
    //noinspection ResultOfMethodCallIgnored
    target.setLastModified(sourceLastModified);
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
    bytes[offset++] = (byte)((v >>> 24) & 0xff);
    bytes[offset++] = (byte)((v >>> 16) & 0xff);
    bytes[offset++] = (byte)((v >>> 8) & 0xff);
    bytes[offset] = (byte)(v & 0xff);
  }

  public static void writeShort(final int v, final byte[] bytes, int offset) {
    bytes[offset++] = (byte)((v >>> 8) & 0xFF);
    bytes[offset] = (byte)(v & 0xFF);
  }

  public static void writeInt(final int v, final OutputStream out) throws IOException {
    out.write((v >>> 24) & 0xff);
    out.write((v >>> 16) & 0xff);
    out.write((v >>> 8) & 0xff);
    out.write(v & 0xff);
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