package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.io.AbstractByteArrayOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

final class SwfUtil {
  // FWS, Version 11
  private static final byte[] SWF_HEADER_P1 = {0x46, 0x57, 0x53, 0x0b};
  private static final byte[] SWF_HEADER_P2 = {0x78, 0x00, 0x03, (byte)0xe8, 0x00, 0x00, 0x0b, (byte)0xb8, 0x00,
      // size [Rect 0 0 8000 6000]
      0x00, 0x0c, 0x01, 0x00, // 16bit le frame rate 12, 16bit be frame count 1
      0x44, 0x11, // Tag type=69 (FileAttributes), length=4
      0x08, 0x00, 0x00, 0x00};

  private static final byte[] SWF_FOOTER = {0x40, 0x00, 0x00, 0x00};
  
  public static int getWrapLength() {
    return getWrapHeaderLength() + SWF_FOOTER.length;
  }

  public static int getWrapHeaderLength() {
    return SWF_HEADER_P1.length + 4 + SWF_HEADER_P2.length;
  }

  public static void header(int length, OutputStream out, ByteBuffer buffer) throws IOException {
    out.write(SWF_HEADER_P1);
    // write length
    buffer.putInt(0, length);
    out.write(buffer.array(), 0, 4);
    out.write(SWF_HEADER_P2);
  }

  public static void header(int length, AbstractByteArrayOutputStream out, ByteBuffer buffer, int position) throws IOException {
    buffer.clear();
    buffer.put(SWF_HEADER_P1);
    buffer.putInt(length);
    buffer.put(SWF_HEADER_P2);
    buffer.flip();
    out.write(buffer, position);
  }

  public static void footer(OutputStream out) throws IOException {
    out.write(SWF_FOOTER);
  }

  public static Encoder mergeDoAbc(ArrayList<Decoder> decoders) throws IOException {
    final Encoder encoder = new Encoder();
    encoder.configure(decoders, null);
    mergeDoAbc(decoders, encoder);
    return encoder;
  }

  public static void mergeDoAbc(ArrayList<Decoder> decoders, Encoder encoder) throws IOException {
    final int abcSize = decoders.size();
    encoder.enablePeepHole();

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
  }
}
