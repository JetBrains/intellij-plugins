// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.refactoring

import com.intellij.codeInsight.CodeInsightUtilCore
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore
import com.intellij.refactoring.IntroduceTargetChooser
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser
import com.intellij.refactoring.listeners.RefactoringEventData
import com.intellij.refactoring.listeners.RefactoringEventListener
import com.intellij.refactoring.util.CommonRefactoringUtil
import org.intellij.terraform.config.model.Type
import org.intellij.terraform.config.model.Types
import org.intellij.terraform.config.model.getType
import org.intellij.terraform.config.psi.TfElementGenerator
import org.intellij.terraform.config.refactoring.BaseIntroduceOperation
import org.intellij.terraform.config.refactoring.BaseIntroduceVariableHandler
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.hil.psi.*
import org.intellij.terraform.hil.psi.impl.getHCLHost
import org.jetbrains.annotations.NonNls

open class ILIntroduceVariableHandler : BaseIntroduceVariableHandler<ILExpression>() {
  companion object {
    @NonNls val REFACTORING_ID = "hil.refactoring.extractVariable"
    fun findOccurrenceUnderCaret(occurrences: List<PsiElement>, editor: Editor): PsiElement? {
      if (occurrences.isEmpty()) {
        return null
      }
      val offset = editor.caretModel.offset
      occurrences.firstOrNull { it.textRange.contains(offset) }?.let { return it }
      val line = editor.document.getLineNumber(offset)
      for (occurrence in occurrences) {
        PsiUtilCore.ensureValid(occurrence)
        if (occurrence.isValid && editor.document.getLineNumber(occurrence.textRange.startOffset) == line) {
          return occurrence
        }
      }
      for (occurrence in occurrences) {
        PsiUtilCore.ensureValid(occurrence)
        return occurrence
      }
      return null
    }

    fun findAnchor(occurrence: PsiElement): PsiElement? {
      return findAnchor(listOf(occurrence))
    }

    fun findAnchor(occurrences: List<PsiElement>): PsiElement? {

      val hosts = occurrences.mapNotNull {
        when (it) {
          is BaseExpression -> it.getHCLHost()
          else -> it
        }
      }

      if (hosts.isEmpty()) return null

      val minOffset = hosts
        .map { it.textRange.startOffset }
        .minOrNull()!!

      val statements = hosts.mapNotNull { it.containingFile }.firstOrNull() ?: return null

      var child: PsiElement? = null
      for (aChildren in statements.children) {
        child = aChildren
        if (child.textRange.contains(minOffset)) {
          break
        }
      }

      return child
    }
  }

  override fun createOperation(editor: Editor, file: PsiFile, project: Project) = IntroduceOperation(project, editor, file, null)

  override fun performAction(operation: BaseIntroduceOperation<ILExpression>) {
    if (operation !is IntroduceOperation) return
    val file = operation.file
    if (!CommonRefactoringUtil.checkReadOnlyStatus(file)) {
      return
    }
    val editor = operation.editor
    if (editor.settings.isVariableInplaceRenameEnabled) {
      val templateState = TemplateManagerImpl.getTemplateState(operation.editor)
      if (templateState != null && !templateState.isFinished) {
        return
      }
    }

    var element1: PsiElement? = null
    var element2: PsiElement? = null
    val selectionModel = editor.selectionModel
    if (selectionModel.hasSelection()) {
      element1 = file.findElementAt(selectionModel.selectionStart)
      element2 = file.findElementAt(selectionModel.selectionEnd - 1)
      if (element1 is PsiWhiteSpace) {
        val startOffset = element1.textRange.endOffset
        element1 = file.findElementAt(startOffset)
      }
      if (element2 is PsiWhiteSpace) {
        val endOffset = element2.textRange.startOffset
        element2 = file.findElementAt(endOffset - 1)
      }
    } else {
      if (smartIntroduce(operation)) {
        return
      }
      val caretModel = editor.caretModel
      val document = editor.document
      val lineNumber = document.getLineNumber(caretModel.offset)
      if (lineNumber >= 0 && lineNumber < document.lineCount) {
        element1 = file.findElementAt(document.getLineStartOffset(lineNumber))
        element2 = file.findElementAt(document.getLineEndOffset(lineNumber) - 1)
      }
    }
    val project = operation.project
    if (element1 == null || element2 == null) {
      showCannotPerformError(project, editor)
      return
    }

    element1 = ILRefactoringUtil.getSelectedExpression(element1, element2)
    if (element1 == null || !isValidIntroduceVariant(element1)) {
      showCannotPerformError(project, editor)
      return
    }

    if (!checkIntroduceContext(file, editor, element1)) {
      return
    }
    operation.element = element1
    performActionOnElement(operation)
  }


