package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.EmbedImageManager;
import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ImageValueWriter extends BinaryValueWriter {
  private final String mimeType;

  public ImageValueWriter(@NotNull VirtualFile virtualFile, @Nullable String mimeType) {
    super(virtualFile);
    this.mimeType = mimeType;
  }

  @Override
  protected int getStyleFlags() {
    return StyleFlags.EMBED_IMAGE;
  }

  @Override
  protected void doWrite(PrimitiveAmfOutputStream out, BaseWriter writer, boolean isStyle) throws InvalidPropertyException {
    if (!isStyle) {
      out.write(AmfExtendedTypes.IMAGE);
    }
    out.writeUInt29(EmbedImageManager.getInstance().add(virtualFile, mimeType, writer.getAssetCounter()));
  }
}