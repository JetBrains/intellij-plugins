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

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.util.TextRange;
import com.intellij.protobuf.lang.lexer.StringLexer;

import java.util.Arrays;

/** A utility class for parsing protobuf strings containing escape sequences. */
public class ProtoString {
  private final String original;
  private final String parsed;
  private final ImmutableList<TextRange> invalidEscapeRanges;
  private final boolean unterminated;
  private final int[] sourceOffsets;

  public String getOriginal() {
    return original;
  }

  @Override
  public String toString() {
    return parsed;
  }

  /** Returns a list of invalid escape sequences within the string. */
  public ImmutableList<TextRange> getInvalidEscapeRanges() {
    return invalidEscapeRanges;
  }

  /**
   * Returns <code>true</code> if the string does not contain a closing quote prior to EOL or EOF.
   */
  public boolean isUnterminated() {
    return unterminated;
  }

  /**
   * Returns the offset in the original string for the given parsed string offset. Note that the
   * original string contains at least an opening quote, so <code>getOriginalOffset(0) == 1</code>.
   *
   * @param offset the original offset
   * @return the parsed offset
   * @throws IndexOutOfBoundsException if the offset is out of bounds
   */
  public int getOriginalOffset(int offset) {
    return sourceOffsets[offset];
  }

  /** Constructs a ProtoString object given the literal proto string. */
  public static ProtoString parse(String encoded) {
    // Since this string is coming from the lexer/parser, it should be guaranteed to be
    // non-null, not empty (at least 1 quote), and start with either a single or double quote.
    if (encoded.isEmpty()) {
      throw new IllegalArgumentException("String must not be empty.");
    }
    if (encoded.charAt(0) != '\'' && encoded.charAt(0) != '"') {
      throw new IllegalArgumentException("String must start with a single or double quote.");
    }
    int[] offsets = new int[encoded.length()];
    ImmutableList.Builder<TextRange> invalidEscapeRanges = ImmutableList.builder();
    StringBuilder result = new StringBuilder(encoded.length());
    StringLexer lexer = new StringLexer();
    lexer.start(encoded);
    boolean lastWasLiteral = false;
    while (lexer.hasMoreTokens()) {
      CharSequence value = lexer.currentTokenValue();
      int start = lexer.getTokenStart();
      for (int i = 0; i < value.length(); i++) {
        offsets[result.length() + i] = start + i;
      }
      result.append(value);

      if (lexer.isCurrentTokenInvalid()) {
        invalidEscapeRanges.add(TextRange.create(lexer.getTokenStart(), lexer.getTokenEnd()));
      }
      lastWasLiteral = lexer.isCurrentTokenLiteral();
      lexer.advance();
    }

    // If the last character doesn't match the opening quote, this string is unterminated. We ensure
    // that the last character was part of a literal string part to prevent an escaped quote from
    // being considered a valid terminator.
    boolean unterminated = false;
    String parsed;
    if (!lastWasLiteral
        || result.charAt(result.length() - 1) != result.charAt(0)
        || result.length() == 1) {
      unterminated = true;
      // Cut off the starting quote
      parsed = result.substring(1);
    } else {
      // Cut off the starting and ending quotes
      parsed = result.substring(1, result.length() - 1);
    }

    offsets = Arrays.copyOfRange(offsets, 1, 1 + parsed.length());
    return new ProtoString(encoded, parsed, unterminated, invalidEscapeRanges.build(), offsets);
  }

  private ProtoString(
      String original,
      String parsed,
      boolean unterminated,
      ImmutableList<TextRange> invalidEscapeRanges,
      int[] offsets) {
    this.original = original;
    this.parsed = parsed;
    this.unterminated = unterminated;
    this.invalidEscapeRanges = invalidEscapeRanges;
    this.sourceOffsets = offsets;
  }
}
