package com.intellij.flex.uiDesigner.mxml;

import com.intellij.openapi.vfs.VirtualFile;

abstract class BinaryValueWriter extends AbstractPrimitiveValueWriter {
  protected final VirtualFile virtualFile;

  public BinaryValueWriter(VirtualFile virtualFile) {
    this.virtualFile = virtualFile;
  }
}
