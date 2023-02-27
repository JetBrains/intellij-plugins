// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.IncorrectOperationException
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.psi.HCLElementGenerator
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.config.model.getTerraformSearchScope

abstract class HCLStringLiteralMixin(node: ASTNode) : HCLLiteralImpl(node), HCLStringLiteral {
  override fun isValidHost() = true

  companion object {
    val LOG = Logger.getInstance(HCLStringLiteralMixin::class.java)
  }

  override fun updateText(s: String): HCLStringLiteralMixin {
    return updateText(s, true)
  }

  private fun updateText(s: String, changeQuotes: Boolean): HCLStringLiteralMixin {
    if (s.length < 2) {
      val message = "New text '$s' too short: ${s.length}"
      LOG.error(message)
      throw IncorrectOperationException(message)
    }
    assert(s.length >= 2)
    val quote = s[0]
    if (quote != s[s.lastIndex]) {
      val message = "First '$quote' and last '${s.last()}' quotes mismatch, text: $s"
      LOG.error(message)
      throw IncorrectOperationException(message)
    }
    if (!(quote == '\'' || quote == '"')) {
      val message = "Quote symbol not ''' or '\"' : $quote"
      LOG.error(message)
      throw IncorrectOperationException(message)
    }
    val buffer = StringBuilder(s)

    // TODO: Use HIL-aware string escaper (?)

    // Fix quotes if needed
    if (quote != quoteSymbol) {
      if (changeQuotes) {
        buffer[0] = quoteSymbol
        buffer[buffer.lastIndex] = quoteSymbol
      } else {
        return replace(HCLElementGenerator(project).createStringLiteral(buffer.toString(), null)) as HCLStringLiteralMixin
      }
    }
    (node.firstChildNode as LeafElement).replaceWithText(buffer.toString())
    return this
  }

  override fun createLiteralTextEscaper(): LiteralTextEscaper<HCLStringLiteralMixin> {
    return HCLStringLiteralTextEscaper(this)
  }

  override fun getName(): String? {
    return this.value
  }

  fun setName(s: String): HCLStringLiteralMixin {
    val buffer = StringBuilder(s.length)
    // TODO: Use HIL-aware string escaper (?)
    if (node.elementType == HCLElementTypes.SINGLE_QUOTED_STRING) {
      buffer.append('\'')
      StringUtil.escapeStringCharacters(s.length, s, "\'", buffer)
      buffer.append('\'')
    } else {
      buffer.append('\"')
      StringUtil.escapeStringCharacters(s.length, s, "\"", buffer)
      buffer.append('\"')
    }
    return updateText(buffer.toString())
  }

  override fun getUseScope(): SearchScope {
    return this.getTerraformSearchScope()
  }

  override fun getResolveScope(): GlobalSearchScope {
    return this.getTerraformSearchScope()
  }
}
