package com.intellij.lang.javascript.frameworks.nextjs

import com.intellij.polySymbols.context.PolyContext
import com.intellij.psi.PsiElement

internal fun isNextJsContext(element: PsiElement): Boolean =
  PolyContext.get("nextjs-project", element).let { it == "nextjs" || it == "true" }
