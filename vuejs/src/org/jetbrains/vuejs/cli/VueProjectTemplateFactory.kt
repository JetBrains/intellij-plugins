// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.cli

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.WebModuleBuilder
import com.intellij.platform.ProjectTemplate
import com.intellij.platform.ProjectTemplatesFactory

class VueProjectTemplateFactory : ProjectTemplatesFactory() {
  override fun getGroups(): Array<String> = arrayOf(WebModuleBuilder.GROUP_NAME)

  override fun createTemplates(group: String?, context: WizardContext): Array<ProjectTemplate> {
    return arrayOf(VueCliProjectGenerator())
  }
}
