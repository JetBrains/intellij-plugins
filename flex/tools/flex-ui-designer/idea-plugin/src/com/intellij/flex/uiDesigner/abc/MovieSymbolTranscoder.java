package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.intellij.flex.uiDesigner.abc.PlaceObjectFlags.*;

public class MovieSymbolTranscoder extends MovieTranscoder {
  private int fileLength;
  private byte[] symbolName;

  private TIntObjectHashMap<PlacedObject> placedObjects;
  // we cannot mark placedObject as used and iterate exisiting map (placedObjects) — order of items in map is not predictable,
  // but we must write placed object in the same order as it was read
  private List<PlacedObject> usedPlacedObjects;

  // symbolName — utf8 bytes
  @SuppressWarnings("UnusedDeclaration")
  @TestOnly
  public void transcode(File in, File out, byte[] symbolName) throws IOException {
    this.symbolName = symbolName;
    transcode(new FileInputStream(in), in.length(), out, false);
  }

  public void transcode(@NotNull VirtualFile in, @NotNull File out, @NotNull String symbolName) throws IOException {
    this.symbolName = symbolName.getBytes();
    transcode(in.getInputStream(), in.getLength(), out, true);
  }

  @Override
  protected void transcode(boolean writeBounds) throws IOException {
    fileLength = SYMBOL_CLASS_TAG_FULL_LENGTH + SwfUtil.getWrapLength();

    final PlacedObject exportedSymbol = transcode();
    buffer.position(exportedSymbol.start + 2);
    final byte[] symbolOwnClassAbc = getSymbolOwnClassAbc(buffer.getShort());
    fileLength += symbolOwnClassAbc.length;

    if (writeBounds) {
      writeMovieBounds();
    }

    SwfUtil.header(fileLength, out, buffer);
    writeUsedPlacedObjects();
    writePlacedObject(exportedSymbol);
    out.write(symbolOwnClassAbc);
    writeSymbolClass(exportedSymbol.newId);
    SwfUtil.footer(out);
  }

  private PlacedObject transcode() throws IOException {
    placedObjects = new TIntObjectHashMap<PlacedObject>();

    int spriteId = -1;

    int tagStart;
    analyze: while ((tagStart = buffer.position()) < buffer.limit()) {
      final int tagCodeAndLength = buffer.getShort();
      final int type = tagCodeAndLength >> 6;
      int length = tagCodeAndLength & 0x3F;
      if (length == 63) {
        length = buffer.getInt();
      }

      final int position = buffer.position();
      switch (type) {
        case TagTypes.End:
          break analyze;

        case TagTypes.DefineShape:
        case TagTypes.DefineShape2:
        case TagTypes.DefineShape3:
        case TagTypes.DefineShape4:
        case TagTypes.DefineSprite:
          placedObjects.put(buffer.getShort(), new PlacedObject(position, length, type, tagStart));
          break;

        case TagTypes.ExportAssets:
        case TagTypes.SymbolClass:
          spriteId = processExportAssetsOrSymbolClass();
          if (spriteId == -1) {
            break;
          }
          else {
            break analyze;
          }
      }

      buffer.position(position + length);
    }
    
    if (spriteId == -1) {
      throw new IOException("Can't find symbol");
    }

    usedPlacedObjects = new ArrayList<PlacedObject>(placedObjects.size());
    bounds = null;
    final PlacedObject exportedSymbol = placedObjects.get(spriteId);
    exportedSymbol.used = true;
    processDefineSprite(exportedSymbol);

    exportedSymbol.newId = usedPlacedObjects.size() + 1;

    fileLength += exportedSymbol.filelength();
    return exportedSymbol;
  }

