package com.intellij.dts.inspections.fixes

import com.intellij.dts.DtsBundle
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.DtsPsiFactory
import com.intellij.dts.lang.psi.DtsStatement
import com.intellij.dts.zephyr.binding.DtsZephyrPropertyBinding
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandQuickFix
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.util.containers.headTail

class DtsCreatePropertyFix(private val properties: List<DtsZephyrPropertyBinding>) : PsiUpdateModCommandQuickFix() {
  override fun getFamilyName(): String {
    return DtsBundle.message("inspections.fix.create_property")
  }

  private fun moveCursor(statement: DtsStatement, updater: ModPsiUpdater) {
    if (statement !is DtsProperty) return

    val values = statement.dtsValues

    if (values.isEmpty()) {
      updater.moveCaretTo(statement.textRange.endOffset)
    }
    else {
      updater.moveCaretTo(values.first().dtsValueRange.endOffset)
    }
  }

  override fun applyFix(project: Project, element: PsiElement, updater: ModPsiUpdater) {
    if (element !is DtsNode || properties.isEmpty()) return

    val (head, tail) = properties.headTail()

    val entry = element.addDtsProperty(DtsPsiFactory.createProperty(project, head.name, head.type))
    moveCursor(entry.dtsStatement, updater)

    for (binding in tail) {
      element.addDtsProperty(DtsPsiFactory.createProperty(project, binding.name, binding.type))
    }
  }
}
