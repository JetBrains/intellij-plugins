package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.abc.AbcFilter;
import com.intellij.flex.uiDesigner.abc.SwfUtil;
import com.intellij.flex.uiDesigner.abc.TagTypes;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.TIntObjectIterator;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

class AbcMerger extends AbcFilter {
  private final Map<CharSequence, Definition> definitionMap;
  private final FileOutputStream out;

  private boolean fileAttributesProcessed;

  private ByteBuffer symbolClassBuffer;
  private int totalNumSymbols;


  public AbcMerger(Map<CharSequence, Definition> definitionMap, @Nullable String flexSdkVersion, File outFile)
    throws IOException {
    super(flexSdkVersion);
    this.definitionMap = definitionMap;

    out = new FileOutputStream(outFile);
    channel = out.getChannel();
    channel.position(PARTIAL_HEADER_LENGTH);
  }

  //public void merge(List<LibrarySetItem> items, Condition<LibrarySetItem> condition) {
  //
  //}

  public void process(Library library) throws IOException {
    final VirtualFile file = library.getSwfFile();
    transcode(file.getInputStream(), file.getLength());
    processTags(null);
  }

  public void end() throws IOException {
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

    writeHeader();

    out.close();
  }

  @Override
  protected void processFileAttributes(int length) throws IOException {
    if (fileAttributesProcessed) {
      skipTag(length);
      fileAttributesProcessed = true;
    }
    else {
      super.processFileAttributes(length);
    }
  }

  @Override
  protected void processEnd(int length) throws IOException {
    skipTag(length);
  }

  @Override
  protected void processShowFrame(int length) throws IOException {
    skipTag(length);
  }

  protected void doAbc2(int length) throws IOException {
    final int off = 4 + transientNameString.length() + 1;
    buffer.position(buffer.position() + off);

    final Definition definition = definitionMap.get(transientNameString);
    definition.doAbcData = createBufferWrapper(length);
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

      int finalSymbolClassTagLength = symbolsClassTagLengthWithoutUselessMainClass;
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

      buffer.position(tagStartPosition - (length < 63 ? 2 : 6));
      buffer.limit(sA);
      symbolClassBuffer.put(buffer);

      buffer.position(sB);
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
