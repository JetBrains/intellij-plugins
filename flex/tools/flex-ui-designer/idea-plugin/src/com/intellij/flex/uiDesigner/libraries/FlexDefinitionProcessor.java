package com.intellij.flex.uiDesigner.libraries;

import com.google.common.base.Charsets;
import com.intellij.flex.uiDesigner.abc.AbcUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.CharArrayUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

public class FlexDefinitionProcessor implements DefinitionProcessor {
  private static final char OVERLOADED_AND_BACKED_CLASS_MARK = 'F';

  @Override
  public void process(CharSequence name, ByteBuffer buffer) throws IOException {
    if (StringUtil.equals(name, FlexOverloadedClasses.STYLE_PROTO_CHAIN)) {
      changeAbcName(FlexOverloadedClasses.STYLE_PROTO_CHAIN, buffer);
    }
    else if (StringUtil.equals(name, FlexOverloadedClasses.SKINNABLE_COMPONENT)) {
      changeAbcName(FlexOverloadedClasses.SKINNABLE_COMPONENT, buffer);
    }
  }

  private static void changeAbcName(final String name, ByteBuffer buffer) throws IOException {
    final int oldPosition = buffer.position();
    buffer.position(buffer.position() + 4 + name.length() + 1 /* null-terminated string */);
    parseCPoolAndRename(name.substring(name.indexOf(':') + 1), buffer);

    // modify abcname
    buffer.position(oldPosition + 4 + 10);
    buffer.put((byte)OVERLOADED_AND_BACKED_CLASS_MARK);
    buffer.position(oldPosition);
  }

  private static void parseCPoolAndRename(String from, ByteBuffer buffer) throws IOException {
    buffer.position(buffer.position() + 4);

    int n = AbcUtil.readU32(buffer);
    while (n-- > 1) {
      AbcUtil.readU32(buffer);
    }

    n = AbcUtil.readU32(buffer);
    while (n-- > 1) {
      AbcUtil.readU32(buffer);
    }

    n = AbcUtil.readU32(buffer);
    if (n != 0) {
      buffer.position(buffer.position() + ((n - 1) * 8));
    }

    n = AbcUtil.readU32(buffer);
    final CharsetEncoder charsetEncoder = Charsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(
      CodingErrorAction.REPLACE);
    while (n-- > 1) {
      int l = AbcUtil.readU32(buffer);
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
}
