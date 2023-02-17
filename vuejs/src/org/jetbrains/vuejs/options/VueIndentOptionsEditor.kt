// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.options

import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.ui.ContextHelpLabel
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.components.fields.ExpandableTextField
import org.jetbrains.vuejs.VueBundle.message
import org.jetbrains.vuejs.lang.html.psi.formatter.VueCodeStyleSettings
import java.awt.Dimension
import java.awt.GridBagConstraints.WEST
import java.awt.GridBagLayout
import javax.swing.*

class VueIndentOptionsEditor : SmartIndentOptionsEditor() {

  private val myUniformIndentRadioButton: JRadioButton by lazy { JRadioButton(message("options.indent.radio.uniform.caption")) }
  private val myBlockSpecificIndentRadioButton: JRadioButton by lazy { JRadioButton(message("options.indent.radio.block-specific.caption")) }
  private val myIndentChildrenOfTopLevelTags: ExpandableTextField by lazy { ExpandableTextField() }
  private val myIndentChildrenOfTopLevelTagsLabel: JLabel by lazy { JLabel(message("options.indent.label.top-level.caption")) }
  private val myBlockSpecificIndentPanel: JPanel by lazy { JPanel() }

  private val myComponentsList get() = listOf(myBlockSpecificIndentPanel, myUniformIndentRadioButton, myIndentChildrenOfTopLevelTagsLabel)

  override fun addComponents() {
    myBlockSpecificIndentRadioButton.addActionListener {
      myUniformIndentRadioButton.isSelected = !myBlockSpecificIndentRadioButton.isSelected
    }
    myBlockSpecificIndentPanel.layout = BoxLayout(myBlockSpecificIndentPanel, BoxLayout.X_AXIS)
    myBlockSpecificIndentPanel.add(myBlockSpecificIndentRadioButton)
    myBlockSpecificIndentPanel.add(Box.createRigidArea(Dimension(10, 0)))
    myBlockSpecificIndentPanel.add(ContextHelpLabel.create(
      StringUtil.replace(message("options.indent.radio.block-specific.tooltip"), "<", "&lt;")))
    add(myBlockSpecificIndentPanel)
    myUniformIndentRadioButton.addActionListener {
      myBlockSpecificIndentRadioButton.isSelected = !myUniformIndentRadioButton.isSelected
    }
    add(myUniformIndentRadioButton)
    super.addComponents()
    add(myIndentChildrenOfTopLevelTagsLabel, myIndentChildrenOfTopLevelTags)
  }

  override fun createPanel(): JPanel {
    val panel = super.createPanel()
    val layout = panel.layout as? GridBagLayout ?: return panel
    panel.components.asSequence()
      .filterIsInstance<JComponent>()
      .filter { it !in myComponentsList }
      .forEach {
        val constraints = layout.getConstraints(it)
        if (constraints.anchor == WEST) {
          constraints.insets.left += IdeBorderFactory.TITLED_BORDER_INDENT
          layout.setConstraints(it, constraints)
        }
      }
    return panel
  }

  override fun isModified(settings: CodeStyleSettings, options: CommonCodeStyleSettings.IndentOptions): Boolean {
    val vueSettings = settings.getCustomSettings(VueCodeStyleSettings::class.java)
    if (vueSettings.UNIFORM_INDENT != myUniformIndentRadioButton.isSelected
        || vueSettings.INDENT_CHILDREN_OF_TOP_LEVEL != myIndentChildrenOfTopLevelTags.text.trim()) {
      return true
    }
    return !myUniformIndentRadioButton.isSelected || super.isModified(settings, options)
  }

  override fun apply(settings: CodeStyleSettings, options: CommonCodeStyleSettings.IndentOptions) {
    val vueSettings = settings.getCustomSettings(VueCodeStyleSettings::class.java)
    vueSettings.UNIFORM_INDENT = myUniformIndentRadioButton.isSelected
    vueSettings.INDENT_CHILDREN_OF_TOP_LEVEL = myIndentChildrenOfTopLevelTags.text
    if (vueSettings.UNIFORM_INDENT) {
      super.apply(settings, options)
    }
  }

  override fun reset(settings: CodeStyleSettings, options: CommonCodeStyleSettings.IndentOptions) {
    val vueSettings = settings.getCustomSettings(VueCodeStyleSettings::class.java)
    myUniformIndentRadioButton.isSelected = vueSettings.UNIFORM_INDENT
    myBlockSpecificIndentRadioButton.isSelected = !vueSettings.UNIFORM_INDENT
    myIndentChildrenOfTopLevelTags.text = vueSettings.INDENT_CHILDREN_OF_TOP_LEVEL
    super.reset(settings, options)
  }

  override fun setEnabled(enabled: Boolean) {
    myUniformIndentRadioButton.isEnabled = enabled
    myBlockSpecificIndentRadioButton.isEnabled = enabled
    myIndentChildrenOfTopLevelTags.isEnabled = enabled
    setIndentSectionEnabled(enabled && myUniformIndentRadioButton.isSelected)
  }

  private fun setIndentSectionEnabled(value: Boolean) {
    super.setEnabled(value)
  }

}