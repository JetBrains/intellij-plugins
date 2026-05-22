// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.lexer

import com.intellij.lang.Language
import org.intellij.plugins.postcss.styleLanguageOrPostCss

fun styleLanguage(styleLang: String?): Language =
  styleLanguageOrPostCss(styleLang)