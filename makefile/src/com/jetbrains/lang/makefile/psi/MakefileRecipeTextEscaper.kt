package com.jetbrains.lang.makefile.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper

class MakefileRecipeTextEscaper(private val recipe: MakefileRecipe) : LiteralTextEscaper<MakefileRecipe>(recipe) {
  override fun isOneLine(): Boolean {
    return !recipe.textContains('\n')
  }

  override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
      var shift = 0
      var i = 0
      for (part in rangeInsideHost.substring(recipe.text).split("$$")) {
        if (i + part.length >= offsetInDecoded) break
        shift++
        i += part.length + 1
      }
      return rangeInsideHost.startOffset + offsetInDecoded + shift
  }

  override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
    return try {
      outChars.append(rangeInsideHost.substring(recipe.text).replace("$$", "$"))
      true
    } catch (e: Throwable) {
      false
    }
  }
}