// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.intentions.extractComponent

import com.intellij.CommonBundle
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.impl.FinishMarkAction
import com.intellij.openapi.command.impl.StartMarkAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import com.intellij.psi.search.SearchScope
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.rename.inplace.InplaceRefactoring
import com.intellij.ui.popup.list.ListPopupImpl
import com.intellij.ui.popup.mock.MockConfirmation
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.tags.VueInsertHandler.Companion.reformatElement

class VueComponentInplaceIntroducer(elementToRename: XmlTag,
                                    editor: Editor,
                                    private val data: VueExtractComponentDataBuilder,
                                    private val oldText: String,
                                    private val validator: (@NonNls String) -> @Nls String?,
                                    private val startMarkAction: StartMarkAction) :
  InplaceRefactoring(editor, elementToRename, elementToRename.project) {

  private val containingFile = myElementToRename.containingFile
  private val oldCaret = editor.caretModel.currentCaret.offset
  private var isCanceled = false

  companion object {
    const val GROUP_ID: String = "VueExtractComponent"
  }

  override fun shouldSelectAll(): Boolean = false

  override fun getCommandName(): String =
    VueBundle.message("vue.template.intention.extract.component.command.name")

  override fun collectRefs(referencesSearchScope: SearchScope?): MutableCollection<PsiReference> {
    return mutableListOf()
  }

  override fun addReferenceAtCaret(refs: MutableCollection<in PsiReference>) {
  }

  override fun startsOnTheSameElement(handler: RefactoringActionHandler?, element: PsiElement?): Boolean {
    return true
  }

  override fun startRename(): StartMarkAction {
    return startMarkAction
  }

  override fun checkLocalScope(): PsiElement? {
    return myElementToRename
  }

  override fun getNameIdentifier(): PsiElement? {
    return myElementToRename.node.findChildByType(TokenSet.create(XmlTokenType.XML_NAME, XmlTokenType.XML_TAG_NAME))?.psi
  }

  override fun performInplaceRefactoring(nameSuggestions: LinkedHashSet<String>?): Boolean {
    nameIdentifier ?: return false
    myEditor!!.caretModel.moveToOffset(nameIdentifier!!.textRange.endOffset)
    return super.performInplaceRefactoring(nameSuggestions)
  }

  override fun performCleanup() {
    try {
      // it is called two times on "no" selected in confirmation popup
      if (isCanceled) return
      isCanceled = true
      WriteAction.run<RuntimeException> {
        val tag = findTagBeingRenamed() ?: return@run
        // for the case with pug
        val embedded = PsiTreeUtil.getParentOfType(tag, JSEmbeddedContent::class.java)
        val offset = embedded?.textRange?.startOffset ?: 0
        myEditor.document.replaceString(offset + tag.textRange.startOffset, offset + tag.textRange.endOffset, oldText)
        myEditor.caretModel.currentCaret.moveToOffset(oldCaret)
      }
    }
    finally {
      FinishMarkAction.finish(myProject, myEditor, myMarkAction)
    }
  }

  override fun restoreCaretOffset(offset: Int): Int = if (isCanceled) oldCaret else offset

  override fun performRefactoring(): Boolean {
    if (myInsertedName == null) return false

    val commandProcessor = CommandProcessor.getInstance()
    val error = validator(myInsertedName)
    if (error != null) {
      if (ApplicationManager.getApplication().isUnitTestMode) {
        performCleanupInCommand()
        return true
      }
      val tag: XmlTag = findTagBeingRenamed() ?: return true
      askAndRestartRename(error, commandProcessor, tag)
    }
    else {
      hijackCommand()
      commandProcessor.executeCommand(myProject, {
        var newPsiFile: PsiFile? = null
        try {
          WriteAction.run<RuntimeException> {
            hijackCommand()

            val insertedName = myInsertedName.trim()
            val virtualFile = data.createNewComponent(insertedName) ?: return@run
            CommandProcessor.getInstance().addAffectedFiles(myProject, virtualFile)
            newPsiFile = PsiManager.getInstance(containingFile.project).findFile(virtualFile)

            data.modifyCurrentComponent(insertedName, containingFile, newPsiFile!!)
            reformatElement(myElementToRename)
          }

          positionOldEditor()
          if (newPsiFile != null) {
            FileEditorManager.getInstance(myProject).openFile(newPsiFile!!.viewProvider.virtualFile, true)
          }
        }
        finally {
          hijackCommand()
          FinishMarkAction.finish(myProject, myEditor, myMarkAction)
        }
      }, commandName, getGroupId())
    }
    return true
  }

  private fun performCleanupInCommand() {
    CommandProcessor.getInstance().executeCommand(myProject, { performCleanup() }, commandName, getGroupId())
  }

  private fun askAndRestartRename(@Nls error: String, commandProcessor: CommandProcessor, tag: XmlTag) {
    askConfirmation(error,
                    onYes = {
                      hijackCommand()
                      commandProcessor.executeCommand(myProject, {
                        VueComponentInplaceIntroducer(tag, myEditor, data, oldText,
                                                      validator, startMarkAction)
                          .performInplaceRefactoring(linkedSetOf())
                      }, commandName, getGroupId())
                    },
                    onNo = this::performCleanupInCommand)
  }

  private fun findTagBeingRenamed(): XmlTag? {
    if (myElementToRename != null && myElementToRename.isValid) {
      return myElementToRename as? XmlTag
    }
    else {
      return PsiTreeUtil.findElementOfClassAtRange(containingFile, myRenameOffset.startOffset,
                                                   myRenameOffset.endOffset, XmlTag::class.java)
    }
  }

  private fun getGroupId() = GROUP_ID

  private fun hijackCommand() {
    val commandProcessor = CommandProcessor.getInstance()
    if (commandProcessor.currentCommand != null) {
      commandProcessor.currentCommandName = commandName
      commandProcessor.currentCommandGroupId = getGroupId()
    }
  }

  private fun askConfirmation(@Nls title: String, onYes: () -> Unit, onNo: () -> Unit) {
    val yesText = VueBundle.message("vue.template.intention.extract.component.continue")
    val step = object : BaseListPopupStep<String>(title, yesText, CommonBundle.getCancelButtonText()) {
      private var yesChosen = false

      override fun onChosen(selectedValue: String, finalChoice: Boolean): PopupStep<*>? {
        yesChosen = selectedValue == yesText
        return doFinalStep(if (yesChosen) onYes else onNo)
      }

      override fun canceled() {
        if (!yesChosen) onNo.invoke()
      }

      override fun isMnemonicsNavigationEnabled(): Boolean {
        return true
      }
    }
    step.defaultOptionIndex = 0

    val listPopup: ListPopup = if (!ApplicationManager.getApplication().isUnitTestMode) ListPopupImpl(step)
    else MockConfirmation(step, yesText)
    listPopup.showInBestPositionFor(myEditor)
  }

  private fun positionOldEditor() {
    if (myEditor != null) {
      val tag = findTagBeingRenamed()
      if (tag != null) {
        myEditor.caretModel.moveToOffset(tag.textRange.startOffset)
      }
      myEditor.selectionModel.setSelection(0, 0)
      myEditor.scrollingModel.scrollToCaret(ScrollType.RELATIVE)
    }
  }
}
