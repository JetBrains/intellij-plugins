package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.util.io.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

abstract class SwfTranscoder extends AbcEncoder {
  private static final ThreadLocal<Inflater> INFLATER = new ThreadLocal<Inflater>() {
    @Override
    protected Inflater initialValue() {
      return new Inflater();
    }
  };

  protected static final int PARTIAL_HEADER_LENGTH = 8;

  private final byte[] partialHeader = new byte[PARTIAL_HEADER_LENGTH];

  protected int lastWrittenPosition;

  protected FileOutputStream transcode(InputStream inputStream, long inputLength, File outFile) throws IOException {
    final int uncompressedBodyLength;
    final boolean compressed;
    byte[] data;
    try {
      int n = inputStream.read(partialHeader);
      assert n == PARTIAL_HEADER_LENGTH;
      uncompressedBodyLength = (partialHeader[4] & 0xFF | (partialHeader[5] & 0xFF) << 8 |
                                (partialHeader[6] & 0xFF) << 16 | partialHeader[7] << 24) - PARTIAL_HEADER_LENGTH;
      compressed = partialHeader[0] == 0x43;
      data = FileUtil.loadBytes(inputStream, compressed ? ((int)inputLength - PARTIAL_HEADER_LENGTH) : uncompressedBodyLength);
    }
    finally {
      inputStream.close();
    }

    if (compressed) {
      final Inflater inflater = INFLATER.get();
      try {
        inflater.setInput(data);
        byte[] uncomressedData = new byte[uncompressedBodyLength];
        try {
          inflater.inflate(uncomressedData);
        }
        catch (DataFormatException e) {
          throw new ZipException(e.getMessage() != null ? e.getMessage() : "Invalid ZLIB data format");
        }
        data = uncomressedData;
      }
      finally {
        inflater.reset();
      }
    }

    buffer = ByteBuffer.wrap(data);
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    // skip FrameSize, FrameRate, FrameCount
    buffer.position((int)Math.ceil((float)(5 + ((data[0] & 0xFF) >> -(5 - 8)) * 4) / 8) + 2 + 2);

    return new FileOutputStream(outFile);
  }

  protected void writePartialHeader(int fileLength) {
    partialHeader[0] = 0x46; // write as uncompressed
    buffer.put(partialHeader, 0, 4);
    buffer.putInt(fileLength);
  }

  protected static class TagPositionInfo {
    public final int start;
    public final int end;

    protected TagPositionInfo(int start, int end) {
      this.start = start;
      this.end = end;
    }
  }


  protected int skipAbcName(final int start) {
    int end = start;
    byte[] array = buffer.array();
    while (array[++end] != 0) {
    }

    return end - start;
  }
}