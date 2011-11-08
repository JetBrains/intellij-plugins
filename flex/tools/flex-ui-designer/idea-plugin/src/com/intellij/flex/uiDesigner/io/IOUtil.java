package com.intellij.flex.uiDesigner.io;

import com.google.common.base.Charsets;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.text.CharArrayCharSequence;
import com.intellij.util.text.CharSequenceReader;
import com.intellij.util.xml.NanoXmlUtil;
import net.n3.nanoxml.IXMLBuilder;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public final class IOUtil {
  private static final Logger LOG = Logger.getInstance(IOUtil.class.getName());

  private IOUtil() {
  }

  public static byte[] getResourceBytes(String name) throws IOException {
    InputStream classDefinition = IOUtil.class.getClassLoader().getResourceAsStream(name);
    try {
      return FileUtil.loadBytes(classDefinition);
    }
    finally {
      classDefinition.close();
    }
  }

  public static void saveStream(URL source, File target) throws IOException {
    final URLConnection sourceConnection = source.openConnection();
    final long sourceLastModified = sourceConnection.getLastModified();
    if (target.lastModified() == sourceLastModified) {
      // target file is already up to date
      return;
    }

    //noinspection ResultOfMethodCallIgnored
    target.getParentFile().mkdirs();
    final FileOutputStream outputStream = new FileOutputStream(target);
    try {
      FileUtil.copy(sourceConnection.getInputStream(), outputStream);
    }
    finally {
      outputStream.close();
    }
    //noinspection ResultOfMethodCallIgnored
    target.setLastModified(sourceLastModified);
  }

  public static int uint29SizeOf(int counter) {
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
      if (digit >= 0 && digit < radix) {
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
    if (result == 0 && (end == 0 || (value.charAt(i - 1) != '0'))) {
      throw new NumberFormatException("Invalid integer representation for " + value.subSequence(start, end));
    }
    if ((result == Long.MIN_VALUE) && !isNegative) {
      throw new NumberFormatException("Overflow parsing " + value.subSequence(start, end));
    }

    return isNegative ? result : -result;
  }

  public static CharSequence getCharSequence(VirtualFile file) throws IOException {
    return (CharSequence)getCharSequenceOrReader(file, false);
  }

  private static Object getCharSequenceOrReader(VirtualFile file, boolean returnReader) throws IOException {
    if (file instanceof LightVirtualFile) {
      final CharSequence content = ((LightVirtualFile)file).getContent();
      return returnReader ? new CharSequenceReader(content) : content;
    }

    return getCharSequenceOrReader(file.getInputStream(), (int)file.getLength(), file.getCharset(), returnReader);
  }

  private static Object getCharSequenceOrReader(InputStream inputStream, int length, Charset charset, boolean returnReader) throws IOException {
    final InputStreamReader reader = new InputStreamReader(inputStream, charset);
    try {
      char[] chars = new char[length];
      int count = 0;
      while (count < chars.length) {
        int n = reader.read(chars, count, chars.length - count);
        if (n <= 0) {
          break;
        }
        count += n;
      }
      return returnReader ? new CharArrayReader(chars, 0, count) : new CharArrayCharSequence(chars, 0, count);
    }
    finally {
      reader.close();
    }
  }

  public static CharArrayReader getCharArrayReader(InputStream inputStream, int length) throws IOException {
    return (CharArrayReader)getCharSequenceOrReader(inputStream, length, Charsets.UTF_8, true);
  }

  public static void parseXml(InputStream inputStream, int length, IXMLBuilder builder) throws IOException {
    NanoXmlUtil.parse(getCharArrayReader(inputStream, length), builder);
  }

  public static void parseXml(VirtualFile file, IXMLBuilder builder) throws IOException {
    NanoXmlUtil.parse((Reader)getCharSequenceOrReader(file, true), builder);
  }
}