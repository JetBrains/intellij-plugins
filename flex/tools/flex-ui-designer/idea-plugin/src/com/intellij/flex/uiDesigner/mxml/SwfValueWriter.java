package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.BinaryFileManager;
import com.intellij.flex.uiDesigner.BinaryFileType;
import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

class SwfValueWriter extends BinaryValueWriter {
  private final String symbol;

  public SwfValueWriter(VirtualFile virtualFile, @Nullable String symbol) {
    super(virtualFile);
    this.symbol = symbol;
  }

  @Override
  protected void write(PrimitiveAmfOutputStream out, BaseWriter writer) throws InvalidPropertyException {
    out.write(AmfExtendedTypes.SWF);

    if (symbol == null) {
      out.write(0);
    }
    else {
      out.writeAmfUtf(symbol);
    }

    final int id;
    BinaryFileManager binaryFileManager = BinaryFileManager.getInstance();
    if (binaryFileManager.isRegistered(virtualFile)) {
      id = binaryFileManager.getId(virtualFile);
    }
    else {
      id = binaryFileManager.registerFile(virtualFile, BinaryFileType.SWF);
    }

    out.writeUInt29(id);
  }
}