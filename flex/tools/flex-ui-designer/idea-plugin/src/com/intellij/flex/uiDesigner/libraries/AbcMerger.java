package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.abc.*;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.TIntObjectIterator;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;

class AbcMerger extends AbcTranscoder {
  private final Map<CharSequence, Definition> definitionMap;
  private final FileOutputStream out;

  private ByteBuffer symbolClassBuffer;
  private int totalNumSymbols;

  public AbcMerger(Map<CharSequence, Definition> definitionMap, @Nullable String flexSdkVersion, File outFile)
    throws IOException {
    this.definitionMap = definitionMap;

    out = new FileOutputStream(outFile);
    channel = out.getChannel();
    channel.position(SwfUtil.getWrapHeaderLength());
  }

  //public void merge(List<LibrarySetItem> items, Condition<LibrarySetItem> condition) {
  //
  //}

  @Override
  protected void readFrameSizeFrameRateAndFrameCount(byte b) throws IOException {
    super.readFrameSizeFrameRateAndFrameCount(b);
    lastWrittenPosition = buffer.position();
  }

  public void process(Library library) throws IOException {
    final VirtualFile file = library.getSwfFile();
    transcode(file.getInputStream(), file.getLength());
    processTags(null);

    out.flush();
    channel.force(true);
  }

  public void end(List<Decoder> decoders, String flexSdkVersion) throws IOException {
    final Encoder encoder = flexSdkVersion != null ? new FlexEncoder("test", flexSdkVersion) : new Encoder();
    encoder.configure(decoders, null);
    SwfUtil.mergeDoAbc(decoders, encoder);
    encoder.writeDoAbc(channel, true);

    // write symbolClass
    buffer.clear();
    symbolClassBuffer.flip();
    encodeTagHeader(TagTypes.SymbolClass, symbolClassBuffer.limit() + 2 /* numSymbols */);
    buffer.putShort((short)totalNumSymbols);
    buffer.flip();
    channel.write(buffer);
    channel.write(symbolClassBuffer);

    // write footer — ShowFrame and End
    buffer.clear();
    SwfUtil.footer(buffer);
    buffer.flip();
    channel.write(buffer);

    SwfUtil.header(channel, buffer);
    out.flush();
    out.close();
    channel = null;
  }

  @Override
  protected int processTag(int type, int length) throws IOException {
    if (super.processTag(type, length) == 1 || type == TagTypes.FileAttributes || type == TagTypes.ShowFrame) {
      return 1;
    }

    switch (type) {
      case TagTypes.DefineBitsLossless2:
      case TagTypes.DefineBitsLossless:
      case TagTypes.PlaceObject:
      case TagTypes.DefineScalingGrid:
      case TagTypes.DefineBitsJPEG2:
      case TagTypes.DefineBitsJPEG3:
      case TagTypes.DefineBitsJPEG4:
      case TagTypes.DefineBits:

    }

    return 1;
  }

  @Override
  protected void processEnd(int length) throws IOException {
    skipTag(length);
  }

  protected void doAbc2(int length) throws IOException {
    final Definition definition = definitionMap.get(transientNameString);
    definition.doAbcData = createBufferWrapper(length);
  }

  @Override
  protected void storeExportAsset(int id, int start, int end) {

  }

  protected void processSymbolClass(final int length) throws IOException {
    final int tagStartPosition = buffer.position();
    writeDataBeforeTag(length);
    buffer.position(tagStartPosition);
    int numSymbols = analyzeClassAssociatedWithMainTimeline(length);
    final boolean hasClassAssociatedWithMainTimeLine = symbolsClassTagLengthWithoutUselessMainClass != -1;
    if (hasClassAssociatedWithMainTimeLine) {
      final Definition removed = definitionMap.remove(transientNameString);
      assert removed != null;
    }

    final boolean hasExportsAssets = exportAssets != null && !exportAssets.isEmpty();
    if (hasExportsAssets) {
      numSymbols += exportAssets.size();
    }

    if (numSymbols != 0) {
      totalNumSymbols += numSymbols;

      int finalSymbolClassTagLength = hasClassAssociatedWithMainTimeLine ? symbolsClassTagLengthWithoutUselessMainClass : length;
      if (hasExportsAssets) {
        final TIntObjectIterator<TagPositionInfo> iterator = exportAssets.iterator();
        for (int i = exportAssets.size(); i-- > 0; ) {
          iterator.advance();
          finalSymbolClassTagLength += iterator.value().length();
        }
      }

      if (symbolClassBuffer == null) {
        symbolClassBuffer = ByteBuffer.allocate(Math.max(finalSymbolClassTagLength, 8192)).order(ByteOrder.LITTLE_ENDIAN);
      }
      else if (finalSymbolClassTagLength > symbolClassBuffer.remaining()) {
        int newSize = symbolClassBuffer.capacity() + finalSymbolClassTagLength;
        symbolClassBuffer = ByteBuffer.allocate(Math.max(newSize, symbolClassBuffer.capacity() * 2)).put(symbolClassBuffer);
      }

      buffer.position(tagStartPosition);
      if (hasClassAssociatedWithMainTimeLine) {
        buffer.limit(sA);
        symbolClassBuffer.put(buffer);

        buffer.limit(buffer.capacity());
        buffer.position(sB);
      }
      buffer.limit(tagStartPosition + length);
      symbolClassBuffer.put(buffer);
      buffer.limit(buffer.capacity());

      if (hasExportsAssets) {
        final TIntObjectIterator<TagPositionInfo> iterator = exportAssets.iterator();
        for (int i = exportAssets.size(); i-- > 0; ) {
          iterator.advance();
          TagPositionInfo exportAsset = iterator.value();
          buffer.position(exportAsset.start);
          buffer.limit(exportAsset.end);
          symbolClassBuffer.put(buffer);
        }

        exportAssets.clear();
        buffer.limit(buffer.capacity());
      }
    }

    symbolsClassTagLengthWithoutUselessMainClass = -1;

    lastWrittenPosition = tagStartPosition + length;
    buffer.position(lastWrittenPosition);
  }
}
