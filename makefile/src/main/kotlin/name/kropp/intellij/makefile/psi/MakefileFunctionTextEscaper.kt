package name.kropp.intellij.makefile.psi

import com.intellij.openapi.util.*
import com.intellij.psi.*

class MakefileFunctionTextEscaper(private val function: MakefileFunction) : LiteralTextEscaper<MakefileFunction>(function) {
  override fun isOneLine(): Boolean {
    return !function.textContains('\n')
  }

  override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
    return rangeInsideHost.startOffset + offsetInDecoded
  }

  override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
    return try {
      outChars.append(rangeInsideHost.substring(function.text))
      true
    } catch (e: Throwable) {
      false
    }
  }
}