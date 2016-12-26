package name.kropp.intellij.makefile

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.FileBasedIndex
import name.kropp.intellij.makefile.psi.MakefileTarget

fun findTargets(project: Project): List<MakefileTarget> {
  val psiManager = PsiManager.getInstance(project)
  return FileBasedIndex.getInstance()
      .getContainingFiles(FileTypeIndex.NAME, MakefileFileType, GlobalSearchScope.allScope(project))
      .mapNotNull { psiManager.findFile(it) }
      .flatMap { PsiTreeUtil.findChildrenOfType(it, MakefileTarget::class.java).asIterable() }
}

fun findTargets(project: Project, name: String): List<MakefileTarget> {
  val psiManager = PsiManager.getInstance(project)
  return FileBasedIndex.getInstance()
      .getContainingFiles(FileTypeIndex.NAME, MakefileFileType, GlobalSearchScope.allScope(project))
      .mapNotNull { psiManager.findFile(it) }
      .flatMap { PsiTreeUtil.findChildrenOfType(it, MakefileTarget::class.java).asIterable() }
      .filter { it.text == name }
}