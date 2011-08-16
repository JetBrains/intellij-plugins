package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.TIntArrayList;
import org.jetbrains.annotations.TestOnly;

import java.io.*;

public class EntireMovieTranscoder extends MovieTranscoder {
  // Sprite ID and FrameCount
  private static final int SPRITE_TAG_LENGTH_EXCEPT_CONTROL_TAGS = 4;
  // we hope, this id is not used in transcoded swf
  private static final int DOCUMENT_SPRITE_ID = 65532;

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

  @Override
  protected void transcode(boolean writeBounds) throws IOException {
    if (writeBounds) {
      writeMovieBounds();
    }

    TIntArrayList ignoredBytesPositions = null;
    // write movie as 1-frame, frameCount = 1
    buffer.putShort(swfHeaderEnd - 2, (short)1);

    int initialStartPosition = swfHeaderEnd;
    int fileAttributesFullLength = 0;
    int tagStart;
    int fileLength = buffer.capacity();
    analyze:
    while ((tagStart = buffer.position()) < buffer.limit()) {
      final int tagCodeAndLength = buffer.getShort();
      final int type = tagCodeAndLength >> 6;
      int length = tagCodeAndLength & 0x3F;
      if (length == 63) {
        length = buffer.getInt();
      }

      switch (type) {
        case TagTypes.End:
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
      }

      buffer.position(buffer.position() + length);
    }

    final int spriteTagLength = (fileLength - initialStartPosition) + SPRITE_TAG_LENGTH_EXCEPT_CONTROL_TAGS;
    final byte[] symbolOwnClassAbc = getSymbolOwnClassAbc(frameCount);
    fileLength += PARTIAL_HEADER_LENGTH + symbolOwnClassAbc.length + recordHeaderLength(spriteTagLength) +
                  SPRITE_TAG_LENGTH_EXCEPT_CONTROL_TAGS + SwfUtil.getWrapFooterLength() + SYMBOL_CLASS_TAG_FULL_LENGTH;

    writePartialHeader(out, fileLength);
    out.write(data, 0, initialStartPosition);

    buffer.position(0);
    encodeTagHeader(TagTypes.DefineSprite, spriteTagLength);
    buffer.putShort((short)DOCUMENT_SPRITE_ID);
    buffer.putShort(frameCount);
    out.write(data, 0, buffer.position());

    if (ignoredBytesPositions == null) {
      out.write(data, initialStartPosition, data.length - initialStartPosition);
    }
    else {
      writeSparceBytes(ignoredBytesPositions, initialStartPosition, data.length);
    }

    out.write(symbolOwnClassAbc);
    writeSymbolClass(DOCUMENT_SPRITE_ID);
    SwfUtil.footer(out);
  }
}