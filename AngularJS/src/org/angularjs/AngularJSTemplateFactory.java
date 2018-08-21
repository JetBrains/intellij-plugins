package org.angularjs;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import org.angular2.cli.AngularCLIProjectGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSTemplateFactory extends ProjectTemplatesFactory {
  @NotNull
  @Override
  public String[] getGroups() {
    return new String[]{WebModuleBuilder.GROUP_NAME};
  }


  @NotNull
  @Override
  public ProjectTemplate[] createTemplates(@Nullable String group, WizardContext context) {
    return new ProjectTemplate[] {
      new AngularJSProjectGenerator(),
      new AngularCLIProjectGenerator()
    };
  }
}
