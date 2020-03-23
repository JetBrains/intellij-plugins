package name.kropp.intellij.makefile.psi

import com.intellij.openapi.util.*
import com.intellij.psi.*

class MakefileFunctionTextEscaper(private val function: MakefileFunction) : LiteralTextEscaper<MakefileFunction>(function) {
  override fun isOneLine(): Boolean {
    return !function.textContains('\n')
  }

  override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
    var shift = 0
    var i = 0
    for (part in rangeInsideHost.substring(function.text).split("$$")) {
      if (i + part.length >= offsetInDecoded) break
      shift++
      i += part.length + 1
    }
    return rangeInsideHost.startOffset + offsetInDecoded + shift
  }

  override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
    return try {
      outChars.append(rangeInsideHost.substring(function.text).replace("$$", "$"))
      true
    } catch (e: Throwable) {
      false
    }
  }
}