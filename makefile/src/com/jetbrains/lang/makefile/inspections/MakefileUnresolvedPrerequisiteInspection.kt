package com.jetbrains.lang.makefile.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.jetbrains.lang.makefile.MakefileLangBundle
import com.jetbrains.lang.makefile.MakefileTargetReference
import com.jetbrains.lang.makefile.findTargetLine
import com.jetbrains.lang.makefile.psi.MakefilePrerequisite
import com.jetbrains.lang.makefile.psi.MakefileVisitor

class MakefileUnresolvedPrerequisiteInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
    return object : MakefileVisitor() {
      override fun visitPrerequisite(prerequisite: MakefilePrerequisite) {
        if (Regex("""\$\((.*)\)""").matches(prerequisite.text)) {
          return
        }

        val targetLine = prerequisite.findTargetLine() ?: return
        val targets = targetLine.targets.targetList

        if (targets.firstOrNull()?.isSpecialTarget == false && targetLine.targetPattern == null) {
          val targetReferences = prerequisite.references.any { it is MakefileTargetReference && it.multiResolve(false).isNotEmpty() }

          var fileReferenceResolved = false
          var unresolvedFile: TextRange? = null
          prerequisite.references.filterIsInstance<FileReference>().forEach {
            if (it.resolve() == null) {
              if (!targetReferences) {
                unresolvedFile = unresolvedFile?.union(it.rangeInElement) ?: it.rangeInElement
              }
            } else {
              fileReferenceResolved = true
            }
          }

          if (!targetReferences && !fileReferenceResolved) {
            val fix = CreateRuleFix()
            holder.registerProblem(
              prerequisite,
              MakefileLangBundle.message("inspection.message.unresolved.prerequisite"),
              fix
            )
          } else if (unresolvedFile != null) {
            holder.registerProblem(
              prerequisite,
              unresolvedFile!!,
              MakefileLangBundle.message("inspection.message.file.not.found")
            )
          }
        }

      }
    }
  }
}