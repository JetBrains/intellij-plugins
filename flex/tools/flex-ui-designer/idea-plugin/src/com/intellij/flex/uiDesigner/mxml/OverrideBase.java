package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.ByteRange;
import gnu.trove.TLinkable;

import java.io.IOException;

abstract class OverrideBase implements TLinkable {
  protected ByteRange dataRange;
  
  OverrideBase next;
  OverrideBase previous;

  OverrideBase(ByteRange dataRange) {
    this.dataRange = dataRange;
  }

  abstract void write(BaseWriter writer, StateWriter stateWriter);

  @Override
  public TLinkable getNext() {
    return next;
  }

  @Override
  public TLinkable getPrevious() {
    return previous;
  }

  @Override
  public void setNext(TLinkable value) {
    next = (OverrideBase) value;
  }

  @Override
  public void setPrevious(TLinkable value) {
    previous = (OverrideBase) value;
  }
}
