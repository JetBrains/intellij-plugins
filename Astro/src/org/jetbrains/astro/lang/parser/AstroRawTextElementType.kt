// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro.lang.parser

import com.intellij.html.embedding.HtmlRawTextElementType
import com.intellij.lexer.Lexer
import org.jetbrains.astro.lang.AstroLanguage
import org.jetbrains.astro.lang.lexer.AstroRawTextLexer

object AstroRawTextElementType : HtmlRawTextElementType("ASTRO_RAW_TEXT", AstroLanguage.INSTANCE) {

  override fun createLexer(): Lexer =
    AstroRawTextLexer(false)

}