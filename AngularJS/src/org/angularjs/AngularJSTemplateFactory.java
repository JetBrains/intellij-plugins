package org.angularjs;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import org.jetbrains.annotations.NotNull;

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
  public ProjectTemplate[] createTemplates(@NotNull String group, WizardContext context) {
    return new ProjectTemplate[] {
      new AngularJSProjectGenerator()
    };
  }
}
