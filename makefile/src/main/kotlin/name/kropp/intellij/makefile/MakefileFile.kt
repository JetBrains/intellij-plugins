package name.kropp.intellij.makefile

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.util.PsiTreeUtil
import name.kropp.intellij.makefile.psi.MakefileTargetLine

class MakefileFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, MakefileLanguage) {
  override fun getFileType() = MakefileFileType

  override fun toString() = "Makefile"

  val targets: List<String>
      get() = PsiTreeUtil.findChildrenOfType(this, MakefileTargetLine::class.java).map { it.target }
}