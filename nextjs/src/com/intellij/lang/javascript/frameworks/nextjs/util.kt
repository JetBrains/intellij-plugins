package com.intellij.lang.javascript.frameworks.nextjs

import com.intellij.psi.PsiElement
import com.intellij.webSymbols.context.WebSymbolsContext

internal fun isNextJsContext(element: PsiElement): Boolean =
  WebSymbolsContext.get("nextjs-project", element).let { it == "nextjs" || it == "true" }
