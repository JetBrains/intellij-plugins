package com.intellij.flex.uiDesigner;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

/**
 * Filter SWF for unresolved definitions. Support only SWF from SWC, i.e. DoABC2 for each script (<DoABC2 name='org/flyti/plexus/events/DispatcherEvent'>)
 * Optimized SWF (merged DoABC2) is not supported.
 */
public class AbcFilter {
  private static final int PARTIAL_HEADER_LENGTH = 8;

  private static final int endTag = 0;
  private static final int stagDoABC2 = 82;
  private static final int symbolClass = 76;

  private ByteBuffer byteBuffer;
  private final char[] abcNameBuffer = new char[256];
  private final byte[] partialHeader = new byte[PARTIAL_HEADER_LENGTH];

  private String flexSdkVersion;
  private boolean flexInjected;

  public boolean replaceMainClass;

  public void inject(VirtualFile inputFile, File out, String flexSdkVersion, AbcNameFilter abcNameFilter) throws IOException {
    this.flexSdkVersion = flexSdkVersion;
    filter(inputFile, out, abcNameFilter);
  }
  
  public void filter(File inputFile, File out, AbcNameFilter abcNameFilter) throws IOException {
    filter(new FileInputStream(inputFile), inputFile.length(), out, abcNameFilter);
  }
  
  public void filter(VirtualFile inputFile, File out, AbcNameFilter abcNameFilter) throws IOException {
    filter(inputFile.getInputStream(), inputFile.getLength(), out, abcNameFilter);
  }

  private void filter(InputStream inputStream, long inputLength, File out, AbcNameFilter abcNameFilter) throws IOException {
    boolean onlyABC = out.getPath().endsWith(".abc");
    final int uncompressedBodyLength;
    final boolean compressed;
    byte[] data;
    try {
      int n = inputStream.read(partialHeader);
      assert n == PARTIAL_HEADER_LENGTH;
      uncompressedBodyLength = (partialHeader[4] & 0xFF | (partialHeader[5] & 0xFF) << 8 | (partialHeader[6] & 0xFF) << 16 | partialHeader[7] << 24) - PARTIAL_HEADER_LENGTH;
      compressed = partialHeader[0] == 0x43;
      data = FileUtil.loadBytes(inputStream, compressed ? ((int) inputLength - PARTIAL_HEADER_LENGTH) : uncompressedBodyLength);
    }
    finally {
      inputStream.close();
    }

    if (compressed) {
      Inflater inflater = new Inflater();
      inflater.setInput(data);
      byte[] uncomressedData = new byte[uncompressedBodyLength];
      try {
        inflater.inflate(uncomressedData);
      }
      catch (DataFormatException e) {
        String s = e.getMessage();
        throw new ZipException(s != null ? s : "Invalid ZLIB data format");
      }
      data = uncomressedData;
    }

    byteBuffer = ByteBuffer.wrap(data);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

    // skip rect, FrameRate, FrameCount
    byteBuffer.position((int) Math.ceil((float) (5 + ((data[0] & 0xFF) >> -(5 - 8)) * 4) / 8) + 2 + 2);

    FileOutputStream outputStream = new FileOutputStream(out);
    FileChannel outputFileChannel = outputStream.getChannel();
    if (!onlyABC) {
      outputFileChannel.position(PARTIAL_HEADER_LENGTH);
    }

    try {
      if (!onlyABC) {
        filterTags(outputFileChannel, abcNameFilter);
        writeHeader(outputFileChannel);
      }
      else {
        filterAbcTags(outputFileChannel, abcNameFilter);
      }
    }
    finally {
      outputStream.flush();
      outputStream.close();
    }
  }

  private void writeHeader(FileChannel outputFileChannel) throws IOException {
    int length = (int) outputFileChannel.position();
    outputFileChannel.position(0);
    byteBuffer.clear();
    byteBuffer.put((byte) 0x46); // write as uncompressed
    byteBuffer.put(partialHeader, 1, 3);
    byteBuffer.putInt(length);

    byteBuffer.flip();
    outputFileChannel.write(byteBuffer);
  }

