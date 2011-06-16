package com.intellij.flex.uiDesigner.io;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

public final class ImageUtil {
  private static final int MAX_BUFFER_LENGTH = 12288;

  private static final ThreadLocal<byte[]> BUFFER = new ThreadLocal<byte[]>() {
    protected byte[] initialValue() {
      return new byte[MAX_BUFFER_LENGTH];
    }
  };

  // ImageIO doesn't support TIFF, but Flex compiler too, so, we don't need JAI
  public static BufferedImage getImage(final VirtualFile virtualFile, final @Nullable String mimeType) throws IOException {
    final InputStream inputStream = virtualFile.getInputStream();
    final BufferedImage image;
    final Iterator<ImageReader> readers;
    if (mimeType == null) {
      readers = ImageIO.getImageReadersBySuffix(virtualFile.getExtension());
    }
    else {
     readers = ImageIO.getImageReadersByMIMEType(mimeType);
    }

    try {
      ImageReader reader = readers.next();
      // skip sanselan, we don't want it (prefer to standard sun impl)
      if (reader.getFormatName().equals("UNKNOWN")) {
        reader = readers.next();
      }

      ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
      reader.setInput(imageInputStream, true, true);
      try {
        image = reader.read(0, reader.getDefaultReadParam());
      }
      finally {
        reader.dispose();
        imageInputStream.close();
      }
    }
    finally {
      inputStream.close();
    }

    return image;
  }

  // http://juick.com/develar/1274330 flex compiler determine BitmapData.transparent by file type,
  // but not actual pixels (i.e. BufferedImage.getTransparency()), so, for png always true, false for others (gif and jpg)
  public static void write(final BufferedImage image, final OutputStream out, final String mimeType, final VirtualFile virtualFile) throws IOException {
    write(image, out, mimeType == null ? virtualFile.getName().endsWith(".png") : mimeType.equals("image/png"));
  }

  public static void write(final BufferedImage image, final OutputStream out, final boolean transparent) throws IOException {
    final int width = image.getWidth();
    final int height = image.getHeight();

    final byte[] byteBuffer = BUFFER.get();

    IOUtil.writeShort(width, byteBuffer, 0);
    IOUtil.writeShort(height, byteBuffer, 2);
    byteBuffer[4] = (byte)(transparent ? 1 : 0);
    out.write(byteBuffer, 0, 5);

    WritableRaster raster = image.getRaster();
    final int nbands = raster.getNumBands();
    assert nbands == 3 || nbands == 4;
    final int unflushedBufferLength;
    if (raster.getDataBuffer() instanceof DataBufferByte) {
      unflushedBufferLength = writeDataByte(image, out, byteBuffer, nbands);
    }
    else {
      unflushedBufferLength = writeDataInt(image, out, byteBuffer, nbands);
    }

    if (unflushedBufferLength > 0) {
      out.write(byteBuffer, 0, unflushedBufferLength);
    }
  }

  private static int writeDataInt(BufferedImage image, OutputStream out, byte[] byteBuffer, int nbands) throws IOException {
    int bufferLength = 0;
    final DataBufferInt dataBuffer = (DataBufferInt)image.getRaster().getDataBuffer();
    assert dataBuffer.getNumBanks() == 1;
    int[] data = dataBuffer.getData();
    if (nbands == 3) {
      assert image.getType() == BufferedImage.TYPE_INT_RGB;
      for (int i = 0, n = data.length; i < n; i += 1) {
        int pixel = data[i];
        byteBuffer[bufferLength++] = (byte)255;
        byteBuffer[bufferLength++] = (byte)((pixel >> 16) & 0xff);
        byteBuffer[bufferLength++] = (byte)((pixel >> 8) & 0xff);
        byteBuffer[bufferLength++] = (byte)(pixel & 0xff);

        if (bufferLength == MAX_BUFFER_LENGTH) {
          out.write(byteBuffer, 0, bufferLength);
          bufferLength = 0;
        }
      }
    }
    else {
      assert image.getType() == BufferedImage.TYPE_INT_ARGB;
      for (int i = 0, n = data.length; i < n; i += 1) {
        int pixel = data[i];
        byteBuffer[bufferLength++] = (byte)((pixel >> 24) & 0xff);
        byteBuffer[bufferLength++] = (byte)((pixel >> 16) & 0xff);
        byteBuffer[bufferLength++] = (byte)((pixel >> 8) & 0xff);
        byteBuffer[bufferLength++] = (byte)(pixel & 0xff);

        if (bufferLength == MAX_BUFFER_LENGTH) {
          out.write(byteBuffer, 0, bufferLength);
          bufferLength = 0;
        }
      }
    }

    return bufferLength;
  }

  private static int writeDataByte(BufferedImage image, OutputStream out, byte[] byteBuffer, int nbands) throws IOException {
    int bufferLength = 0;
    final DataBufferByte dataBuffer = (DataBufferByte)image.getRaster().getDataBuffer();
    assert dataBuffer.getNumBanks() == 1;
    byte[] data = dataBuffer.getData();
    if (nbands == 3) {
      assert image.getType() == BufferedImage.TYPE_3BYTE_BGR;
      for (int i = 0, n = data.length; i < n; i += 3) {
        byteBuffer[bufferLength++] = (byte)255;
        byteBuffer[bufferLength++] = (byte)(data[i + 2] & 0xff);
        byteBuffer[bufferLength++] = (byte)(data[i + 1] & 0xff);
        byteBuffer[bufferLength++] = (byte)(data[i] & 0xff);

        if (bufferLength == MAX_BUFFER_LENGTH) {
          out.write(byteBuffer, 0, bufferLength);
          bufferLength = 0;
        }
      }
    }
    else {
      assert image.getType() == BufferedImage.TYPE_4BYTE_ABGR;
      for (int i = 0, n = data.length; i < n; i += 4) {
        byteBuffer[bufferLength++] = (byte)(data[i] & 0xff);
        byteBuffer[bufferLength++] = (byte)(data[i + 3] & 0xff);
        byteBuffer[bufferLength++] = (byte)(data[i + 2] & 0xff);
        byteBuffer[bufferLength++] = (byte)(data[i + 1] & 0xff);

        if (bufferLength == MAX_BUFFER_LENGTH) {
          out.write(byteBuffer, 0, bufferLength);
          bufferLength = 0;
        }
      }
    }

    return bufferLength;
  }
}
