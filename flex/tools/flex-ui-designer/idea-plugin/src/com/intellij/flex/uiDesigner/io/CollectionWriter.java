package com.intellij.flex.uiDesigner.io;

import org.jetbrains.annotations.Nullable;

abstract class CollectionWriter {
  protected final ByteArrayOutputStreamEx headerByteOutput;
  protected final ByteArrayOutputStreamEx byteOutput;

  protected final AmfOutputStream headerOutput;
  protected final AmfOutputStream out;

  protected int counter = 0;

  private final boolean isReferenceTablesShared;

  protected CollectionWriter(int headerBufferSize, int bodyBufferSize, @Nullable CollectionWriter sharedReferenceTablesOwner) {
    headerByteOutput = new ByteArrayOutputStreamEx(headerBufferSize);
    byteOutput = new ByteArrayOutputStreamEx(bodyBufferSize);

    headerOutput = new AmfOutputStream(headerByteOutput);
    out = new AmfOutputStream(byteOutput);

    isReferenceTablesShared = sharedReferenceTablesOwner != null;
    if (isReferenceTablesShared) {
      if (sharedReferenceTablesOwner.headerOutput.stringTable == null) {
        sharedReferenceTablesOwner.headerOutput.stringTable = sharedReferenceTablesOwner.out.stringTable = new ObjectIntHashMap<String>();
      }
      if (sharedReferenceTablesOwner.headerOutput.traitsTable == null) {
        sharedReferenceTablesOwner.headerOutput.traitsTable = sharedReferenceTablesOwner.out.traitsTable = new ObjectIntHashMap<String>();
      }

      headerOutput.stringTable = out.stringTable = sharedReferenceTablesOwner.headerOutput.stringTable;
      headerOutput.traitsTable = out.traitsTable = sharedReferenceTablesOwner.headerOutput.traitsTable;
    }
  }

  public void prepareIteration() {
    // must be reset for empty collection
    if (counter != 0 || headerOutput.size() > 0 || out.size() > 0) {
      counter = 0;
      if (isReferenceTablesShared) {
        headerOutput.resetSizeAndPosition();
        out.resetSizeAndPosition();
      }
      else {
        headerOutput.reset();
        out.reset();
      }
    }
  }

  public void writeArrayValueHeader(int length) {
    out.write(Amf3Types.ARRAY);
    out.writeUInt29((length << 1) | 1);
    out.write(1);
  }

  public void writeObjectValueHeader(String className) {
    out.write(Amf3Types.OBJECT);
    out.writeObjectTraits(className);
  }

  abstract byte[] get();

  public void writeTo(AmfOutputStream out) {
    writeHeader(out);
    byteOutput.writeTo(out);
  }

  protected abstract void writeHeader(AmfOutputStream out);
}
