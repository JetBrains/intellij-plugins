package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.IOUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.awt.image.BufferedImage;
import java.io.*;

public class Reader extends DataInputStream {
  Reader(InputStream in) {
    super(in);
    //super(new InputStreamDumper(in));
  }

  @SuppressWarnings("UnusedDeclaration")
  static class InputStreamDumper extends InputStream {
    final FileOutputStream fileOut;
    private InputStream in;

    InputStreamDumper(InputStream in) {
      this.in = in;

      try {
        fileOut = new FileOutputStream(new File("/Users/develar/clientOut"));
      }
      catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void close() throws IOException {
      super.close();
      fileOut.close();
    }

    @Override
    public int read() throws IOException {
      int read = in.read();
      fileOut.write(read);
      return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
      byte[] bytes = new byte[b.length];
      int length = super.read(bytes);
      fileOut.write(bytes, 0, length);
      System.arraycopy(bytes, 0, b, 0, length);
      return length;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      byte[] bytes = new byte[len];
      int length = super.read(bytes, off, len);
      fileOut.write(bytes, 0, length);
      System.arraycopy(bytes, 0, b, off, length);
      return length;
    }
  }

  public BufferedImage readImage() throws IOException {
    return IOUtil.readARGBImage(this);
  }

  public int[] readIntArray() throws IOException {
    skipBytes(1);
    int n = readUInt29() >> 1;
    int[] array = new int[n];
    skipBytes(1);
    for (int i = 0; i < n; i++) {
      array[i] = readInt();
    }

    return array;
  }

  private int readUInt29() throws IOException {
    int value;
    int b;
    if ((b = readByte() & 0xFF) < 128) {
      return b;
    }

    value = (b & 0x7F) << 7;
    if ((b = readByte() & 0xFF) < 128) {
      return (value | b);
    }

    value = (value | (b & 0x7F)) << 7;
    if ((b = readByte() & 0xFF) < 128) {
      return value | b;
    }

    return (value | (b & 0x7F)) << 8 | (readByte() & 0xFF);
  }

  public VirtualFile readFile() throws IOException {
    String url = readUTF();
    VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(url);
    if (file == null) {
      SocketInputHandlerImpl.LOG.error("Can't find file " + url);
    }

    return file;
  }
}