  private void processDefineSprite(PlacedObject placedObject) throws IOException {
    buffer.position(placedObject.start + 4);
    final int endPosition = placedObject.start + placedObject.length;
    while (true) {
      final int tagStart = buffer.position();
      final int tagCodeAndLength = buffer.getShort();
      final int type = tagCodeAndLength >> 6;
      int length = tagCodeAndLength & 0x3F;
      if (length == 63) {
        length = buffer.getInt();
      }

      final int start = buffer.position();
      switch (type) {
        case TagTypes.DoAction:
        case TagTypes.DoInitAction:
          placedObject.prepareSparseWrite();
          if (placedObject.positions == null) {
            placedObject.positions = new TIntArrayList();
            placedObject.actualLength = placedObject.length;
          }
          placedObject.positions.add(tagStart);
          final int fullLength = length + (start - tagStart);
          placedObject.positions.add(tagStart + fullLength);
          placedObject.actualLength -= fullLength;
          continue;

        case TagTypes.PlaceObject:
        case TagTypes.PlaceObject3:
          throw new IOException("PlaceObject and PlaceObject3 are not supported");

        case TagTypes.PlaceObject2:
          processPlaceObject2(placedObject, length, start);
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
  }

  private static int computeFullLength(int length) {
    return recordHeaderLength(length) + length;
  }

  private void processPlaceObject2(final PlacedObject placedObject, final int length, final int position) throws IOException {
    int flags = buffer.get();
    int objectIdPosition = -1;
    if ((flags & HAS_CLIP_ACTION) != 0) {
      flags = flags &~ HAS_CLIP_ACTION;
      int bufferPosition = buffer.position();
      buffer.put(bufferPosition - 1, (byte)flags);

      bufferPosition += 2; // Depth

      if ((flags & HAS_CHARACTER) != 0) {
        objectIdPosition = bufferPosition;
        bufferPosition += 2;
      }

      if ((flags & HAS_MATRIX) != 0) {
        buffer.position(bufferPosition);
        decodeMatrix();
        bufferPosition = buffer.position();
      }

      if ((flags & HAS_COLOR_TRANSFORM) != 0) {
        decodeColorTransform();
        bufferPosition = buffer.position();
      }

      if ((flags & HAS_RATIO) != 0) {
        bufferPosition += 2;
      }

      if ((flags & HAS_NAME) != 0) {
        final int nameLengthWithTerminator = skipAbcName(bufferPosition) + 1;
        bufferPosition += nameLengthWithTerminator;
      }

      if ((flags & HAS_CLIP_DEPTH) != 0) {
        bufferPosition += 2;
      }

      placedObject.prepareSparseWrite();
      placedObject.actualLength -= length - (bufferPosition - position);

      placedObject.positions.add(bufferPosition);
      placedObject.positions.add(position + length);
    }
    else if ((flags & HAS_CHARACTER) != 0) {
      objectIdPosition = buffer.position() + 2;
    }

    // swf spec: "CharacterId is used only when a new character is being added. If a character that is already on the display map is being modified, the CharacterId field is absent."
    // but in any case we check and use flag referredObject.used — swf may be invalid (but this problem is not encountered yet, develar 05.08.11)
    if (objectIdPosition != -1) {
      final int objectId = buffer.getShort(objectIdPosition);
      final PlacedObject referredObject = placedObjects.get(objectId);
      if (referredObject.used) {
        return;
      }

      referredObject.used = true;
      if (referredObject.tagType == TagTypes.DefineSprite) {
        processDefineSprite(referredObject);
        fileLength += referredObject.filelength();
      }
      else if (bounds == null) {
        findBounds(referredObject);
        fileLength += referredObject.computeFullLengthAsProvided();
      }

      usedPlacedObjects.add(referredObject);
      referredObject.newId = usedPlacedObjects.size();
      buffer.putShort(objectIdPosition, (short)referredObject.newId);
    }
  }

  private void findBounds(PlacedObject placedObject) throws IOException {
    buffer.position(placedObject.start + 2);
    decodeRect();
  }

  private void writeUsedPlacedObjects() throws IOException {
    // must be written in the same order as it was read
    Collections.sort(usedPlacedObjects, new Comparator<PlacedObject>() {
      @Override
      public int compare(PlacedObject o1, PlacedObject o2) {
        return o1.start < o2.start ? -1 : 1;
      }
    });

    for (PlacedObject object : usedPlacedObjects) {
      writePlacedObject(object);
    }
  }

  private void writePlacedObject(PlacedObject object) throws IOException {
    final TIntArrayList positions = object.positions;
    if (positions == null) {
      out.write(data, object.tagStart, object.start - object.tagStart);
      // little-endian short new id
      out.write(0xff & object.newId);
      out.write(0xff & (object.newId >> 8));
      out.write(data, object.start + 2, object.length - 2);
    }
    else {
      buffer.position(0);
      encodeTagHeader(object.tagType, object.actualLength);
      buffer.putShort((short)object.newId);
      out.write(data, 0, buffer.position());
      writeSparceBytes(positions, object.start + 2, object.start + object.length);
    }
  }

  private void decodeColorTransform() throws IOException {
    syncBits();
    boolean hasAdd = readBit();
    boolean hasMult = readBit();
    int nbits = readUBits(4);
    if (hasMult) {
      readSBits(nbits);
      readSBits(nbits);
      readSBits(nbits);
      readSBits(nbits);
    }
    if (hasAdd) {
      readSBits(nbits);
      readSBits(nbits);
      readSBits(nbits);
      readSBits(nbits);
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  private void decodeMatrix() throws IOException {
    syncBits();
    boolean hasScale = readBit();
    if (hasScale) {
      int nScaleBits = readUBits(5);
      int scaleX = readSBits(nScaleBits);
      int scaleY = readSBits(nScaleBits);
    }

    boolean hasRotate = readBit();
    if (hasRotate) {
      int nRotateBits = readUBits(5);
      int rotateSkew0 = readSBits(nRotateBits);
      int rotateSkew1 = readSBits(nRotateBits);
    }

    int nTranslateBits = readUBits(5);
    int translateX = readSBits(nTranslateBits);
    int translateY = readSBits(nTranslateBits);
  }

  private boolean readBit() throws IOException {
    return readUBits(1) != 0;
  }

  private int processExportAssetsOrSymbolClass() {
    final int numSymbols = buffer.getShort();
    if (numSymbols == 0) {
      return -1;
    }

    final byte[] data = buffer.array();

    for (int i = 0; i < numSymbols; i++) {
      final int id = buffer.getShort();
      int j = buffer.position();
      int k = 0;
      while (true) {
        final int b = data[j++];
        if (b == 0) {
          if (k == symbolName.length) {
            return id;
          }
          else {
            break;
          }
        }
        
        if (b != symbolName[k++]) {
          while (data[j++] != 0) {
          }

          break;
        }
      }

      buffer.position(j);
    }

    return -1;
  }

  private static class PlacedObject {
    private boolean used;
    private int newId = -1;

    // we cannot calculate tagStart by length and start — length may be less than 63, but encoded as long tag header
    private final int tagStart;
    private final int start;
    private final int length;
    private final int tagType;

    private int actualLength = -1;
    private TIntArrayList positions;

    public void prepareSparseWrite() {
      if (positions == null) {
        positions = new TIntArrayList();
        actualLength = length;
      }
    }

    private PlacedObject(int start, int length, int tagType, int tagStart) {
      this.start = start;
      this.length = length;
      this.tagType = tagType;
      this.tagStart = tagStart;
    }
    
    public int computeFullLengthAsProvided() {
      return length + (start - tagStart);
    }

    public int filelength() {
      if (positions == null) {
        // we encode length as provided, just copy bytes
        return computeFullLengthAsProvided();
      }
      else {
        // we encode length according to rules about long or short tag header
        return computeFullLength(actualLength);
      }
    }
  }
}
