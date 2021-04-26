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
package com.intellij.protobuf.lang.psi;

import com.intellij.psi.tree.IElementType;

import java.util.HashMap;
import java.util.Map;

/** Contains token types used by ProtoLexer, shared between protobuf and prototext files. */
public final class ProtoTokenTypes {
  private static final Map<String, IElementType> TOKEN_TYPES = new HashMap<>();

  private static IElementType put(String token, IElementType type) {
    TOKEN_TYPES.put(token, type);
    return type;
  }

  // Symbols
  public static final IElementType ASSIGN = put("=", new ProtoTokenType("="));
  public static final IElementType COLON = put(":", new ProtoTokenType(":"));
  public static final IElementType COMMA = put(",", new ProtoTokenType(","));
  public static final IElementType DOT = put(".", new ProtoTokenType("."));
  public static final IElementType GT = put(">", new ProtoTokenType(">"));
  public static final IElementType LBRACE = put("{", new ProtoTokenType("{"));
  public static final IElementType LBRACK = put("[", new ProtoTokenType("["));
  public static final IElementType LPAREN = put("(", new ProtoTokenType("("));
  public static final IElementType LT = put("<", new ProtoTokenType("<"));
  public static final IElementType MINUS = put("-", new ProtoTokenType("-"));
  public static final IElementType RBRACE = put("}", new ProtoTokenType("}"));
  public static final IElementType RBRACK = put("]", new ProtoTokenType("]"));
  public static final IElementType RPAREN = put(")", new ProtoTokenType(")"));
  public static final IElementType SEMI = put(";", new ProtoTokenType(";"));
  public static final IElementType SLASH = put("/", new ProtoTokenType("/"));

  // Literal types
  public static final IElementType FLOAT_LITERAL =
      put("FLOAT_LITERAL", new ProtoTokenType("float"));
  public static final IElementType IDENTIFIER_LITERAL =
      put("IDENTIFIER_LITERAL", new ProtoTokenType("identifier"));
  public static final IElementType INTEGER_LITERAL =
      put("INTEGER_LITERAL", new ProtoTokenType("integer"));
  public static final IElementType STRING_LITERAL =
      put("STRING_LITERAL", new ProtoTokenType("string"));

  // Special types
  public static final IElementType BLOCK_COMMENT =
      put("BLOCK_COMMENT", new ProtoCommentTokenType("BLOCK_COMMENT"));
  public static final IElementType BUILT_IN_TYPE =
      put("BUILT_IN_TYPE", new ProtoTokenType("#built_in_type#"));
  public static final IElementType IDENTIFIER_AFTER_NUMBER =
      put("IDENTIFIER_AFTER_NUMBER", new ProtoTokenType("IDENTIFIER_AFTER_NUMBER"));
  public static final IElementType LINE_COMMENT =
      put("LINE_COMMENT", new ProtoCommentTokenType("LINE_COMMENT"));
  public static final IElementType SYMBOL = put("SYMBOL", new ProtoTokenType("SYMBOL"));

  // Keywords found in .proto files.
  public static final IElementType DEFAULT = put("default", new ProtoKeywordTokenType("default"));
  public static final IElementType ENUM = put("enum", new ProtoKeywordTokenType("enum"));
  public static final IElementType EXTEND = put("extend", new ProtoKeywordTokenType("extend"));
  public static final IElementType EXTENSIONS =
      put("extensions", new ProtoKeywordTokenType("extensions"));
  public static final IElementType FALSE = put("false", new ProtoKeywordTokenType("false"));
  public static final IElementType GROUP = put("group", new ProtoKeywordTokenType("group"));
  public static final IElementType IMPORT = put("import", new ProtoKeywordTokenType("import"));
  public static final IElementType JSON_NAME =
      put("json_name", new ProtoKeywordTokenType("json_name"));
  public static final IElementType MAP = put("map", new ProtoKeywordTokenType("map"));
  public static final IElementType MAX = put("max", new ProtoKeywordTokenType("max"));
  public static final IElementType MESSAGE = put("message", new ProtoKeywordTokenType("message"));
  public static final IElementType ONEOF = put("oneof", new ProtoKeywordTokenType("oneof"));
  public static final IElementType OPTION = put("option", new ProtoKeywordTokenType("option"));
  public static final IElementType OPTIONAL =
      put("optional", new ProtoKeywordTokenType("optional"));
  public static final IElementType PACKAGE = put("package", new ProtoKeywordTokenType("package"));
  public static final IElementType PUBLIC = put("public", new ProtoKeywordTokenType("public"));
  public static final IElementType REPEATED =
      put("repeated", new ProtoKeywordTokenType("repeated"));
  public static final IElementType REQUIRED =
      put("required", new ProtoKeywordTokenType("required"));
  public static final IElementType RESERVED =
      put("reserved", new ProtoKeywordTokenType("reserved"));
  public static final IElementType RETURNS = put("returns", new ProtoKeywordTokenType("returns"));
  public static final IElementType RPC = put("rpc", new ProtoKeywordTokenType("rpc"));
  public static final IElementType SERVICE = put("service", new ProtoKeywordTokenType("service"));
  public static final IElementType STREAM = put("stream", new ProtoKeywordTokenType("stream"));
  public static final IElementType SYNTAX = put("syntax", new ProtoKeywordTokenType("syntax"));
  public static final IElementType TO = put("to", new ProtoKeywordTokenType("to"));
  public static final IElementType TRUE = put("true", new ProtoKeywordTokenType("true"));
  public static final IElementType WEAK = put("weak", new ProtoKeywordTokenType("weak"));

  static IElementType get(String token) {
    IElementType type = TOKEN_TYPES.get(token);
    if (type == null) {
      throw new AssertionError("Unknown token type: " + token);
    }
    return type;
  }
}
