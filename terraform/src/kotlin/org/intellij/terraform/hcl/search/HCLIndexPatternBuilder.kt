/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.hcl.search

import com.intellij.lexer.Lexer
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.search.IndexPatternBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.HCLParserDefinition
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.HCLFile

class HCLIndexPatternBuilder : IndexPatternBuilder {
  override fun getIndexingLexer(file: PsiFile): Lexer? {
    return if (file is HCLFile) {
      HCLParserDefinition.createLexer()
    } else null
  }

  override fun getCommentTokenSet(file: PsiFile): TokenSet? {
    return if (file is HCLFile) {
      HCLTokenTypes.HCL_COMMENTARIES
    } else null
  }

  override fun getCommentStartDelta(tokenType: IElementType): Int {
    return when (tokenType) {
      HCLElementTypes.LINE_C_COMMENT -> 2
      HCLElementTypes.LINE_HASH_COMMENT -> 1
      HCLElementTypes.BLOCK_COMMENT -> 2
      else -> 0
    }
  }

  override fun getCommentEndDelta(tokenType: IElementType): Int {
    return when (tokenType) {
      HCLElementTypes.BLOCK_COMMENT -> 2
      else -> 0
    }
  }
}