package com.intellij.dts.completion

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.dts.completion.provider.*
import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class DtsCompletionContributor : CompletionContributor() {
  class AutoPopup : TypedHandlerDelegate() {
    override fun checkAutoPopup(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
      if (file !is DtsFile || c != '/') return Result.CONTINUE

      AutoPopupController.getInstance(project).scheduleAutoPopup(editor)

      return Result.CONTINUE
    }
  }

  init {
    val base = dtsBasePattern().and(dtsInsideContainer())

    extend(CompletionType.BASIC, base, DtsCompilerDirectiveProvider)
    extend(CompletionType.BASIC, base, DtsRootNodeProvider)

    val propertyName = base
      .withElementType(DtsTypes.NAME)
      .withParent(dtsProperty())
    extend(CompletionType.BASIC, propertyName, DtsPropertyNameProvider)

    val subNodeName = base
      .withElementType(DtsTypes.NAME)
      .withParent(dtsSubNode())
    extend(CompletionType.BASIC, subNodeName, DtsNodeNameProvider)

    val insideString = dtsBasePattern()
      .withElementType(DtsTypes.STRING_LITERAL)
      .withParent(dtsFirstValue())
    extend(CompletionType.BASIC, insideString, DtsStringValueProvider)

    val insideInt = dtsBasePattern()
      .withParent(dtsFirstCell().withParent(dtsFirstValue()))
    extend(CompletionType.BASIC, insideInt, DtsIntValueProvider)
  }
}