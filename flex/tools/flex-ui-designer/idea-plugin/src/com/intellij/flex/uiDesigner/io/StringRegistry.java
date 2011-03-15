package com.intellij.flex.uiDesigner.io;

import com.intellij.openapi.components.ServiceManager;
import gnu.trove.TObjectIntIterator;
import org.jetbrains.annotations.Nullable;

public class StringRegistry {
  private final ObjectIntHashMap<String> table = new ObjectIntHashMap<String>(1024);
//  private final Map<String, Integer> table = new LinkedHashMap<String, Integer>();
  private StringWriter activeWriter;

  public static StringRegistry getInstance() {
    return ServiceManager.getService(StringRegistry.class);
  }

  public void reset() {
    table.clear();
    assert activeWriter == null;
  }
  
  public boolean isEmpty() {
    return table.isEmpty();
  }
  
  public int getSize() {
    return table.size();
  }
  
  public TObjectIntIterator<String> getIterator() {
    return table.iterator();
  }
  
  private int getNameReference(String string, StringWriter writer) {
    assert activeWriter == writer;
    
    int reference = table.get(string);
//    Integer reference = table.get(string);
    if (reference == -1) {
//    if (reference == null) {
      reference = table.size() + 1;
      table.put(string, reference);
      writer.counter++;
      writer.out.writeAmfUTF(string, false);
    }
    
    return reference;
  }
  
  public static class StringWriter {
    private final StringRegistry stringRegistry;
    
    private final PrimitiveAmfOutputStream out;
    private int counter;

    public StringWriter(StringRegistry stringRegistry) {
      this(stringRegistry, 1024);
    }
    
    public StringWriter() {
      this(StringRegistry.getInstance());
    }
    
    public StringWriter(int size) {
      this(StringRegistry.getInstance(), size);
    }

    public StringWriter(StringRegistry stringRegistry, int size) {
      this.stringRegistry = stringRegistry;
      out = new PrimitiveAmfOutputStream(new ByteArrayOutputStreamEx(size));
    }

    public void startChange() {
      assert stringRegistry.activeWriter == null;
      stringRegistry.activeWriter = this;
    }
    
    public void finishChange() {
      counter = 0;
      out.reset();
      
      stringRegistry.activeWriter = null;
    }
    
    public int getReference(String string) {
      return stringRegistry.getNameReference(string, this);
    }
    
    public void writeReference(@Nullable String string, PrimitiveAmfOutputStream out) {
      if (string == null) {
        out.write(0);
      }
      else {
        out.writeUInt29(getReference(string));
      }
    }

    public int getCounter() {
      return counter;
    }
    
    public int size() {
      return (counter < 0x80 ? 1 : 2) + out.size();
    }

    public ByteArrayOutputStreamEx getByteArrayOut() {
      return out.getByteArrayOut();
    }
    
    public void writeTo(PrimitiveAmfOutputStream to) {
      to.writeUInt29(counter);
      out.writeTo(to);
      
      finishChange();
    }

    public void writeTo(byte[] bytes) {
      final int offset;
      if (counter < 0x80) {
        bytes[0] = (byte) counter;
        offset = 1;
      }
      else if (counter < 0x4000) {
        bytes[0] = (byte) (((counter >> 7) & 0x7F) | 0x80);
        bytes[1] = (byte) (counter & 0x7F);
        offset = 2;
      }
      else {
        throw new IllegalArgumentException("Integer out of range: " + counter);
      }
      
      out.getByteArrayOut().writeTo(bytes, offset);
    }
  }
}
