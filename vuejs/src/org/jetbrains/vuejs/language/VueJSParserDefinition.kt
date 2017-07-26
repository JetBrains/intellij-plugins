package org.jetbrains.vuejs.language

import com.intellij.lang.PsiParser
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lang.javascript.JavascriptParserDefinition
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IFileElementType

/**
 * @author Irina.Chernushina on 7/21/2017.
 */
class VueJSParserDefinition : JavascriptParserDefinition() {
  override fun createParser(project: Project?): PsiParser {
    return PsiParser({ root, builder ->
                       VueJSLanguage.VueJSParser(builder).parseVue(root)
                       return@PsiParser builder.treeBuilt
                     })
  }

  override fun createLexer(project: Project?): Lexer {
    return JSFlexAdapter(DialectOptionHolder.ECMA_6)
  }

  override fun getFileNodeType(): IFileElementType {
    return VueElementTypes.FILE
  }
}