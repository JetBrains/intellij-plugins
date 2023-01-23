// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang

import com.intellij.lang.Language
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSInheritedLanguagesConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import org.jetbrains.astro.codeInsight.astroRoot

class AstroLanguageConfigurableProvider : JSInheritedLanguagesConfigurableProvider() {

  override fun getLanguage(): Language {
    return AstroLanguage.INSTANCE
  }

  override fun createJSContentFromText(project: Project, text: String, dialect: JSLanguageDialect?): JSElement {
    val astroFile = PsiFileFactory.getInstance(project).createFileFromText("dummy.astro", language, "---$text", false, true)
    val root = (astroFile as AstroFileImpl).astroRoot()!!
    // Delete `---` - the JS code assumes that the `getFirstChild` return `JSExpression`
    root.deleteChildInternal(root.firstChild.node)
    return root
  }


}