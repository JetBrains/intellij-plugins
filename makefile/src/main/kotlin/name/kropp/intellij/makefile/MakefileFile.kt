package name.kropp.intellij.makefile

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.util.PsiTreeUtil
import name.kropp.intellij.makefile.psi.MakefileRule
import name.kropp.intellij.makefile.psi.MakefileTarget
import name.kropp.intellij.makefile.psi.MakefileVariable

class MakefileFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, MakefileLanguage) {
  override fun getFileType() = MakefileFileType

  override fun toString() = "Makefile"

  val rules: Collection<MakefileRule>
      get() = PsiTreeUtil.findChildrenOfType(this, MakefileRule::class.java)

  val allTargets: Collection<MakefileTarget>
      get() = PsiTreeUtil.findChildrenOfType(this, MakefileTarget::class.java)

  val targets: Collection<MakefileTarget>
      get() = allTargets.filter { !it.isSpecialTarget }

  val specialTargets: Collection<MakefileTarget>
      get() = allTargets.filter { it.isSpecialTarget }

  val phonyRules: Collection<MakefileRule>
    get() = rules.filter { it.targets.size == 1 && it.targets.single().name == ".PHONY" }

  val variables: Collection<MakefileVariable>
    get() = PsiTreeUtil.findChildrenOfType(this, MakefileVariable::class.java)
}