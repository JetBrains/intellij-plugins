package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.CharArrayUtil;
import gnu.trove.TObjectHashingStrategy;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.channels.FileChannel;

abstract public class AbcTranscoder extends SwfTranscoder {
  public static final TObjectHashingStrategy<CharSequence> HASHING_STRATEGY = new HashingStrategy();

  protected FileChannel channel;
  protected final TransientString transientNameString = new TransientString();
  protected int lastWrittenPosition;

  protected void processTags(@Nullable final Condition<CharSequence> abcNameFilter) throws IOException {
    while (buffer.position() < buffer.limit()) {
      final int tagCodeAndLength = buffer.getShort();
      final int type = tagCodeAndLength >> 6;
      int length = tagCodeAndLength & 0x3F;
      if (length == 63) {
        length = buffer.getInt();
      }

      switch (type) {
        case TagTypes.DoABC2:
          readAbcName(buffer.position() + 4);
          //System.out.print("\n" + transientNameString.toString() + " \n");
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

        case TagTypes.End:
          processEnd(length);
          return;

        case TagTypes.SymbolClass:
          processSymbolClass(length);
          continue;

        case TagTypes.ExportAssets:
          processExportAssets(length, false);
          continue;

        default:
          final int s = processTag(type, length);
          if (s == 1) {
            skipTag(length);
          }
          else if (s == 0) {
            buffer.position(buffer.position() + length);
          }
      }
    }
  }

  protected abstract void processSymbolClass(int length) throws IOException;

  protected abstract void processEnd(int length) throws IOException;

  protected abstract void doAbc2(int length) throws IOException;

  protected int processTag(int type, int length) throws IOException {
    switch (type) {
      case TagTypes.EnableDebugger:
      case TagTypes.EnableDebugger2:
      case TagTypes.SetBackgroundColor:
      case TagTypes.ProductInfo:
      case TagTypes.DebugID:
      case TagTypes.ScriptLimits:
      case TagTypes.Metadata:
        return 1;

      default:
        return 0;
    }
  }

  protected void processExportAssets(int length, boolean readMainSymbolName) throws IOException {
    final int bodyPosition = buffer.position();
    writeDataBeforeTag(length);
    buffer.position(bodyPosition);

    final int numSymbols = buffer.getShort();
    if (numSymbols == 0) {
      return;
    }

    ensureExportAssetsStorageCreated(numSymbols);

    for (int i = 0; i < numSymbols; i++) {
      int id = buffer.getShort();
      int start = buffer.position();

      final int nameLength;
      if (readMainSymbolName && id == 0) {
        readAbcName(start);
        nameLength = transientNameString.length;
      }
      else {
        nameLength = skipAbcName(start);
      }

      int end = start + nameLength + 1;
      storeExportAsset(id, start, end, readMainSymbolName);
      buffer.position(end);
    }
  }

  protected abstract void ensureExportAssetsStorageCreated(int numSymbols);
  protected abstract void storeExportAsset(int id, int start, int end, boolean mainNameRead);

  protected BufferWrapper createBufferWrapper(int length) {
    final int off = 4 + transientNameString.length() + 1;
    return new BufferWrapper(buffer.array(), buffer.position() + off, length - off);
  }

  protected boolean writeDataBeforeTag(int length) throws IOException {
    int headerLength = length < 63 ? 2 : 6;
    int limit = buffer.position() - headerLength;
    if (limit == lastWrittenPosition) {
      lastWrittenPosition += length + headerLength;
      return false;
    }

    buffer.limit(limit);
    buffer.position(lastWrittenPosition);
    channel.write(buffer);

    lastWrittenPosition = buffer.limit() + length + headerLength;
    buffer.limit(buffer.capacity());

    return true;
  }

  protected void skipTag(int tagLength) throws IOException {
    writeDataBeforeTag(tagLength);

    buffer.position(lastWrittenPosition);
  }

  protected void readAbcName(final int start) {
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
    protected final char[] chars = new char[256];
    protected int length;
    protected int hash;

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
      // must be o2.equals(o1) because o1 is String (cannot equals) and o2 is TransientString or com.intellij.util.text.CharSequenceBackedByArray
      return o1 instanceof String ? o2.equals(o1) : o1.equals(o2);
    }
  }
}
