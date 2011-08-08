package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.io.IOUtil;
import com.intellij.openapi.util.io.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImageWrapper extends AbcEncoder {
  private static byte[] BITMAP_ASSET_SWF;

  private static final ThreadLocal<ByteBuffer> BUFFER = new ThreadLocal<ByteBuffer>() {
    protected ByteBuffer initialValue() {
      final ByteBuffer buffer = ByteBuffer.allocate(6 + 8);
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      return buffer;
    }
  };

  private static final int SYMBOL_CLASS_TAG_LENGTH = 2 + 2 + 1 + 1;

  private final int dataLength;
  private final int totalLength;

  public ImageWrapper(int dataLength) throws IOException {
    this.dataLength = dataLength;
    initAbcBlank();
    totalLength = SwfUtil.getWrapLength() + BITMAP_ASSET_SWF.length + 6 + 2 + dataLength + 2 + SYMBOL_CLASS_TAG_LENGTH;
    buffer = BUFFER.get();
  }

  private static void initAbcBlank() throws IOException {
    if (BITMAP_ASSET_SWF == null) {
      BITMAP_ASSET_SWF = IOUtil.getResourceBytes("BitmapAsset.abc");
    }
  }

  public int getLength() {
    return totalLength;
  }

  public void wrap(InputStream in, OutputStream out) throws IOException {
    SwfUtil.header(totalLength, out, buffer);
    // class for defineBits
    out.write(BITMAP_ASSET_SWF);

    // defineBits
    encodeLongTagHeader(TagTypes.DefineBitsJPEG2, 2 /* CharacterID */ + dataLength);
    buffer.putShort((short)1);
    write(out);
    FileUtil.copy(in, out);

    // SymbolClass
    encodeTagHeader(TagTypes.SymbolClass, SYMBOL_CLASS_TAG_LENGTH);
    buffer.putShort((short)1);
    buffer.putShort((short)1);
    writeSwfString('B');
    write(out);

    SwfUtil.footer(out);
  }

  private void write(OutputStream out) throws IOException {
    out.write(buffer.array(), 0, buffer.position());
    buffer.position(0);
  }
}