  private void filterTags(FileChannel outputFileChannel, AbcNameFilter abcNameFilter) throws IOException {
    int lastWrittenPosition = 0;

    while (byteBuffer.position() < byteBuffer.limit()) {
      int tagCodeAndLength = byteBuffer.getShort();
      int type = tagCodeAndLength >> 6;
      int length = tagCodeAndLength & 0x3F;
      if (length == 63) {
        length = byteBuffer.getInt();
      }

      switch (type) {
        case endTag:
          byteBuffer.position(lastWrittenPosition);
          outputFileChannel.write(byteBuffer);
          return;

        case symbolClass: {
          final int tagStartPosition = byteBuffer.position();
          if (replaceMainClass) {
            lastWrittenPosition = parseSymbolClassTagAndRenameClassAssociatedWithMainTimeline(lastWrittenPosition, outputFileChannel, length);
          }
          byteBuffer.position(tagStartPosition + length);
        }
        break;

        case stagDoABC2:
          String name = readAbcName(byteBuffer.position() + 4);
          if (!abcNameFilter.accept(name)) {
            byteBuffer.limit(byteBuffer.position() - 6);
            byteBuffer.position(lastWrittenPosition);
            outputFileChannel.write(byteBuffer);

            lastWrittenPosition = byteBuffer.limit() + length + 6;
            byteBuffer.limit(byteBuffer.capacity());
            byteBuffer.position(lastWrittenPosition);
            continue;
          }
          else if (flexSdkVersion != null && !flexInjected) {
            boolean isStyleProtoChain = name.equals("mx.styles:StyleProtoChain");
            if (isStyleProtoChain) {
              final int oldPosition = byteBuffer.position();
              byteBuffer.position(byteBuffer.position() + 4 + name.length() + 1 /* null-terminated string */);
              parseCPoolAndRenameStyleProtoChain();

              // modify abcname
              byteBuffer.position(oldPosition + 4 + 10);
              byteBuffer.put((byte) 'F');
              byteBuffer.position(oldPosition);
            }

            // для flex 4.5 мы можем размещать наши классы сразу после StyleProtoChain, но для 4.1 (из него еще не вынесли mx.swc) не можем — CSSStyleDeclaration определяется позже, поэтому мы размещаем как раз после него
            // в то же время располагать после CSSStyleDeclaration для 4.5 мы не можем, поэтому место инжектирования зависит от версии flex sdk
            if (isStyleProtoChain ? flexSdkVersion.equals("4.5") : (flexSdkVersion.equals("4.1") && name.equals("mx.styles:CSSStyleDeclaration"))) {
              flexInjected = true;

              byteBuffer.limit(byteBuffer.position() + length);
              byteBuffer.position(lastWrittenPosition);
              outputFileChannel.write(byteBuffer);
              lastWrittenPosition = byteBuffer.limit();
              byteBuffer.limit(byteBuffer.capacity());

              final String injectionFileName = "flex-injection-" + flexSdkVersion + ".abc";
              if (System.getProperty("fud.debug") == null) {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(injectionFileName);
                try {
                  outputFileChannel.write(ByteBuffer.wrap(FileUtil.loadBytes(inputStream)));
                }
                finally {
                  inputStream.close();
                }
              }
              else {
                final FileChannel injection = new FileInputStream(new File(DebugPathManager.getFudHome() + "/flex-injection/target/" + injectionFileName)).getChannel();
                try {
                  injection.transferTo(0, injection.size(), outputFileChannel);
                }
                finally {
                  injection.close();
                }
              }

              continue;
            }
          }
          // through

        default:
          byteBuffer.position(byteBuffer.position() + length);
          break;
      }
    }
  }

  private void filterAbcTags(FileChannel outputFileChannel, AbcNameFilter abcNameFilter) throws IOException {
    while (true) {
      int tagCodeAndLength = byteBuffer.getShort();
      int type = tagCodeAndLength >> 6;
      int length = tagCodeAndLength & 0x3F;
      if (length == 63) {
        length = byteBuffer.getInt();
      }

      switch (type) {
        case endTag:
          return;

        case stagDoABC2:
          String name = readAbcName(byteBuffer.position() + 4);
          if (abcNameFilter.accept(name)) {
            byteBuffer.position(byteBuffer.position() - 6);
            byteBuffer.limit(byteBuffer.position() + length + 6);
            outputFileChannel.write(byteBuffer);

            byteBuffer.limit(byteBuffer.capacity());
            continue;
          }

        default:
          byteBuffer.position(byteBuffer.position() + length);
      }
    }
  }

