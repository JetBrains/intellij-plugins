// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template.editor

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.file.exclude.OverrideFileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.runtime.TfProjectSettings
import org.intellij.terraform.template.TerraformTemplateFileType
import org.intellij.terraform.template.getLanguageByExtension
import org.intellij.terraform.template.model.findTemplateUsage

internal class MaybeTfTemplateInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    if (isFileWithAlreadyOverriddenTemplateType(holder.file.virtualFile)
        || TfProjectSettings.getInstance(holder.project).isIgnoredTemplateCandidate(holder.file.virtualFile.url)
        || !isPossibleTemplateFile(holder.file)
    ) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    return object : PsiElementVisitor() {
      override fun visitFile(file: PsiFile) {
        holder.registerProblem(file,
                               HCLBundle.message("inspection.possible.template.name"),
                               TfConsiderFileATemplateFix(file.virtualFile),
                               TfIgnoreTemplateCandidateFix(file.createSmartPointer())
        )
      }
    }
  }

  private fun isPossibleTemplateFile(file: PsiFile): Boolean {
    return findTemplateUsage(file).any()
  }
}

internal class TfConsiderFileATemplateFix(private val file: VirtualFile) : LocalQuickFix {
  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val dataLanguageExtension = file.extension
    if (!dataLanguageExtension.isNullOrBlank()) {
      val possibleDataLanguage = getLanguageByExtension(dataLanguageExtension)
      TemplateDataLanguageMappings.getInstance(project).setMapping(file, possibleDataLanguage)
    }
    OverrideFileTypeManager.getInstance().addFile(file, TerraformTemplateFileType)
  }

  override fun getFamilyName(): String {
    return HCLBundle.message("inspection.possible.template.add.association.fix.name")
  }

  override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
    return IntentionPreviewInfo.EMPTY
  }
}

internal class TfIgnoreTemplateCandidateFix(private val filePointer: SmartPsiElementPointer<PsiFile>) : LocalQuickFix {
  override fun getFamilyName(): String {
    return HCLBundle.message("inspection.possible.template.ignore.association.fix.name")
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val psiFile = filePointer.dereference() ?: return
    TfProjectSettings.getInstance(project).addIgnoredTemplateCandidate(psiFile.virtualFile.url)
    DaemonCodeAnalyzer.getInstance(project).restart(psiFile)
  }

  override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
    return IntentionPreviewInfo.EMPTY
  }
}