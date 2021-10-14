package com.intellij.deno

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.NlsContexts.ConfigurableName
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class DenoConfigurable(private val project: Project) : Configurable {

  private val myUseDeno = JBCheckBox(DenoBundle.message("deno.enabled"))
  private val myDenoPath = TextFieldWithBrowseButton()
  private val myDenoCache = TextFieldWithBrowseButton()

  override fun getDisplayName(): @ConfigurableName String {
    return DenoBundle.message("deno.name")
  }

  override fun createComponent(): JComponent {
    val service = DenoSettings.getService(project)
    myUseDeno.isSelected = service.isUseDeno()
    myDenoPath.text = service.getDenoPath()
    myDenoCache.text = FileUtil.toSystemDependentName(service.getDenoCache())
    val mainFormBuilder = FormBuilder.createFormBuilder()
    mainFormBuilder.addComponent(myUseDeno)
    mainFormBuilder.addLabeledComponent(DenoBundle.message("deno.path"), myDenoPath)
    mainFormBuilder.addLabeledComponent(DenoBundle.message("deno.cache"), myDenoCache)
    val wrapper = JPanel(BorderLayout())
    wrapper.add(mainFormBuilder.panel, BorderLayout.NORTH)
    return wrapper
  }

  override fun isModified(): Boolean {
    val service = DenoSettings.getService(project)
    return myUseDeno.isSelected != service.isUseDeno() ||
           myDenoPath.text != service.getDenoPath() ||
           myDenoCache.text != FileUtil.toSystemDependentName(service.getDenoCache())
  }

  @Throws(ConfigurationException::class)
  override fun apply() {
    if (isModified) {
      val service = DenoSettings.getService(project)
      service.setDenoPath(myDenoPath.text)
      service.setDenoCache(FileUtil.toSystemIndependentName(myDenoCache.text))
      service.setUseDenoAndReload(myUseDeno.isSelected)
    }
  }
}