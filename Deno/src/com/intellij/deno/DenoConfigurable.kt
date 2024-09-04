package com.intellij.deno

import com.intellij.codeInsight.template.impl.TemplateEditorUtil
import com.intellij.json.JsonLanguage
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorSettings
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.NlsContexts.ConfigurableName
import com.intellij.openapi.util.NlsContexts.Tooltip
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.ContextHelpLabel
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.SwingHelper
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JRadioButton

class DenoConfigurable(private val project: Project) : Configurable {

  private val myConfigureAutomaticallyDenoRb = JRadioButton(DenoBundle.message("deno.automatically"))
  private val myEnableDenoRb = JRadioButton(DenoBundle.message("deno.enabled"))
  private val myDisableDenoRb = JRadioButton(DenoBundle.message("deno.disabled"))
  private val myDenoPath = TextFieldWithBrowseButton()
  private val myDenoCache = TextFieldWithBrowseButton()
  private val myDenoFormattingEnabled = JBCheckBox(DenoBundle.message("deno.formatting.enable"))
  private lateinit var mySettingsEditor: Editor

  override fun getDisplayName(): @ConfigurableName String {
    return DenoBundle.message("deno.name")
  }

  override fun createComponent(): JComponent {
    val service = DenoSettings.getService(project)
    val buttonGroup = ButtonGroup()
    myConfigureAutomaticallyDenoRb.isSelected = service.isConfigureDenoAutomatically()
    myEnableDenoRb.isSelected = service.isEnableDeno()
    myDisableDenoRb.isSelected = service.isDisableDeno()
    buttonGroup.add(myConfigureAutomaticallyDenoRb)
    buttonGroup.add(myEnableDenoRb)
    buttonGroup.add(myDisableDenoRb)
    myDenoPath.text = service.getDenoPath()
    myDenoCache.text = FileUtil.toSystemDependentName(service.getDenoCache())
    myDenoFormattingEnabled.isSelected = service.isDenoFormattingEnabled()
    val fakeFile = PsiFileFactory.getInstance(project).createFileFromText("dummy", JsonLanguage.INSTANCE, service.getDenoInit(), true, false)
    val denoDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor().withTitle(DenoBundle.message("deno.name"))
    SwingHelper.installFileCompletionAndBrowseDialog(project, myDenoPath, denoDescriptor)
    val denoCacheDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle(DenoBundle.message("deno.cache.title"))
    SwingHelper.installFileCompletionAndBrowseDialog(project, myDenoCache, denoCacheDescriptor)
    val document = PsiDocumentManager.getInstance(project).getDocument(fakeFile)
    mySettingsEditor = TemplateEditorUtil.createEditor(false, document, project)
    val editorSettings: EditorSettings = mySettingsEditor.settings
    editorSettings.additionalLinesCount = 0 
    val mainFormBuilder = FormBuilder.createFormBuilder()
    val autoConfigurableHelpText = DenoBundle.message("deno.automatically.help.text")
    mainFormBuilder.addComponent(createPanelWithHelpLink(myConfigureAutomaticallyDenoRb, autoConfigurableHelpText))
    mainFormBuilder.addComponent(myEnableDenoRb)
    mainFormBuilder.addComponent(myDisableDenoRb)
    mainFormBuilder.addLabeledComponent(DenoBundle.message("deno.path"), myDenoPath)
    mainFormBuilder.addLabeledComponent(DenoBundle.message("deno.cache"), myDenoCache)
    mainFormBuilder.addLabeledComponent(DenoBundle.message("deno.cache.init"), mySettingsEditor.component)
    mainFormBuilder.addComponent(myDenoFormattingEnabled)
    val wrapper = JPanel(BorderLayout())
    wrapper.add(mainFormBuilder.panel, BorderLayout.NORTH)
    return wrapper
  }

  override fun isModified(): Boolean {
    val service = DenoSettings.getService(project)
    return myConfigureAutomaticallyDenoRb.isSelected != service.isConfigureDenoAutomatically() ||
           myEnableDenoRb.isSelected != service.isEnableDeno() ||
           myDisableDenoRb.isSelected != service.isDisableDeno() ||
           myDenoPath.text != service.getDenoPath() ||
           myDenoCache.text != FileUtil.toSystemDependentName(service.getDenoCache()) ||
           mySettingsEditor.document.text.trim() != service.getDenoInit() ||
           myDenoFormattingEnabled.isSelected != service.isDenoFormattingEnabled()
  }

  override fun disposeUIResources() {
    super.disposeUIResources()
    EditorFactory.getInstance().releaseEditor(mySettingsEditor)
  }

  @Throws(ConfigurationException::class)
  override fun apply() {
    if (isModified) {
      val service = DenoSettings.getService(project)
      service.setDenoPath(myDenoPath.text)
      service.setDenoCache(FileUtil.toSystemIndependentName(myDenoCache.text))
      val nextUseDeno = when {
        myConfigureAutomaticallyDenoRb.isSelected -> UseDeno.CONFIGURE_AUTOMATICALLY
        myEnableDenoRb.isSelected -> UseDeno.ENABLE
        else -> UseDeno.DISABLE
      }
      service.setUseDenoAndReload(nextUseDeno)
      service.setDenoInit(mySettingsEditor.document.text.trim())
      service.setDenoFormattingEnabled(myDenoFormattingEnabled.isSelected)
    }
  }

  private fun createPanelWithHelpLink(rb: JRadioButton, text: @Tooltip String): JComponent {
    val panel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
    panel.add(rb)
    val helpLabel = ContextHelpLabel.create(text)
    helpLabel.border = JBUI.Borders.emptyLeft(UIUtil.DEFAULT_HGAP)
    panel.add(helpLabel)
    return panel
  }
}
