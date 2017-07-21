package org.jetbrains.vuejs.language

import com.intellij.lang.PsiParser
import com.intellij.lang.javascript.JavascriptParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IFileElementType

/**
 * @author Irina.Chernushina on 7/21/2017.
 */
class VueJSParserDefinition : JavascriptParserDefinition() {
  override fun createParser(project: Project?): PsiParser {
    return PsiParser({ root, builder ->
                       VueJSLanguage.VueJSParser(builder).parseJS(root)
                       return@PsiParser builder.treeBuilt
                     })
  }

  override fun getFileNodeType(): IFileElementType {
    return VueElementTypes.FILE
  }
}