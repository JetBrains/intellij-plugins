// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.formatter

import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.ide.highlighter.HighlighterFactory
import com.intellij.lang.Language
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.highlighter.EditorHighlighter
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.UIUtil
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.TerraformLanguage
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLSyntaxHighlighter
import org.intellij.terraform.hcl.createHclLexer
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JPanel

internal class HclCodeStyleOtherPanel(settings: CodeStyleSettings, language: Language) : CodeStyleAbstractPanel(settings) {
  private val alignmentComboBox = CollectionComboBoxModel(PropertyAlignment.entries)
  private val commentSymbolComboBox = CollectionComboBoxModel(LineCommenterPrefix.entries)
  private lateinit var reformatCheckBox: JBCheckBox
  private lateinit var importProviders: JBCheckBox

  private val settingsPanel: JPanel = JPanel(BorderLayout())

  init {
    settingsPanel.add(panel {
      row(HCLBundle.message("code.style.align.properties.title")) { comboBox(alignmentComboBox) }
      row(HCLBundle.message("code.style.line.commenter.character.title")) { comboBox(commentSymbolComboBox) }
      row {
        importProviders = checkBox(HCLBundle.message("code.style.import.provider.automatically")).gap(RightGap.SMALL).component
        contextHelp(HCLBundle.message("code.style.import.provider.text"), HCLBundle.message("code.style.import.provider.header"))
      }.visible(language is TerraformLanguage)
      row {
        reformatCheckBox = checkBox(HCLBundle.message("code.style.run.tf.fmt.title")).gap(RightGap.SMALL).component
        contextHelp(HCLBundle.message("code.style.run.tf.fmt.comment"), HCLBundle.message("code.style.run.tf.fmt.header"))
      }.visible(language is TerraformLanguage)
    }.apply {
      border = BorderFactory.createEmptyBorder(UIUtil.DEFAULT_VGAP, UIUtil.DEFAULT_HGAP, UIUtil.DEFAULT_VGAP, UIUtil.DEFAULT_HGAP)
    }, BorderLayout.WEST)

    val previewPanel = JPanel()
    installPreviewPanel(previewPanel)

    settingsPanel.add(previewPanel, BorderLayout.CENTER)
  }

  override fun getRightMargin(): Int = 0

  override fun createHighlighter(scheme: EditorColorsScheme): EditorHighlighter =
    HighlighterFactory.createHighlighter(HCLSyntaxHighlighter(createHclLexer()), scheme)

  override fun getFileType(): FileType = TerraformFileType

  override fun getPreviewText(): String = ALIGNMENT_SAMPLE

  override fun apply(settings: CodeStyleSettings) {
    val tfSettings = settings.getCustomSettings(HclCodeStyleSettings::class.java)

    tfSettings.PROPERTY_ALIGNMENT = alignmentComboBox.selected ?: PropertyAlignment.ON_EQUALS
    tfSettings.LINE_COMMENTER_CHARACTER = commentSymbolComboBox.selected ?: LineCommenterPrefix.POUND_SIGN
    tfSettings.RUN_TF_FMT_ON_REFORMAT = reformatCheckBox.isSelected
    tfSettings.IMPORT_PROVIDERS_AUTOMATICALLY = importProviders.isSelected
  }

  override fun isModified(settings: CodeStyleSettings): Boolean {
    val tfSettings = settings.getCustomSettings(HclCodeStyleSettings::class.java)

    return reformatCheckBox.isSelected != tfSettings.RUN_TF_FMT_ON_REFORMAT ||
           alignmentComboBox.selected != tfSettings.PROPERTY_ALIGNMENT ||
           commentSymbolComboBox.selected != tfSettings.LINE_COMMENTER_CHARACTER ||
           importProviders.isSelected != tfSettings.IMPORT_PROVIDERS_AUTOMATICALLY
  }

  override fun resetImpl(settings: CodeStyleSettings) {
    val tfSettings = settings.getCustomSettings(HclCodeStyleSettings::class.java)

    alignmentComboBox.selectedItem = tfSettings.PROPERTY_ALIGNMENT
    commentSymbolComboBox.selectedItem = tfSettings.LINE_COMMENTER_CHARACTER
    reformatCheckBox.isSelected = tfSettings.RUN_TF_FMT_ON_REFORMAT
    importProviders.isSelected = tfSettings.IMPORT_PROVIDERS_AUTOMATICALLY
  }

  override fun getPanel(): JPanel = settingsPanel
}

private const val ALIGNMENT_SAMPLE: String = """
provider "aws" {
  region = "us-east-1"
}

resource "aws_instance" "example" {
  ami               = "ami-0c55b159cbfafe1f0"
  instance_type     = "t2.micro"
  key_name          = aws_key_pair.deployer.key_name
  vpc_security_group_ids = [aws_security_group.allow_ssh.id]

  tags = {
    Name = "ExampleInstance"
  }
}
"""