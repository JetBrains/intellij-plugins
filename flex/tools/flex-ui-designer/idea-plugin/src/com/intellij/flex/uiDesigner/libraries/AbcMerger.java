package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.abc.*;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectProcedure;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

class AbcMerger extends AbcTranscoder {
  private final Map<CharSequence, Definition> definitionMap;
  private final FileOutputStream out;

  // old id to new
  private final TIntObjectHashMap<SymbolInfo> currentSymbolsInfo = new TIntObjectHashMap<SymbolInfo>();
  private final ArrayList<SymbolInfo> symbols = new ArrayList<SymbolInfo>();
  private int symbolCounter;

  public AbcMerger(Map<CharSequence, Definition> definitionMap, @SuppressWarnings("UnusedParameters") @Nullable String flexSdkVersion, File outFile)
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

    if (!currentSymbolsInfo.isEmpty()) {
      symbols.ensureCapacity(symbols.size() + currentSymbolsInfo.size());
      currentSymbolsInfo.forEachValue(new TObjectProcedure<SymbolInfo>() {
        @Override
        public boolean execute(SymbolInfo info) {
          // not all objects with character id are exported
          if (info.start == -1) {
            assert info.end == -1;
          }
          else {
            symbols.add(info);
          }
          return true;
        }
      });

      currentSymbolsInfo.clear();
    }

    //out.flush();
    //channel.force(true);
  }

  public void end(List<Decoder> decoders, String flexSdkVersion) throws IOException {
    final Encoder encoder = flexSdkVersion != null ? new FlexEncoder("test", flexSdkVersion) : new Encoder();
    encoder.configure(decoders, null);
    SwfUtil.mergeDoAbc(decoders, encoder);
    encoder.writeDoAbc(channel, true);

    int length = 0;
    for (SymbolInfo info : symbols) {
      length += 2 + (info.end - info.start);
    }

    Collections.sort(symbols, new Comparator<SymbolInfo>() {
      @Override
      public int compare(SymbolInfo o1, SymbolInfo o2) {
        return o1.newId - o2.newId;
      }
    });

    buffer.clear();
    encodeTagHeader(TagTypes.SymbolClass, length + 2);
    buffer.putShort((short)symbols.size());
    buffer.flip();
    channel.write(buffer);
    buffer.clear();

    for (SymbolInfo info : symbols) {
      final ByteBuffer b = info.buffer;
      final int start = info.start - 2;
      b.putShort(start, (short)info.newId);
      b.position(start);
      b.limit(info.end);
      channel.write(b);
      b.limit(b.capacity());
    }

    // write symbolClass
    //buffer.clear();
    //symbolClassBuffer.flip();
    //encodeTagHeader(TagTypes.SymbolClass, symbolClassBuffer.limit() + 2 /* numSymbols */);
    //buffer.putShort((short)totalNumSymbols);
    //buffer.flip();
    //channel.write(buffer);
    //channel.write(symbolClassBuffer);

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

    int characterIdPosition = -1;

    switch (type) {
      case TagTypes.DefineSprite:
      case TagTypes.DefineShape:
      case TagTypes.DefineShape2:
      case TagTypes.DefineShape3:
      case TagTypes.DefineShape4:
      case TagTypes.DefineBinaryData:
      case TagTypes.DefineBitsLossless2:
      case TagTypes.DefineBitsLossless:
      case TagTypes.PlaceObject:
      case TagTypes.DefineScalingGrid:
      case TagTypes.DefineBitsJPEG2:
      case TagTypes.DefineBitsJPEG3:
      case TagTypes.DefineBitsJPEG4:
      case TagTypes.DefineBits:
        // character id after header
        characterIdPosition = buffer.position();
        break;

      case TagTypes.PlaceObject2:
        if ((buffer.get(buffer.position()) & PlaceObjectFlags.HAS_CHARACTER) != 0) {
          characterIdPosition = buffer.position() + 2;
        }
        break;

      case TagTypes.PlaceObject3:
        throw new IOException("PlaceObject3 are not supported");
    }

    if (characterIdPosition != -1) {
      changeCharacterId(characterIdPosition);

      if (type == TagTypes.DefineSprite) {
        processDefineSprite(length);
      }
    }

    return 0;
  }

  private void changeCharacterId(int characterIdPosition) {
    final int oldId = buffer.getShort(characterIdPosition);
    SymbolInfo info = currentSymbolsInfo.get(oldId);
    // swf may be invalid
    if (info == null) {
      info = new SymbolInfo(++symbolCounter, buffer);
      currentSymbolsInfo.put(oldId, info);
    }

    buffer.putShort(characterIdPosition, (short)info.newId);
  }

  private void processDefineSprite(int spriteTagLength) throws IOException {
    buffer.mark();
    buffer.position(buffer.position() + 4);
    final int endPosition = buffer.position() + spriteTagLength;
    while (true) {
      final int tagCodeAndLength = buffer.getShort();
      final int type = tagCodeAndLength >> 6;
      int length = tagCodeAndLength & 0x3F;
      if (length == 63) {
        length = buffer.getInt();
      }

      final int start = buffer.position();
      switch (type) {
        case TagTypes.PlaceObject3:
          throw new IOException("PlaceObject3 are not supported");

        case TagTypes.PlaceObject:
        case TagTypes.PlaceObject2:
          changeCharacterId(start);
          break;
      }

      final int newPosition = start + length;
      if (newPosition < endPosition) {
        buffer.position(newPosition);
      }
      else {
        break;
      }
    }

    buffer.reset();
  }

  @Override
  protected void ensureExportAssetsStorageCreated(int numSymbols) {
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
  protected void storeExportAsset(int id, int start, int end, boolean mainNameRead) {
    if (id == 0) {
      if (mainNameRead) {
        Definition removed = definitionMap.remove(transientNameString);
        assert removed != null;
      }
      return;
    }

    SymbolInfo info = currentSymbolsInfo.get(id);
    if (info == null) {
      throw new IllegalStateException("ff");
    }
    else {
      info.start = start;
      info.end = end;
    }
  }

  protected void processSymbolClass(final int length) throws IOException {
    processExportAssets(length, true);
  }

  private static class SymbolInfo {
    public int start = -1;
    public int end = -1;
    public final int newId;
    public final ByteBuffer buffer;

    private SymbolInfo(int newId, ByteBuffer buffer) {
      this.newId = newId;
      this.buffer = buffer;
    }
  }
}
