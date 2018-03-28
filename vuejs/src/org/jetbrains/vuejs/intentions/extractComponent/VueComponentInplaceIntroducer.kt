// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.intentions.extractComponent

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.ex.ApplicationManagerEx
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.impl.FinishMarkAction
import com.intellij.openapi.command.impl.StartMarkAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
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
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.VueInsertHandler.Companion.reformatElement
import java.util.*

/**
 * @author Irina.Chernushina on 12/22/2017.
 */
class VueComponentInplaceIntroducer(elementToRename: XmlTag,
                                    editor: Editor,
                                    private val data: VueExtractComponentDataBuilder,
                                    private val oldText: String,
                                    private val validator: (String) -> String?,
                                    private val startMarkAction: StartMarkAction):
  InplaceRefactoring(editor, elementToRename, elementToRename.project) {
  private val commandName = VueBundle.message("vue.template.intention.extract.component")
  private val containingFile = myElementToRename.containingFile
  private val oldCaret = editor.caretModel.currentCaret.offset
  private var isCanceled = false

  companion object {
    const val GROUP_ID = "VueExtractComponent"
  }

  override fun collectAdditionalElementsToRename(stringUsages: MutableList<Pair<PsiElement, TextRange>>) {
  }

  override fun shouldSelectAll() = false

  override fun getCommandName(): String {
    return commandName
  }

  override fun collectRefs(referencesSearchScope: SearchScope?): MutableCollection<PsiReference> {
    return mutableListOf()
  }

  override fun addReferenceAtCaret(refs: MutableCollection<PsiReference>?) {
  }

  override fun startsOnTheSameElement(handler: RefactoringActionHandler?, element: PsiElement?): Boolean {
    return true
  }

  override fun startRename(): StartMarkAction? {
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
    } finally {
      FinishMarkAction.finish(myProject, myEditor, myMarkAction)
    }
  }

  override fun restoreCaretOffset(offset: Int): Int = if (isCanceled) oldCaret else offset

  override fun performRefactoring(): Boolean {
    if (myInsertedName == null) return false

    val commandProcessor = CommandProcessor.getInstance()
    val error = validator.invoke(myInsertedName)
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

            data.modifyCurrentComponent(insertedName, containingFile, newPsiFile!!, myEditor)
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
      }, getCommandName(), getGroupId())
    }
    return true
  }

  private fun performCleanupInCommand() {
    CommandProcessor.getInstance().executeCommand(myProject, { performCleanup()}, commandName, getGroupId())
  }

  private fun askAndRestartRename(error: String, commandProcessor: CommandProcessor, tag: XmlTag) {
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
      commandProcessor.currentCommandName = getCommandName()
      commandProcessor.currentCommandGroupId = getGroupId()
    }
  }

  private fun askConfirmation(title: String, onYes: () -> Unit, onNo: () -> Unit) {
    val yesText = "Continue editing"
    val step = object : BaseListPopupStep<String>(title, yesText, "Cancel") {
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

    val app = ApplicationManagerEx.getApplicationEx()
    val listPopup: ListPopup = if (app == null || !app.isUnitTestMode) JBPopupFactory.getInstance().createListPopup(step) else MockConfirmation(step, yesText)
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