package com.intellij.openRewrite.run.editor

import com.intellij.execution.ui.FragmentWrapper
import com.intellij.openRewrite.recipe.OpenRewriteRecipeDescriptor
import com.intellij.openRewrite.recipe.OpenRewriteType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorTextField
import com.intellij.ui.Expandable
import com.intellij.ui.ExpandableEditorSupport
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.util.execution.ParametersListUtil
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import java.util.function.Supplier
import javax.swing.JComponent
import javax.swing.JPanel

internal class OpenRewriteActiveRecipesEditor(project: Project,
                                              configSupplier: Supplier<Collection<VirtualFile>>,
                                              type: OpenRewriteType,
                                              @Nls accessibleName: String) :
  JPanel(BorderLayout()), FragmentWrapper, Expandable {

  private val editor: TextFieldWithAutoCompletion<OpenRewriteRecipeDescriptor>
  private var popupEditor: TextFieldWithAutoCompletion<OpenRewriteRecipeDescriptor>? = null
  private val support: ExpandableEditorSupport

  init {
    val profileCompletionProvider = OpenRewriteRecipeCompletionProvider(project, configSupplier, type)
    editor = TextFieldWithAutoCompletion(project, profileCompletionProvider, true, "")

    editor.accessibleContext.accessibleName = accessibleName
    add(editor, BorderLayout.CENTER)
    support = object : ExpandableEditorSupport(editor, ParametersListUtil.DEFAULT_LINE_PARSER, ParametersListUtil.DEFAULT_LINE_JOINER) {
      override fun createPopupEditor(field: EditorTextField, text: String): EditorTextField {
        val popupEditor = TextFieldWithAutoCompletion(project, profileCompletionProvider, true, text)
        popupEditor.setOneLineMode(false)
        this@OpenRewriteActiveRecipesEditor.popupEditor = popupEditor
        return popupEditor
      }
    }
  }

  val textField: EditorTextField
    get() = if (support.isExpanded) popupEditor!! else editor

  override fun getComponentToRegister(): JComponent = editor

  override fun expand() {
    support.expand()
  }

  override fun collapse() {
    support.collapse()
  }

  override fun isExpanded(): Boolean = support.isExpanded
}