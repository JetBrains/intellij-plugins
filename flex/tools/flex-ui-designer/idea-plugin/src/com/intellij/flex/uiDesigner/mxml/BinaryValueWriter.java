package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.BinaryFileManager;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.openapi.vfs.VirtualFile;

abstract class BinaryValueWriter extends AbstractPrimitiveValueWriter {
  protected final VirtualFile virtualFile;

  public BinaryValueWriter(VirtualFile virtualFile) {
    this.virtualFile = virtualFile;
  }

  protected boolean checkRegistered(PrimitiveAmfOutputStream out) {
    int id = BinaryFileManager.getInstance().getId(virtualFile);
    if (id == -1) {
      id = BinaryFileManager.getInstance().add(virtualFile);
      out.writeUInt29(id << 1);
      return false;
    }
    else {
      out.writeUInt29((id << 1) | 1);
      return true;
    }
  }
}
