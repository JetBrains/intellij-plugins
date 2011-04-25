package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

/**
 * Filter SWF for unresolved definitions. Support only SWF from SWC, i.e. DoABC2 for each script (<DoABC2 
 name='org/flyti/plexus/events/DispatcherEvent'>)
 * Optimized SWF (merged DoABC2) is not supported.
 */
public class AbcFilter extends AbcEncoder {
  private static final int PARTIAL_HEADER_LENGTH = 8;

  private final char[] abcNameBuffer = new char[256];
  private final byte[] partialHeader = new byte[PARTIAL_HEADER_LENGTH];

  public boolean replaceMainClass;
  protected int lastWrittenPosition;
  protected FileChannel channel;
  
  protected List<Decoder> decoders = new ArrayList<Decoder>(256);

  public void filter(File inputFile, File out, AbcNameFilter abcNameFilter) throws IOException {
    filter(new FileInputStream(inputFile), inputFile.length(), out, abcNameFilter);
  }

  public void filter(VirtualFile inputFile, File out, AbcNameFilter abcNameFilter) throws IOException {
    filter(inputFile.getInputStream(), inputFile.getLength(), out, abcNameFilter);
  }

  private void filter(InputStream inputStream, long inputLength, File out, AbcNameFilter abcNameFilter) throws IOException {
    final int uncompressedBodyLength;
    final boolean compressed;
    byte[] data;
    try {
      int n = inputStream.read(partialHeader);
      assert n == PARTIAL_HEADER_LENGTH;
      uncompressedBodyLength = (partialHeader[4] & 0xFF | (partialHeader[5] & 0xFF) << 8 |
                                (partialHeader[6] & 0xFF) << 16 | partialHeader[7] << 24) - PARTIAL_HEADER_LENGTH;
      compressed = partialHeader[0] == 0x43;
      data = FileUtil.loadBytes(inputStream, compressed ? ((int)inputLength - PARTIAL_HEADER_LENGTH) : uncompressedBodyLength);
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

    buffer = ByteBuffer.wrap(data);
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    //bufferWrapper = new BufferWrapper(buffer);

    // skip rect, FrameRate, FrameCount
    buffer.position((int)Math.ceil((float)(5 + ((data[0] & 0xFF) >> -(5 - 8)) * 4) / 8) + 2 + 2);

    FileOutputStream outputStream = new FileOutputStream(out);
    channel = outputStream.getChannel();
    final boolean onlyABC = out.getPath().endsWith(".abc");
    if (!onlyABC) {
      channel.position(PARTIAL_HEADER_LENGTH);
    }

    try {
      if (!onlyABC) {
        filterTags(abcNameFilter);
        writeHeader();
      }
      else {
        filterAbcTags(abcNameFilter);
      }
    }
    catch (DecoderException e) {
      throw new IOException(e);
    }
    finally {
      channel = null;
      outputStream.flush();
      outputStream.close();
    }

    //if (onlyABC) {
    //  return;
    //}
    //
    //// todo yes, unnecessary recopying, but experimental, will be optimized later
    //BufferedInputStream in = new BufferedInputStream(new FileInputStream(out));
    //try {
    //  Optimizer.optimize(in, out);
    //}
    //finally {
    //  in.close();
    //}
  }

  private void writeHeader() throws IOException {
    int length = (int)channel.position();
    channel.position(0);
    buffer.clear();
    buffer.put((byte)0x46); // write as uncompressed
    buffer.put(partialHeader, 1, 3);
    buffer.putInt(length);

    buffer.flip();
    channel.write(buffer);
  }

  private void filterTags(AbcNameFilter abcNameFilter) throws IOException, DecoderException {
    lastWrittenPosition = 0;

    while (buffer.position() < buffer.limit()) {
      int tagCodeAndLength = buffer.getShort();
      int type = tagCodeAndLength >> 6;
      int length = tagCodeAndLength & 0x3F;
      if (length == 63) {
        length = buffer.getInt();
      }

      switch (type) {
        case TagTypes.End:
          buffer.position(lastWrittenPosition);
          channel.write(buffer);
          return;

        case TagTypes.SymbolClass: {
          final int tagStartPosition = buffer.position();
          writeDataBeforeTag(length);

          mergeDoAbc();
          lastWrittenPosition = tagStartPosition - (length < 63 ? 2 : 6);
          buffer.position(tagStartPosition);

          if (replaceMainClass) {
            lastWrittenPosition =
              parseSymbolClassTagAndRenameClassAssociatedWithMainTimeline(lastWrittenPosition, length);
          }
          buffer.position(tagStartPosition + length);
        }
        continue;

        case TagTypes.EnableDebugger:
        case TagTypes.EnableDebugger2:
        case TagTypes.SetBackgroundColor:
        case TagTypes.ProductInfo:
          skipTag(length);
          continue;

        case TagTypes.DoABC2:
          String name = readAbcName(buffer.position() + 4);
          if (!abcNameFilter.accept(name)) {
            skipTag(length);
            continue;
          }
          else {
            int oldPosition = buffer.position();
            writeDataBeforeTag(length);
            buffer.position(oldPosition);

            if (doAbc2(length, name)) {
            }
            else {
              buffer.position(buffer.position() + 4 + name.length() + 1 /* null-terminated string */);
              decoders.add(new Decoder(new BufferWrapper(buffer, length)));
            }

            buffer.position(lastWrittenPosition);
            continue;
          }

        default:
          buffer.position(buffer.position() + length);
          break;
      }
    }
  }

  private void mergeDoAbc() throws DecoderException, IOException {
    final Decoder[] decoders = this.decoders.toArray(new Decoder[this.decoders.size()]);
    final int abcSize = decoders.length;
    final ConstantPool[] pools = new ConstantPool[abcSize];
    for (int i = 0; i < abcSize; i++) {
      pools[i] = decoders[i].constantPool;
    }

    final Encoder encoder = new Encoder(46, 16);
    encoder.enablePeepHole();
    encoder.configure(decoders);
    encoder.addConstantPools(pools);

    Decoder decoder;
    // decode methodInfo...
    for (int i = 0; i < abcSize; i++) {
      decoder = decoders[i];
      encoder.useConstantPool(i);

      Decoder.MethodInfo methodInfo = decoder.methodInfo;
      for (int j = 0, infoSize = methodInfo.size(); j < infoSize; j++) {
        methodInfo.decode(j, encoder);
      }
    }

    // decode metadataInfo...
    for (int j = 0; j < abcSize; j++) {
      decoder = decoders[j];
      encoder.useConstantPool(j);

      Decoder.MetaDataInfo metadataInfo = decoder.metadataInfo;
      for (int k = 0, infoSize = metadataInfo.size(); k < infoSize; k++) {
        metadataInfo.decode(k, encoder);
      }
    }

    // decode classInfo...
    for (int j = 0; j < abcSize; j++) {
      decoder = decoders[j];
      encoder.useConstantPool(j);

      Decoder.ClassInfo classInfo = decoder.classInfo;
      for (int k = 0, infoSize = classInfo.size(); k < infoSize; k++) {
        classInfo.decodeInstance(k, encoder);
      }
    }

    for (int j = 0; j < abcSize; j++) {
      decoder = decoders[j];
      encoder.useConstantPool(j);

      Decoder.ClassInfo classInfo = decoder.classInfo;
      for (int k = 0, infoSize = classInfo.size(); k < infoSize; k++) {
        classInfo.decodeClass(k, encoder);
      }
    }

    // decode scripts...
    for (int j = 0; j < abcSize; j++) {
      decoder = decoders[j];
      encoder.useConstantPool(j);

      Decoder.ScriptInfo scriptInfo = decoder.scriptInfo;

      for (int k = 0, scriptSize = scriptInfo.size(); k < scriptSize; k++) {
        scriptInfo.decode(k, encoder);
      }
    }

    // decode method bodies...
    for (int j = 0; j < abcSize; j++) {
      decoder = decoders[j];
      encoder.useConstantPool(j);

      Decoder.MethodBodies methodBodies = decoder.methodBodies;
      for (int k = 0, bodySize = methodBodies.size(); k < bodySize; k++) {
        methodBodies.decode(k, 2, encoder);
      }
    }

    ByteBuffer b = ByteBuffer.allocate(2 * 1024 * 1024);
    b.order(ByteOrder.LITTLE_ENDIAN);
    encoder.toABC(b);
    b.flip();
    channel.write(b);
  }

  private void writeDataBeforeTag(int tagLength) throws IOException {
    int tagHeaderLength = tagLength < 63 ? 2 : 6;
    buffer.limit(buffer.position() - tagHeaderLength);
    buffer.position(lastWrittenPosition);
    channel.write(buffer);

    lastWrittenPosition = buffer.limit() + tagLength + tagHeaderLength;
    buffer.limit(buffer.capacity());
  }

  private void skipTag(int tagLength) throws IOException {
    writeDataBeforeTag(tagLength);

    buffer.position(lastWrittenPosition);
  }
  
  protected boolean doAbc2(int length, String name) throws IOException, DecoderException {
    return false;
  }

  private void filterAbcTags(AbcNameFilter abcNameFilter) throws IOException {
    while (true) {
      int tagCodeAndLength = buffer.getShort();
      int type = tagCodeAndLength >> 6;
      int length = tagCodeAndLength & 0x3F;
      if (length == 63) {
        length = buffer.getInt();
      }

      switch (type) {
        case TagTypes.End:
          return;

        case TagTypes.DoABC2:
          String name = readAbcName(buffer.position() + 4);
          if (abcNameFilter.accept(name)) {
            buffer.position(buffer.position() - 6);
            buffer.limit(buffer.position() + length + 6);
            channel.write(buffer);

            buffer.limit(buffer.capacity());
            continue;
          }

        default:
          buffer.position(buffer.position() + length);
      }
    }
  }
  
  private int parseSymbolClassTagAndRenameClassAssociatedWithMainTimeline(int lastWrittenPosition, int tagLength) throws IOException {
    final int startTagPosition = buffer.position() - (tagLength >= 63 ? 6 : 2);
    int numSymbols = buffer.getShort();
    for (int i = 0; i < numSymbols; i++) {
      int id = buffer.getShort();
      final int position = buffer.position();
      String name = readAbcName(position);
      if (id == 0) {
        byte[] nb = "com.intellij.flex.uiDesigner.Main".getBytes();
        buffer.put(nb);
        buffer.put((byte)0);

        buffer.position(startTagPosition);
        encodeTagHeader(TagTypes.SymbolClass, tagLength - (name.length() - nb.length));

        buffer.position(lastWrittenPosition);
        buffer.limit(position + nb.length + 1);
        channel.write(buffer);

        buffer.limit(buffer.capacity());
        return position + name.length() + 1;
      }
      else {
        buffer.position(position + name.length() + 1);
      }
    }

    throw new IllegalArgumentException("can't find 0 symbol");
  }
  
  private String readAbcName(final int start) {
    int end = start;
    byte[] array = buffer.array();
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
          abcNameBuffer[index] = (char)c;
      }

      index++;
    }

    if (lastSlashPosition != -1) {
      abcNameBuffer[lastSlashPosition] = ':';
    }

    return new String(abcNameBuffer, 0, index);
  }
}