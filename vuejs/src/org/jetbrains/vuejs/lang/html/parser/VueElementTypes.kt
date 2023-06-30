// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.LighterASTNode
import com.intellij.lang.LighterLazyParseableNode
import com.intellij.lang.PsiBuilderFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.ILazyParseableElementType
import com.intellij.psi.tree.ILightLazyParseableElementType
import com.intellij.util.diff.FlyweightCapableTreeStructure
import org.jetbrains.vuejs.lang.VueScriptLangs
import org.jetbrains.vuejs.lang.html.VueFileElementType
import org.jetbrains.vuejs.lang.html.VueFileType.Companion.isDotVueFile
import org.jetbrains.vuejs.lang.html.VueLanguage

object VueElementTypes {

  val VUE_EMBEDDED_CONTENT: IElementType = EmbeddedVueContentElementType()

  class EmbeddedVueContentElementType : ILazyParseableElementType("VUE_EMBEDDED_CONTENT", VueLanguage.INSTANCE),
                                        ILightLazyParseableElementType {

    override fun parseContents(chameleon: LighterLazyParseableNode): FlyweightCapableTreeStructure<LighterASTNode> {
      val file = chameleon.containingFile ?: error(chameleon)

      val htmlCompatMode = !file.isDotVueFile
      val lexer = VueParserDefinition.createLexer(file.project, null, htmlCompatMode, file.getUserData(VueScriptLangs.LANG_MODE))
      val builder = PsiBuilderFactory.getInstance().createBuilder(file.project, chameleon, lexer, language, chameleon.text)
      builder.putUserData(VueScriptLangs.LANG_MODE, file.getUserData(VueScriptLangs.LANG_MODE))
      builder.putUserData(VueParsing.HTML_COMPAT_MODE, htmlCompatMode)
      VueParser().parseWithoutBuildingTree(VueFileElementType.INSTANCE, builder)
      return builder.lightTree
    }

    override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode {
      val file = psi.containingFile ?: error(chameleon)

      val htmlCompatMode = !file.isDotVueFile
      val lexer = VueParserDefinition.createLexer(file.project, null, htmlCompatMode, file.getUserData(VueScriptLangs.LANG_MODE))
      val builder = PsiBuilderFactory.getInstance().createBuilder(file.project, chameleon, lexer, language, chameleon.chars)
      builder.putUserData(VueScriptLangs.LANG_MODE, file.getUserData(VueScriptLangs.LANG_MODE))
      builder.putUserData(VueParsing.HTML_COMPAT_MODE, htmlCompatMode)
      val node = VueParser().parse(VueFileElementType.INSTANCE, builder)
      return node.firstChildNode
    }
  }
}
