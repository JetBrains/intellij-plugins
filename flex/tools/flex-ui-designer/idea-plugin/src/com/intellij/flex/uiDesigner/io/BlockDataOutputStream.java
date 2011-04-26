package com.intellij.flex.uiDesigner.io;

import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BlockDataOutputStream extends AbstractByteArrayOutputStream {
  private static final int SERVICE_DATA_SIZE = 4;

  private int lastBlockBegin;
  private OutputStream out;
  private final List<Marker> markers = new ArrayList<Marker>();

  public void setOut(@NotNull OutputStream out) {
    String debugFilename = System.getProperty("fud.socket.dump");
    DebugOutput debugOut;
    if (debugFilename != null) {
      File debugFile = new File(debugFilename);
      try {
        debugOut = new DebugOutput(out, debugFile);
      }
      catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }

      this.out = debugOut;
    }
    else if (ApplicationManager.getApplication().isUnitTestMode()) {
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
    this.out = null;
  }

  public BlockDataOutputStream() {
    this(64 * 1024);
  }

  public BlockDataOutputStream(int size) {
    super(size);
    count = SERVICE_DATA_SIZE;
  }
  
  public OutputStream writeUnbufferedHeader(int size) throws IOException {
    out.write((size >>> 24) & 0xFF);
    out.write((size >>> 16) & 0xFF);
    out.write((size >>> 8) & 0xFF);
    out.write(size & 0xFF);
    
    return out;
  }

  private void writeHeader() {
    int length = count - lastBlockBegin - SERVICE_DATA_SIZE;
    IOUtil.writeInt(length, buffer, lastBlockBegin);
  }

  private void flushBuffer() throws IOException {
    out.write(buffer, 0, count);
    lastBlockBegin = 0;
    count = SERVICE_DATA_SIZE;
  }

  public void assertStart() {
    assert count - lastBlockBegin == 4;
  }

  // WARNING: you can't call flush after this, you must or end, or flush.
  public void end() throws IOException {
    if ((count - lastBlockBegin) == SERVICE_DATA_SIZE) {
      return;
    }

    writeHeader();
    if (count >= 8192 && out != null) {
      flushBuffer();
    }
    else {
      lastBlockBegin = count;
      count += SERVICE_DATA_SIZE;
    }
  }

  public void beginWritePrepended(int additionalSize, int insertPosition) throws IOException {
    count += additionalSize;
    writeHeader();
    count -= additionalSize;
    out.write(buffer, 0, insertPosition);
  }

  public void writePrepended(int counter, ByteArrayOutputStreamEx byteArrayOutput) throws IOException {
    writePrepended(counter);
    if (counter > 0) {
      byteArrayOutput.writeTo(out);
    }
  }

  public void writePrepended(int counter) throws IOException {
    if (counter < 0x80) {
      out.write(counter);
    }
    else if (counter < 0x4000) {
      out.write(((counter >> 7) & 0x7F) | 0x80);
      out.write(counter & 0x7F);
    }
    else {
      throw new IllegalArgumentException("Integer out of range: " + counter);
    }
  }

  public void endWritePrepended(int insertPosition) throws IOException {
    int lastEnd = insertPosition;
    if (markers.isEmpty()) {
      out.write(buffer, insertPosition, count - insertPosition);
    }
    else {
      AuditorOutput auditorOutput = null;
      if (out instanceof AuditorOutput) {
        auditorOutput = (AuditorOutput)out;
        auditorOutput.watchCount = 0;
      }

      for (Marker marker : markers) {
        if (marker.getEnd() < lastEnd) {
          // nested
          continue;
        }

        int length = marker.getStart() - lastEnd;
        if (length > 0) {
          out.write(buffer, lastEnd, length);
        }

        if (marker instanceof ByteRangeMarker) {
          writeDataRange(((ByteRangeMarker)marker).getDataRange());
        }

        lastEnd = marker.getEnd();
      }

      markers.clear();
      int tailLength = count - lastEnd;
      if (tailLength > 0) {
        out.write(buffer, lastEnd, tailLength);
      }

      if (auditorOutput != null) {
        assert auditorOutput.watchCount == (count - insertPosition);
        auditorOutput.watchCount = -1;
      }
    }

    lastBlockBegin = 0;
    count = SERVICE_DATA_SIZE;
  }

  private void writeDataRange(ByteRange dataRange) throws IOException {
    int childIndex = dataRange.getIndex() + 1;
    int start = dataRange.getStart();
    final int ownEnd = dataRange.getEnd();
    while (true) {
      Marker possibleChild = markers.get(childIndex++);
      int childEnd = possibleChild.getEnd();
      if (childEnd < ownEnd) {
        int length = possibleChild.getStart() - start;
        if (length > -1) {
          if (length != 0) {
            out.write(buffer, start, length);
          }
          start = childEnd;
        }
      }
      else {
        out.write(buffer, start, ownEnd - start);
        break;
      }
    }
  }

  public int getDataRangeOwnLength(ByteRange dataRange) {
    if (dataRange.getOwnLength() != -1) {
      return dataRange.getOwnLength();
    }

    int childIndex = dataRange.getIndex() + 1;
    int start = dataRange.getStart();
    final int ownEnd = dataRange.getEnd();
    int ownLength = 0;

    while (true) {
      // this check is not needed for writeDataRange, because writeDataRange call only after build message – opposite to 
      // getDataRangeOwnLength
      if (childIndex == markers.size()) {
        ownLength += ownEnd - start;
        break;
      }

      Marker possibleChild = markers.get(childIndex++);
      int childEnd = possibleChild.getEnd();
      if (childEnd < ownEnd) {
        int length = possibleChild.getStart() - start;
        if (length > -1) {
          ownLength += length;
          start = childEnd;
        }
      }
      else {
        ownLength += ownEnd - start;
        break;
      }
    }

    dataRange.setOwnLength(ownLength);
    return ownLength;
  }

  @Override
  public void flush() throws IOException {
    if (lastBlockBegin != (count - SERVICE_DATA_SIZE)) {
      writeHeader();
      flushBuffer();
    }

    out.flush();
  }

  @Override
  public void close() throws IOException {
    if (out != null) {
      out.close();
    }
  }

  public ByteRange startRange() {
    return startRange(count);
  }

  public int getNextMarkerIndex() {
    return markers.size();
  }

  public ByteRange startRange(int start) {
    ByteRange byteRange = new ByteRange(start, markers.size());
    markers.add(byteRange);
    return byteRange;
  }

  public ByteRange startRange(int start, int index) {
    ByteRange byteRange = new ByteRange(start, index);
    markers.add(index, byteRange);
    return byteRange;
  }

  public void removeLastMarkerAndAssert(ByteRange dataRange) {
    Marker removed = markers.remove(markers.size() - 1);
    assert removed == dataRange;
  }

  public void endRange(ByteRange range) {
    range.setEnd(count);
  }

  public void addMarker(Marker marker) {
    markers.add(marker);
  }

  public void setPosition(int position) {
    count = position;
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

  private static class AuditorOutput extends OutputStream {
    private final OutputStream out;
    private int watchCount = -1;

    private AuditorOutput(OutputStream out) {
      this.out = out;
    }

    @Override
    public void write(int b) throws IOException {
      if (watchCount != -1) {
        watchCount++;
      }

      out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
      if (watchCount != -1) {
        watchCount += b.length;
      }

      out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      if (watchCount != -1) {
        watchCount += len;
      }

      out.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
      out.flush();
    }

    @Override
    public void close() throws IOException {
      out.close();
    }
  }
}