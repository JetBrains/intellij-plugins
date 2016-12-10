package name.kropp.intellij.makefile

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider

class MakefileFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, MakefileLanguage) {
  override fun getFileType() = MakefileFileType

  override fun toString() = "Makefile"
}