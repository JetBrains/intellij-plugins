package org.jetbrains.qodana.yaml

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentsWithSelf
import icons.QodanaIcons
import org.jetbrains.qodana.QodanaBundle
import javax.swing.Icon

class QodanaShowInspectionIntention : IntentionAction, Iconable {
  override fun getText(): String = QodanaBundle.message("qodana.yaml.intention.show.inspection")

  override fun getElementToMakeWritable(currentFile: PsiFile): PsiElement = currentFile

  override fun getFamilyName(): String = QodanaBundle.message("qodana.yaml.intention.family")

  override fun startInWriteAction(): Boolean = false

  override fun getIcon(flags: Int): Icon = QodanaIcons.Icons.Qodana

  override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
    if (file == null || !isQodanaYaml(file)) return false
    if (editor == null) return false
    val offset = editor.caretModel.offset

    return findInspectionName(offset, file) != null
  }

  override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
    if (file == null || editor == null) return

    val inspectionName = findInspectionName(editor.caretModel.offset, file)
    if (inspectionName != null) {
      showInspection(file.project, inspectionName)
    }
  }

  private fun findInspectionName(offset: Int, file: PsiFile): String? {
    return iterateOverElements(offset, file).firstNotNullOfOrNull(::getInspectionFromElement)?.shortName
  }

  private fun iterateOverElements(offset: Int, file: PsiFile): Sequence<PsiElement> {
    val leaf1 = file.findElementAt(offset)
    val leaf2 = file.findElementAt(offset - 1)
    val commonParent = if (leaf1 != null && leaf2 != null) PsiTreeUtil.findCommonParent(leaf1, leaf2) else null

    var elementsToCheck: Sequence<PsiElement> = emptySequence()
    if (leaf1 != null) elementsToCheck += leaf1.parentsWithSelf.takeWhile { it != commonParent }
    if (leaf2 != null) elementsToCheck += leaf2.parentsWithSelf.takeWhile { it != commonParent }
    if (commonParent != null && commonParent !is PsiFile) elementsToCheck += commonParent.parentsWithSelf

    return elementsToCheck
  }
}