// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.sfc.lexer

import com.intellij.psi.xml.XmlTokenType
import org.jetbrains.astro.lang.jsx.parser.AstroJsxBaseExpressionTokenType.Companion.AstroJsxExpressionAttributeTokenType
import org.jetbrains.astro.lang.jsx.parser.AstroJsxBaseExpressionTokenType.Companion.AstroJsxExpressionTokenType
import org.jetbrains.astro.lang.jsx.parser.AstroJsxBaseExpressionTokenType.Companion.AstroJsxShorthandAttributeTokenType
import org.jetbrains.astro.lang.jsx.parser.AstroJsxBaseExpressionTokenType.Companion.AstroJsxSpreadAttributeTokenType
import org.jetbrains.astro.lang.jsx.parser.AstroJsxBaseExpressionTokenType.Companion.AstroJsxTemplateLiteralAttributeTokenType
import org.jetbrains.astro.lang.typescript.AstroFrontmatterScriptTokenType

interface AstroSfcTokenTypes : XmlTokenType {
  companion object {

    @JvmField
    val FRONTMATTER_SEPARATOR = AstroSfcTokenType("ASTRO:FRONTMATTER_SEPARATOR")

    @JvmField
    val FRONTMATTER_SCRIPT = AstroFrontmatterScriptTokenType

    @JvmField
    val SHORTHAND_ATTRIBUTE = AstroJsxShorthandAttributeTokenType

    @JvmField
    val SPREAD_ATTRIBUTE = AstroJsxSpreadAttributeTokenType

    @JvmField
    val EXPRESSION_ATTRIBUTE = AstroJsxExpressionAttributeTokenType

    @JvmField
    val TEMPLATE_LITERAL_ATTRIBUTE = AstroJsxTemplateLiteralAttributeTokenType

    @JvmField
    val EXPRESSION = AstroJsxExpressionTokenType
  }
}
