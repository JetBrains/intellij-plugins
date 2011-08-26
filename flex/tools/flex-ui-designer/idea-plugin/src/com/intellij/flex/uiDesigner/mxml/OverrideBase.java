package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.ByteRange;

abstract class OverrideBase {
  protected final ByteRange dataRange;

  OverrideBase(ByteRange dataRange) {
    this.dataRange = dataRange;
  }

  abstract void write(BaseWriter writer, StateWriter stateWriter);
}