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
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.SwingHelper
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class DenoConfigurable(private val project: Project) : Configurable {

  private val myUseDeno = JBCheckBox(DenoBundle.message("deno.enabled"))
  private val myDenoPath = TextFieldWithBrowseButton()
  private val myDenoCache = TextFieldWithBrowseButton()
  private lateinit var mySettingsEditor: Editor

  override fun getDisplayName(): @ConfigurableName String {
    return DenoBundle.message("deno.name")
  }

  override fun createComponent(): JComponent {
    val service = DenoSettings.getService(project)
    myUseDeno.isSelected = service.isUseDeno()
    myDenoPath.text = service.getDenoPath()
    myDenoCache.text = FileUtil.toSystemDependentName(service.getDenoCache())
    val fakeFile = PsiFileFactory.getInstance(project).createFileFromText("dummy", JsonLanguage.INSTANCE, service.getDenoInit(), true,
      false)
    SwingHelper.installFileCompletionAndBrowseDialog(
      project,
      myDenoPath,
      DenoBundle.message("deno.name"),
      FileChooserDescriptorFactory.createSingleFileDescriptor()
    )
    
    SwingHelper.installFileCompletionAndBrowseDialog(
      project,
      myDenoCache,
      DenoBundle.message("deno.cache.title"),
      FileChooserDescriptorFactory.createSingleFolderDescriptor()
    )
    
    val document = PsiDocumentManager.getInstance(project).getDocument(fakeFile)
    mySettingsEditor = TemplateEditorUtil.createEditor(false, document, project)
    val editorSettings: EditorSettings = mySettingsEditor.settings
    editorSettings.additionalLinesCount = 0 
    val mainFormBuilder = FormBuilder.createFormBuilder()
    mainFormBuilder.addComponent(myUseDeno)
    mainFormBuilder.addLabeledComponent(DenoBundle.message("deno.path"), myDenoPath)
    mainFormBuilder.addLabeledComponent(DenoBundle.message("deno.cache"), myDenoCache)
    mainFormBuilder.addLabeledComponent(DenoBundle.message("deno.cache.init"), mySettingsEditor.component)
    val wrapper = JPanel(BorderLayout())
    wrapper.add(mainFormBuilder.panel, BorderLayout.NORTH)
    return wrapper
  }

  override fun isModified(): Boolean {
    val service = DenoSettings.getService(project)
    return myUseDeno.isSelected != service.isUseDeno() ||
           myDenoPath.text != service.getDenoPath() ||
           myDenoCache.text != FileUtil.toSystemDependentName(service.getDenoCache()) ||
           mySettingsEditor.document.text.trim() != service.getDenoInit()
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
      service.setUseDenoAndReload(myUseDeno.isSelected)
      service.setDenoInit(mySettingsEditor.document.text.trim())
    }
  }
}