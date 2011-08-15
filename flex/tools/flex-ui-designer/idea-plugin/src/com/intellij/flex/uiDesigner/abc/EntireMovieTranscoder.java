package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.TIntArrayList;
import org.jetbrains.annotations.TestOnly;

import java.io.*;

public class EntireMovieTranscoder extends MovieTranscoder {
  private short frameCount;
  private int swfHeaderEnd;

  @TestOnly
  public void transcode(File in, File out) throws IOException {
    transcode(new FileInputStream(in), in.length(), out, false);
  }

  public void transcode(VirtualFile in, File out) throws IOException {
    transcode(in.getInputStream(), in.getLength(), out, true);
  }

  @Override
  protected void readFrameSizeFrameRateAndFrameCount(byte b) throws IOException {
    decodeRect();
    buffer.position(buffer.position() + 2);
    frameCount = buffer.getShort();
    swfHeaderEnd = buffer.position();
  }

  private void transcode(InputStream inputStream, long inputLength, File outFile, boolean writeBounds) throws IOException {
    final FileOutputStream out = transcode(inputStream, inputLength, outFile);
    try {
      transcode(out);
    }
    finally {
      out.flush();
      out.close();
    }
  }

  private void transcode(FileOutputStream out) throws IOException {
    TIntArrayList ignoredBytesPositions = null;

    // write movie as 1-frame, frameCount = 1
    buffer.putShort(swfHeaderEnd - 2, (short)1);

    int initialStartPosition = swfHeaderEnd;
    int fileAttributesFullLength = 0;
    int tagStart;
    int f = 0;
    int fileLength = buffer.capacity();
    analyze: while ((tagStart = buffer.position()) < buffer.limit()) {
      final int tagCodeAndLength = buffer.getShort();
      final int type = tagCodeAndLength >> 6;
      int length = tagCodeAndLength & 0x3F;
      if (length == 63) {
        length = buffer.getInt();
      }



      switch (type) {
        case TagTypes.End:
          f += length + (buffer.position() - tagStart);
          break analyze;

        case TagTypes.FileAttributes:
          fileAttributesFullLength = length + (buffer.position() - tagStart);
          initialStartPosition = tagStart + fileAttributesFullLength;
          break;

        case TagTypes.Metadata:
        case TagTypes.DebugID:
        case TagTypes.EnableDebugger:
        case TagTypes.EnableDebugger2:
        case TagTypes.ScriptLimits:
        case TagTypes.ProductInfo:
        case TagTypes.ExportAssets:
        case TagTypes.SymbolClass:
          if (ignoredBytesPositions == null) {
            ignoredBytesPositions = new TIntArrayList();
          }
          ignoredBytesPositions.add(tagStart);
          final int fullLength = length + (buffer.position() - tagStart);
          ignoredBytesPositions.add(tagStart + fullLength);
          fileLength -= fullLength;
          break;

        default:
          f += length + (buffer.position() - tagStart);
      }

      buffer.position(buffer.position() + length);
    }

    final int spriteTagLength = (fileLength - initialStartPosition) + 4;
    fileLength += PARTIAL_HEADER_LENGTH + (spriteTagLength >= 63 ? 6 : 2) + 4 + 4 /* swf end */;

    writePartialHeader(out, fileLength);

    final byte[] data = buffer.array();
    out.write(data, 0, initialStartPosition);

    buffer.position(0);

    encodeLongTagHeader(TagTypes.DefineSprite, spriteTagLength /* Sprite ID and FrameCount */);
    buffer.putShort((short)65532);
    buffer.putShort(frameCount);
    out.write(data, 0, buffer.position());

    out.flush();

    if (ignoredBytesPositions == null) {
      throw new IllegalStateException();
    }
    else {
      final int maxI = ignoredBytesPositions.size() - 1;
      int prevOffset = initialStartPosition;
      int i = 0;
      while (true) {
        if (i >= maxI) {
          out.write(data, prevOffset, data.length - prevOffset);
          break;
        }
        else {
          out.write(data, prevOffset, ignoredBytesPositions.getQuick(i++) - prevOffset);
          prevOffset = ignoredBytesPositions.getQuick(i++);
        }
      }
    }

    SwfUtil.footer(out);
  }
}