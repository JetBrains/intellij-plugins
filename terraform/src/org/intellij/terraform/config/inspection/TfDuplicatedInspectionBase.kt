// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandQuickFix
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.*
import com.intellij.usageView.UsageInfo
import com.intellij.usages.UsageInfo2UsageAdapter
import com.intellij.usages.UsageSearcher
import com.intellij.usages.UsageViewManager
import com.intellij.usages.UsageViewPresentation
import com.intellij.util.text.UniqueNameGenerator
import org.intellij.terraform.config.model.getTerraformSearchScope
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.isTerraformCompatiblePsiFile

abstract class TfDuplicatedInspectionBase : LocalInspectionTool() {

  override fun isAvailableForFile(file: PsiFile): Boolean {
    return isTerraformCompatiblePsiFile(file)
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val file = holder.file
    if (!TfPsiPatterns.TerraformConfigFile.accepts(file)) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    return createVisitor(holder)
  }

  abstract fun createVisitor(holder: ProblemsHolder): PsiElementVisitor

  fun getDefaultFixes(current: HCLElement, duplicates: List<HCLElement>): Array<LocalQuickFix> {
    val fixes = arrayListOf<LocalQuickFix>()

    val first = duplicates.firstOrNull { it != current }
    first?.containingFile?.virtualFile?.let {
      fixes.add(NavigateToDuplicatesQuickFix(first))
    }
    current.containingFile?.virtualFile?.let {
      fixes.add(ShowDuplicatesQuickFix(current, duplicates))
    }

    return fixes.toTypedArray()
  }
}

internal class NavigateToDuplicatesQuickFix(psiElement: PsiElement) : LocalQuickFix {
  private val psiPointer = psiElement.createSmartPointer()

  override fun startInWriteAction(): Boolean = false

  override fun getFamilyName(): String = HCLBundle.message("navigate.to.duplicate.quick.fix.name")

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = psiPointer.element ?: return
    if ((element as? Navigatable)?.canNavigate() == true) {
      element.navigate(true)
    }
    else {
      OpenFileDescriptor(project, element.containingFile.originalFile.virtualFile, element.textOffset).navigate(true)
    }
  }
}

internal class ShowDuplicatesQuickFix(psiElement: PsiElement, duplicates: Collection<PsiElement>) : LocalQuickFix {
  private val psiPointer: SmartPsiElementPointer<PsiElement> = psiElement.createSmartPointer()
  private val duplicatePointers: List<SmartPsiElementPointer<PsiElement>> = duplicates.map { it.createSmartPointer() }

  override fun startInWriteAction(): Boolean = false

  override fun getFamilyName(): String = HCLBundle.message("show.all.duplicates.quick.fix.name")

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = psiPointer.element ?: return

    val target = PsiElement2UsageTargetAdapter(element, true)
    val presentation = UsageViewPresentation().apply {
      val title = HCLBundle.message("show.duplicates.usage.view.title", target.presentableText)
      tabName = title
      tabText = title
      searchString = title
      scopeText = descriptor.psiElement.getTerraformSearchScope().displayName
    }

    val usageInfos = runReadAction {
      duplicatePointers.mapNotNull { pointer ->
        pointer.element?.let { UsageInfo(it) }
      }
    }
    if (usageInfos.isEmpty()) return

    UsageViewManager.getInstance(project).searchAndShowUsages(arrayOf(target), {
      UsageSearcher { processor ->
        usageInfos.forEach { processor.process(UsageInfo2UsageAdapter(it)) }
      }
    }, false, false, presentation, null)
  }
}

internal class RenameBlockQuickFix : PsiUpdateModCommandQuickFix() {
  override fun getFamilyName(): String {
    return HCLBundle.message("rename.hcl.block.quick.fix.name")
  }

  override fun applyFix(project: Project, element: PsiElement, updater: ModPsiUpdater) {
    val block = element as? HCLBlock ?: return
    val currentName = block.name
    val uniqueName = UniqueNameGenerator.generateUniqueNameOneBased(currentName) { it != currentName }
    updater.rename(block, listOf(currentName, uniqueName))
  }
}

internal class DeleteBlockQuickFix : PsiUpdateModCommandQuickFix(), LowPriorityAction {
  override fun getFamilyName(): String {
    return HCLBundle.message("delete.hcl.block.quick.fix.name")
  }

  override fun applyFix(project: Project, element: PsiElement, updater: ModPsiUpdater) {
    (element as? HCLBlock)?.delete()
  }
}