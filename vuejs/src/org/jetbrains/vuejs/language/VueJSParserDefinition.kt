// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.language

import com.intellij.lang.PsiParser
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lang.javascript.JavascriptParserDefinition
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IFileElementType

/**
 * @author Irina.Chernushina on 7/21/2017.
 */
class VueJSParserDefinition : JavascriptParserDefinition() {
  private val FILE: IFileElementType = JSFileElementType.create(VueJSLanguage.INSTANCE)

  override fun createParser(project: Project?): PsiParser {
    return PsiParser { root, builder ->
      VueJSLanguage.VueJSParser(builder).parseVue(root)
      return@PsiParser builder.treeBuilt
    }
  }

  override fun createLexer(project: Project?): Lexer {
    return JSFlexAdapter(JSRootConfiguration.getInstance(project).languageLevel.dialect.optionHolder)
  }

  override fun getFileNodeType(): IFileElementType {
    return FILE
  }
}
