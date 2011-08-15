package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.TIntArrayList;
import org.jetbrains.annotations.TestOnly;

import java.io.*;

public class EntireMovieTranscoder extends SwfTranscoder {
  // symbolName — utf8 bytes
  @TestOnly
  public void transcode(File in, File out) throws IOException {
    transcode(new FileInputStream(in), in.length(), out, false);
  }

  public void transcode(VirtualFile in, File out) throws IOException {
      transcode(in.getInputStream(), in.getLength(), out, true);
    }

  private void transcode(InputStream inputStream, long inputLength, File outFile, boolean writeBounds) throws IOException {
    final FileOutputStream out = transcode(inputStream, inputLength, outFile);
    try {
      fL
      transcode();
    }
    finally {
      out.flush();
      out.close();
    }
  }

  private void transcode() throws IOException {
    lastWrittenPosition = 0;

    TIntArrayList ignoredBytesPositions = null;
    int ignoredBytesCount ;

    int tagStart;
    analyze: while ((tagStart = buffer.position()) < buffer.limit()) {
      final int tagCodeAndLength = buffer.getShort();
      final int type = tagCodeAndLength >> 6;
      int length = tagCodeAndLength & 0x3F;
      if (length == 63) {
        length = buffer.getInt();
      }

      switch (type) {
        case TagTypes.End:
          break analyze;

        case TagTypes.ExportAssets:
        case TagTypes.SymbolClass:
          if (ignoredBytesPositions == null) {
            ignoredBytesPositions = new TIntArrayList();
          }
          ignoredBytesPositions.add(tagStart);
          final int fullLength = length + (buffer.position() - tagStart);
          ignoredBytesPositions.add(tagStart + fullLength);
          ignoredBytesCount -= fullLength;
      }

      buffer.position(position + length);
    }
  }
}
