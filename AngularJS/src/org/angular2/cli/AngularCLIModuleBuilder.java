// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli;

import com.intellij.ide.util.projectWizard.WebTemplateNewProjectWizardBuilder;

public class AngularCLIModuleBuilder extends WebTemplateNewProjectWizardBuilder {
  protected AngularCLIModuleBuilder() {
    super(new AngularCliProjectGenerator());
  }
}
