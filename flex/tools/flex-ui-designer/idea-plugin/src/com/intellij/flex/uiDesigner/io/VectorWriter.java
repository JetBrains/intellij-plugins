package com.intellij.flex.uiDesigner.io;

public class VectorWriter extends CollectionWriter {
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

  public VectorWriter(String elementClassName, String vectorClassName, CollectionWriter sharedReferenceTablesOwner) {
    super(1 + 3 + 1, 1024 * 8, sharedReferenceTablesOwner);

    this.elementClassName = elementClassName;
    this.vectorClassName = vectorClassName == null ? elementClassName : vectorClassName;
  }

  public AmfOutputStream getOutputForIteration() {
    out.write(Amf3Types.OBJECT);
    out.writeObjectTraits(elementClassName);

    counter++;

    return out;
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

  public byte[] get() {
    writeHeader(headerOutput);

    byte[] bytes = new byte[headerOutput.size() + out.size()];
    headerByteOutput.writeTo(bytes, 0);
    byteOutput.writeTo(bytes, headerOutput.size());
    return bytes;
  }

  public byte[] get(StringRegistry.StringWriter stringWriter) {
    writeHeader(headerOutput);

    int stringWriterSize = stringWriter.size();
    byte[] bytes = new byte[stringWriterSize + headerOutput.size() + out.size()];
    stringWriter.writeTo(bytes);
    headerByteOutput.writeTo(bytes, stringWriterSize);
    byteOutput.writeTo(bytes, stringWriterSize + headerOutput.size());

    stringWriter.finishChange();
    return bytes;
  }
}