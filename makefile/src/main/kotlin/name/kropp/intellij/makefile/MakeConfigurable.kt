package name.kropp.intellij.makefile

import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.*
import com.intellij.ui.components.*
import com.intellij.uiDesigner.core.*
import com.intellij.util.ui.*
import javax.swing.*

class MakeConfigurable(project: Project?, private val settings: MakefileProjectSettings) : Configurable {
  private val pathField = TextFieldWithBrowseButton()
  private val cygwinField = JBCheckBox("Use Cygwin${if (!SystemInfo.isWindows) " (Windows only)" else ""}")

  init {
    pathField.addBrowseFolderListener("Make", "Path to make executable", project, FileChooserDescriptor(true, false, false, false, false, false))
  }

  override fun isModified(): Boolean {
    return settings.settings?.path != pathField.text ||
           settings.settings?.useCygwin != cygwinField.isSelected
  }

  override fun getDisplayName() = "Make"
  override fun apply() {
    settings.settings?.path = pathField.text
    settings.settings?.useCygwin = cygwinField.isSelected
  }

  override fun createComponent(): JComponent {
    return FormBuilder.createFormBuilder()
        .setAlignLabelOnRight(false)
        .setHorizontalGap(UIUtil.DEFAULT_HGAP)
        .setVerticalGap(UIUtil.DEFAULT_VGAP)
        .addLabeledComponent("Path to &Make executable", pathField)
        .addComponent(cygwinField)
        .addComponentFillVertically(Spacer(), 0)
        .panel
  }

  override fun reset() {
    pathField.text = settings.settings?.path ?: ""
    cygwinField.isSelected = settings.settings?.useCygwin ?: false
  }

  override fun getHelpTopic(): String? = null
}