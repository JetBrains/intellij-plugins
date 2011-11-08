package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.util.Condition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AbcExtractor extends AbcTranscoder {
  public List<BufferWrapper> extract(File in, Condition<CharSequence> abcNameFilter) throws IOException {
    List<BufferWrapper> list = new ArrayList<BufferWrapper>();
    transcode(new FileInputStream(in), in.length());
    extract(abcNameFilter, list);
    return list;
  }

  private void extract(Condition<CharSequence> abcNameFilter, List<BufferWrapper> list) throws IOException {
    while (true) {
      int tagCodeAndLength = buffer.getShort();
      int type = tagCodeAndLength >> 6;
      int length = tagCodeAndLength & 0x3F;
      if (length == 63) {
        length = buffer.getInt();
      }

      switch (type) {
        case TagTypes.End:
          return;

        case TagTypes.DoABC2:
          readAbcName(buffer.position() + 4);
          if (abcNameFilter.value(transientNameString)) {
            list.add(createBufferWrapper(length));
          }

        default:
          buffer.position(buffer.position() + length);
      }
    }
  }

  @Override
  protected void processSymbolClass(int length) throws IOException {
  }

  @Override
  protected void processEnd(int length) throws IOException {
  }

  @Override
  protected void doAbc2(int length) throws IOException {
  }

  @Override
  protected void ensureExportAssetsStorageCreated(int numSymbols) {
  }

  @Override
  protected void storeExportAsset(int id, int start, int end, boolean mainNameRead) {
  }
}
