package com.intellij.lang.javascript.flex.importer;

import org.jetbrains.annotations.NonNls;

/**
 * @author Maxim.Mossienko
 */
class Swf {
  private static class Rect {
    int nBits;
    int xMin, xMax;
    int yMin, yMax;

    @Override
    public @NonNls String toString() {
      return "[Rect " + xMin + " " + yMin + " " + xMax + " " + yMax + "]";
    }
  }

  private final FlexByteCodeInformationProcessor processor;
  private int bitPos;
  private int bitBuf;

  private final ByteBuffer data;

  private static final int stagDoABC = 72;   // embedded .abc (AVM+) bytecode
  private static final int stagDoABC2 = 82;   // revised ABC version with a name

  private static final @NonNls String[] tagNames = {
    "End",                  // 00
    "ShowFrame",            // 01
    "DefineShape",          // 02
    "FreeCharacter",        // 03
    "PlaceObject",          // 04
    "RemoveObject",         // 05
    "DefineBits",           // 06
    "DefineButton",         // 07
    "JPEGTables",           // 08
    "SetBackgroundColor",   // 09

    "DefineFont",           // 10
    "DefineText",           // 11
    "DoAction",             // 12
    "DefineFontInfo",       // 13

    "DefineSound",          // 14
    "StartSound",           // 15
    "StopSound",            // 16

    "DefineButtonSound",    // 17

    "SoundStreamHead",      // 18
    "SoundStreamBlock",     // 19

    "DefineBitsLossless",   // 20
    "DefineBitsJPEG2",      // 21

    "DefineShape2",         // 22
    "DefineButtonCxform",   // 23

    "Protect",              // 24

    "PathsArePostScript",   // 25

    "PlaceObject2",         // 26
    "27 (invalid)",         // 27
    "RemoveObject2",        // 28

    "SyncFrame",            // 29
    "30 (invalid)",         // 30
    "FreeAll",              // 31

    "DefineShape3",         // 32
    "DefineText2",          // 33
    "DefineButton2",        // 34
    "DefineBitsJPEG3",      // 35
    "DefineBitsLossless2",  // 36
    "DefineEditText",       // 37

    "DefineVideo",          // 38

    "DefineSprite",         // 39
    "NameCharacter",        // 40
    "ProductInfo",          // 41
    "DefineTextFormat",     // 42
    "FrameLabel",           // 43
    "DefineBehavior",       // 44
    "SoundStreamHead2",     // 45
    "DefineMorphShape",     // 46
    "FrameTag",             // 47
    "DefineFont2",          // 48
    "GenCommand",           // 49
    "DefineCommandObj",     // 50
    "CharacterSet",         // 51
    "FontRef",              // 52

    "DefineFunction",       // 53
    "PlaceFunction",        // 54

    "GenTagObject",         // 55

    "ExportAssets",         // 56
    "ImportAssets",         // 57

    "EnableDebugger",       // 58

    "DoInitAction",         // 59
    "DefineVideoStream",    // 60
    "VideoFrame",           // 61

    "DefineFontInfo2",      // 62
    "DebugID",              // 63
    "EnableDebugger2",      // 64
    "ScriptLimits",         // 65

    "SetTabIndex",          // 66

    "DefineShape4",         // 67
    "68 (invalid)",         // 68

    "FileAttributes",       // 69

    "PlaceObject3",         // 70
    "ImportAssets2",        // 71

    "DoABC",                // 72
    "DefineFontAlignZones", // 73
    "CSMTextSettings",      // 74
    "DefineFont3",          // 75
    "SymbolClass",          // 76
    "Metadata",             // 77
    "ScalingGrid",          // 78
    "79 (invalid)",         // 79
    "80 (invalid)",         // 80
    "81 (invalid)",         // 81
    "DoABC2",               // 82
    "DefineShape4",         // 83
    "DefineMorphShape2",    // 84
    "85 (invalid)",         // 85
    "DefineSceneAndFrameLabelData", // 86
    "DefineBinaryData",     // 87
    "DefineFontName",       // 88
    "89 (unknown)  ",       // 89
    "90 (unknown)  ",       // 90
    "DefineFont4",          // 91
    "(invalid)"             // end
  };


  Swf(final ByteBuffer _data, final FlexByteCodeInformationProcessor _processor) {
    data = _data;
    processor = _processor;

    final Rect rect = decodeRect();
    final int rate = data.readUnsignedByte() << 8 | data.readUnsignedByte();
    final int count = data.readUnsignedShort();

    processor.dumpStat("size " + rect + "\n");
    processor.dumpStat("frame rate " + rate + "\n");
    processor.dumpStat("frame count " + count + "\n");

    decodeTags();
  }

  private void decodeTags() {
    int type, h, length;

    while (data.getPosition() < data.bytesSize()) {
      type = (h = data.readUnsignedShort()) >> 6;

      if (((length = h & 0x3F) == 0x3F)) length = data.readInt();

      processor.dumpStat(
        (type < tagNames.length ? tagNames[type] : "undefined") + " " + length + "b " + ((int)100f * length / data.bytesSize()) + "%\n");

      switch (type) {
        case 0:
          return;
        case stagDoABC2:
          int pos1 = data.getPosition();
          data.readInt();
          final String abcName = readString();
          processor.dumpStat("\nabc name " + abcName + "\n");
          length -= (data.getPosition() - pos1);
          // fall through
        case stagDoABC:
          ByteBuffer data2 = new ByteBuffer();
          data2.setLittleEndian();
          data.readBytes(data2, length);
          new Abc(data2, processor).dump(processor.getAbcInSwfIndent());
          processor.append("\n");
          break;
        default:
          data.incPosition(length);
      }
    }
  }

  private String readString() {
    String s = "";
    int c;

    while ((c = data.readUnsignedByte()) != 0) s += (char)c;

    return s;
  }

  private void syncBits() {
    bitPos = 0;
  }

  private Rect decodeRect()

  {
    syncBits();

    Rect rect = new Rect();

    int nBits = readUBits(5);
    rect.xMin = readSBits(nBits);
    rect.xMax = readSBits(nBits);
    rect.yMin = readSBits(nBits);
    rect.yMax = readSBits(nBits);

    return rect;
  }

  int readSBits(int numBits) {
    if (numBits > 32) throw new Error("Number of bits > 32");

    int num = readUBits(numBits);
    int shift = 32 - numBits;
    // sign extension
    num = (num << shift) >> shift;
    return num;
  }

  int readUBits(int numBits) {
    if (numBits == 0) return 0;

    int bitsLeft = numBits;
    int result = 0;

    if (bitPos == 0) //no value in the buffer - read a byte
    {
      bitBuf = data.readUnsignedByte();
      bitPos = 8;
    }

    while (true) {
      int shift = bitsLeft - bitPos;
      if (shift > 0) {
        // Consume the entire buffer
        result |= bitBuf << shift;
        bitsLeft -= bitPos;

        // Get the next byte from the input stream
        bitBuf = data.readUnsignedByte();
        bitPos = 8;
      }
      else {
        // Consume a portion of the buffer
        result |= bitBuf >> -shift;
        bitPos -= bitsLeft;
        bitBuf &= 0xff >> (8 - bitPos); // mask off the consumed bits

        //                if (sb.append) System.out.sb.appendln("  read"+numBits+" " + result);
        return result;
      }
    }
  }
}