  private fun smartIntroduce(operation: IntroduceOperation): Boolean {
    val editor = operation.editor
    val file = operation.file
    val offset = editor.caretModel.offset
    var elementAtCaret = file.findElementAt(offset)
    if ((elementAtCaret is PsiWhiteSpace && offset == elementAtCaret.textOffset || elementAtCaret == null) && offset > 0) {
      elementAtCaret = file.findElementAt(offset - 1)
    }
    if (!checkIntroduceContext(file, editor, elementAtCaret)) return true
    val expressions = ArrayList<ILExpression>()
    while (elementAtCaret != null) {
      if (elementAtCaret is ILExpressionHolder || elementAtCaret is ILPsiFile) {
        break
      }
      if (elementAtCaret is ILExpression && isValidIntroduceVariant(elementAtCaret)) {
        expressions.add(elementAtCaret)
      }
      elementAtCaret = elementAtCaret.parent
    }
    if (expressions.size == 1 || ApplicationManager.getApplication().isUnitTestMode) {
      operation.element = expressions[0]
      performActionOnElement(operation)
      return true
    } else if (expressions.size > 1) {
      IntroduceTargetChooser.showChooser(editor, expressions, object : Pass<ILExpression>() {
        override fun pass(expression: ILExpression) {
          operation.element = expression
          performActionOnElement(operation)
        }
      }, ILExpression::getText)
      return true
    }
    return false
  }


  private fun checkIntroduceContext(file: PsiFile, editor: Editor, element: PsiElement?): Boolean {
    if (!isValidIntroduceContext(element)) {
      showCannotPerformError(file.project, editor)
      return false
    }
    return true
  }

  private fun isValidIntroduceContext(element: PsiElement?): Boolean {
    // TODO: Investigate cases when refactoring should not be supported
    return element != null
  }

  private fun isValidIntroduceVariant(element: PsiElement): Boolean {
    val call = element.parent as? ILMethodCallExpression
    if (call != null && call.callee === element) {
      return false
    }
    if (element is ILParameterList) return false
    return element is ILLiteralExpression
  }

  private fun performActionOnElement(operation: IntroduceOperation) {
    val element = operation.element
    val initializer = element as ILExpression?
    operation.initializer = initializer

    if (initializer != null) {
      operation.occurrences = getOccurrences(element, initializer)
      operation.suggestedNames = getSuggestedNames(initializer)
    }
    if (operation.occurrences.isEmpty()) {
      operation.isReplaceAll = false
    }

    performActionOnElementOccurrences(operation)
  }

  private fun performActionOnElementOccurrences(operation: IntroduceOperation) {
    val editor = operation.editor
    if (editor.settings.isVariableInplaceRenameEnabled) {
      ensureName(operation)
      if (operation.isReplaceAll) {
        performInplaceIntroduce(operation)
      } else {
        OccurrencesChooser.simpleChooser<PsiElement>(editor).showChooser(operation.element, operation.occurrences, object : Pass<OccurrencesChooser.ReplaceChoice>() {
          override fun pass(replaceChoice: OccurrencesChooser.ReplaceChoice) {
            operation.isReplaceAll = replaceChoice == OccurrencesChooser.ReplaceChoice.ALL
            performInplaceIntroduce(operation)
          }
        })
      }
    } else {
      performIntroduceWithDialog(operation)
    }
  }


  protected fun performInplaceIntroduce(operation: IntroduceOperation) {
    val statement = performRefactoring(operation)
    if (statement is HCLBlock) {
      val target = statement.nameIdentifier!!
      val occurrences = operation.occurrences
      val occurrence = findOccurrenceUnderCaret(occurrences, operation.editor)
      var elementForCaret = occurrence ?: target
      if (elementForCaret is ILSelectExpression) {
        elementForCaret = elementForCaret.field as ILVariable
      }
      operation.editor.caretModel.moveToOffset(elementForCaret.textRange.startOffset)
      // TODO: Uncomment once have idea hw to change name of variable from it's usage
//      val introducer: InplaceVariableIntroducer<PsiElement> = object : InplaceVariableIntroducer<PsiElement>(target as HCLStringLiteralMixin, operation.editor, operation.project, "Introduce Variable", operation.occurrences.toTypedArray(), null) {
//        override fun checkLocalScope(): PsiElement? {
//          return target.containingFile
//        }
//      }
//      introducer.performInplaceRefactoring(LinkedHashSet(operation.suggestedNames))
    }
  }

  private fun performIntroduceWithDialog(operation: IntroduceOperation) {
    val project = operation.project
    if (operation.name == null) {
      val dialog = ILIntroduceDialog(project, HCLBundle.message("hil.introduce.variable.handler.dialog.caption"), validator, operation)
      if (!dialog.showAndGet()) {
        return
      }
      operation.name = dialog.name
      operation.isReplaceAll = dialog.doReplaceAllOccurrences()
      // TODO: Support introducing in separate file
      //operation.setInitPlace(dialog.getInitPlace())
    }

    val declaration = performRefactoring(operation) ?: return
    val editor = operation.editor
    editor.caretModel.moveToOffset(declaration.textRange.endOffset)
    editor.selectionModel.removeSelection()
  }


