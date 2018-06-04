package name.kropp.intellij.makefile

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.uiDesigner.core.Spacer
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import javax.swing.JComponent

class MakeConfigurable(private val project: Project?, private val settings: MakefileProjectSettings) : Configurable {
  private val pathField = TextFieldWithBrowseButton()

  init {
    pathField.addBrowseFolderListener("Make", "Path to make executable", project, FileChooserDescriptor(true, false, false, false, false, false))
  }

  override fun isModified(): Boolean {
    return settings.settings?.path != pathField.text
  }

  override fun getDisplayName() = "Make"
  override fun apply() {
    settings.settings?.path = pathField.text
  }

  override fun createComponent(): JComponent {
    return FormBuilder.createFormBuilder()
        .setAlignLabelOnRight(false)
        .setHorizontalGap(UIUtil.DEFAULT_HGAP)
        .setVerticalGap(UIUtil.DEFAULT_VGAP)
        .addLabeledComponent("Path to &Make executable", pathField)
        .addComponentFillVertically(Spacer(), 0)
        .panel
  }

  override fun reset() {
    pathField.text = settings.settings?.path ?: ""
  }

  override fun getHelpTopic() = null
}