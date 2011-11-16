package com.intellij.flex.uiDesigner.abc;

public class FlexEncoder extends Encoder {
  private final byte[] debugBasepath;

  public FlexEncoder(String inputFilename) {
    debugBasepath = new byte[inputFilename.length() + 1];
    debugBasepath[0] = '$';
    //noinspection deprecation
    inputFilename.getBytes(0, inputFilename.length(), debugBasepath, 1);
  }

  @SuppressWarnings("UnusedDeclaration")
  private static char[] readChars(DataBuffer in) {
    final int stringLength = in.readU32();
    char[] chars = new char[stringLength];
    final int offset = in.position + in.offset;
    for (int i = 0; i < stringLength; i++) {
      chars[i] = (char)in.data[offset + i];
    }

    return chars;
  }

  @SuppressWarnings("UnusedDeclaration")
  private static String dd(DataBuffer in) {
    int stringLength = in.readU32();
    char[] s = new char[stringLength];
    for (int j = 0; j < stringLength; j++) {
      s[j] = (char)in.data[in.position + in.offset + j];
    }
    return new String(s);
  }

  @Override
  protected void writeDebugFile(DataBuffer in, int oldIndex) {
    int insertionIndex = history.getMapIndex(IndexHistory.STRING, oldIndex);
    int newIndex = history.getNewIndex(insertionIndex);
    if (newIndex == 0) {
      // E:\dev\hero_private\frameworks\projects\framework\src => _
      // but for included file (include "someFile.as") another format — just 'debugfile "C:\Vellum\branches\v2\2.0\dev\output\openSource\textLayout\src\flashx\textLayout\formats\TextLayoutFormatInc.as' — we don't support it yet
      int originalPosition = in.position();
      int start = history.getRawPartPoolPositions(IndexHistory.STRING)[oldIndex];
      in.seek(start);
      int stringLength = in.readU32();
      //char[] s = new char[n];
      //for (int j = 0; j < n; j++) {
      //  s[j] = (char)in.data[in.position + in.offset + j];
      //}
      //String file = new String(s);

      byte[] data = in.data;
      int c;
      int actualStart = -1;
      for (int i = 0; i < stringLength; i++) {
        c = data[in.position + in.offset + i];
        if (c > 127) {
          break; // supports only ASCII
        }

        if (c == ';') {
          if (i < debugBasepath.length) {
            // may be, our injected classes
            break;
          }
          actualStart = in.position + i - debugBasepath.length;
          final int p = in.offset + actualStart;

          System.arraycopy(debugBasepath, 0, data, p, debugBasepath.length);

          stringLength = stringLength - i + debugBasepath.length;
          if (stringLength < 128) {
            actualStart--;
            data[p - 1] = (byte)stringLength;
          }
          else {
            actualStart -= 2;
            data[p - 2] = (byte)((stringLength & 0x7F) | 0x80);
            data[p - 1] = (byte)((stringLength >> 7) & 0x7F);
          }
          break;
        }
      }
      in.seek(originalPosition);

      newIndex = history.getIndex(IndexHistory.STRING, oldIndex, insertionIndex, actualStart);
    }

    opcodes.writeU32(newIndex);
  }
}
