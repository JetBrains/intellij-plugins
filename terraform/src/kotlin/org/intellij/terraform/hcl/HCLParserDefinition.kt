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
package org.intellij.terraform.hcl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.intellij.terraform.hcl.HCLElementTypes.*
import org.intellij.terraform.hcl.psi.impl.HCLFileImpl
import org.jetbrains.annotations.NotNull
import java.util.*

open class HCLParserDefinition : ParserDefinition {

  override fun createLexer(project: Project): Lexer = createLexer()

  override fun createParser(project: Project): PsiParser {
    return HCLParser()
  }

  override fun getFileNodeType(): IFileElementType {
    return FILE
  }

  override fun getCommentTokens(): TokenSet {
    return HCLTokenTypes.HCL_COMMENTARIES
  }

  override fun getStringLiteralElements(): TokenSet {
    return HCLTokenTypes.STRING_LITERALS
  }

  override fun createElement(node: ASTNode): PsiElement {
    val type = node.elementType
    if (type is HCLElementType) {
      return Factory.createElement(node)
    }
    return ASTWrapperPsiElement(node)
  }

  override fun createFile(fileViewProvider: @NotNull FileViewProvider): @NotNull PsiFile {
    return HCLFileImpl(fileViewProvider, HCLLanguage)
  }

  override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): ParserDefinition.SpaceRequirements {
    if (HCLTokenTypes.HCL_LINE_COMMENTS.contains(left?.elementType)) return ParserDefinition.SpaceRequirements.MUST_LINE_BREAK
    return ParserDefinition.SpaceRequirements.MAY
  }

  companion object {
    val FILE: IFileElementType = IFileElementType(HCLLanguage)

    @JvmStatic
    fun createLexer(): HCLLexer {
      return HCLLexer(EnumSet.of(HCLCapability.INTERPOLATION_LANGUAGE))
    }
  }
}
