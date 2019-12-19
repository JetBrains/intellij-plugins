package name.kropp.intellij.makefile.psi

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import java.lang.StringBuilder

abstract class MakefileCommandElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), MakefileCommand {
  override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
    return CommandTextEscaper(this)
  }

  override fun updateText(text: String): PsiLanguageInjectionHost {
    val command = MakefileElementFactory.createCommand(project, text)
    return this.replace(command) as MakefileCommand
  }

  override fun isValidHost(): Boolean {
    return true
  }
}

class CommandTextEscaper(private val command: MakefileCommand) : LiteralTextEscaper<MakefileCommand>(command) {
  override fun isOneLine(): Boolean {
    return true
  }

  override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
    return rangeInsideHost.startOffset + offsetInDecoded
  }

  override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
    return try {
      val start = rangeInsideHost.startOffset - command.textRange.startOffset
      val end = rangeInsideHost.endOffset - command.textRange.startOffset
      outChars.append(command.text.substring(start, end))
      true
    } catch (e: Throwable) {
      false
    }
  }
}