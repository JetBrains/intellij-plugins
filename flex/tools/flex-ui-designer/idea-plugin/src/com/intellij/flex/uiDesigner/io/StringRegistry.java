package com.intellij.flex.uiDesigner.io;

import com.intellij.openapi.components.ServiceManager;
import gnu.trove.TObjectIntIterator;
import org.jetbrains.annotations.Nullable;

public class StringRegistry {
  private final TransactionableStringIntHashMap table = new TransactionableStringIntHashMap(1024, 1);
  private StringWriter activeWriter;

  public static StringRegistry getInstance() {
    return ServiceManager.getService(StringRegistry.class);
  }
  
  private void startChange(StringWriter activeWriter) {
    table.startTransaction();
    assert this.activeWriter == null;
    this.activeWriter = activeWriter;
  }
  
  private void rollbackChange() {
    table.rollbackTransaction();

    resetAfterChange();
  }

  private void commitChange() {
    resetAfterChange();
  }

  private void resetAfterChange() {
    activeWriter = null;
  }

  public void reset() {
    table.clear();
    assert activeWriter == null;
  }

  public boolean isEmpty() {
    return table.isEmpty();
  }
  
  public String[] toArray() {
    int size = table.size();
    TObjectIntIterator<String> iterator = table.iterator();
    String[] strings = new String[size];
    for (int i = size; i-- > 0; ) {
      iterator.advance();
      strings[iterator.value() - 1] = iterator.key();
    }
    
    return strings;
  }

  private int getNameReference(String string, StringWriter writer) {
    assert activeWriter == writer;

    int reference = table.get(string);
    if (reference == -1) {
      reference = table.size() + 1;
      table.put(string, reference);
      writer.counter++;
      writer.out.writeAmfUtf(string, false);
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
      stringRegistry.startChange(this);
    }
    
    public void rollbackChange() {
      reset();
      stringRegistry.rollbackChange();
    }

    public void finishChange() {
      reset();
      stringRegistry.commitChange();
    }
    
    private void reset() {
      counter = 0;
      out.reset();
    }

    public int getReference(String string) {
      return stringRegistry.getNameReference(string, this);
    }

    public void write(String string, PrimitiveAmfOutputStream out) {
      out.writeUInt29(getReference(string));
    }

    public void writeNullable(@Nullable String string, PrimitiveAmfOutputStream out) {
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
      return IOUtil.uint29SizeOf(counter) + out.size();
    }

    public ByteArrayOutputStreamEx getByteArrayOut() {
      return out.getByteArrayOut();
    }
    
    public void writeToIfStarted(PrimitiveAmfOutputStream to) {
      if (stringRegistry.activeWriter == null) {
        assert counter == 0;
        to.writeUInt29(0);
        return;
      }
      
      writeTo(to); 
    }

    public void writeTo(PrimitiveAmfOutputStream to) {
      to.writeUInt29(counter);
      out.writeTo(to);

      finishChange();
    }

    public boolean hasChanges() {
      return counter != 0;
    }
  }
}
