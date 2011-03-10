package com.intellij.flex.uiDesigner.io;

public class VectorWriter extends CollectionWriter {
  private String elementClassName;

  public VectorWriter(String elementClassName) {
    this(elementClassName, null);
  }

  public VectorWriter(String elementClassName, CollectionWriter sharedReferenceTablesOwner) {
    super(1 + 3 + 1, 1024 * 8, sharedReferenceTablesOwner);

    this.elementClassName = elementClassName;
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

    out.writeStringWithoutType(elementClassName);
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
