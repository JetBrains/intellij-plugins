package com.intellij.dts.inspections.fixes

import com.intellij.dts.DtsBundle
import com.intellij.dts.completion.insert.dtsInsertMetaData
import com.intellij.dts.completion.insert.writePropertyValue
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsPsiFactory
import com.intellij.dts.lang.symbols.DtsPropertySymbol
import com.intellij.dts.zephyr.binding.DtsZephyrPropertyBinding
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandQuickFix
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import com.intellij.util.containers.headTail

class DtsCreatePropertyFix(private val properties: List<DtsZephyrPropertyBinding>) : PsiUpdateModCommandQuickFix() {
  override fun getFamilyName(): String {
    return DtsBundle.message("inspections.fix.create_property")
  }

  private fun moveCursor(element: PsiElement, symbol: DtsPropertySymbol, updater: ModPsiUpdater) {
    val metaData = dtsInsertMetaData {
      write(symbol.name)
      writePropertyValue(symbol)
    }

    updater.moveCaretTo(element.startOffset + metaData.offset)
  }

  override fun applyFix(project: Project, element: PsiElement, updater: ModPsiUpdater) {
    if (element !is DtsNode || properties.isEmpty()) return

    val (head, tail) = properties.headTail()

    val symbol = DtsPropertySymbol(head)
    val entry = element.addDtsProperty(DtsPsiFactory.createProperty(project, symbol))
    moveCursor(entry, symbol, updater)

    for (binding in tail) {
      element.addDtsProperty(DtsPsiFactory.createProperty(project, DtsPropertySymbol(binding)))
    }
  }
}
