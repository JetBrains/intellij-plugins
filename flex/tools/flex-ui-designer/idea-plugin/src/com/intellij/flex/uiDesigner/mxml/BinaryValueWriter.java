package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.BinaryFileManager;
import com.intellij.flex.uiDesigner.io.DirectWriter;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.io.OutputStream;

abstract class BinaryValueWriter extends AbstractPrimitiveValueWriter {
  protected static final Logger LOG = Logger.getInstance(BinaryValueWriter.class.getName());
  
  protected final VirtualFile virtualFile;

  public BinaryValueWriter(VirtualFile virtualFile) {
    this.virtualFile = virtualFile;
  }

  protected int checkRegistered(PrimitiveAmfOutputStream out) {
    int id = BinaryFileManager.getInstance().getId(virtualFile);
    if (id == -1) {
      id = BinaryFileManager.getInstance().add(virtualFile);
      out.writeUInt29(id);
      return id;
    }
    else {
      out.writeUInt29(id);
      return -1;
    }
  }

  protected void writeId(int id, OutputStream out) throws IOException {
    out.write((id >>> 8) & 0xFF);
    out.write((id) & 0xFF);
  }
}
