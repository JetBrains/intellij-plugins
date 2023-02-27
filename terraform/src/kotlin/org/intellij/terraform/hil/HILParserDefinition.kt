/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package org.intellij.terraform.hil

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.intellij.terraform.hil.psi.HILLexer
import org.intellij.terraform.hil.psi.ILPsiFile
import org.jetbrains.annotations.NotNull

class HILParserDefinition : ParserDefinition {

  override fun createLexer(project: Project) = HILLexer()

  override fun createParser(project: Project) = HILParser()

  override fun getFileNodeType() = FILE

  override fun getCommentTokens(): TokenSet {
    return TokenSet.EMPTY
  }

  override fun getStringLiteralElements() = HILTokenTypes.STRING_LITERALS

  override fun createElement(node: ASTNode): PsiElement {
    val type = node.elementType
    if (type is HILElementType) {
      return HILElementTypes.Factory.createElement(node)
    }
    if (type is HILTokenType) {
      return HILElementTypes.Factory.createElement(node)
    }
    return ASTWrapperPsiElement(node)
  }

  override fun createFile(viewProvider: @NotNull FileViewProvider): @NotNull PsiFile {
    return ILPsiFile(viewProvider)
  }

  override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?) = ParserDefinition.SpaceRequirements.MAY

  companion object {
    val FILE: IFileElementType = IFileElementType(HILLanguage)
  }
}
