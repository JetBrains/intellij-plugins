package org.jetbrains.vuejs.cli

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.WebModuleBuilder
import com.intellij.platform.ProjectTemplate
import com.intellij.platform.ProjectTemplatesFactory

class VueTemplateFactory : ProjectTemplatesFactory() {
  override fun getGroups() = arrayOf(WebModuleBuilder.GROUP_NAME)

  override fun createTemplates(group: String?, context: WizardContext): Array<ProjectTemplate> {
    return arrayOf(VueCliProjectGenerator())
  }
}
