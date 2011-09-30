package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.ByteRange;

class SetPropertyOrStyle extends OverrideBase {
  int targetId = -1;

  private ByteRange targetRange;
  private int reference = -1;
  private int referencePosition = -1;

  public void setTargetRange(ByteRange targetRange) {
    this.targetRange = targetRange;
  }

  SetPropertyOrStyle(ByteRange dataRange) {
    super(dataRange);
  }

  @Override
  void write(BaseWriter writer, StateWriter stateWriter) {
    if (referencePosition == -1) {
      referencePosition = writer.referableHeader();

      writer.getOut().write(AmfExtendedTypes.OBJECT);
      writer.addMarker(dataRange);
      reference = -1;

      if (targetRange == null) {
        writer.property(stateWriter.TARGET).objectReference(targetId);
      }
      else {
        writer.addMarker(targetRange);
      }

      writer.endObject();
    }
    else {
      if (reference == -1) {
        reference = writer.allocateAbsoluteStaticObjectId();
        StaticObjectContext.initializeReference(reference, writer.getOut(), referencePosition);
      }

      writer.objectReference(reference);
    }
  }
}