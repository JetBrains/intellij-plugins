// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSInheritedLanguagesConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.angular2.lang.expr.Angular2ExprDialect
import org.jetbrains.annotations.NonNls

class Angular2LanguageConfigurableProvider : JSInheritedLanguagesConfigurableProvider() {
  override fun isNeedToBeTerminated(element: PsiElement): Boolean {
    return false
  }

  override fun createJSContentFromText(project: Project, text: @NonNls String, dialect: JSLanguageDialect?): JSElement =
    if (dialect is Angular2ExprDialect && text.startsWith("/*") && text.endsWith("*/"))
      super.createJSContentFromText(project, text, JavaScriptSupportLoader.TYPESCRIPT)
    else
      super.createJSContentFromText(project, text, dialect)
}
