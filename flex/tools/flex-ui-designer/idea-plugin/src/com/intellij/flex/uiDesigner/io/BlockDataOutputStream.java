package com.intellij.flex.uiDesigner.io;

import com.intellij.openapi.application.ApplicationManager;
import gnu.trove.TLinkable;
import gnu.trove.TLinkedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.channels.WritableByteChannel;

public class BlockDataOutputStream extends AbstractByteArrayOutputStream implements WritableByteChannel {
  private static final int SERVICE_DATA_SIZE = 8;

  private int lastBlockBegin;
  private OutputStream out;
  private final TLinkedList<Marker> markers = new TLinkedList<Marker>();

  private int messageCounter;

  public BlockDataOutputStream() {
    this(64 * 1024);
  }

  public BlockDataOutputStream(int size) {
    super(size);
    count = SERVICE_DATA_SIZE;
  }

  @SuppressWarnings("ConstantConditions")
  public void setOut(@NotNull OutputStream out) {
    String debugFilename = System.getProperty("fud.socket.dump");
    DebugOutput debugOut;
    if (debugFilename != null) {
      try {
        debugOut = new DebugOutput(out, new File(debugFilename));
      }
      catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }

      this.out = debugOut;
    }
    else if (true || ApplicationManager.getApplication().isUnitTestMode()) {
      this.out = new AuditorOutput(out);
    }
    else {
      this.out = out;
    }
  }

  public void reset() {
    count = SERVICE_DATA_SIZE;
    lastBlockBegin = 0;
    markers.clear();
    out = null;
  }

  private void writeHeader() {
    IOUtil.writeInt(count - lastBlockBegin - SERVICE_DATA_SIZE, buffer, lastBlockBegin);
    IOUtil.writeInt(messageCounter++, buffer, lastBlockBegin + 4);
  }

  private void flushBuffer() throws IOException {
    if (markers.isEmpty()) {
      out.write(buffer, 0, count);
    }
    else {
      writeMarkered();
    }

    lastBlockBegin = 0;
    count = SERVICE_DATA_SIZE;
  }

  public void assertStart() {
    assert count - lastBlockBegin == SERVICE_DATA_SIZE;
  }

  public void rollback() {
    count = lastBlockBegin + SERVICE_DATA_SIZE;
    if (!markers.isEmpty()) {
      markers.clear();
    }
  }

  public void end() throws IOException {
    if ((count - lastBlockBegin) == SERVICE_DATA_SIZE) {
      return;
    }

    writeHeader();
    if (!markers.isEmpty() || (count >= 8192 && out != null)) {
      flushBuffer();
    }
    else {
      lastBlockBegin = count;
      count += SERVICE_DATA_SIZE;
    }
  }

  private void writeMarkered() throws IOException {
    int lastEnd = 0;
    AuditorOutput auditorOutput = null;
    if (out instanceof AuditorOutput) {
      auditorOutput = (AuditorOutput)out;
      auditorOutput.written = 0;
    }

    Marker marker = markers.getFirst();
    do {
      int length = marker.getStart() - lastEnd;
      // may be < 0 if nested
      if (length >= 0) {
        if (length > 0) {
          out.write(buffer, lastEnd, length);
        }
        lastEnd = marker.getEnd();
      }

      if (marker instanceof ByteRangePointer) {
        writeDataRange(((ByteRangePointer)marker).getDataRange());
      }
    }
    while ((marker = (Marker)marker.getNext()) != null);

    int tailLength = count - lastEnd;
    if (tailLength > 0) {
      out.write(buffer, lastEnd, tailLength);
    }

    if (auditorOutput != null) {
      //assert auditorOutput.written == (count - (lastBlockBegin + SERVICE_DATA_SIZE));
      auditorOutput.written = -1;
    }

    markers.clear();
  }

  private void writeDataRange(ByteRange dataRange) throws IOException {
    ByteRange possibleChild = dataRange;
    int start = dataRange.getStart();
    final int ownEnd = dataRange.getEnd();
    while (true) {
      TLinkable next = possibleChild.getNext();
      while (next != null && !(next instanceof ByteRange)) {
        next = next.getNext();
      }

      possibleChild = (ByteRange)next;
      if (possibleChild == null || possibleChild.getEnd() > ownEnd) {
        out.write(buffer, start, ownEnd - start);
        break;
      }
      else {
        int length = possibleChild.getStart() - start;
        if (length > -1) {
          if (length != 0) {
            out.write(buffer, start, length);
          }
          start = possibleChild.getEnd();
        }
      }
    }
  }

  public static int getDataRangeOwnLength(ByteRange dataRange) {
    if (dataRange.getOwnLength() != -1) {
      return dataRange.getOwnLength();
    }

    Marker possibleChild = dataRange;
    int start = dataRange.getStart();
    final int ownEnd = dataRange.getEnd();
    int ownLength = 0;

    while (true) {
      possibleChild = (Marker)possibleChild.getNext();
      if (possibleChild == null || possibleChild.getEnd() > ownEnd) {
        ownLength += ownEnd - start;
        break;
      }
      else {
        int length = possibleChild.getStart() - start;
        if (length > -1) {
          ownLength += length;
          start = possibleChild.getEnd();
        }
      }
    }

    dataRange.setOwnLength(ownLength);
    return ownLength;
  }

  @Override
  public void flush() throws IOException {
    if (out == null) {
      end();
      return;
    }
    
    if (lastBlockBegin != (count - SERVICE_DATA_SIZE)) {
      writeHeader();
      flushBuffer();
    }

    out.flush();
  }

  @Override
  public boolean isOpen() {
    return out != null;
  }

  @Override
  public void close() throws IOException {
    if (out != null) {
      out.close();
    }
  }

  @Nullable
  public Marker getLastMarker() {
    return markers.getLast();
  }

  public ByteRange startRange() {
    return startRange(count);
  }

  public ByteRange startRange(int start) {
    ByteRange byteRange = new ByteRange(start);
    markers.addLast(byteRange);
    return byteRange;
  }

  public ByteRange startRange(int start, @Nullable Marker after) {
    ByteRange byteRange = new ByteRange(start);
    if (after == null) {
      markers.addFirst(byteRange);
    }
    else {
      markers.addBefore((Marker)after.getNext(), byteRange);
    }
    return byteRange;
  }

  public void endRange(ByteRange range) {
    range.setEnd(count);
  }

  public void removeLastMarkerAndAssert(ByteRange dataRange) {
    Marker removed = markers.removeLast();
    assert removed == dataRange;
  }

  public void insert(final int position, final ByteRange dataRange) {
    markers.addFirst(new ByteRangePointer(position, dataRange));
  }

  public void append(ByteRange dataRange) {
    markers.addLast(new ByteRangePointer(count, dataRange));
  }

  private static class DebugOutput extends AuditorOutput {
    private final FileOutputStream fileOut;

    private DebugOutput(OutputStream out, File file) throws FileNotFoundException {
      super(out);
      fileOut = new FileOutputStream(file);
    }

    @Override
    public void write(int b) throws IOException {
      fileOut.write(b);
      super.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
      fileOut.write(b);
      super.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      fileOut.write(b, off, len);
      super.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
      fileOut.flush();
      super.flush();
    }

    @Override
    public void close() throws IOException {
      fileOut.close();
      super.close();
    }
  }
}