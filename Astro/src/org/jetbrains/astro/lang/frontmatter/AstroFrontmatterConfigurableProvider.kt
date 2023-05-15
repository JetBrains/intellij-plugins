// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.frontmatter

import com.intellij.lang.Language
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSInheritedLanguagesConfigurableProvider
import com.intellij.openapi.project.Project

class AstroFrontmatterConfigurableProvider : JSInheritedLanguagesConfigurableProvider() {

  override fun getLanguage(): Language {
    return AstroFrontmatterLanguage.INSTANCE
  }

  override fun createJSContentFromText(project: Project, text: String, dialect: JSLanguageDialect?): JSElement =
    super.createJSContentFromText(project, text, JavaScriptSupportLoader.TYPESCRIPT)


}