package com.intellij.flex.uiDesigner.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BlockDataOutputStream extends AbstractByteArrayOutputStream {
  private static final int SERVICE_DATA_SIZE = 4;

  private int lastBlockBegin;

  private final DebugOutput debugOut;
  private OutputStream out;
  
  private final List<Marker> markers = new ArrayList<Marker>();

  public BlockDataOutputStream(@NotNull OutputStream out) {
    this(out, 64 * 1024);
  }

  public BlockDataOutputStream(@NotNull OutputStream out, int size) {
    super(size);
    count = SERVICE_DATA_SIZE;
    
    String debugFilename = System.getProperty("fud.socket.dump");
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
    else {
      debugOut = null;
      this.out = out;
    }
  }

  private void writeHeader() throws IOException {
    if (debugOut != null) {
      debugOut.reset();
    }
    
    int length = count - lastBlockBegin - SERVICE_DATA_SIZE;
    buffer[lastBlockBegin] = (byte) ((length >>> 24) & 0xFF);
    buffer[lastBlockBegin + 1] = (byte) ((length >>> 16) & 0xFF);
    buffer[lastBlockBegin + 2] = (byte) ((length >>> 8) & 0xFF);
    buffer[lastBlockBegin + 3] = (byte) (length & 0xFF);
  }

  private void flushBuffer() throws IOException {
    out.write(buffer, 0, count);
    lastBlockBegin = 0;
    count = SERVICE_DATA_SIZE;
  }

  // WARNING: you can't call flush after this, you must or end, or flush.
  public void end() throws IOException {
    writeHeader();
    if (count >= 8192) {
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
  
  public void writePrepended(byte[] additionalData) throws IOException {
    out.write(additionalData);
  }

  public void endWritePrepended(int insertPosition) throws IOException {
    int lastEnd = insertPosition;
    if (markers.isEmpty()) {
      out.write(buffer, insertPosition, count - insertPosition);
    }
    else {
      for (Marker marker : markers) {
        if (marker.getEnd() < lastEnd) {
          // nested
          continue;
        }

        int length = marker.getStart() - lastEnd;
        if (length > 0) {
          out.write(buffer, lastEnd, length);
        }

        ByteRange dataRange = marker.getDataRange();
        if (dataRange != null) {
          writeDataRange(dataRange);
        }
        
        lastEnd = marker.getEnd();
      }

      markers.clear();
      int tailLength = count - lastEnd;
      if (tailLength > 0) {
        out.write(buffer, lastEnd, tailLength);
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
      // this check is not needed for writeDataRange, because writeDataRange call only after build message – opposite to getDataRangeOwnLength
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
    out.close();
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
  
  public void endRange(ByteRange range) {
    range.setEnd(count);
  }
  
  public void addMarker(Marker marker) {
    markers.add(marker);
  }

  public void setPosition(int position) {
    count = position;
  }

  private static class DebugOutput extends OutputStream {
    private final OutputStream out;
    private FileOutputStream fileOut;
    private File file;

    private DebugOutput(OutputStream out, File file) throws FileNotFoundException {
      this.file = file;
      this.out = out;
    }

    @Override
    public void write(int b) throws IOException {
      fileOut.write(b);
      out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
      fileOut.write(b);
      out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      fileOut.write(b, off, len);
      out.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
      out.flush();
      if (fileOut != null) {
        fileOut.flush();
      }
    }

    @Override
    public void close() throws IOException {
      if (fileOut != null) {
        fileOut.close();
      }
      
      out.close();
    }

    public void reset() throws IOException {
      if (fileOut != null) {
        fileOut.close();
      }

      fileOut = new FileOutputStream(file);
    }
  }
}