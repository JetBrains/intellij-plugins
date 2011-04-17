package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.Client;
import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
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

class BitmapValueWriter extends BinaryValueWriter {
  private static final int MAX_BUFFER_LENGTH = 12288;
  private static final byte[] byteBuffer = new byte[MAX_BUFFER_LENGTH];

  private final String mimeType;

  public BitmapValueWriter(@NotNull VirtualFile virtualFile, @Nullable String mimeType) {
    super(virtualFile);
    this.mimeType = mimeType;
  }

  @Override
  protected void write(PrimitiveAmfOutputStream out, BaseWriter writer) throws InvalidPropertyException {
    out.write(AmfExtendedTypes.BITMAP);
    int id;
    if ((id = checkRegistered(out)) == -1) {
      return;
    }

    final BufferedImage image;
    try {
      image = getImage();
    }
    catch (IOException e) {
      throw new InvalidPropertyException(e, "error.cannot.write.binary.file", virtualFile.getName());
      // todo write special error image
    }

    try {
      OutputStream output = writer.getBlockOut().writeUnbufferedHeader(2 + 2 + 2 + 2 + 1 + (image.getWidth() * image.getHeight() * 4));
      output.write(Client.ClientMethod.METHOD_CLASS);
      output.write(Client.ClientMethod.registerBitmap.ordinal());
      write(id, image, output);
    }
    catch (IOException e) {
      throw new InvalidPropertyException(e, "error.cannot.write.binary.file", virtualFile.getName());
    }
  }
  
  private void write(int id, BufferedImage image, OutputStream out) throws IOException {
    writeId(id, out);

    final int width = image.getWidth();
    final int height = image.getHeight();

    out.write((width >>> 8) & 0xFF);
    out.write((width) & 0xFF);

    out.write((height >>> 8) & 0xFF);
    out.write((height) & 0xFF);

    // http://juick.com/develar/1274330 flex compiler determine BitmapData.transparent by file type, 
    // but not actual pixels (i.e. BufferedImage.getTransparency()), so, for png always true, false for others (gif and jpg)
    out.write((mimeType == null ? virtualFile.getName().endsWith(".png") : mimeType.equals("image/png")) ? 1 : 0);

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

  /**
   * ImageIO doesn't support TIFF, but Flex compiler too, so, we don't need JAI
   */
  private BufferedImage getImage() throws IOException {
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
}