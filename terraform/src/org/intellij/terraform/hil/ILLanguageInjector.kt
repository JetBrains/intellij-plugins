// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil

import com.intellij.openapi.util.TextRange
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiLanguageInjectionHost
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntList
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.hcl.psi.HCLHeredocContent
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.hcl.psi.impl.HCLPsiImplUtils
import org.intellij.terraform.hil.HILElementTypes.*

internal class ILLanguageInjector : LanguageInjector {
  override fun getLanguagesToInject(host: PsiLanguageInjectionHost, places: InjectedLanguagePlaces) {
    return Companion.getLanguagesToInject(host, places)
  }

  enum class Type(val prefixes: Set<String>) {
    INTERPOLATION("\${"),
    TEMPLATE("%{"),
    ANY("\${", "%{"),

    ;

    constructor(vararg prefixes: String) : this(prefixes.toSet())
  }

  companion object {
    fun getLanguagesToInject(host: PsiLanguageInjectionHost, places: InjectedLanguagePlaces, type: Type = Type.ANY) {
      if (host !is HCLStringLiteral && host !is HCLHeredocContent) return
      // Only .tf (Terraform config) files
      val file = host.containingFile
      if (file !is HCLFile || !file.isInterpolationsAllowed()) return
      // Restrict interpolations in .tfvars files // TODO: This file shouldn't know about .tfvars here
      if (TfPsiPatterns.TerraformVariablesFile.accepts(file)) return
      if (host is HCLStringLiteral) return getStringLiteralInjections(host, places, type)
      if (host is HCLHeredocContent) return getHCLHeredocContentInjections(host, places, type)
      return
    }

    fun getStringLiteralInjections(host: HCLStringLiteral, places: InjectedLanguagePlaces, type: Type = Type.ANY) {
      if (!host.textContains('{')) return

      val text = host.text
      if (!type.prefixes.any { text.contains(it) }) return // TODO: Consider removing check

      for (pair in host.textFragments) {
        val fragment = pair.second
        if (!type.prefixes.any { fragment.startsWith(it) }) continue
        val ranges = getILRangesInText(fragment, type)
        for (range in ranges) {
          val rng = range.shiftRight(pair.first.startOffset)
          places.addPlace(HILLanguage, rng, null, null)
        }
      }
    }

    fun getHCLHeredocContentInjections(host: HCLHeredocContent, places: InjectedLanguagePlaces, type: Type = Type.ANY) {
      if (host.linesCount == 0) return
      val lines = HCLPsiImplUtils.getLinesInternal(host)
      if (lines.isEmpty()) return
      if (!type.prefixes.any { host.text.contains(it) }) return // TODO: Consider removing check

      for (pair in host.textFragments) {
        val fragment = pair.second
        if (!type.prefixes.any { fragment.startsWith(it) }) continue
        val ranges = getILRangesInText(fragment, type)
        for (range in ranges) {
          val rng = range.shiftRight(pair.first.startOffset)
          places.addPlace(HILLanguage, rng, null, null)
        }
      }
    }

    fun getILRangesInText(text: String, interpolationType: Type = Type.ANY): ArrayList<TextRange> {
      if (!interpolationType.prefixes.any { text.contains(it) }) return arrayListOf()

      var skip = findInterpolationStart(text, interpolationType)
      if (skip == -1) return arrayListOf()

      val ranges: ArrayList<TextRange> = ArrayList()
      out@ while (true) {
        if (skip >= text.length) break

        val lexer = HILLexer()
        lexer.start(text, skip, text.length)
        val stack = IntArrayList(4)
        while (true) {
          when (lexer.tokenType) {
            INTERPOLATION_START, TEMPLATE_START -> {
              stack.push(lexer.tokenStart)
            }
            L_CURLY -> {
              stack.push(-1)
            }
            R_CURLY -> {
              if (stack.isEmpty) {
                // Incorrect state, probably just '}' in text retry from current position.
                skip = lexer.tokenStart + 1
                continue@out
              }
              val start = stack.popInt()
              if (start != -1 && getDeepestNonNegative(stack) == -1) {
                // No more interpolations in stack, only curly braces
                ranges.add(TextRange(start, lexer.tokenEnd))
                skip = lexer.tokenEnd
                continue@out
              }
            }
            null -> {
              if (lexer.tokenEnd >= text.length) {
                // Real end of string
                // Maybe non finished interpolation
                val start = getDeepestNonNegative(stack)
                if (start != -1) {
                  ranges.add(TextRange(start, Math.min(lexer.tokenEnd, text.length)))
                }
                break@out
              } else {
                // Non-parsable, probably not IL, retry from current position.
                skip = lexer.tokenStart + 1
                continue@out
              }
            }
            else -> {
              if (stack.isEmpty) {
                // Non-parsable, probably not IL, retry from current position.
                skip = lexer.tokenStart + 1
                continue@out
              }
            }
          }
          lexer.advance()
        }
      }
      return ranges
    }

    private fun getDeepestNonNegative(stack: IntList): Int {
      for (i in 0 until stack.size) {
        val element = stack.getInt(i)
        if (element >= 0) return element
      }
      return -1
    }

    private fun findInterpolationStart(text: String, type: Type): Int {
      var index: Int = -1
      do {
        index = text.indexOfAny(type.prefixes.toList(), index + 1)
      } while (index > 0 && text[index - 1] == text[index])
      return index
    }

  }
}
