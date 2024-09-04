package com.jetbrains.lang.makefile

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.uiDesigner.core.Spacer
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import javax.swing.JComponent

class MakeConfigurable(project: Project?) : Configurable {
  private val settings = project?.getService(MakefileProjectSettings::class.java)
  private val pathField = TextFieldWithBrowseButton()
  @Suppress("DialogTitleCapitalization")
  private val cygwinField = JBCheckBox(MakefileLangBundle.message("configurable.use.cygwin.checkbox", if (!SystemInfo.isWindows) 0 else 1))

  init {
    pathField.addBrowseFolderListener(project, FileChooserDescriptor(true, false, false, false, false, false)
      .withTitle(MakefileLangBundle.message("make.file.chooser.title"))
      .withDescription(MakefileLangBundle.message("make.file.chooser.description")))
  }

  override fun isModified(): Boolean {
    return settings?.settings?.path != pathField.text ||
           settings.settings?.useCygwin != cygwinField.isSelected
  }

  override fun getDisplayName() = MakefileLangBundle.message("configurable.name")
  override fun apply() {
    settings?.settings?.path = pathField.text
    settings?.settings?.useCygwin = cygwinField.isSelected
  }

  override fun createComponent(): JComponent {
    return FormBuilder.createFormBuilder()
        .setAlignLabelOnRight(false)
        .setHorizontalGap(UIUtil.DEFAULT_HGAP)
        .setVerticalGap(UIUtil.DEFAULT_VGAP)
        .addLabeledComponent(MakefileLangBundle.message("configurable.path.field.label"), pathField)
        .addComponent(cygwinField)
        .addComponentFillVertically(Spacer(), 0)
        .panel
  }

  override fun reset() {
    pathField.text = settings?.settings?.path ?: ""
    cygwinField.isSelected = settings?.settings?.useCygwin ?: false
  }

  override fun getHelpTopic(): String = "settings.buildtools.make"
}
