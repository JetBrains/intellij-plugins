package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.ByteRange;

class SetPropertyOrStyle extends OverrideBase {
  int targetId = -1;

  private ByteRange targetRange;
  private int reference = -2;

  public void setTargetRange(ByteRange targetRange) {
    this.targetRange = targetRange;
  }

  SetPropertyOrStyle(ByteRange dataRange) {
    super(dataRange);
  }

  @Override
  void write(BaseWriter writer, StateWriter stateWriter) {
    if (reference == -2) {
      writer.getOut().write(Amf3Types.OBJECT);
      writer.addMarker(dataRange);
      reference = -1;

      if (targetRange == null) {
        writer.writeObjectReference(stateWriter.TARGET, targetId);
      }
      else {
        writer.addMarker(targetRange);
      }

      writer.endObject();
    }
    else {
      if (reference == -1) {
        // set object reference
        final byte[] data = writer.getBlockOut().getBuffer();
        // skip class name
        final int referencePosition = skipUInt29(dataRange.getStart(), data);
        assert data[referencePosition] == 0 && data[referencePosition + 1] == 0;
        reference = writer.allocateAbsoluteStaticObjectId();
        writer.getOut().putShort(reference + 1, referencePosition);
      }

      writer.writeObjectReference(reference);
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