// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.terraform.template.editor

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.psi.PsiElementVisitor
import com.intellij.terraform.template.TftplBundle
import com.intellij.terraform.template.doComputeTemplateDataLanguage
import com.intellij.terraform.template.psi.TftplDataLanguageSegment
import com.intellij.terraform.template.psi.TftplVisitor

internal class TfUnselectedDataLanguageInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val dataLanguage = doComputeTemplateDataLanguage(holder.file.virtualFile, holder.project)
    return if (dataLanguage != PlainTextLanguage.INSTANCE) {
      PsiElementVisitor.EMPTY_VISITOR
    }
    else {
      object : TftplVisitor() {
        override fun visitDataLanguageSegment(segment: TftplDataLanguageSegment) {
          holder.registerProblem(segment, TftplBundle.message("inspection.unselected.data.language.name"), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
        }
      }
    }
  }
}