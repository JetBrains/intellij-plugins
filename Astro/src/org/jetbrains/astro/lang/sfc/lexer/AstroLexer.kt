// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.sfc.lexer

import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.openapi.project.Project

interface AstroLexer {
  val project: Project?
}