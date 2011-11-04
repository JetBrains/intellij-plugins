package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.util.Condition;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;

/**
 * Filter SWF for unresolved definitions. Support only SWF from SWC, i.e. DoABC2 for each script (<DoABC2
 * name='org/flyti/plexus/events/DispatcherEvent'>)
 * Optimized SWF (merged DoABC2) is not supported.
 */
public class AbcFilter extends AbcTranscoder {
  protected final ArrayList<Decoder> decoders = new ArrayList<Decoder>();

  private final boolean onlyAbcAsTag;
  private String inputFileParentName;

  protected TIntObjectHashMap<TagPositionInfo> exportAssets;
  protected int symbolsClassTagLengthWithoutUselessMainClass = -1;
  protected int sA;
  protected int sB;
  private final String flexSdkVersion;

  public AbcFilter(@Nullable String flexSdkVersion) {
    this(flexSdkVersion, false);
  }

  public AbcFilter(@Nullable String flexSdkVersion, boolean onlyAbcAsTag) {
    this.flexSdkVersion = flexSdkVersion;
    this.onlyAbcAsTag = onlyAbcAsTag;
  }

  public void filter(File in, File out, @Nullable Condition<CharSequence> abcNameFilter) throws IOException {
    inputFileParentName = in.getParentFile().getName();
    int index = inputFileParentName.lastIndexOf('.');
    if (index > 0) {
      inputFileParentName = inputFileParentName.substring(0, index);
    }

    filter(new FileInputStream(in), in.length(), out, abcNameFilter);
  }

  //public void filter(VirtualFile in, File out, @Nullable Condition<CharSequence> abcNameFilter) throws IOException {
  //  inputFileParentName = in.getParent().getNameWithoutExtension();
  //  filter(in.getInputStream(), in.getLength(), out, abcNameFilter);
  //
  //  if (exportAssets != null && !exportAssets.isEmpty()) {
  //    exportAssets.clear();
  //  }
  //}

  private void filter(InputStream inputStream, long inputLength, File outFile, @Nullable Condition<CharSequence> abcNameFilter)
    throws IOException {
    final boolean onlyABC = outFile.getPath().endsWith(".abc");
    final FileOutputStream out = transcode(inputStream, inputLength, outFile);
    channel = out.getChannel();
    if (!onlyABC) {
      channel.position(PARTIAL_HEADER_LENGTH);
    }
    try {
      if (!onlyABC) {
        lastWrittenPosition = 0;
        processTags(abcNameFilter);
        writeHeader();
      }
      else {
        filterAbcTags(abcNameFilter);
      }
    }
    finally {
      channel = null;
      out.close();

      decoders.clear();
    }
  }

  protected void writeHeader() throws IOException {
    final int length = (int)channel.position();
    channel.position(0);
    buffer.clear();
    writePartialHeader(length);
    buffer.flip();
    channel.write(buffer);
  }
  
  @Override
  protected int processTag(int type, int length) throws IOException {
    if (type == TagTypes.ShowFrame) {
      processShowFrame(length);
      return -1;
    }

    return super.processTag(type, length);
  }

  private void processShowFrame(int length) throws IOException {
    if (decoders.isEmpty()) {
      buffer.position(buffer.position() + length);
    }
    else {
      final int limit = buffer.position();
      writeDataBeforeTag(length);
      mergeDoAbc(true, false);
      lastWrittenPosition = limit - 2;
      buffer.position(limit + length);
    }
  }

  protected void processEnd(int length) throws IOException {
    buffer.position(lastWrittenPosition);
    channel.write(buffer);
  }

  protected void processFileAttributes(int length) throws IOException {
    buffer.put(buffer.position(), (byte)104); // HasMetadata = false
    buffer.position(buffer.position() + length);
  }

  @Override
  protected void ensureExportAssetsStorageCreated(int numSymbols) {
    if (exportAssets == null) {
      exportAssets = new TIntObjectHashMap<TagPositionInfo>(numSymbols);
    }
    else {
      exportAssets.ensureCapacity(numSymbols);
    }
  }

  @Override
  protected void storeExportAsset(int id, int start, int end) {
    exportAssets.put(id, new TagPositionInfo(start - 2, end));
  }

