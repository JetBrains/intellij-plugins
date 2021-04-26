/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.util;

import com.intellij.openapi.util.TextRange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class ProtoStringTest {

  @Test(expected = IllegalArgumentException.class)
  public void emptyStringRaises() {
    ProtoString.parse("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void stringWithoutQuoteRaises() {
    ProtoString.parse("foo");
  }

  @Test
  public void simpleEscapeSequences() {
    assertEqualsAfterParse("'\\a'", "\007");
    assertEqualsAfterParse("'\\b'", "\b");
    assertEqualsAfterParse("'\\f'", "\f");
    assertEqualsAfterParse("'\\n'", "\n");
    assertEqualsAfterParse("'\\r'", "\r");
    assertEqualsAfterParse("'\\t'", "\t");
    assertEqualsAfterParse("'\\v'", "\013");
    assertEqualsAfterParse("'\\\\'", "\\");
    assertEqualsAfterParse("'\\''", "'");
    assertEqualsAfterParse("'\\\"'", "\"");
  }

  @Test
  public void multipleEscapeSequencesInOneString() {
    assertThat(ProtoString.parse("'\\n\\''").toString()).isEqualTo("\n'");
  }

  @Test
  public void unterminatedString() {
    assertThat(ProtoString.parse("'foo").isUnterminated()).isTrue();
    assertThat(ProtoString.parse("'foo\\'").isUnterminated()).isTrue();
    assertThat(ProtoString.parse("'").isUnterminated()).isTrue();
    assertThat(ProtoString.parse("\"").isUnterminated()).isTrue();
  }

  @Test
  public void validOctalEscape() {
    // \40 is space, \41 is !
    ProtoString parsed = ProtoString.parse("'\\001\\40\\0410\\111\\40'");
    assertThat(parsed.toString()).isEqualTo("\1 !0\111 ");
    assertThat(parsed.getInvalidEscapeRanges()).isEmpty();
    assertThat(parsed.isUnterminated()).isFalse();
  }

  @Test
  public void validHexEscape() {
    // \x20 is space, \x2a is *
    ProtoString parsed = ProtoString.parse("'\\x00\\x20\\x2A0\\xb11\\x20'");
    assertThat(parsed.toString()).isEqualTo("\0 *0\u00b11 ");
    assertThat(parsed.getInvalidEscapeRanges()).isEmpty();
    assertThat(parsed.isUnterminated()).isFalse();
  }

  @Test
  public void invalidHexEscape() {
    ProtoString parsed = ProtoString.parse("'\\x'");
    assertThat(parsed.toString()).isEqualTo("\\x");
    assertThat(parsed.getInvalidEscapeRanges()).contains(TextRange.create(1, 3));
    assertThat(parsed.isUnterminated()).isFalse();
  }

  @Test
  public void validShortUnicodeEscape() {
    ProtoString parsed = ProtoString.parse("'\\u0000\\u0020\\u00210\\u00A11\\u002a'");
    assertThat(parsed.toString()).isEqualTo("\0 !0\u00A11*");
    assertThat(parsed.getInvalidEscapeRanges()).isEmpty();
    assertThat(parsed.isUnterminated()).isFalse();
  }

  @Test
  public void invalidShortUnicodeEscape() {
    // Short unicode escapes must be exactly 4 digits
    ProtoString parsed = ProtoString.parse("'\\u20xyz'");
    assertThat(parsed.toString()).isEqualTo("\\u20xyz");
    assertThat(parsed.getInvalidEscapeRanges()).contains(TextRange.create(1, 5));
    assertThat(parsed.isUnterminated()).isFalse();

    // Invalid sequence at end
    parsed = ProtoString.parse("'\\u2a'");
    assertThat(parsed.toString()).isEqualTo("\\u2a");
    assertThat(parsed.getInvalidEscapeRanges()).contains(TextRange.create(1, 5));
    assertThat(parsed.isUnterminated()).isFalse();
  }

  @Test
  public void validLongUnicodeEscape() {
    ProtoString parsed =
        ProtoString.parse("'\\U00000000\\U00000020\\U000000210\\U000000A11\\U0000001f'");
    assertThat(parsed.toString()).isEqualTo("\0 !0\u00A11\u001f");
    assertThat(parsed.getInvalidEscapeRanges()).isEmpty();
    assertThat(parsed.isUnterminated()).isFalse();

    assertThat(ProtoString.parse("'\\U0010ffff'").toString())
        .isEqualTo(new StringBuilder().appendCodePoint(0x10ffff).toString());
  }

  @Test
  public void invalidLongUnicodeEscape() {
    // Short unicode escapes must be exactly 8 digits
    ProtoString parsed = ProtoString.parse("'\\U20xyz'");
    assertThat(parsed.toString()).isEqualTo("\\U20xyz");
    assertThat(parsed.getInvalidEscapeRanges()).contains(TextRange.create(1, 5));
    assertThat(parsed.isUnterminated()).isFalse();

    // Invalid sequence at end
    parsed = ProtoString.parse("'\\U2a'");
    assertThat(parsed.toString()).isEqualTo("\\U2a");
    assertThat(parsed.getInvalidEscapeRanges()).contains(TextRange.create(1, 5));
    assertThat(parsed.isUnterminated()).isFalse();

    // Invalid sequence at end
    parsed = ProtoString.parse("'\\U2a'");
    assertThat(parsed.toString()).isEqualTo("\\U2a");
    assertThat(parsed.getInvalidEscapeRanges()).contains(TextRange.create(1, 5));
    assertThat(parsed.isUnterminated()).isFalse();

    // Codepoint out of bounds
    parsed = ProtoString.parse("'\\U00110000'");
    assertThat(parsed.toString()).isEqualTo("\\U00110000");
    assertThat(parsed.getInvalidEscapeRanges()).contains(TextRange.create(1, 11));
    assertThat(parsed.isUnterminated()).isFalse();
  }

  @Test
  public void invalidStringEscapeSequences() {
    ProtoString parsed = ProtoString.parse("'\\zzz'");
    assertThat(parsed.toString()).isEqualTo("\\zzz");
    assertThat(parsed.getInvalidEscapeRanges()).contains(TextRange.create(1, 3));
  }

  @Test
  public void multipleInvalidEscapeSequences() {
    ProtoString parsed = ProtoString.parse("'\\zzz\\ppp'");
    assertThat(parsed.toString()).isEqualTo("\\zzz\\ppp");
    assertThat(parsed.getInvalidEscapeRanges())
        .containsExactly(TextRange.create(1, 3), TextRange.create(5, 7));
  }

  @Test
  public void originalOffsetWithEscapes() {
    ProtoString parsed = ProtoString.parse("'123\\n456\\x52789'"); // '\x52' == 'R'
    assertThat(parsed.toString()).isEqualTo("123\n456R789");
    assertThat(parsed.getOriginalOffset(0)).isEqualTo(1); // 1 due to the opening quote in original
    assertThat(parsed.getOriginalOffset(3)).isEqualTo(4); // start of newline
    assertThat(parsed.getOriginalOffset(4)).isEqualTo(6); // after newline
    assertThat(parsed.getOriginalOffset(7)).isEqualTo(9); // start of hex character
    assertThat(parsed.getOriginalOffset(8)).isEqualTo(13); // after hex character
    assertThat(parsed.getOriginalOffset(10)).isEqualTo(15); // last character
    try {
      parsed.getOriginalOffset(11);
      throw new AssertionError("Expected IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException e) {
      // ignored
    }
    try {
      parsed.getOriginalOffset(-1);
      throw new AssertionError("Expected IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException e) {
      // ignored
    }
  }

  @Test
  public void originalOffsetWithoutEscapes() {
    ProtoString parsed = ProtoString.parse("'123456'");
    assertThat(parsed.toString()).isEqualTo("123456");
    assertThat(parsed.getOriginalOffset(0)).isEqualTo(1); // 1 due to the opening quote in original
    assertThat(parsed.getOriginalOffset(3)).isEqualTo(4); // middle
    assertThat(parsed.getOriginalOffset(5)).isEqualTo(6); // last character
    try {
      parsed.getOriginalOffset(6);
      throw new AssertionError("Expected IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException e) {
      // ignored
    }
    try {
      parsed.getOriginalOffset(-1);
      throw new AssertionError("Expected IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException e) {
      // ignored
    }
  }

  private static void assertEqualsAfterParse(String encoded, String expected) {
    ProtoString parsed = ProtoString.parse(encoded);
    assertThat(parsed.toString()).isEqualTo(expected);
    assertThat(parsed.isUnterminated()).isFalse();
    assertThat(parsed.getInvalidEscapeRanges()).isEmpty();
  }
}
