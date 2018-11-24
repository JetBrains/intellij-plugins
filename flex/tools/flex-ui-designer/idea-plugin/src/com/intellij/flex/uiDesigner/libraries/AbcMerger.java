package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.abc.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectProcedure;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;

class AbcMerger extends AbcTranscoder {
  private static final Logger LOG = Logger.getInstance(AbcMerger.class.getName());

  private final Map<CharSequence, Definition> definitionMap;
  private final FileOutputStream out;

  // old id to new
  private final TIntObjectHashMap<SymbolInfo> currentSymbolsInfo = new TIntObjectHashMap<>();
  private final ArrayList<SymbolInfo> symbols = new ArrayList<>();
  private int symbolCounter;

  @Nullable
  private Library library;

  @Nullable
  private DefinitionProcessor definitionProcessor;

  public AbcMerger(Map<CharSequence, Definition> definitionMap, File outFile, @Nullable DefinitionProcessor definitionProcessor) throws IOException {
    this.definitionMap = definitionMap;
    this.definitionProcessor = definitionProcessor;

    //noinspection IOResourceOpenedButNotSafelyClosed
    out = new FileOutputStream(outFile);
    channel = out.getChannel();
    channel.position(SwfUtil.getWrapHeaderLength());
  }

  public void setDefinitionProcessor(@Nullable DefinitionProcessor definitionProcessor) {
    this.definitionProcessor = definitionProcessor;
  }

  @Override
  protected void readFrameSizeFrameRateAndFrameCount(byte b) throws IOException {
    super.readFrameSizeFrameRateAndFrameCount(b);
    lastWrittenPosition = buffer.position();
  }

  public void process(Library library) throws IOException {
    this.library = library;

    VirtualFile file = library.getSwfFile();
    process(file.getInputStream(), (int)file.getLength());
  }

  public void process(InputStream in) throws IOException {
    process(in, in.available());
  }

  public void process(InputStream in, int length) throws IOException {
    readSource(in, length);
    processTags(null);
    library = null;

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
  }

  public void end(List<Decoder> decoders, Encoder encoder) throws IOException {
    encoder.configure(decoders, null);
    SwfUtil.mergeDoAbc(decoders, encoder);
    encoder.writeDoAbc(channel, true);

    int length = 0;
    for (SymbolInfo info : symbols) {
      length += 2 + (info.end - info.start);
    }

    Collections.sort(symbols, (o1, o2) -> o1.newId - o2.newId);

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

    // write footer - ShowFrame and End
    buffer.clear();
    SwfUtil.footer(buffer);
    buffer.flip();
    channel.write(buffer);

    SwfUtil.header(channel, buffer);
  }

  public void close() throws IOException {
    if (out != null) {
      out.close();
    }
    channel = null;
  }

  @Override
  protected int processTag(int type, int length) throws IOException {
    if (super.processTag(type, length) == 1 || type == TagTypes.FileAttributes || type == TagTypes.ShowFrame) {
      return 1;
    }

    int characterIdPosition = -1;
    switch (type) {
      case TagTypes.DefineScalingGrid:
      case TagTypes.DefineSprite:

      case TagTypes.DefineShape:
      case TagTypes.DefineShape2:
      case TagTypes.DefineShape3:
      case TagTypes.DefineShape4:

      case TagTypes.DefineBinaryData:
      case TagTypes.DefineBitsLossless2:
      case TagTypes.DefineBitsLossless:

      case TagTypes.DefineBitsJPEG2:
      case TagTypes.DefineBitsJPEG3:
      case TagTypes.DefineBitsJPEG4:

      case TagTypes.DefineBits:

      case TagTypes.PlaceObject:

      case TagTypes.DefineFont:
      case TagTypes.DefineFont2:
      case TagTypes.DefineFont3:
      case TagTypes.DefineText:
      case TagTypes.DefineEditText:
      case TagTypes.DefineButton:
      case TagTypes.DefineButton2:
      case TagTypes.DefineMorphShape:
      case TagTypes.DefineMorphShape2:
        // character id after header
        characterIdPosition = buffer.position();
        break;

      case TagTypes.PlaceObject2:
      case TagTypes.PlaceObject3:
        if (updatePlaceObject2Or3Reference(type == TagTypes.PlaceObject2)) {
          return 0;
        }
        break;

      case TagTypes.DefineFontName:
      case TagTypes.DefineFontAlignZones:
      case TagTypes.DefineFontInfo:
        updateReferenceById(buffer.position());
        return 0;
    }

    if (characterIdPosition != -1) {
      changeCharacterId(characterIdPosition);

      if (type == TagTypes.DefineSprite) {
        processDefineSprite(length);
      }
    }

    return 0;
  }

  private boolean updatePlaceObject2Or3Reference(boolean is2) {
    if ((buffer.get(buffer.position()) & PlaceObjectFlags.HAS_CHARACTER) == 0) {
      return false;
    }

    int bufferPosition;
    if (is2) {
      bufferPosition = buffer.position() + 3;
    }
    else {
      bufferPosition = buffer.position() + 4;
      final byte flags2 = buffer.get(buffer.position() + 1);
      if ((flags2 & PlaceObject3Flags.HAS_CLASS_NAME) != 0 || (flags2 & PlaceObject3Flags.HAS_IMAGE) != 0) {
        bufferPosition += skipAbcName(bufferPosition) + 1;
      }
    }

    updateReferenceById(bufferPosition);
    return true;
  }

  private void updateReferenceById(int idPosition) {
    final int oldId = buffer.getShort(idPosition);
    SymbolInfo info = currentSymbolsInfo.get(oldId);
    // swf may be invalid
    if (info != null) {
      buffer.putShort(idPosition, (short)info.newId);
    }
    else {
      LOG.warn("swf invalid, cannot update reference " + oldId + " due to object with this id is not defined yet");
    }
  }

  private void changeCharacterId(int idPosition) {
    final int oldId = buffer.getShort(idPosition);
    SymbolInfo info = currentSymbolsInfo.get(oldId);
    // swf may be invalid
    if (info == null) {
      info = new SymbolInfo(++symbolCounter, buffer);
      currentSymbolsInfo.put(oldId, info);
    }

    buffer.putShort(idPosition, (short)info.newId);
  }

  private void processDefineSprite(int spriteTagLength) {
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
        case TagTypes.PlaceObject:
          updateReferenceById(start);
          break;

        case TagTypes.PlaceObject2:
        case TagTypes.PlaceObject3:
          updatePlaceObject2Or3Reference(type == TagTypes.PlaceObject2);
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

  @Override
  protected void doAbc2(int length) throws IOException {
    final Definition definition = definitionMap.get(transientNameString);
    // may be overloaded (i.e. new definition with high timestamp exists)
    if (definition != null && (definition.getLibrary() == null || definition.getLibrary().library == library)) {
      definition.doAbcData = createBufferWrapper(length);
      if (definitionProcessor != null) {
        definitionProcessor.process(transientNameString, buffer, definition, definitionMap);
      }
    }
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
      throw new IllegalStateException("info cannot be null, library: " + (library == null ? "<InputStream>" : library.getFile().getPath()));
    }
    else {
      info.start = start;
      info.end = end;
    }
  }

  @Override
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
