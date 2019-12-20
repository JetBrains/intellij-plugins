package name.kropp.intellij.makefile.psi

import com.intellij.openapi.util.*
import com.intellij.psi.*

class MakefileRecipeTextEscaper(private val recipe: MakefileRecipe) : LiteralTextEscaper<MakefileRecipe>(recipe) {
  override fun isOneLine(): Boolean {
    return !recipe.textContains('\n')
  }

  override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
    return rangeInsideHost.startOffset + offsetInDecoded
  }

  override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
    return try {
      outChars.append(rangeInsideHost.substring(recipe.text))
      true
    } catch (e: Throwable) {
      false
    }
  }
}