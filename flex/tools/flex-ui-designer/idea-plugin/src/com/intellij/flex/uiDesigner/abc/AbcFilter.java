package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.text.CharArrayUtil;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import gnu.trove.TObjectHashingStrategy;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Filter SWF for unresolved definitions. Support only SWF from SWC, i.e. DoABC2 for each script (<DoABC2 
 name='org/flyti/plexus/events/DispatcherEvent'>)
 * Optimized SWF (merged DoABC2) is not supported.
 */
public class AbcFilter extends SwfTranscoder {
  public static final TObjectHashingStrategy<CharSequence> HASHING_STRATEGY = new HashingStrategy();

  protected FileChannel channel;
  protected final TransientString transientNameString = new TransientString();
  protected int lastWrittenPosition;
  
  protected final ArrayList<Decoder> decoders = new ArrayList<Decoder>();

  private final String flexSdkVersion;
  private final boolean onlyAbcAsTag;
  private String inputFileParentName;

  protected TIntObjectHashMap<TagPositionInfo> exportAssets;
  protected int symbolsClassTagLengthWithoutUselessMainClass = -1;
  protected int sA;
  protected int sB;

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

  public void filter(VirtualFile in, File out, @Nullable Condition<CharSequence> abcNameFilter) throws IOException {
    inputFileParentName = in.getParent().getNameWithoutExtension();
    filter(in.getInputStream(), in.getLength(), out, abcNameFilter);

    if (exportAssets != null && !exportAssets.isEmpty()) {
      exportAssets.clear();
    }
  }

  private void filter(InputStream inputStream, long inputLength, File outFile, @Nullable Condition<CharSequence> abcNameFilter) throws IOException {
    final boolean onlyABC = outFile.getPath().endsWith(".abc");
    final FileOutputStream out = transcode(inputStream, inputLength, outFile);
    channel = out.getChannel();
    if (!onlyABC) {
      channel.position(PARTIAL_HEADER_LENGTH);
    }
    try {
      if (!onlyABC) {
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

  protected void processTags(@Nullable final Condition<CharSequence> abcNameFilter) throws IOException {
    lastWrittenPosition = 0;

    while (buffer.position() < buffer.limit()) {
      final int tagCodeAndLength = buffer.getShort();
      final int type = tagCodeAndLength >> 6;
      int length = tagCodeAndLength & 0x3F;
      if (length == 63) {
        length = buffer.getInt();
      }

      switch (type) {
        case TagTypes.End:
          processEnd(length);
          return;

        case TagTypes.ShowFrame:
          processShowFrame(length);
          continue;

        case TagTypes.SymbolClass:
          processSymbolClass(length);
          continue;

        case TagTypes.ExportAssets:
          processExportAssets(length);
          continue;

        case TagTypes.FileAttributes:
          processFileAttributes(length);
          continue;

        case TagTypes.EnableDebugger:
        case TagTypes.EnableDebugger2:
        case TagTypes.SetBackgroundColor:
        case TagTypes.ProductInfo:
        case TagTypes.DebugID:
        case TagTypes.ScriptLimits:
        case TagTypes.Metadata:
          skipTag(length);
          continue;

        case TagTypes.DoABC2:
          readAbcName(buffer.position() + 4);
          if (abcNameFilter != null && !abcNameFilter.value(transientNameString)) {
            skipTag(length);
          }
          else {
            int oldPosition = buffer.position();
            writeDataBeforeTag(length);
            buffer.position(oldPosition);
            doAbc2(length);
            buffer.position(lastWrittenPosition);
          }
          continue;
      }
    }
  }

  protected void processShowFrame(int length) throws IOException {
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

  private void processExportAssets(int length) throws IOException {
    final int tagStartPosition = buffer.position();
    writeDataBeforeTag(length);
    buffer.position(tagStartPosition);

    final int numSymbols = buffer.getShort();
    if (numSymbols == 0) {
      return;
    }

    if (exportAssets == null) {
      exportAssets = new TIntObjectHashMap<TagPositionInfo>(numSymbols);
    }
    else {
      exportAssets.ensureCapacity(numSymbols);
    }

    for (int i = 0; i < numSymbols; i++) {
      int id = buffer.getShort();
      int start = buffer.position();
      int end = start + skipAbcName(start) + 1;
      if (id != 0) {
        exportAssets.put(id, new TagPositionInfo(start - 2, end));
      }
      buffer.position(end);
    }
  }
  
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

  protected boolean writeDataBeforeTag(int tagLength) throws IOException {
    int tagHeaderLength = tagLength < 63 ? 2 : 6;
    int newLimit = buffer.position() - tagHeaderLength;
    if (newLimit == lastWrittenPosition) {
      lastWrittenPosition += tagLength + tagHeaderLength;
      return false;
    }

    buffer.limit(newLimit);
    buffer.position(lastWrittenPosition);
    channel.write(buffer);

    lastWrittenPosition = buffer.limit() + tagLength + tagHeaderLength;
    buffer.limit(buffer.capacity());

    return true;
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

  protected static final class TransientString implements CharSequence {
    private final char[] chars = new char[256];
    private int length;
    private int hash;

    @Override
    public int length() {
      return length;
    }

    @Override
    public char charAt(int index) {
      return chars[index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof CharSequence) {
        return StringUtil.equals(this, (CharSequence)obj);
      }
      else {
        return super.equals(obj);
      }
    }

    public char[] cloneChars() {
      char[] clonedChars = new char[length];
      System.arraycopy(chars, 0, clonedChars, 0, length);
      return clonedChars;
    }

    public boolean same(char[] name) {
      return CharArrayUtil.equals(chars, 0, chars.length, name, 0, name.length);
    }

    @Override
    public String toString() {
      return new String(chars, 0, length);
    }

    @Override
    public int hashCode() {
      if (hash == 0 && length > 0) {
        hash = StringUtil.stringHashCode(chars, 0, length);
      }
      return hash;
    }
  }

  private static final class HashingStrategy implements TObjectHashingStrategy<CharSequence> {
    @Override
    public int computeHashCode(CharSequence object) {
      return object.hashCode();
    }

    @Override
    public boolean equals(CharSequence o1, CharSequence o2) {
      return o2.equals(o1); // must be o2.equals(o1) because o1 is String (cannot equals) and o2 is TransientString
    }
  }
}
