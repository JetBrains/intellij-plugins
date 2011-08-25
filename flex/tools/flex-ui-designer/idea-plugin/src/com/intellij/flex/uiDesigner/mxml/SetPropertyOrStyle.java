package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.ByteRange;

class SetPropertyOrStyle extends OverrideBase {
  int targetId;

  private ByteRange targetRange;
  // note: final id for write, already incremented by 1
  private int reference = -1;

  public void setTargetRange(ByteRange targetRange) {
    this.targetRange = targetRange;
  }

  SetPropertyOrStyle(ByteRange dataRange) {
    super(dataRange);
  }

  @Override
  void write(BaseWriter writer, StateWriter stateWriter) {
    if (reference > -1) {
      if (reference == 0) {
        // set object reference
        final byte[] data = writer.getBlockOut().getBuffer();
        // skip class name
        final int referencePosition = skipUInt29(dataRange.getStart(), data);
        assert data[referencePosition] == 0 && data[referencePosition + 1] == 0;
        reference = writer.allocateAbsoluteStaticObjectId() + 1;
        writer.getOut().putShort(reference, referencePosition);
      }

      writer.writeObjectReference(reference);
    }
    else {
      writer.addMarker(dataRange);
      reference = 0;
    }

    if (targetRange == null) {
      writer.writeObjectReference(stateWriter.TARGET, targetId);
    }
    else {
      writer.addMarker(targetRange);
    }
  }

  private static int skipUInt29(int offset, byte[] data)  {
    if ((data[offset++] & 0xFF) < 128) {
      return offset;
    }

    if ((data[offset++] & 0xFF) < 128) {
      return offset;
    }

    if ((data[offset++] & 0xFF) < 128) {
      return offset;
    }

    return offset + 1;
  }
}