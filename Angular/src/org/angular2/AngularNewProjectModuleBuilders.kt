// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2

import com.intellij.ide.util.projectWizard.WebTemplateNewProjectWizard
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.GeneratorNewProjectWizardBuilderAdapter
import com.intellij.lang.javascript.boilerplate.JavaScriptNewTemplatesFactoryBase
import com.intellij.platform.ProjectTemplate
import org.angular2.cli.AngularCliProjectGenerator

//no angular.js for new API
class AngularCLIModuleBuilder : GeneratorNewProjectWizardBuilderAdapter(WebTemplateNewProjectWizard(AngularCliProjectGenerator())){
  override fun getWeight(): Int = WEB_WEIGHT + 10
}

class AngularProjectTemplatesFactory : JavaScriptNewTemplatesFactoryBase() {
  override fun createTemplates(context: WizardContext?): Array<ProjectTemplate> = arrayOf(AngularCliProjectGenerator())
}
