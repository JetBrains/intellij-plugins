package com.intellij.flex.uiDesigner.abc;

import com.google.common.base.Charsets;
import com.intellij.flex.uiDesigner.ComplementSwfBuilder;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.flex.uiDesigner.libraries.FlexOverloadedClasses;
import com.intellij.flex.uiDesigner.libraries.FlexOverloadedClasses.InjectionClassifier;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.CharArrayUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

public abstract class FlexSdkAbcInjector extends AbcFilter {
  private static final char OVERLOADED_AND_BACKED_CLASS_MARK = 'F';

  protected boolean flexInjected;
  private final String flexSdkVersion;
  private final URLConnection injectionUrlConnection;

  FlexSdkAbcInjector(String flexSdkVersion, URLConnection injectionUrlConnection) {
    super(flexSdkVersion);

    this.flexSdkVersion = flexSdkVersion;
    this.injectionUrlConnection = injectionUrlConnection;
  }

  protected void changeAbcName(final String className) throws IOException {
    final int oldPosition = buffer.position();
    buffer.position(buffer.position() + 4 + transientNameString.length() + 1 /* null-terminated string */);
    parseCPoolAndRename(className.substring(className.indexOf(':') + 1));

    // modify abcname
    buffer.position(oldPosition + 4 + 10);
    buffer.put((byte)OVERLOADED_AND_BACKED_CLASS_MARK);
    buffer.position(oldPosition);
  }
  
  abstract InjectionClassifier getInjectionClassifier();

  protected void inject() throws IOException {
    flexInjected = true;
    if (injectionUrlConnection == null) {
      decoders.add(new Decoder(new DataBuffer(FileUtil.loadFileBytes(ComplementSwfBuilder.createAbcFile(
        DebugPathManager.getFudHome() + "/flex-injection/target", flexSdkVersion, getInjectionClassifier())))));
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
  }

  protected void parseCPoolAndRename(String from) throws IOException {
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
    final CharsetEncoder charsetEncoder = Charsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(
      CodingErrorAction.REPLACE);
    while (n-- > 1) {
      int l = readU32();
      buffer.limit(buffer.position() + l);
      buffer.mark();
      final CharBuffer charBuffer = Charsets.UTF_8.decode(buffer);
      buffer.limit(buffer.capacity());
      final int index = CharArrayUtil.indexOf(charBuffer, from, 0);
      if (index == -1) {
        continue;
      }

      charBuffer.put(index, OVERLOADED_AND_BACKED_CLASS_MARK);
      buffer.reset();
      charsetEncoder.encode(charBuffer, buffer, true);
      charsetEncoder.reset();
    }
  }

  public static class FrameworkAbcInjector extends FlexSdkAbcInjector {
    public FrameworkAbcInjector(String flexSdkVersion, URLConnection injectionUrlConnection) {
      super(flexSdkVersion, injectionUrlConnection);
    }

    @Override
    InjectionClassifier getInjectionClassifier() {
      return InjectionClassifier.framework;
    }

    @Override
    protected void doAbc2(int length) throws IOException {
      if (flexInjected) {
        super.doAbc2(length);
        return;
      }

      if (StringUtil.equals(transientNameString, FlexOverloadedClasses.STYLE_PROTO_CHAIN)) {
        changeAbcName(FlexOverloadedClasses.STYLE_PROTO_CHAIN);
      }

      super.doAbc2(length);

      if (StringUtil.equals(transientNameString, "mx.styles:CSSStyleDeclaration")) {
        inject();
      }
    }
  }

  public static class SparkAbcInjector extends FlexSdkAbcInjector {
    public SparkAbcInjector(String flexSdkVersion, URLConnection injectionUrlConnection) {
      super(flexSdkVersion, injectionUrlConnection);
    }

    @Override
    InjectionClassifier getInjectionClassifier() {
      return InjectionClassifier.spark;
    }

    @Override
    protected void doAbc2(int length) throws IOException {
      if (flexInjected) {
        super.doAbc2(length);
        return;
      }

      if (StringUtil.equals(transientNameString, FlexOverloadedClasses.SKINNABLE_COMPONENT)) {
        changeAbcName(FlexOverloadedClasses.SKINNABLE_COMPONENT);
        super.doAbc2(length);
        inject();
      }
      else {
        super.doAbc2(length);
      }
    }
  }
}
