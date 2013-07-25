package com.intellij.coldFusion.UI;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import icons.CFMLIcons;
import org.jetbrains.annotations.NonNls;

/**
 * @author: vnikolaenko
 */
public class CfmlCreateFileAction extends CreateFileFromTemplateAction implements DumbAware {

  @NonNls private static final String DEFAULT_HTML_TEMPLATE_PROPERTY = "DefaultCfmlFileTemplate";

  public CfmlCreateFileAction() {
    super("CFML/CFC file", "Creates new CFML/CFC file", CFMLIcons.Cfml);
  }

  @Override
  protected String getDefaultTemplateProperty() {
    return DEFAULT_HTML_TEMPLATE_PROPERTY;
  }

  @Override
  protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
    builder
      .setTitle("CFML/CFC File")
      .addKind("CFML File", CFMLIcons.Cfml, "ColdFusion File.cfm")
      .addKind("Script Component", CFMLIcons.Cfml, "ColdFusion Script Component.cfc")
      .addKind("Script Interface", CFMLIcons.Cfml, "ColdFusion Script Interface.cfc")
      .addKind("Tag Component", CFMLIcons.Cfml, "ColdFusion Tag Component.cfc")
      .addKind("Tag Interface", CFMLIcons.Cfml, "ColdFusion Tag Interface.cfc");
  }

  @Override
  protected String getActionName(PsiDirectory directory, String newName, String templateName) {
    return "CFML/CFC file";
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof CfmlCreateFileAction;
  }
}
