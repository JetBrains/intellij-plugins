package name.kropp.intellij.makefile

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.util.PsiTreeUtil
import name.kropp.intellij.makefile.psi.MakefileRule
import name.kropp.intellij.makefile.psi.MakefileTarget

class MakefileFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, MakefileLanguage) {
  override fun getFileType() = MakefileFileType

  override fun toString() = "Makefile"

  val rules: Collection<MakefileRule>
      get() = PsiTreeUtil.findChildrenOfType(this, MakefileRule::class.java)

  val targets: Collection<MakefileTarget>
      get() = PsiTreeUtil.findChildrenOfType(this, MakefileTarget::class.java)
}