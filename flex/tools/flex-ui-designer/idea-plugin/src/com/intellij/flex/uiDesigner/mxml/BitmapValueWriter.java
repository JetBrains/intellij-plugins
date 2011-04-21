package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.Client;
import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.ImageUtil;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

class BitmapValueWriter extends BinaryValueWriter {
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
      image = ImageUtil.getImage(virtualFile, mimeType);
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
    ImageUtil.write(image, out, mimeType, virtualFile);
  }
}