  private void parseCPoolAndRenameStyleProtoChain() throws IOException {
    byteBuffer.position(byteBuffer.position() + 4);

    int n = readU32();
    while (n-- > 1) {
      readU32();
    }

    n = readU32();
    while (n-- > 1) {
      readU32();
    }

    n = readU32();
    if (n != 0) {
      byteBuffer.position(byteBuffer.position() + ((n - 1) * 8));
    }

    n = readU32();
    while (n-- > 1) {
      int l = readU32();
      String name = readUTFBytes(l).replace("StyleProtoChain", "FtyleProtoChain");
      byteBuffer.position(byteBuffer.position() - l);
      writeUTF(name, l);
    }
  }

  private int parseSymbolClassTagAndRenameClassAssociatedWithMainTimeline(int lastWrittenPosition, FileChannel outputFileChannel, int tagLength) throws IOException {
    final int startTagPosition = byteBuffer.position() - (tagLength >= 63 ? 6 : 2);
    int numSymbols = byteBuffer.getShort();
    for (int i = 0; i < numSymbols; i++) {
      int id = byteBuffer.getShort();
      final int position = byteBuffer.position();
      String name = readAbcName(position);
      if (id == 0) {
        byte[] nb = "com.intellij.flex.uiDesigner.Main".getBytes();
        byteBuffer.put(nb);
        byteBuffer.put((byte) 0);

        byteBuffer.position(startTagPosition);
        encodeTagHeader(symbolClass, tagLength - (name.length() - nb.length));

        byteBuffer.position(lastWrittenPosition);
        byteBuffer.limit(position + nb.length + 1);
        outputFileChannel.write(byteBuffer);

        byteBuffer.limit(byteBuffer.capacity());
        return position + name.length() + 1;
      }
      else {
        byteBuffer.position(position + name.length() + 1);
      }
    }

    throw new IllegalArgumentException("can't find 0 symbol");
  }

  private void encodeTagHeader(int code, int length) {
    if (length >= 63) {
      writeUI16((short) ((code << 6) | 63));
      write32(length);
    }
    else {
      writeUI16((short) ((code << 6) | length));
    }
  }

  private void writeUI16(int c) {
    if (c == -1) {
      c = 65535;
    }

    byteBuffer.put((byte) c);
    byteBuffer.put((byte) (c >> 8));
  }

  private void write32(int c) {
    byteBuffer.put((byte) c);
    byteBuffer.put((byte) (c >> 8));
    byteBuffer.put((byte) (c >> 16));
    byteBuffer.put((byte) (c >> 24));
  }

  private void writeUTF(String str, int utflen) throws IOException {
    int strlen = str.length();
    int c, count = 0;

    byte[] bytearr = new byte[utflen];

    int i;
    for (i = 0; i < strlen; i++) {
      c = str.charAt(i);
      if (!((c >= 0x0001) && (c <= 0x007F))) break;
      bytearr[count++] = (byte) c;
    }

    for (; i < strlen; i++) {
      c = str.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) {
        bytearr[count++] = (byte) c;

      }
      else if (c > 0x07FF) {
        bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
        bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
        bytearr[count++] = (byte) (0x80 | ((c) & 0x3F));
      }
      else {
        bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
        bytearr[count++] = (byte) (0x80 | ((c) & 0x3F));
      }
    }
    byteBuffer.put(bytearr);
  }

  private int readUnsignedByte() {
    return byteBuffer.get() & 0xFF;
  }

  private int readU32() {
    int result = readUnsignedByte();
    if ((result & 0x80) == 0) return result;
    result = result & 0x7F | readUnsignedByte() << 7;
    if ((result & 0x4000) == 0) return result;
    result = result & 0x3FFF | readUnsignedByte() << 14;
    if ((result & 0x200000) == 0) return result;
    result = result & 0x1FFFFF | readUnsignedByte() << 21;
    if ((result & 0x10000000) == 0) return result;
    return result & 0xFFFFFFF | readUnsignedByte() << 28;
  }

  private String readUTFBytes(int i) {
    try {
      byte[] buf = new byte[i];
      while (i > 0) {
        buf[(buf.length - i)] = byteBuffer.get();
        --i;
      }
      return new String(buf, "utf-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private String readAbcName(final int start) {
    int end = start;
    byte[] array = byteBuffer.array();
    int lastSlashPosition = -1;
    byte c;
    int index = 0;
    while ((c = array[end++]) != 0) {
      switch (c) {
        case '/':
          lastSlashPosition = index;
          abcNameBuffer[index] = '.';
          break;

        default:
          abcNameBuffer[index] = (char) c;
      }

      index++;
    }

    if (lastSlashPosition != -1) {
      abcNameBuffer[lastSlashPosition] = ':';
    }

    return new String(abcNameBuffer, 0, index);
  }
}