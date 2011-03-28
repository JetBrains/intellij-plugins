package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.ByteRange;

class SetPropertyOrStyle extends OverrideBase {
  int targetId;

  private ByteRange targetRange;

  public void setTargetRange(ByteRange targetRange) {
    this.targetRange = targetRange;
  }

  SetPropertyOrStyle(ByteRange dataRange) {
    super(dataRange);
  }

  @Override
  void write(BaseWriter writer, StateWriter stateWriter) {
    writer.addMarker(dataRange);

    if (targetRange == null) {
      writer.writeObjectReference(stateWriter.TARGET, targetId);
    }
    else {
      writer.addMarker(targetRange);
    }
  }
}
