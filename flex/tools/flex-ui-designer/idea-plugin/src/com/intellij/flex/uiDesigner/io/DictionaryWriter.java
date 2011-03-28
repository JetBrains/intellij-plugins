package com.intellij.flex.uiDesigner.io;

public class DictionaryWriter extends CollectionWriter {
  public DictionaryWriter() {
    this(null);
  }

  public int getCounter() {
    return counter;
  }

  public DictionaryWriter(CollectionWriter sharedReferenceTablesOwner) {
    super(1 + 3 + 1, 1024 * 2, sharedReferenceTablesOwner);
  }

  public void writeTrue(String key) {
    // string reference table not used
    out.write(Amf3Types.STRING);
    out.writeAmfUtf(key, true);

    out.write(Amf3Types.TRUE);
    counter++;
  }

  public void write(String key, String value) {
    writeKey(key);
    writeValue(value);
  }

  public void writeKey(String key) {
    out.write(key);

    counter++;
  }

  public void writeValue(String v) {
    out.write(v);
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public void writeValue(String[] values) {
    out.write(Amf3Types.ARRAY);
    out.writeUInt29((values.length << 1) | 1);
    out.write(1);
    for (String v : values) {
      out.write(Amf3Types.STRING);
      out.writeStringWithoutType(v);
    }
  }

  @Override
  protected void writeHeader(AmfOutputStream out) {
    out.write(Amf3Types.DICTIONARY);
    out.writeUInt29((counter << 1) | 1);
    out.write(0);
  }

  public byte[] get() {
    writeHeader(headerOutput);

    byte[] bytes = new byte[headerOutput.size() + out.size()];
    headerByteOutput.writeTo(bytes, 0);
    byteOutput.writeTo(bytes, headerOutput.size());
    return bytes;
  }
}
