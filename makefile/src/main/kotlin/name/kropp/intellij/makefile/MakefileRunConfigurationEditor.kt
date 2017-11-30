package name.kropp.intellij.makefile

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.PathMacros
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.FixedSizeButton
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.EditorTextField
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.components.JBList
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentEvent

class MakefileRunConfigurationEditor(private val project: Project) : SettingsEditor<MakefileRunConfiguration>() {
  private val filenameField = TextFieldWithBrowseButton()
  private val targetCompletionProvider = TextFieldWithAutoCompletion.StringsCompletionProvider(emptyList(), MakefileTargetIcon)
  private val targetField = TextFieldWithAutoCompletion<String>(project, targetCompletionProvider, true, "")
  private val argumentsField = EditorTextField()
  private val workingDirectoryField = TextFieldWithBrowseButton()
  private val environmentVarsComponent = EnvironmentVariablesComponent()

  private val panel: JPanel by lazy {
    FormBuilder.createFormBuilder()
        .setAlignLabelOnRight(false)
        .setHorizontalGap(UIUtil.DEFAULT_HGAP)
        .setVerticalGap(UIUtil.DEFAULT_VGAP)
        .addLabeledComponent("&Makefile", filenameField)
        .addLabeledComponent("&Target", targetField)
        .addLabeledComponent("&Arguments", argumentsField)
        .addLabeledComponent("&Working Directory", createComponentWithMacroBrowse(workingDirectoryField))
        .addComponent(environmentVarsComponent)
        .panel
  }

  init {
    filenameField.addBrowseFolderListener("Makefile", "Makefile path", project, MakefileFileChooserDescriptor())
    filenameField.textField.document.addDocumentListener(object : DocumentAdapter() {
      override fun textChanged(event: DocumentEvent) {
        updateTargetCompletion(filenameField.text)
      }
    })
    workingDirectoryField.addBrowseFolderListener("Choose Working Directory", "Choose Working Directory", project, FileChooserDescriptorFactory.createSingleFolderDescriptor())
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
    configuration.workingDirectory = workingDirectoryField.text
    configuration.environmentVariables = environmentVarsComponent.envData
    configuration.arguments = argumentsField.text
  }

  override fun resetEditorFrom(configuration: MakefileRunConfiguration) {
    filenameField.text = configuration.filename
    targetField.text = configuration.target
    workingDirectoryField.text = configuration.workingDirectory
    environmentVarsComponent.envData = configuration.environmentVariables
    argumentsField.text = configuration.arguments

    updateTargetCompletion(configuration.filename)
  }


  // copied & converted to Kotlin from com.intellij.execution.ui.CommonProgramParametersPanel
  protected fun createComponentWithMacroBrowse(textAccessor: TextFieldWithBrowseButton): JComponent {
    val button = FixedSizeButton(textAccessor)
    button.icon = AllIcons.RunConfigurations.Variables
    button.addActionListener {
      val list = JBList(PathMacros.getInstance().userMacroNames)
      JBPopupFactory.getInstance().createListPopupBuilder(list).setItemChoosenCallback {
        val value = list.selectedValue
        if (value is String) {
          textAccessor.text = "$$value$"
        }
      }.setMovable(false).setResizable(false).createPopup().showUnderneathOf(button)
    }

    return JPanel(BorderLayout()).apply {
      add(textAccessor, BorderLayout.CENTER)
      add(button, BorderLayout.EAST)
    }
  }
}