package com.jetbrains.lang.makefile

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.makefile.psi.MakefileTarget
import icons.MakefileIcons
import javax.swing.Icon

val MakefileTargetIcon: Icon = AllIcons.RunConfigurations.TestState.Run

fun findAllTargets(project: Project) = MakefileTargetIndex.getAllKeys(project)

fun findTargets(project: Project, name: String): Collection<MakefileTarget> =
    MakefileTargetIndex.get(name, project, GlobalSearchScope.allScope(project))

fun findTargets(psiFile: PsiFile) = PsiTreeUtil.findChildrenOfType(psiFile, MakefileTarget::class.java).asIterable()
