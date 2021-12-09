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
package com.intellij.protobuf.lang.lexer;

import com.intellij.lexer.FlexAdapter;

/** A lexer shared between protobuf and prototext languages. */
public class ProtoLexer extends FlexAdapter {

  /** Comment style to support. */
  public enum CommentStyle {
    C_STYLE,
    SH_STYLE
  }

  private ProtoLexer(CommentStyle commentStyle, boolean allowFloatCast, boolean returnKeywords) {
    super(new _ProtoLexer(commentStyle, allowFloatCast, returnKeywords));
  }

  public static ProtoLexer forProtobufWithKeywords() {
    return new ProtoLexer(CommentStyle.C_STYLE, false, true);
  }

  public static ProtoLexer forProtobufWithoutKeywords() {
    return new ProtoLexer(CommentStyle.C_STYLE, false, false);
  }

  public static ProtoLexer forPrototext() {
    return new ProtoLexer(CommentStyle.SH_STYLE, true, false);
  }
}
