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
package com.intellij.protobuf.lang.psi

sealed class SyntaxLevel(val id: String, val version: String) {
  class DeprecatedSyntax(version: String) : SyntaxLevel(SYNTAX_KEYWORD, version)
  class Edition(version: String) : SyntaxLevel(EDITION_KEYWORD, version)

  companion object {
    @JvmStatic
    fun parse(syntaxId: String?, version: String?): SyntaxLevel? {
      return when {
        syntaxId.isNullOrEmpty() || version.isNullOrEmpty() -> null
        syntaxId == "syntax" -> DeprecatedSyntax(version)
        syntaxId == "edition" -> Edition(version)
        else -> null
      }
    }
  }
}

internal fun isDeprecatedProto2Syntax(syntaxLevel: SyntaxLevel): Boolean {
  return syntaxLevel is SyntaxLevel.DeprecatedSyntax && syntaxLevel.version == PROTO_SYNTAX_V2
}

internal fun isDeprecatedProto3Syntax(syntaxLevel: SyntaxLevel): Boolean {
  return syntaxLevel is SyntaxLevel.DeprecatedSyntax && syntaxLevel.version == PROTO_SYNTAX_V3
}

internal const val SYNTAX_KEYWORD = "syntax"
internal const val EDITION_KEYWORD = "edition"
internal const val PROTO_SYNTAX_V2 = "proto2"
internal const val PROTO_SYNTAX_V3 = "proto3"
