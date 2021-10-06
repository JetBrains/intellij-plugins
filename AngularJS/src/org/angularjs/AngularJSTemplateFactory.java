package org.angularjs;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.lang.javascript.boilerplate.JavaScriptNewTemplatesFactoryBase;
import com.intellij.platform.ProjectTemplate;
import org.angular2.cli.AngularCliProjectGenerator;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSTemplateFactory extends JavaScriptNewTemplatesFactoryBase {

  @Override
  public ProjectTemplate @NotNull [] createTemplates(WizardContext context) {
    return new ProjectTemplate[]{
      new AngularJSProjectGenerator(),
      new AngularCliProjectGenerator()
    };
  }
}
