package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.EmbedSwfManager;
import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

class SwfValueWriter extends BinaryValueWriter {
  private @Nullable final String symbol;

  public SwfValueWriter(VirtualFile virtualFile, @Nullable String symbol) {
    super(virtualFile);
    this.symbol = symbol;
  }

  @Override
  protected int getStyleFlags() {
    return StyleFlags.EMBED_SWF;
  }

  @Override
  protected void doWrite(PrimitiveAmfOutputStream out, BaseWriter writer, boolean isStyle) throws InvalidPropertyException {
    if (!isStyle) {
      out.write(AmfExtendedTypes.SWF);
    }
    out.writeUInt29(EmbedSwfManager.getInstance().add(virtualFile, symbol, writer.getAssetCounter()));
  }
}