// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.cli

import com.intellij.ide.util.projectWizard.WebTemplateNewProjectWizardBuilder
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.lang.javascript.boilerplate.JavaScriptNewTemplatesFactoryBase
import com.intellij.openapi.module.WebModuleBuilder
import com.intellij.platform.ProjectTemplate
import com.intellij.platform.ProjectTemplatesFactory

class VueCLIProjectModuleBuilder : WebTemplateNewProjectWizardBuilder(VueCliProjectGenerator())

class VueProjectTemplateFactory : JavaScriptNewTemplatesFactoryBase() {
  override fun createTemplates(context: WizardContext?): Array<ProjectTemplate> = arrayOf(VueCliProjectGenerator())
}
