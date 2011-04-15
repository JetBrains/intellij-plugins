package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.BinaryFileManager;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.io.OutputStream;

abstract class BinaryValueWriter extends AbstractPrimitiveValueWriter {
  protected final VirtualFile virtualFile;

  public BinaryValueWriter(VirtualFile virtualFile) {
    this.virtualFile = virtualFile;
  }

  protected int checkRegistered(PrimitiveAmfOutputStream out) {
    BinaryFileManager binaryFileManager = BinaryFileManager.getInstance();
    if (binaryFileManager.isRegistered(virtualFile)) {
      out.writeUInt29(binaryFileManager.getId(virtualFile));
      return -1;
    }
    else {
      int id = binaryFileManager.add(virtualFile);
      out.writeUInt29(id);
      return id;
    }
  }

  protected void writeId(int id, OutputStream out) throws IOException {
    out.write((id >>> 8) & 0xFF);
    out.write((id) & 0xFF);
  }
}
