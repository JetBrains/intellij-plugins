// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.angular2.inspections.quickfixes.Angular2FixesPsiUtil.insertJSObjectLiteralProperty
import org.angular2.inspections.quickfixes.Angular2FixesPsiUtil.reformatJSObjectLiteralProperty
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls

class AddJSPropertyQuickFix(objectLiteral: JSObjectLiteralExpression,
                            private val myPropertyName: String,
                            private val myValue: String,
                            private val myCaretOffset: Int,
                            private val myUseTemplateString: Boolean) : LocalQuickFixOnPsiElement(objectLiteral) {

  @Nls(capitalization = Nls.Capitalization.Sentence)
  override fun getText(): String {
    return Angular2Bundle.message("angular.quickfix.decorator.add-property.name", myPropertyName)
  }

  @Nls(capitalization = Nls.Capitalization.Sentence)
  override fun getFamilyName(): String {
    return Angular2Bundle.message("angular.quickfix.decorator.add-property.family")
  }

  override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
    val objectLiteral = startElement as? JSObjectLiteralExpression ?: return
    val quote: String = if (myUseTemplateString) "`"
    else
      JSCodeStyleSettings.getQuote(objectLiteral)

    val documentManager = PsiDocumentManager.getInstance(objectLiteral.project)
    val document = documentManager.getDocument(objectLiteral.containingFile)

    val value = if (document == null)
      quote + myValue + quote
    else
      quote + myValue.substring(0, myCaretOffset) + CARET_MARKER + myValue.substring(myCaretOffset) + quote

    val added = reformatJSObjectLiteralProperty(insertJSObjectLiteralProperty(objectLiteral, myPropertyName, value))
    val valueExpression = added.value!!

    if (document == null) return

    val caretOffset = valueExpression.textOffset + valueExpression.text.indexOf(CARET_MARKER)

    val targetFile = added.containingFile.virtualFile
    document.replaceString(caretOffset, caretOffset + CARET_MARKER.length, "")
    PsiDocumentManager.getInstance(project).commitDocument(document)

    PsiNavigationSupport.getInstance().createNavigatable(project, targetFile, caretOffset).navigate(true)
  }

  companion object {
    @NonNls
    private const val CARET_MARKER = "___caret___"
  }
}
