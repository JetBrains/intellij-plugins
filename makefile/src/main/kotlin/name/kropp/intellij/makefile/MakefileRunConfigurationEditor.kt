package name.kropp.intellij.makefile

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import javax.swing.JPanel
import javax.swing.event.DocumentEvent

class MakefileRunConfigurationEditor(private val project: Project) : SettingsEditor<MakefileRunConfiguration>() {
  private val filenameField = TextFieldWithBrowseButton()
  private val targetCompletionProvider = TextFieldWithAutoCompletion.StringsCompletionProvider(emptyList(), MakefileTargetIcon)
  private val targetField = TextFieldWithAutoCompletion<String>(project, targetCompletionProvider, true, "")

  private val panel: JPanel by lazy {
    FormBuilder.createFormBuilder()
        .setAlignLabelOnRight(false)
        .setHorizontalGap(UIUtil.DEFAULT_HGAP)
        .setVerticalGap(UIUtil.DEFAULT_VGAP)
        .addLabeledComponent("&Makefile", filenameField)
        .addLabeledComponent("&Target", targetField)
        .panel
  }

  init {
    filenameField.addBrowseFolderListener("Makefile", "Makefile path", project, MakefileFileChooserDescriptor())
    filenameField.textField.document.addDocumentListener(object : DocumentAdapter() {
      override fun textChanged(event: DocumentEvent) {
        updateTargetCompletion(filenameField.text)
      }
    })
  }

  fun updateTargetCompletion(filename: String) {
    val file = LocalFileSystem.getInstance().findFileByPath(filename)
    if (file != null) {
      val psiFile = PsiManager.getInstance(project).findFile(file)
      if (psiFile != null) {
        targetCompletionProvider.setItems(name.kropp.intellij.makefile.findTargets(psiFile).map { it.name })
        return
      }
    }
    targetCompletionProvider.setItems(emptyList())
  }

  override fun createEditor() = panel

  override fun applyEditorTo(configuration: MakefileRunConfiguration) {
    configuration.filename = filenameField.text
    configuration.target = targetField.text
  }

  override fun resetEditorFrom(configuration: MakefileRunConfiguration) {
    filenameField.text = configuration.filename
    targetField.text = configuration.target

    updateTargetCompletion(configuration.filename)
  }
}