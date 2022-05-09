/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * @author vnikolaenko
 */
public class CfmlCreateFileAction extends CreateFileFromTemplateAction implements DumbAware {

  @NonNls private static final String DEFAULT_HTML_TEMPLATE_PROPERTY = "DefaultCfmlFileTemplate";

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
