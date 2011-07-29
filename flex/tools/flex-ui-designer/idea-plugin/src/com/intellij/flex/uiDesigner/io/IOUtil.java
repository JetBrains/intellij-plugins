package com.intellij.flex.uiDesigner.io;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

public final class IOUtil {
  private static final Logger LOG = Logger.getInstance(IOUtil.class.getName());

  public static void saveStream(File source, File target) throws IOException {
    saveStream(source.toURI().toURL(), target);
  }

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

  public static void writeAmfIntOrDouble(final PrimitiveAmfOutputStream out, final CharSequence value,
                                         final boolean isNegative, final boolean isInt) {
    if (isInt || StringUtil.indexOf(value, '.') == -1) {
      out.writeAmfInt(parseInt(value, 0, isNegative, 10));
    }
    else {
      final double v = Double.parseDouble(value.toString());
      out.writeAmfDouble(isNegative ? -v : v);
    }
  }

  /**
   * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
   * Copyright (C) 2006 - Javolution (http://javolution.org/)
   * All rights reserved.
   *
   * Permission to use, copy, modify, and distribute this software is
   * freely granted, provided that this notice is preserved.
   */
  public static int parseInt(final CharSequence value, final int start, final boolean isNegative, final int radix) {
    final int end = value.length();
    int result = 0; // Accumulates negatively (avoid MIN_VALUE overflow).
    int i = start;
    for (; i < end; i++) {
      char c = value.charAt(i);
      int digit = (c <= '9') ? c - '0'
                             : ((c <= 'Z') && (c >= 'A')) ? c - 'A' + 10
                                                          : ((c <= 'z') && (c >= 'a')) ? c - 'a' + 10 : -1;
      if ((digit >= 0) && (digit < radix)) {
        int newResult = result * radix - digit;
        if (newResult > result) {
          throw new NumberFormatException("Overflow parsing " + value.subSequence(start, end));
        }
        result = newResult;
      }
      else {
        break;
      }
    }
    // Requires one valid digit character and checks for opposite overflow.
    if ((result == 0) && ((end == 0) || (value.charAt(i - 1) != '0'))) {
      throw new NumberFormatException("Invalid integer representation for " + value.subSequence(start, end));
    }
    if ((result == Integer.MIN_VALUE) && !isNegative) {
      throw new NumberFormatException("Overflow parsing " + value.subSequence(start, end));
    }
    return isNegative ? result : -result;
  }

  public static long parseLong(final CharSequence value, final int start, final boolean isNegative, final int radix) {
    final int end = value.length();
    long result = 0; // Accumulates negatively (avoid MIN_VALUE overflow).
    int i = start;
    for (; i < end; i++) {
      char c = value.charAt(i);
      int digit = (c <= '9') ? c - '0'
                             : ((c <= 'Z') && (c >= 'A')) ? c - 'A' + 10
                                                          : ((c <= 'z') && (c >= 'a')) ? c - 'a' + 10 : -1;
      if ((digit >= 0) && (digit < radix)) {
        long newResult = result * radix - digit;
        if (newResult > result) {
          throw new NumberFormatException("Overflow parsing " + value.subSequence(start, end));
        }
        result = newResult;
      }
      else {
        break;
      }
    }
    // Requires one valid digit character and checks for opposite overflow.
    if ((result == 0) && ((end == 0) || (value.charAt(i - 1) != '0'))) {
      throw new NumberFormatException("Invalid integer representation for " + value.subSequence(start, end));
    }
    if ((result == Long.MIN_VALUE) && !isNegative) {
      throw new NumberFormatException("Overflow parsing " + value.subSequence(start, end));
    }

    return isNegative ? result : -result;
  }
}