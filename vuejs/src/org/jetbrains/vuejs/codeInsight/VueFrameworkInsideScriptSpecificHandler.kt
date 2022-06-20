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
import com.intellij.lang.javascript.psi.types.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.index.VueFrameworkHandler
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.source.COMPUTED_PROP
import org.jetbrains.vuejs.model.source.METHODS_PROP
import org.jetbrains.vuejs.model.source.WATCH_PROP

class VueFrameworkInsideScriptSpecificHandler : JSFrameworkSpecificHandler {
  companion object {
    fun isInsideScript(element: PsiElement): Boolean {
      val tag = PsiTreeUtil.getParentOfType(element, XmlTag::class.java) ?: return false
      return HtmlUtil.isScriptTag(tag)
    }
  }

  override fun findExpectedType(element: PsiElement, expectedTypeKind: JSExpectedTypeKind): JSType? {
    if (element is JSObjectLiteralExpression) {
      val language = DialectDetector.languageOfElement(element)
      if ((VueFileType.INSTANCE == element.containingFile?.fileType
           && isInsideScript(element)
           && VueJSLanguage.INSTANCE != language
           && (VueFrameworkHandler.hasComponentIndicatorProperties(element) || element.context is ES6ExportDefaultAssignment))
          || (element.containingFile is JSFile && VueFrameworkHandler.hasComponentIndicatorProperties(element)
              && isVueContext(element))) {
        return getObjectLiteralTypeForComponent(element)
      }
    }
    return null
  }

  private fun getObjectLiteralTypeForComponent(obj: JSObjectLiteralExpression): JSType? {
    /* The information about `this` type is processed only in TS context. */
    val thisType = VueModelManager.findEnclosingComponent(obj)
      ?.thisType
      ?.let {
        JSGenericTypeImpl(it.source, JSNamedTypeFactory.createType("ThisType", it.source, JSTypeContext.INSTANCE), it)
      }
    val propertiesWithThisType =
      if (thisType != null)
        JSRecordTypeImpl(thisType.source, sequenceOf(COMPUTED_PROP, METHODS_PROP, WATCH_PROP)
          .map { JSRecordTypeImpl.PropertySignatureImpl(it, thisType, true, false) }
          .toList()
        )
      else null
    val definitionType = resolveSymbolFromNodeModule(obj, VUE_MODULE, "Component", TypeScriptTypeAlias::class.java)
      ?.typeDeclaration
      ?.jsType
    return listOfNotNull(definitionType, thisType, propertiesWithThisType)
      .takeIf { it.isNotEmpty() }
      ?.let {
        JSCompositeTypeFactory.createUnionType(it[0].source, it)
      }
      ?.let {
        if (it is JSUnionType) {
          JSCompositeTypeFactory.createContextualUnionType(it.types, it.source)
        }
        else it
      }
  }
}
