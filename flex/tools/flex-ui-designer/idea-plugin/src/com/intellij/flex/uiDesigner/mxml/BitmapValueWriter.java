package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.BinaryFileManager;
import com.intellij.flex.uiDesigner.Client;
import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.SocketInputHandler;
import com.intellij.flex.uiDesigner.io.ImageUtil;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.flex.uiDesigner.io.UnbufferedOutput;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;

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
      BinaryFileManager.getInstance().remove(virtualFile);
      throw new InvalidPropertyException(e, "error.cannot.write.binary.file", virtualFile.getName());
      // todo write special error image
    }

    UnbufferedOutput rawOut = null;
    try {
      rawOut = writer.getBlockOut().writeUnbufferedHeader(2 + 2 + 2 + 2 + 1 + (image.getWidth() * image.getHeight() * 4));
      rawOut.write(Client.ClientMethod.METHOD_CLASS);
      rawOut.write(Client.ClientMethod.registerBitmap.ordinal());
      writeId(id, rawOut);
      ImageUtil.write(image, rawOut, mimeType, virtualFile);
    }
    catch (Throwable e) {
      try {
        BinaryFileManager.getInstance().remove(virtualFile);
      }
      finally {
        if (rawOut != null) {
          try {
            notifyClientSocket(rawOut, virtualFile);
          }
          catch (IOException inner) {
            inner.initCause(e);
            e = inner;
          }
        }
      }

      throw new InvalidPropertyException(e, "error.cannot.write.binary.file", virtualFile.getName());
    }
  }

  private void notifyClientSocket(UnbufferedOutput rawOut, VirtualFile virtualFile) throws IOException {
    DataOutputStream errorOut = ServiceManager.getService(SocketInputHandler.class).getErrorOut();
    errorOut.writeInt(rawOut.messageId);
    errorOut.writeInt(rawOut.written);
    errorOut.flush();
  }
}