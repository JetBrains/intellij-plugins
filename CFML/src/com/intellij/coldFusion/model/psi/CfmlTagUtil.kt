// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.coldFusion.model.psi

import com.intellij.coldFusion.model.CfmlLanguage
import com.intellij.coldFusion.model.CfmlUtil.getCfmlLangInfo
import com.intellij.coldFusion.model.files.CfmlFileViewProvider
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil

object CfmlTagUtil {

  fun getStartTagNameElement(tag: CfmlTag): LeafPsiElement? {
    val node = tag.node ?: return null

    var current: ASTNode? = node.firstChildNode
    var elementType: IElementType? = current?.elementType
    while (current != null && elementType !== CfmlTokenTypes.CF_TAG_NAME) {
      current = current.treeNext
      elementType = current.elementType
    }
    return if (current == null) null else current.psi as LeafPsiElement
  }

  private fun getEndTagNameElement(tag: CfmlTag): LeafPsiElement? {
    val node = tag.node ?: return null

    var current: ASTNode? = node.lastChildNode
    var prev = current

    while (current != null) {
      val elementType = prev!!.elementType
      if ((elementType === CfmlTokenTypes.CF_TAG_NAME) && current.elementType === CfmlTokenTypes.LSLASH_ANGLEBRACKET) {
        return prev.psi as LeafPsiElement
      }

      prev = current
      current = current.treePrev

    }
    return null
  }

  fun isClosingTag(provider: CfmlFileViewProvider, offset: Int): Boolean {
    return getTextByOffset(provider, offset) == "/" && getTextByOffset(provider, offset - 1) == "<"
  }

  /**
   * returns a parent CfmlTag if it is not closed; null it opposite case.
   */
  fun getUnclosedParentTag(cfmlElement: PsiElement): CfmlTag? {
    val cfmlParentTag = PsiTreeUtil.getParentOfType(cfmlElement, CfmlTag::class.java)
    return if (cfmlParentTag != null && CfmlTagUtil.isUnclosedTag(cfmlParentTag)) cfmlParentTag
    else null
  }

  fun isUnclosedTag(cfmlTag: CfmlTag): Boolean {
    val tagDescription = getCfmlLangInfo(cfmlTag.project).tagAttributes[cfmlTag.tagName] ?: return false
    if (tagDescription.isSingle) return false
    return getEndTagNameElement(cfmlTag) == null
  }

  private fun getTextByOffset(provider: CfmlFileViewProvider, offset: Int) =
    provider.findElementAt(offset - 1, CfmlLanguage::class.java)?.text

}