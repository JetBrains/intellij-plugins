// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandlersFactory
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory
import com.intellij.lang.javascript.psi.types.JSUnionType
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.html.VueFileType

class VueFrameworkInsideScriptSpecificHandlersFactory : JSFrameworkSpecificHandlersFactory {
  companion object {
    fun isInsideScript(element: PsiElement): Boolean {
      val tag = PsiTreeUtil.getParentOfType(element, XmlTag::class.java) ?: return false
      return HtmlUtil.isScriptTag(tag)
    }
  }

  override fun findExpectedType(parent: JSExpression, expectedTypeKind: JSExpectedTypeKind): JSType? {
    val language = DialectDetector.languageOfElement(parent)
    if (VueFileType.INSTANCE == parent.containingFile?.fileType
        && isInsideScript(parent)
        && VueJSLanguage.INSTANCE != language) {
      val obj = parent as? JSObjectLiteralExpression
      if (obj?.parent is ES6ExportDefaultAssignment) {
        return createExportedObjectLiteralTypeEvaluator(obj)
      }
    }
    return null
  }

  private fun createExportedObjectLiteralTypeEvaluator(obj: JSObjectLiteralExpression): JSType? {
    val typeAlias = JSFileReferencesUtil.resolveModuleReference(obj.containingFile, "vue")
                      .asSequence()
                      .mapNotNull {
                        if (it !is JSElement) return@mapNotNull null
                        ES6PsiUtil.resolveSymbolInModule("Component", obj, it)
                          .filter { resolveResult ->
                            resolveResult.isValidResult
                          }
                      }
                      .flatten()
                      .filter {
                        val psiFile = it.element?.containingFile
                        psiFile != null && TypeScriptUtil.isDefinitionFile(psiFile)
                      }
                      .mapNotNull { (it.element as? TypeScriptTypeAlias)?.typeDeclaration }
                      .firstOrNull() ?: return null

    var typeFromTypeScript = typeAlias.jsType
    if (typeFromTypeScript is JSUnionType) {
      typeFromTypeScript = JSCompositeTypeFactory.createContextualUnionType(typeFromTypeScript.types,
                                                                            typeFromTypeScript.source)
    }
    return typeFromTypeScript
  }
}
