// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template.editor

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.psi.PsiElementVisitor
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.template.doComputeTemplateDataLanguage
import org.intellij.terraform.template.psi.TftplDataLanguageSegment
import org.intellij.terraform.template.psi.TftplVisitor

internal class TfUnselectedDataLanguageInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val dataLanguage = doComputeTemplateDataLanguage(holder.file.virtualFile, holder.project)
    return if (dataLanguage != PlainTextLanguage.INSTANCE) {
      PsiElementVisitor.EMPTY_VISITOR
    }
    else {
      object : TftplVisitor() {
        override fun visitDataLanguageSegment(segment: TftplDataLanguageSegment) {
          holder.registerProblem(segment, HCLBundle.message("inspection.unselected.data.language.name"), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
        }
      }
    }
  }
}