// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import icons.CFMLIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CfmlCreateFileAction extends CreateFileFromTemplateAction implements DumbAware {

  private static final @NonNls String DEFAULT_HTML_TEMPLATE_PROPERTY = "DefaultCfmlFileTemplate";

  public CfmlCreateFileAction() {
    super(CfmlBundle.message("action.name.cfml.cfc.file"), CfmlBundle.message("action.CfmlCreateFileAction.description"), CFMLIcons.Cfml);
  }

  @Override
  protected String getDefaultTemplateProperty() {
    return DEFAULT_HTML_TEMPLATE_PROPERTY;
  }

  @Override
  protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
    builder
      .setTitle(CfmlBundle.message("dialog.title.cfml.cfc.file2"))
      .addKind(CfmlBundle.message("dialog.kind.cfml.file"), CFMLIcons.Cfml, "ColdFusion File.cfm")
      .addKind(CfmlBundle.message("dialog.kind.script.component"), CFMLIcons.Cfml, "ColdFusion Script Component.cfc")
      .addKind(CfmlBundle.message("dialog.kind.script.interface"), CFMLIcons.Cfml, "ColdFusion Script Interface.cfc")
      .addKind(CfmlBundle.message("dialog.kind.tag.component"), CFMLIcons.Cfml, "ColdFusion Tag Component.cfc")
      .addKind(CfmlBundle.message("dialog.kind.tag.interface"), CFMLIcons.Cfml, "ColdFusion Tag Interface.cfc");
  }

  @Override
  protected String getActionName(PsiDirectory directory, @NotNull String newName, String templateName) {
    return CfmlBundle.message("action.name.cfml.cfc.file");
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
