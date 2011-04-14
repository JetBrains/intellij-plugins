package com.intellij.flex.uiDesigner.io;

import org.jetbrains.annotations.Nullable;

public class VectorWriter extends CollectionWriter implements ByteProvider {
  private final String elementClassName;
  private final String vectorClassName;

  public VectorWriter(String elementClassName) {
    this(elementClassName, (String)null);
  }

  public VectorWriter(String elementClassName, String vectorClassName) {
    this(elementClassName, vectorClassName, null);
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public VectorWriter(String elementClassName, CollectionWriter sharedReferenceTablesOwner) {
    this(elementClassName, null, sharedReferenceTablesOwner);
  }

  public VectorWriter(String elementClassName, @Nullable String vectorClassName, @Nullable CollectionWriter sharedReferenceTablesOwner) {
    super(1 + 3 + 1, 1024 * 8, sharedReferenceTablesOwner);

    this.elementClassName = elementClassName;
    this.vectorClassName = vectorClassName == null ? elementClassName : vectorClassName;
  }

  public AmfOutputStream getOutputForIteration() {
    out.start();
    out.write(Amf3Types.OBJECT);
    out.writeObjectTraits(elementClassName);

    counter++;

    return out;
  }

  public void rollbackLastIteration() {
    counter--;
    out.rollback();
  }

  public AmfOutputStream getOutputForCustomData() {
    return out;
  }

  @Override
  public void prepareIteration() {
    super.prepareIteration();

    out.writeStringWithoutType(vectorClassName);
  }

  @Override
  protected void writeHeader(AmfOutputStream out) {
    out.write(Amf3Types.VECTOR_OBJECT);
    out.writeUInt29((counter << 1) | 1);
    out.write(1);
  }

  @Override
  public int size() {
    writeHeader(headerOutput);
    return headerOutput.size() + out.size();
  }

  @Override
  public int writeTo(byte[] bytes, int offset) {
    headerByteOutput.writeTo(bytes, offset);
    offset += headerOutput.size();
    byteOutput.writeTo(bytes, offset);

    return offset + byteOutput.size();
  }
}