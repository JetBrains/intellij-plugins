package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import gnu.trove.TObjectHashingStrategy;
import org.jetbrains.annotations.NotNull;
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

  private FileChannel channel;
  protected final TransientString transientNameString = new TransientString();
  
  protected final ArrayList<Decoder> decoders = new ArrayList<Decoder>() {
    @Override
    public boolean add(@NotNull Decoder decoder) {
      return super.add(decoder);
    }
  };

  private final boolean useFlexEncoder;
  private final boolean onlyAbcAsTag;
  private String inputFileParentName;

  private TIntObjectHashMap<TagPositionInfo> exportAssets;
  private int sL = -1;
  private int sA;
  private int sB;

  public AbcFilter(boolean useFlexEncoder) {
    this(useFlexEncoder, false);
  }

  public AbcFilter(boolean useFlexEncoder, boolean onlyAbcAsTag) {
    this.useFlexEncoder = useFlexEncoder;
    this.onlyAbcAsTag = onlyAbcAsTag;
  }

  public void filter(File in, File out, @Nullable AbcNameFilter abcNameFilter) throws IOException {
    inputFileParentName = in.getParentFile().getName();
    int index = inputFileParentName.lastIndexOf('.');
    if (index > 0) {
      inputFileParentName = inputFileParentName.substring(0, index);
    }

    filter(new FileInputStream(in), in.length(), out, abcNameFilter);
  }

  public void filter(VirtualFile in, File out, @Nullable AbcNameFilter abcNameFilter) throws IOException {
    inputFileParentName = in.getParent().getNameWithoutExtension();
    filter(in.getInputStream(), in.getLength(), out, abcNameFilter);
  }

  private void filter(InputStream inputStream, long inputLength, File outFile, @Nullable AbcNameFilter abcNameFilter) throws IOException {
    final boolean onlyABC = outFile.getPath().endsWith(".abc");
    final FileOutputStream outputStream = transcode(inputStream, inputLength, outFile);
    channel = outputStream.getChannel();
    if (!onlyABC) {
      channel.position(PARTIAL_HEADER_LENGTH);
    }
    try {
      if (!onlyABC) {
        filterTags(abcNameFilter);
        writeHeader();
      }
      else {
        filterAbcTags(abcNameFilter);
      }
    }
    finally {
      channel = null;
      outputStream.flush();
      outputStream.close();

      decoders.clear();
    }
  }

  private void writeHeader() throws IOException {
    final int length = (int)channel.position();
    channel.position(0);
    buffer.clear();
    writePartialHeader(length);
    buffer.flip();
    channel.write(buffer);
  }

  private void filterTags(AbcNameFilter abcNameFilter) throws IOException {
    lastWrittenPosition = 0;

    while (buffer.position() < buffer.limit()) {
      int tagCodeAndLength = buffer.getShort();
      int type = tagCodeAndLength >> 6;
      int length = tagCodeAndLength & 0x3F;
      if (length == 63) {
        length = buffer.getInt();
      }

      switch (type) {
        case TagTypes.End:
          buffer.position(lastWrittenPosition);
          channel.write(buffer);
          return;

        case TagTypes.ShowFrame:
          if (decoders.isEmpty()) {
            break;
          }
          else {
            final int limit = buffer.position();
            writeDataBeforeTag(length);
            mergeDoAbc(true, false);
            lastWrittenPosition = limit - 2;
            buffer.position(limit + length);
            continue;
          }

        case TagTypes.SymbolClass:
          processSymbolClass(length);
          continue;

        case TagTypes.ExportAssets:
          processExportAssets(length);
          continue;

        case TagTypes.FileAttributes:
          buffer.put(buffer.position(), (byte)104); // HasMetadata = false
          break;

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
          if (abcNameFilter != null && !abcNameFilter.accept(transientNameString)) {
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

      buffer.position(buffer.position() + length);
    }
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
  
  private void processSymbolClass(int length) throws IOException {
    final int tagStartPosition = buffer.position();
    writeDataBeforeTag(length);
    buffer.position(tagStartPosition);
    int numSymbols = analyzeClassAssociatedWithMainTimeline(length);
    final boolean hasClassAssociatedWithMainTimeLine = sL != -1;
    mergeDoAbc(true, hasClassAssociatedWithMainTimeLine);

    lastWrittenPosition = tagStartPosition - (length < 63 ? 2 : 6);
    buffer.position(lastWrittenPosition);
    boolean hasExportsAssets = exportAssets != null && !exportAssets.isEmpty();
    if (hasClassAssociatedWithMainTimeLine || hasExportsAssets) {
      if (hasExportsAssets) {
        numSymbols += exportAssets.size();
      }

      if (numSymbols == 0) {
        lastWrittenPosition = tagStartPosition + length;
      }
      else {
        encodeTagHeader(TagTypes.SymbolClass, sL);
        buffer.putShort((short)numSymbols);
        buffer.position(lastWrittenPosition);
        buffer.limit(sA);
        channel.write(buffer);
        lastWrittenPosition = sB;
        buffer.limit(buffer.capacity());

        if (exportAssets != null) {
          final TIntObjectIterator<TagPositionInfo> iterator = exportAssets.iterator();
          for (int i = exportAssets.size(); i-- > 0; ) {
            iterator.advance();
            TagPositionInfo exportAsset = iterator.value();
            buffer.position(exportAsset.start);
            buffer.limit(exportAsset.end);
            channel.write(buffer);
          }

          exportAssets.clear();
        }
      }

      sL = -1;
    }

    decoders.clear();
    buffer.position(tagStartPosition + length);
  }

  protected void doAbc2(int length) throws IOException {
    final int off = 4 + transientNameString.length() + 1;
    buffer.position(buffer.position() + off);
    decoders.add(
      new Decoder(new BufferWrapper(buffer, length - off), transientNameString.charAt(0) == '_' ? transientNameString.cloneChars() : null));
  }

  private void mergeDoAbc(boolean asTag, boolean hasClassAssociatedWithMainTimeLine) throws IOException {
    final int abcSize = decoders.size();
    final Encoder encoder = useFlexEncoder ? new FlexEncoder(inputFileParentName) : new Encoder();
    encoder.enablePeepHole();
    encoder.configure(decoders, hasClassAssociatedWithMainTimeLine ? transientNameString : null);

    Decoder decoder;
    // decode methodInfo...
    for (int i = 0; i < abcSize; i++) {
      decoder = decoders.get(i);
      if (decoder == null) {
        continue;
      }

      encoder.useConstantPool(i);
      Decoder.MethodInfo methodInfo = decoder.methodInfo;
      for (int j = 0, infoSize = methodInfo.size(); j < infoSize; j++) {
        methodInfo.decode(j, encoder);
      }
    }

    // decode metadataInfo...
    for (int j = 0; j < abcSize; j++) {
      decoder = decoders.get(j);
      if (decoder == null) {
        continue;
      }

      encoder.useConstantPool(j);
      Decoder.MetaDataInfo metadataInfo = decoder.metadataInfo;
      for (int k = 0, infoSize = metadataInfo.size(); k < infoSize; k++) {
        metadataInfo.decode(k, encoder);
      }
    }

    // decode classInfo...
    for (int j = 0; j < abcSize; j++) {
      decoder = decoders.get(j);
      if (decoder == null) {
        continue;
      }

      encoder.useConstantPool(j);
      Decoder.ClassInfo classInfo = decoder.classInfo;
      for (int k = 0, infoSize = classInfo.size(); k < infoSize; k++) {
        classInfo.decodeInstance(k, encoder);
      }
    }

    for (int j = 0; j < abcSize; j++) {
      decoder = decoders.get(j);
      if (decoder == null) {
        continue;
      }

      encoder.useConstantPool(j);
      Decoder.ClassInfo classInfo = decoder.classInfo;
      for (int k = 0, infoSize = classInfo.size(); k < infoSize; k++) {
        classInfo.decodeClass(k, encoder);
      }
    }

    // decode scripts...
    for (int j = 0; j < abcSize; j++) {
      decoder = decoders.get(j);
      if (decoder == null) {
        continue;
      }

      encoder.useConstantPool(j);
      Decoder.ScriptInfo scriptInfo = decoder.scriptInfo;
      for (int k = 0, scriptSize = scriptInfo.size(); k < scriptSize; k++) {
        scriptInfo.decode(k, encoder);
      }
    }

    // decode method bodies...
    for (int j = 0; j < abcSize; j++) {
      decoder = decoders.get(j);
      if (decoder == null) {
        continue;
      }

      encoder.useConstantPool(j);
      Decoder.MethodBodies methodBodies = decoder.methodBodies;
      for (int k = 0, bodySize = methodBodies.size(); k < bodySize; k++) {
        methodBodies.decode(k, 2, encoder);
      }
    }

    encoder.writeDoAbc(channel, asTag);
  }

  private boolean writeDataBeforeTag(int tagLength) throws IOException {
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

  private void skipTag(int tagLength) throws IOException {
    writeDataBeforeTag(tagLength);

    buffer.position(lastWrittenPosition);
  }

  private void filterAbcTags(AbcNameFilter abcNameFilter) throws IOException {
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
          if (abcNameFilter.accept(transientNameString)) {
            final int off = 4 + transientNameString.length() + 1;
            buffer.position(buffer.position() + off);
            final int abcLength = length - off;
            decoders.add(new Decoder(new BufferWrapper(buffer, abcLength), null));
            buffer.position(buffer.position() + abcLength);
            continue;
          }

        default:
          buffer.position(buffer.position() + length);
      }
    }
  }

  private int analyzeClassAssociatedWithMainTimeline(int tagLength) throws IOException {
    int numSymbols = buffer.getShort();
    for (int i = 0; i < numSymbols; i++) {
      int id = buffer.getShort();
      final int position = buffer.position();
      if (id == 0) {
        readAbcName(position);
        numSymbols--;
        sL = tagLength - (transientNameString.length() + 1 + 2);
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
  
  private void readAbcName(final int start) {
    int end = start;
    byte[] array = buffer.array();
    int lastSlashPosition = -1;
    byte c;
    char[] chars = transientNameString.chars;
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
      if (length != name.length) {
        return false;
      }

      for (int i = 0; i < length; i++) {
        if (chars[i] != name[i]) {
          return false;
        }
      }
      return true;
    }

    @Override
    public String toString() {
      return new String(chars, 0, length);
    }

    @Override
    public int hashCode() {
      int h = hash;
      int len = length;
      if (h == 0 && len > 0) {
        hash = h = StringUtil.stringHashCode(chars, 0, len);
      }
      return h;
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
