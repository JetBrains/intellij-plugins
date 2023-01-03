// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding

interface Angular2Interpolation : Angular2EmbeddedExpression {
  val expression: JSExpression?

  companion object {
    @JvmStatic
    fun get(attribute: XmlAttribute): Array<Angular2Interpolation> {
      if (attribute is Angular2HtmlPropertyBinding) {
        return attribute.interpolations
      }
      if (attribute.language.isKindOf(Angular2HtmlLanguage.INSTANCE)) {
        return emptyArray()
      }
      val value = attribute.valueElement
      // Pug and PHP support
      if (value != null && value.textLength >= 2 && Angular2LangUtil.isAngular2Context(value)) {
        var possibleHost = value.containingFile.findElementAt(value.textOffset + 1)
        while (possibleHost != null && possibleHost !== value && possibleHost !is PsiLanguageInjectionHost) {
          possibleHost = possibleHost.parent
        }
        if (possibleHost != null) {
          val injections = InjectedLanguageManager.getInstance(attribute.project).getInjectedPsiFiles(possibleHost)
          if (injections != null) {
            return injections
              .mapNotNull { PsiTreeUtil.findChildOfType(it.first, Angular2Interpolation::class.java) }
              .toTypedArray()
          }
        }
      }
      return emptyArray()
    }
  }
}