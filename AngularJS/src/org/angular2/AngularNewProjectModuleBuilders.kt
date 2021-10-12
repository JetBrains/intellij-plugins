// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2

import com.intellij.ide.util.projectWizard.WebTemplateNewProjectWizard
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.GeneratorNewProjectWizardBuilderAdapter
import com.intellij.lang.javascript.boilerplate.JavaScriptNewTemplatesFactoryBase
import com.intellij.platform.ProjectTemplate
import org.angular2.cli.AngularCliProjectGenerator
import org.angularjs.AngularJSProjectGenerator

//no angular.js for new API
class AngularCLIModuleBuilder : GeneratorNewProjectWizardBuilderAdapter(WebTemplateNewProjectWizard(AngularCliProjectGenerator()))

class AngularAndAngularJSTemplateFactory : JavaScriptNewTemplatesFactoryBase() {
  override fun createTemplates(context: WizardContext?): Array<ProjectTemplate> =
    arrayOf(
      AngularCliProjectGenerator(),
      AngularJSProjectGenerator(),
    )
}
