package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.util.io.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImageWrapper {
  private static byte[] B_ABC;

  private static final ThreadLocal<ByteBuffer> BUFFER = new ThreadLocal<ByteBuffer>() {
    protected ByteBuffer initialValue() {
      final ByteBuffer buffer = ByteBuffer.allocate(6 + 8);
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      return buffer;
    }
  };

  // FWS, Version 10
  private static final byte[] SWF_HEADER_P1 = {0x46, 0x57, 0x53, 0x0a};
  private static final byte[] SWF_HEADER_P2 = {0x78, 0x00, 0x03, (byte)0xe8, 0x00, 0x00, 0x0b, (byte)0xb8, 0x00, // size [Rect 0 0 8000 6000]
			0x00, 0x0c, 0x01, 0x00, // 16bit le frame rate 12, 16bit be frame count 1
			0x44, 0x11, // Tag type=69 (FileAttributes), length=4
			0x08, 0x00, 0x00, 0x00};

  private static final byte[] SWF_FOOTER = {0x40, 0x00, 0x00, 0x00};
  private static final int SYMBOL_CLASS_TAG_LENGTH = 2 + 2 + 1 + 1;

  private final int dataLength;
  private final int totalLength;
  private final ByteBuffer buffer;

  public ImageWrapper(int dataLength) throws IOException {
    this.dataLength = dataLength;
    initAbcBlank();
    totalLength = SWF_HEADER_P1.length + 4 + SWF_HEADER_P2.length + B_ABC.length + 6 + 2 + dataLength +
                  2 + SYMBOL_CLASS_TAG_LENGTH + SWF_FOOTER.length;
    buffer = BUFFER.get();
  }

  private static void initAbcBlank() throws IOException {
    if (B_ABC == null) {
      InputStream classDefinition = ImageWrapper.class.getClassLoader().getResourceAsStream("B.abc");
      try {
        B_ABC = FileUtil.loadBytes(classDefinition);
      }
      finally {
        classDefinition.close();
      }
    }
  }

  public int getLength() {
    return totalLength;
  }

  public void wrap(InputStream in, OutputStream out) throws IOException {
    out.write(SWF_HEADER_P1);
    // write length
    buffer.putInt(totalLength);
    write(out);
    out.write(SWF_HEADER_P2);
    // class for defineBits
    out.write(B_ABC);

    // defineBits
    encodeLongTagHeader(TagTypes.DefineBitsJPEG2, 2 /* CharacterID */ + dataLength);
    buffer.putShort((short)1);
    write(out);
    FileUtil.copy(in, out);

    // SymbolClass
    encodeTagHeader(TagTypes.SymbolClass, SYMBOL_CLASS_TAG_LENGTH);
    buffer.putShort((short)1);
    buffer.putShort((short)1);
    buffer.put((byte)'B');
    buffer.put((byte)0);
    write(out);

    out.write(SWF_FOOTER);
  }

  private void write(OutputStream out) throws IOException {
    out.write(buffer.array(), 0, buffer.position());
    buffer.position(0);
  }

  private void encodeLongTagHeader(int type, int length) {
    buffer.putShort((short)((type << 6) | 63));
    buffer.putInt(length);
  }

  private void encodeTagHeader(int code, int length) {
    if (length >= 63) {
      encodeLongTagHeader(code, length);
    }
    else {
      buffer.putShort((short)((code << 6) | length));
    }
  }
}
