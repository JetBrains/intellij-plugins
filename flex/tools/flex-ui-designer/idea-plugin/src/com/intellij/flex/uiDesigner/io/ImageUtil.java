package com.intellij.flex.uiDesigner.io;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
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

  /**
   * ImageIO doesn't support TIFF, but Flex compiler too, so, we don't need JAI
   */
   public static BufferedImage getImage(final VirtualFile virtualFile, final @Nullable String mimeType) throws IOException {
    final InputStream inputStream = virtualFile.getInputStream();
    final BufferedImage image;
    try {
      if (mimeType == null) {
        image = ImageIO.read(inputStream);
      }
      else {
        Iterator iter = ImageIO.getImageReadersByMIMEType(mimeType);
        ImageReader reader = (ImageReader)iter.next();
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
    final DataBufferByte dataBuffer = (DataBufferByte)raster.getDataBuffer();
    assert dataBuffer.getNumBanks() == 1;
    byte[] data = dataBuffer.getData();
    int bufferLength = 0;
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

    if (bufferLength > 0) {
      out.write(byteBuffer, 0, bufferLength);
    }
  }
}
