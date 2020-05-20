package com.intellij.deno

import com.intellij.lang.typescript.compiler.TypeScriptCompilerService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.openapi.util.NlsContexts.ConfigurableName
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ThrowableRunnable
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class DenoConfigurable(private val project: Project) : Configurable {

  private val myUseDeno = JBCheckBox(DenoBundle.message("deno.enabled"))

  override fun getDisplayName(): @ConfigurableName String? {
    return DenoBundle.message("deno.name")
  }

  override fun createComponent(): JComponent? {
    myUseDeno.isSelected = DenoSettings.getService(project).isUseDeno()
    val mainFormBuilder = FormBuilder.createFormBuilder()
    mainFormBuilder.addComponent(myUseDeno)
    val wrapper = JPanel(BorderLayout())
    wrapper.add(mainFormBuilder.panel, BorderLayout.NORTH)
    return wrapper
  }

  override fun isModified(): Boolean {
    return myUseDeno.isSelected != DenoSettings.getService(project).isUseDeno()
  }

  @Throws(ConfigurationException::class)
  override fun apply() {
    if (isModified) {
      DenoSettings.getService(project).setUseDenoAndReload(myUseDeno.isSelected)
    }
  }
}