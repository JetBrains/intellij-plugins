package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

class BitmapValueWriter extends BinaryValueWriter {
  private static final Logger LOG = Logger.getInstance(BitmapValueWriter.class.getName());

  private final String mimeType;

  public BitmapValueWriter(@NotNull VirtualFile virtualFile, @Nullable String mimeType) {
    super(virtualFile);
    this.mimeType = mimeType;
  }

  @Override
  protected void write(PrimitiveAmfOutputStream out, BaseWriter writer) {
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
      LOG.error(e);
      // todo write special error image 
      return;
    }

    writer.addDirectWriter(2 + 2 + 2 + 1 + (image.getWidth() * image.getHeight() * 4),
                           new MyDirectWriter(id, image, mimeType == null
                                                         ? virtualFile.getName().endsWith(".png")
                                                         : mimeType.equals("image/png")));
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

  private static class MyDirectWriter extends AbstractDirectWriter {
    private final BufferedImage image;
    private final boolean transparent;

    public MyDirectWriter(int id, BufferedImage image, boolean transparent) {
      super((id << 1) | 1);
      this.image = image;
      this.transparent = transparent;
    }

    public void write(OutputStream out) throws IOException {
      writeId(out);

      final int width = image.getWidth();
      final int height = image.getHeight();

      out.write((width >>> 8) & 0xFF);
      out.write((width) & 0xFF);

      out.write((height >>> 8) & 0xFF);
      out.write((height) & 0xFF);

      // http://juick.com/develar/1274330 flex compiler determine BitmapData.transparent by file type, 
      // but not actual pixels (i.e. BufferedImage.getTransparency()), so, for png always true, false for others (gif and jpg)
      out.write(transparent ? 1 : 0);
      for (int pixel : image.getRGB(0, 0, width, height, null, 0, width)) {
        out.write((pixel >>> 24) & 0xFF);
        out.write((pixel >>> 16) & 0xFF);
        out.write((pixel >>> 8) & 0xFF);
        out.write((pixel) & 0xFF);
      }
    }
  }
}