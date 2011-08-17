package com.intellij.flex.uiDesigner.io;

import com.intellij.openapi.vfs.VirtualFile;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.FileOutputStream;
import java.io.IOException;

public final class ImageUtil {
  private static final int MAX_BUFFER_LENGTH = 12288;

  private static final ThreadLocal<byte[]> BUFFER = new ThreadLocal<byte[]>() {
    protected byte[] initialValue() {
      return new byte[MAX_BUFFER_LENGTH];
    }
  };

  public static void write(VirtualFile file, String mimeType, FileOutputStream out) throws IOException {
    Image image = Toolkit.getDefaultToolkit().createImage(file.contentsToByteArray());
    PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, -1, -1, true);
    try {
      pixelGrabber.grabPixels();
    }
    catch (InterruptedException e) {
      throw new IOException("Failed to grab pixels for image " + file.getPresentableUrl());
    }

    if (((pixelGrabber.getStatus() & ImageObserver.WIDTH) == 0) ||
        ((pixelGrabber.getStatus() & ImageObserver.HEIGHT) == 0)) {
      throw new IOException("Failed to grab pixels for image " + file.getPresentableUrl());
    }

    final byte[] byteBuffer = BUFFER.get();
    IOUtil.writeShort(pixelGrabber.getWidth(), byteBuffer, 0);
    IOUtil.writeShort(pixelGrabber.getHeight(), byteBuffer, 2);
    byteBuffer[4] = (byte)((mimeType == null ? file.getName().endsWith(".jpg") : mimeType.equals("image/jpeg")) ? 0 : 1);
    out.write(byteBuffer, 0, 5);

    int bufferLength = 0;
    for (int pixel : (int[])pixelGrabber.getPixels()) {
      byteBuffer[bufferLength++] = (byte)((pixel >> 24) & 0xff);
      byteBuffer[bufferLength++] = (byte)((pixel >> 16) & 0xff);
      byteBuffer[bufferLength++] = (byte)((pixel >> 8) & 0xff);
      byteBuffer[bufferLength++] = (byte)(pixel & 0xff);

      if (bufferLength == MAX_BUFFER_LENGTH) {
        out.write(byteBuffer, 0, bufferLength);
        bufferLength = 0;
      }
    }

    if (bufferLength > 0) {
      out.write(byteBuffer, 0, bufferLength);
    }
  }
}