  private fun performRefactoring(operation: IntroduceOperation): PsiElement? {
    var declaration: PsiElement? = createDeclaration(operation)
    if (declaration == null) {
      showCannotPerformError(operation.project, operation.editor)
      return null
    }

    declaration = performReplace(declaration, operation)
    declaration = CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(declaration)
    return declaration
  }

  private fun createDeclaration(operation: IntroduceOperation): PsiElement? {
    val expr = operation.initializer ?: return null
    val name = operation.name ?: return null
    val type: Type = expr.getType() ?: Types.String
    return TfElementGenerator(operation.project).createVariable(name, type, expr)
  }


  private fun performReplace(declaration: PsiElement,
                             operation: IntroduceOperation): PsiElement {
    val expression = operation.initializer!!
    val project = operation.project
    return WriteCommandAction.writeCommandAction(project, expression.getHCLHost()?.containingFile
        ?: expression.containingFile).compute<PsiElement, Throwable> {
      try {
        val afterData = RefactoringEventData()
        afterData.addElement(declaration)
        project.messageBus.syncPublisher(RefactoringEventListener.REFACTORING_EVENT_TOPIC)
            .refactoringStarted(REFACTORING_ID, afterData)

        val result = addDeclaration(operation, declaration)

        val newExpression = createExpression(project, operation.name!!)

        if (operation.isReplaceAll) {
          operation.occurrences = operation.occurrences.map { replaceExpression(it, newExpression) }
        } else {
          val replaced = replaceExpression(expression, newExpression)
          operation.occurrences = listOf(replaced)
        }

        return@compute result
      } finally {
        val afterData = RefactoringEventData()
        afterData.addElement(declaration)
        project.messageBus.syncPublisher(RefactoringEventListener.REFACTORING_EVENT_TOPIC)
            .refactoringDone(REFACTORING_ID, afterData)
      }
    }
  }

  private fun createExpression(project: Project, name: String): PsiElement {
    return ILElementGenerator(project).createVarReference(name)
  }

  private fun replaceExpression(expression: PsiElement, newExpression: PsiElement): PsiElement {
    return outermostParenthesizedILExpression(expression).replace(newExpression)
  }

  private fun addDeclaration(operation: IntroduceOperation, declaration: PsiElement): PsiElement? {
    val anchor = if (operation.isReplaceAll) findAnchor(operation.occurrences) else findAnchor(operation.initializer!!)
    if (anchor == null) {
      CommonRefactoringUtil.showErrorHint(
        operation.project,
        operation.editor,
        RefactoringBundle.getCannotRefactorMessage(HCLBundle.message("refactoring.introduce.anchor.error")),
        HCLBundle.message("refactoring.introduce.error"), null
      )
      return null
    }
    return anchor.parent.addBefore(declaration, anchor)
  }


  private fun getOccurrences(element: PsiElement?, expression: ILExpression): List<PsiElement> {
    var context: PsiElement? = PsiTreeUtil.getParentOfType(element, ILExpressionHolder::class.java, true) ?: element
    if (context == null) {
      context = expression.containingFile
    }
    return ILRefactoringUtil.getOccurrences(expression, context)
  }

  val validator = IntroduceValidator()

  private fun getSuggestedNames(expression: ILExpression): Collection<String> {
    val candidates = generateSuggestedNames(expression)

    val res = candidates
        .filter { validator.checkPossibleName(it, expression) }
        .toMutableList()

    if (res.isEmpty()) {  // no available names found, generate disambiguated suggestions
      for (name in candidates) {
        var index = 1
        while (!validator.checkPossibleName(name + index, expression)) {
          index++
        }
        res.add(name + index)
      }
    }
    if (res.isEmpty()) {
      res += "a"
    }
    return res
  }

  private fun ensureName(operation: IntroduceOperation) {
    if (operation.name == null) {
      val suggestedNames = operation.suggestedNames
      if (!suggestedNames.isNullOrEmpty()) {
        operation.name = suggestedNames.first()
      } else {
        operation.name = "x"
      }
    }
  }

  private fun generateSuggestedNames(expression: ILExpression): Collection<String> {
    val candidates = LinkedHashSet<String>()
    var text = expression.text
    if (expression is ILMethodCallExpression) {
      text = expression.callee.text
    } // TODO: Add candidates based on HCLBlock property name
    if (text != null) {
      candidates.addAll(ILRefactoringUtil.generateNames(text))
    }
    val type = expression.getType()
    if (type != null) {
      candidates.addAll(ILRefactoringUtil.generateNamesByType(type.presentableText))
    }
    val list = PsiTreeUtil.getParentOfType(expression, ILParameterList::class.java)
    if (list != null) {
      val call = list.parent as ILMethodCallExpression
      // TODO: resolve called method name and all known argument names
      candidates.add("arg")
    }
    return candidates
  }


  private fun outermostParenthesizedILExpression(expr: PsiElement): PsiElement {
    if (expr !is ILExpression) return expr
    var e: ILExpression = expr
    while (e.parent is ILParenthesizedExpression) {
      e = e.parent as ILParenthesizedExpression
    }
    return e
  }
}

