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
package com.intellij.protobuf.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.protobuf.lang.psi.ProtoKeywordTokenType;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;
import org.jetbrains.annotations.Nullable;

/** Static parsing utility functions for parsing proto files. */
public final class PbParserUtil extends GeneratedParserUtilBase {

  /**
   * This function is used to parse a keyword token as an identifier. The proto language allows
   * keywords (e.g., "message" or "import") to be used as identifiers, such as type names. If the
   * parser is parsing a token that should be an identifier, and its lexer type is not
   * IDENTIFIER_LITERAL, it will call this function.
   */
  public static boolean parseKeywordIdentifier(PsiBuilder builder, int level) {
    if (builder.eof()) {
      return false;
    }
    if (builder.getTokenType() instanceof ProtoKeywordTokenType) {
      builder.remapCurrentToken(ProtoTokenTypes.IDENTIFIER_LITERAL);
      builder.advanceLexer();
      return true;
    }
    return false;
  }

  /** Parses a prototext Field element at the builder's current position. */
  public static boolean parseTextField(PsiBuilder builder, int level) {
    return PbTextParser.Field(builder, level);
  }

  /** Parses a prototext FieldName element at the builder's current position. */
  public static boolean parseTextFieldName(PsiBuilder builder, int level) {
    return PbTextParser.FieldName(builder, level);
  }

  /*
   * Generated parsers call exit_section(...) after normal parsing for a rule has finished and
   * recovery (if any) should start. We override exit_section_ and wrap the recovery parser such
   * that it consumes blocks.
   */
  public static void exit_section_(
      PsiBuilder builder,
      int level,
      PsiBuilder.Marker marker,
      @Nullable IElementType elementType,
      boolean result,
      boolean pinned,
      @Nullable Parser eatMore) {
    GeneratedParserUtilBase.exit_section_(
        builder, level, marker, elementType, result, pinned, wrapRecovery(eatMore, builder));
  }

  public static void exit_section_(
      PsiBuilder builder,
      int level,
      PsiBuilder.Marker marker,
      boolean result,
      boolean pinned,
      @Nullable Parser eatMore) {
    exit_section_(builder, level, marker, null, result, pinned, eatMore);
  }

  /**
   * Wraps a recovery parser with a new parser that also detects and consumes tokens contained in
   * blocks.
   *
   * @see RecoveryBlockDetector
   */
  private static Parser wrapRecovery(Parser recovery, PsiBuilder builder) {
    if (recovery == null) {
      return null;
    }
    Parser blockDetector = new RecoveryBlockDetector(builder.getCurrentOffset());
    return (innerBuilder, level) -> {
      if (blockDetector.parse(innerBuilder, level)) {
        return true; // skip tokens that are contained within a block.
      }
      return recovery.parse(innerBuilder, level);
    };
  }

  /**
   * A {@link Parser} that returns <code>true</code> if the current token exists within braces,
   * starting from the beginning of recovery.
   *
   * <p>For example: <code>optional int32 bogus = 3 { foo: bar }</code>
   *
   * <p><code>{ foo: bar }</code> is not a valid block in this context and should be ignored as part
   * of recovery. All tokens, including the left and right braces, should be ignored.
   *
   * <p>This parser will be invoked for each token until recovery finishes. The naive algorithm is
   * to start at the current token position and scan backwards to the beginning of recovery, keeping
   * a count of unmatched braces. If the count is greater than 0 once the start of recovery is
   * reached, the current token exists within a block. To avoid unnecessary duplicate work, each
   * <code>parse</code> invocation uses the starting position and current open brace count from the
   * previous invocation.
   */
  private static class RecoveryBlockDetector implements Parser {
    private int start;
    private int braceCount = 0;

    private RecoveryBlockDetector(int start) {
      this.start = start;
    }

    @Override
    public boolean parse(PsiBuilder builder, int level) {
      int index = -1;
      while (true) {
        int pos = builder.rawTokenTypeStart(index);
        if (pos < start) {
          break;
        }
        IElementType currentType = builder.rawLookup(index);
        if (currentType == null) {
          break;
        }
        if (ProtoTokenTypes.LBRACE.equals(currentType)) {
          braceCount++;
        } else if (ProtoTokenTypes.RBRACE.equals(currentType)) {
          braceCount--;
        }
        index--;
      }
      start = builder.getCurrentOffset(); // Update the starting position for next time
      return braceCount > 0;
    }
  }

  private PbParserUtil() {}
}
