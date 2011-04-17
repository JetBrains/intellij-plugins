package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.ComplementSwfBuilder;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.*;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FlexSdkAbcInjector extends AbcFilter {
  public static final String STYLE_PROTO_CHAIN = "mx.styles:StyleProtoChain";
  public static final String LAYOUT_MANAGER = "mx.managers:LayoutManager";

  private boolean flexInjected;
  private String flexSdkVersion;
  private final URLConnection injectionUrlConnection;

  public FlexSdkAbcInjector(URLConnection injectionUrlConnection) {
    this.injectionUrlConnection = injectionUrlConnection;
  }

  public void inject(VirtualFile inputFile, File out, String flexSdkVersion, AbcNameFilter abcNameFilter) throws IOException {
    this.flexSdkVersion = flexSdkVersion;
    filter(inputFile, out, abcNameFilter);
  }

  @Override
  protected boolean doAbc2(int length, String name, FileChannel outputFileChannel) throws IOException {
    if (flexInjected) {
      return false;
    }

    boolean isStyleProtoChain = name.equals(STYLE_PROTO_CHAIN);
    if (isStyleProtoChain) {
      final int oldPosition = byteBuffer.position();
      byteBuffer.position(byteBuffer.position() + 4 + name.length() + 1 /* null-terminated string */);
      parseCPoolAndRenameStyleProtoChain();

      // modify abcname
      byteBuffer.position(oldPosition + 4 + 10);
      byteBuffer.put((byte)'F');
      byteBuffer.position(oldPosition);
    }

    // for flex 4.5 we can inject our classes after StyleProtoChain, but for 4.1 (mx.swc is not yet extracted in this SDK version)
    // we cannot — CSSStyleDeclaration located later, so, we inject after it
    // at the same time we cannot inject after CSSStyleDeclaration for 4.5, so, injection place depends on Flex SDK version
    if (isStyleProtoChain ? flexSdkVersion.equals("4.5") : (flexSdkVersion.equals("4.1") && name.equals("mx.styles:CSSStyleDeclaration"))) {
      flexInjected = true;

      byteBuffer.limit(byteBuffer.position() + length);
      byteBuffer.position(lastWrittenPosition);
      outputFileChannel.write(byteBuffer);
      lastWrittenPosition = byteBuffer.limit();
      byteBuffer.limit(byteBuffer.capacity());

      if (injectionUrlConnection == null) {
        final FileChannel injection = new FileInputStream(new File(DebugPathManager.getFudHome() + "/flex-injection/target/" +
                                                                   ComplementSwfBuilder.generateInjectionName(flexSdkVersion))).getChannel();
        try {
          injection.transferTo(0, injection.size(), outputFileChannel);
        }
        finally {
          injection.close();
        }
      }
      else {
        InputStream inputStream = injectionUrlConnection.getInputStream();
        try {
          outputFileChannel.write(ByteBuffer.wrap(FileUtil.loadBytes(inputStream)));
        }
        finally {
          inputStream.close();
        }
      }

      return true;
    }
    
    return false;
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

  private void writeUTF(String str, int utflen) throws IOException {
    int strlen = str.length();
    int c, count = 0;

    byte[] bytearr = new byte[utflen];

    int i;
    for (i = 0; i < strlen; i++) {
      c = str.charAt(i);
      if (!((c >= 0x0001) && (c <= 0x007F))) break;
      bytearr[count++] = (byte)c;
    }

    for (; i < strlen; i++) {
      c = str.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) {
        bytearr[count++] = (byte)c;
      }
      else if (c > 0x07FF) {
        bytearr[count++] = (byte)(0xE0 | ((c >> 12) & 0x0F));
        bytearr[count++] = (byte)(0x80 | ((c >> 6) & 0x3F));
        bytearr[count++] = (byte)(0x80 | ((c) & 0x3F));
      }
      else {
        bytearr[count++] = (byte)(0xC0 | ((c >> 6) & 0x1F));
        bytearr[count++] = (byte)(0x80 | ((c) & 0x3F));
      }
    }
    byteBuffer.put(bytearr);
  }
}
