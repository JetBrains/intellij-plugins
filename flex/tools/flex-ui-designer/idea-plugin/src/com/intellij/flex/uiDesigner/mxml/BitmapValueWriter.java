package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class BitmapValueWriter extends AbstractPrimitiveValueWriter {
  private final VirtualFile virtualFile;
  private final String mimeType;

  public BitmapValueWriter(@NotNull VirtualFile virtualFile, @Nullable String mimeType) {
    this.virtualFile = virtualFile;
    this.mimeType = mimeType;
  }

  @Override
  protected void write(PrimitiveAmfOutputStream out) {
    final BufferedImage image;
    try {
      image = getImage();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

    out.writeShort(image.getWidth());
    out.writeShort(image.getHeight());
    // http://juick.com/develar/1274330 flex compiler determine BitmapData.transparent by file type, 
    // but not actual pixels (i.e. BufferedImage.getTransparency()), so, for png always true, false for others (gif and jpg)
    out.write(mimeType == null ? virtualFile.getName().endsWith(".png") : mimeType.equals("image/png"));
    for (int pixel : image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth())) {
      out.writeInt(pixel);
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
        Iterator iter = ImageIO.getImageReaders(mimeType);
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
