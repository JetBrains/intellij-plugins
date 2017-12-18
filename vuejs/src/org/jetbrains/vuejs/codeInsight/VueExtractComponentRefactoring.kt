package org.jetbrains.vuejs.codeInsight

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.*
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.util.CommonRefactoringUtil
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBTextField
import com.intellij.util.PathUtilRt
import com.intellij.util.ui.FormBuilder
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.codeInsight.VueInsertHandler.Companion.reformatElement
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JLabel

/**
 * @author Irina.Chernushina on 12/14/2017.
 */
class VueExtractComponentRefactoring(private val project: Project,
                                     private val list: List<XmlTag>,
                                     private val editor: Editor?) {
  fun perform() {
    if (!doChecks()) return
    val data = generatePreview()
    if (!showDialog(data)) return
    performRefactoring(data)
  }

  private fun performRefactoring(data: MyData) {
    var newPsiFile: PsiFile? = null
    var newlyAdded: PsiElement? = null

    wrapInCommand {
      PostprocessReformattingAspect.getInstance(project).postponeFormattingInside {
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        WriteAction.run<RuntimeException> {
          newPsiFile = data.generateNewComponent() ?: return@run
          newlyAdded = data.modifyCurrentComponent(newPsiFile!!, editor)
        }
      }
      reformatElement(newPsiFile)
      reformatElement(newlyAdded)
      positionOldEditor(editor, newlyAdded)
    }
    if (newPsiFile != null) {
      FileEditorManager.getInstance(project).openFile(newPsiFile!!.viewProvider.virtualFile, true)
    }
  }

  private fun positionOldEditor(editor: Editor?, newlyAdded: PsiElement?) {
    if (editor != null) {
      editor.caretModel.moveToOffset(newlyAdded!!.textRange.startOffset)
      editor.selectionModel.setSelection(0, 0)
      editor.scrollingModel.scrollToCaret(ScrollType.RELATIVE)
    }
  }

  private fun doChecks(): Boolean {
    if (list.isEmpty()) return false
    return CommonRefactoringUtil.checkReadOnlyStatus(project, list[0].containingFile)
  }

  private fun generatePreview(): MyData {
    return MyData(list)
  }

  private fun showDialog(data: MyData): Boolean {
    val nameField = JBTextField(20)
    nameField.emptyText.text = "Component name (in kebab notation)"
    val errorLabel = JLabel("")
    errorLabel.foreground = JBColor.red
    val panel = FormBuilder()
      .addLabeledComponent("Component name:", nameField)
      .addComponent(errorLabel)
      .panel

    val builder = DialogBuilder()
    builder.setTitle(VueBundle.message("vue.template.intention.extract.component"))
    builder.setCenterPanel(panel)
    builder.setPreferredFocusComponent(nameField)
    builder.setDimensionServiceKey(VueExtractComponentRefactoring::class.java.name)

    val changesHandler = {
      val normalized = toAsset(nameField.text.trim()).capitalize()
      val fileName = normalized + ".vue"
      errorLabel.text = ""
      if (normalized.isEmpty() || !PathUtilRt.isValidFileName(fileName, false) || normalized.contains(' ')) {
        builder.okActionEnabled(false)
      } else if (data.folder!!.findFile(fileName) != null) {
        builder.okActionEnabled(false)
        errorLabel.text = "File $fileName already exists"
      } else {
        builder.okActionEnabled(true)
      }
    }
    nameField.addActionListener({ changesHandler.invoke() })
    nameField.addKeyListener(object: KeyAdapter() {
      override fun keyReleased(e: KeyEvent?) {
        changesHandler.invoke()
      }
    })

    val result = builder.showAndGet()
    data.newComponentName = nameField.text.trim()
    return result
  }

  private fun wrapInCommand(refactoring: () -> Unit) {
    val name = VueBundle.message("vue.template.intention.extract.component")
    CommandProcessor.getInstance().executeCommand(project, refactoring, name, name)
  }
}

// todo add options, regenerate
private class MyData(private val list: List<XmlTag>) {
  val folder: PsiDirectory? = list[0].containingFile.parent
  private val detectedLanguage = detectLanguage(list[0].containingFile)
  var newComponentName = "NewComponent"
  private val baseText: String

  init {
    baseText =
"""<template>
  ${list.joinToString("") { it.text }}
</template>"""
  }

  private fun generateText() =
"""$baseText
<script $detectedLanguage>
export default {
  name: '$newComponentName'
}
</script>"""

  private fun detectLanguage(file: PsiFile?): String {
    val xmlFile = file as? XmlFile ?: return ""
    // todo keep script in field
    val lang = findScriptTag(xmlFile)?.getAttribute("lang")?.value ?: return ""
    return "lang=\"$lang\""
  }

  fun generateNewComponent(): PsiFile? {
    folder ?: return null
    val newFile = folder.virtualFile.createChildData(this, toAsset(newComponentName).capitalize() + ".vue")
    VfsUtil.saveText(newFile, generateText())
    return PsiManager.getInstance(folder.project).findFile(newFile)
  }

  fun modifyCurrentComponent(newPsiFile: PsiFile, editor: Editor?): PsiElement? {
    val leader = list[0]
    val newTagName = fromAsset(newComponentName)
    val replaceText = "<template><$newTagName/></template>"
    val dummyFile = PsiFileFactory.getInstance(leader.project).createFileFromText("dummy.vue", VueFileType.INSTANCE, replaceText)
    val template = PsiTreeUtil.findChildOfType(dummyFile, XmlTag::class.java)!!
    val newTag = template.findFirstSubTag(newTagName)!!
    val newlyAdded = leader.replace(newTag)
    list.subList(1, list.size).forEach { it.delete() }

    VueInsertHandler.InsertHandlerWorker().insertComponentImport(newlyAdded, newComponentName, newPsiFile, editor)
    return newlyAdded
  }
}