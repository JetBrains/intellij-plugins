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
package com.intellij.protobuf.ide.highlighter;

import com.intellij.lexer.LayeredLexer;
import com.intellij.protobuf.lang.lexer.ProtoLexer;
import com.intellij.protobuf.lang.lexer.StringLexer;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;

/**
 * This lexer can be used to highlight keywords in addition to other token types (strings, numbers,
 * etc.), at the cost of possibly mis-highlighting identifiers whose names are used as keywords
 * elsewhere. It can be used for highlighting files in color and formatting settings, where the
 * {@link PbHighlightingAnnotator} is not used.
 */
public class PbFullHighlightingLexer extends LayeredLexer {
  PbFullHighlightingLexer() {
    super(ProtoLexer.forProtobufWithKeywords());
    registerLayer(
        StringLexer.mergingStringLexer(ProtoTokenTypes.STRING_LITERAL),
        ProtoTokenTypes.STRING_LITERAL);
    registerLayer(
        new BuiltInTypeLexer(ProtoLexer.forProtobufWithKeywords()),
        ProtoTokenTypes.IDENTIFIER_LITERAL);
  }
}
