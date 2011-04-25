package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.ComplementSwfBuilder;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.flex.uiDesigner.RequiredAssetsInfo;
import com.intellij.openapi.util.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;

public class FlexSdkAbcInjector extends AbcFilter {
  public static final String STYLE_PROTO_CHAIN = "mx.styles:StyleProtoChain";
  public static final String LAYOUT_MANAGER = "mx.managers:LayoutManager";
  public static final String RESOURCE_MANAGER = "mx.resources:ResourceManager";

  private boolean flexInjected;
  private final String flexSdkVersion;
  private final URLConnection injectionUrlConnection;
  private final RequiredAssetsInfo requiredAssetsInfo;

  public FlexSdkAbcInjector(String flexSdkVersion, URLConnection injectionUrlConnection, RequiredAssetsInfo requiredAssetsInfo) {
    this.flexSdkVersion = flexSdkVersion;
    this.injectionUrlConnection = injectionUrlConnection;
    this.requiredAssetsInfo = requiredAssetsInfo;
  }

  @Override
  protected boolean doAbc2(int length, String name) throws IOException, DecoderException {
    if (flexInjected) {
      return false;
    }

    boolean isStyleProtoChain = name.equals(STYLE_PROTO_CHAIN);
    if (isStyleProtoChain) {
      final int oldPosition = buffer.position();
      buffer.position(buffer.position() + 4 + name.length() + 1 /* null-terminated string */);
      parseCPoolAndRenameStyleProtoChain();

      // modify abcname
      buffer.position(oldPosition + 4 + 10);
      buffer.put((byte)'F');
      buffer.position(oldPosition);
    }

    // for flex 4.5 we can inject our classes after StyleProtoChain, but for 4.1 (mx.swc is not yet extracted in this SDK version)
    // we cannot — CSSStyleDeclaration located later, so, we inject after it
    // at the same time we cannot inject after CSSStyleDeclaration for 4.5, so, injection place depends on Flex SDK version
    if (isStyleProtoChain ? flexSdkVersion.equals("4.5") : (flexSdkVersion.equals("4.1") && name.equals("mx.styles:CSSStyleDeclaration"))) {
      flexInjected = true;

      buffer.limit(buffer.position() + length);
      buffer.position(lastWrittenPosition);
      channel.write(buffer);
      lastWrittenPosition = buffer.limit();
      buffer.limit(buffer.capacity());

      if (injectionUrlConnection == null) {
        decoders.add(new Decoder(new DataBuffer(FileUtil.loadFileBytes(new File(
          DebugPathManager.getFudHome() + "/flex-injection/target/" + ComplementSwfBuilder.generateInjectionName(flexSdkVersion))))));
      }
      else {
        InputStream inputStream = injectionUrlConnection.getInputStream();
        try {
          decoders.add(new Decoder(new DataBuffer((FileUtil.loadBytes(inputStream)))));
        }
        finally {
          inputStream.close();
        }
      }

      if (requiredAssetsInfo.bitmapCount != 0) {
        ImageClassPoolGenerator.generate(channel, requiredAssetsInfo.bitmapCount);
      }

      return true;
    }
    
    return false;
  }

  private void parseCPoolAndRenameStyleProtoChain() throws IOException {
    buffer.position(buffer.position() + 4);

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
      buffer.position(buffer.position() + ((n - 1) * 8));
    }

    n = readU32();
    while (n-- > 1) {
      int l = readU32();
      String name = readUTFBytes(l).replace("StyleProtoChain", "FtyleProtoChain");
      buffer.position(buffer.position() - l);
      writeUTF(name, l);
    }
  }

  private String readUTFBytes(int i) {
    try {
      byte[] buf = new byte[i];
      while (i > 0) {
        buf[(buf.length - i)] = buffer.get();
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
    buffer.put(bytearr);
  }
}
