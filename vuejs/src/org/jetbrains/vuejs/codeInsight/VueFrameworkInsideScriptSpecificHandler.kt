// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandler
import com.intellij.lang.javascript.psi.JSExpectedTypeKind
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory
import com.intellij.lang.javascript.psi.types.JSUnionType
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.index.VueFrameworkHandler
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.html.VueFileType

class VueFrameworkInsideScriptSpecificHandler : JSFrameworkSpecificHandler {
  companion object {
    fun isInsideScript(element: PsiElement): Boolean {
      val tag = PsiTreeUtil.getParentOfType(element, XmlTag::class.java) ?: return false
      return HtmlUtil.isScriptTag(tag)
    }
  }

  override fun findExpectedType(parent: PsiElement, expectedTypeKind: JSExpectedTypeKind): JSType? {
    if (parent is JSObjectLiteralExpression) {
      val language = DialectDetector.languageOfElement(parent)
      if ((VueFileType.INSTANCE == parent.containingFile?.fileType
           && isInsideScript(parent)
           && VueJSLanguage.INSTANCE != language
           && (VueFrameworkHandler.hasComponentIndicatorProperties(parent) || parent.context is ES6ExportDefaultAssignment))
          || (parent.containingFile is JSFile && VueFrameworkHandler.hasComponentIndicatorProperties(parent))) {
        return getObjectLiteralTypeForComponent(parent)
      }
    }
    return null
  }

  private fun getObjectLiteralTypeForComponent(obj: JSObjectLiteralExpression): JSType? =
    resolveSymbolFromNodeModule(obj, VUE_MODULE, "Component", TypeScriptTypeAlias::class.java)
      ?.typeDeclaration
      ?.jsType
      ?.let {
        if (it is JSUnionType) {
          JSCompositeTypeFactory.createContextualUnionType(it.types, it.source)
        }
        else {
          it
        }
      }
}
