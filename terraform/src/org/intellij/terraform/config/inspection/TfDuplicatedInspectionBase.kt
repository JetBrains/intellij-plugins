// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.*
import com.intellij.usageView.UsageInfo
import com.intellij.usages.*
import com.intellij.util.NullableFunction
import org.intellij.terraform.config.model.getTerraformSearchScope
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.HCLBundle
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

  protected fun createNavigateToDupeFix(psiElement: PsiElement, single: Boolean): LocalQuickFix {
    val psiPointer = psiElement.createSmartPointer()
    return object : LocalQuickFix {
      override fun startInWriteAction(): Boolean = false

      override fun getFamilyName(): String {
        val first = if (!single) HCLBundle.message("duplicated.inspection.base.navigate.to.duplicate.quick.fix.name.first") else ""
        return HCLBundle.message("duplicated.inspection.base.navigate.to.duplicate.quick.fix.name", first)
      }

      override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = psiPointer.element ?: return
        if (element is Navigatable && (element as Navigatable).canNavigate()) {
          (element as Navigatable).navigate(true)
        }
        else {
          OpenFileDescriptor(project, element.containingFile.originalFile.virtualFile, element.textOffset).navigate(true)
        }
      }
    }
  }

  protected fun createShowOtherDupesFix(psiElement: PsiElement, duplicates: NullableFunction<PsiElement, List<PsiElement>?>): LocalQuickFix {
    val psiPointer = psiElement.createSmartPointer()
    return object : LocalQuickFix {
      var myTitle: String? = null

      override fun startInWriteAction(): Boolean = false
      override fun getFamilyName(): String = HCLBundle.message("duplicated.inspection.base.show.other.duplicates.quick.fix.name")

      override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        @Suppress("NAME_SHADOWING")
        val duplicates = ApplicationManager.getApplication().runReadAction<List<PsiElement>?> {
          duplicates.`fun`(descriptor.psiElement)
        } ?: return

        val presentation = UsageViewPresentation()
        val element = psiPointer.element ?: return
        val target = PsiElement2UsageTargetAdapter(element, true)

        if (myTitle == null) myTitle = "Duplicate of " + target.presentableText
        val title = myTitle!!
        presentation.searchString = title
        presentation.tabName = title
        presentation.tabText = title
        val scope = descriptor.psiElement.getTerraformSearchScope()
        presentation.scopeText = scope.displayName

        UsageViewManager.getInstance(project).searchAndShowUsages(arrayOf<UsageTarget>(target), {
          UsageSearcher { processor ->
            val infos = ApplicationManager.getApplication().runReadAction<List<UsageInfo>> {
              duplicates.map { dup -> UsageInfo(dup) }
            }
            for (info in infos) {
              processor.process(UsageInfo2UsageAdapter(info))
            }
          }
        }, false, false, presentation, null)
      }
    }
  }
}