  @Override
  protected void processSymbolClass(final int length) throws IOException {
    final int tagStartPosition = buffer.position();
    writeDataBeforeTag(length);
    buffer.position(tagStartPosition);
    int numSymbols = analyzeClassAssociatedWithMainTimeline(length);
    final boolean hasClassAssociatedWithMainTimeLine = symbolsClassTagLengthWithoutUselessMainClass != -1;
    mergeDoAbc(true, hasClassAssociatedWithMainTimeLine);

    lastWrittenPosition = tagStartPosition - (length < 63 ? 2 : 6);
    buffer.position(lastWrittenPosition);
    final boolean hasExportsAssets = exportAssets != null && !exportAssets.isEmpty();
    if (hasClassAssociatedWithMainTimeLine || hasExportsAssets) {
      if (hasExportsAssets) {
        numSymbols += exportAssets.size();
      }

      if (numSymbols == 0) {
        lastWrittenPosition = tagStartPosition + length;
      }
      else {
        int finalSymbolClassTagLength = symbolsClassTagLengthWithoutUselessMainClass;
        if (hasExportsAssets) {
          final TIntObjectIterator<TagPositionInfo> iterator = exportAssets.iterator();
          for (int i = exportAssets.size(); i-- > 0; ) {
            iterator.advance();
            finalSymbolClassTagLength += iterator.value().length();
          }
        }

        encodeTagHeader(TagTypes.SymbolClass, finalSymbolClassTagLength);
        buffer.putShort((short)numSymbols);
        buffer.position(lastWrittenPosition);
        buffer.limit(sA);
        channel.write(buffer);
        lastWrittenPosition = sB;
        buffer.limit(buffer.capacity());

        if (hasExportsAssets) {
          final TIntObjectIterator<TagPositionInfo> iterator = exportAssets.iterator();
          for (int i = exportAssets.size(); i-- > 0; ) {
            iterator.advance();
            TagPositionInfo exportAsset = iterator.value();
            buffer.position(exportAsset.start);
            buffer.limit(exportAsset.end);
            channel.write(buffer);
          }

          exportAssets.clear();
          buffer.limit(buffer.capacity());
        }
      }

      symbolsClassTagLengthWithoutUselessMainClass = -1;
    }

    decoders.clear();
    buffer.position(tagStartPosition + length);
  }

  protected int analyzeClassAssociatedWithMainTimeline(final int length) throws IOException {
    int numSymbols = buffer.getShort();
    if (numSymbols == 0) {
      symbolsClassTagLengthWithoutUselessMainClass = -1;
      sA = 0;
      sB = 0;
      return 0;
    }

    for (int i = 0; i < numSymbols; i++) {
      int id = buffer.getShort();
      final int position = buffer.position();
      if (id == 0) {
        readAbcName(position);
        numSymbols--;
        symbolsClassTagLengthWithoutUselessMainClass = length - (transientNameString.length() + 1 + 2);
        sA = position - 2;
        sB = position + transientNameString.length() + 1;
        return numSymbols;
      }
      else {
        if (exportAssets != null && !exportAssets.isEmpty()) {
          exportAssets.remove(id);
        }
        buffer.position(position + skipAbcName(position) + 1);
      }
    }

    return numSymbols;
  }

  protected void doAbc2(int length) throws IOException {
    decoders.add(new Decoder(createBufferWrapper(length), transientNameString.charAt(0) == '_' ? transientNameString.cloneChars() : null));
  }

  protected BufferWrapper createBufferWrapper(int length) {
    final int off = 4 + transientNameString.length() + 1;
    return new BufferWrapper(buffer.array(), buffer.position() + off, length - off);
  }

  private void mergeDoAbc(boolean asTag, boolean hasClassAssociatedWithMainTimeLine) throws IOException {
    final Encoder encoder = flexSdkVersion != null ? new FlexEncoder(inputFileParentName, flexSdkVersion) : new Encoder();
    encoder.configure(decoders, hasClassAssociatedWithMainTimeLine ? transientNameString : null);
    SwfUtil.mergeDoAbc(decoders, encoder);
    encoder.writeDoAbc(channel, asTag);
  }

  protected void skipTag(int tagLength) throws IOException {
    writeDataBeforeTag(tagLength);

    buffer.position(lastWrittenPosition);
  }

  private void filterAbcTags(Condition<CharSequence> abcNameFilter) throws IOException {
    while (true) {
      int tagCodeAndLength = buffer.getShort();
      int type = tagCodeAndLength >> 6;
      int length = tagCodeAndLength & 0x3F;
      if (length == 63) {
        length = buffer.getInt();
      }

      switch (type) {
        case TagTypes.End:
          mergeDoAbc(onlyAbcAsTag, false);
          return;

        case TagTypes.DoABC2:
          readAbcName(buffer.position() + 4);
          if (abcNameFilter.value(transientNameString)) {
            decoders.add(new Decoder(createBufferWrapper(length)));
            continue;
          }

        default:
          buffer.position(buffer.position() + length);
      }
    }
  }

  private void readAbcName(final int start) {
    int end = start;
    byte[] array = buffer.array();
    int lastSlashPosition = -1;
    byte c;
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    final char[] chars = transientNameString.chars;
    int index = 0;
    while ((c = array[end++]) != 0) {
      switch (c) {
        case '/':
          lastSlashPosition = index;
          chars[index] = '.';
          break;

        default:
          chars[index] = (char)c;
      }

      index++;
    }

    if (lastSlashPosition != -1) {
      chars[lastSlashPosition] = ':';
    }

    transientNameString.hash = 0;
    transientNameString.length = index;
  }
}