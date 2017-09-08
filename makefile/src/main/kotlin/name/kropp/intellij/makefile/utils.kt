package name.kropp.intellij.makefile

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import name.kropp.intellij.makefile.psi.MakefileTarget

fun findAllTargets(project: Project) = MakefileTargetIndex.getAllKeys(project)

fun findTargets(project: Project, name: String): Collection<MakefileTarget> =
    MakefileTargetIndex.get(name, project, GlobalSearchScope.allScope(project))

fun findTargets(psiFile: PsiFile) = PsiTreeUtil.findChildrenOfType(psiFile, MakefileTarget::class.java).asIterable()
