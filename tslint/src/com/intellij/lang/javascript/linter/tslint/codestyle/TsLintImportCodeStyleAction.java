// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.lang.javascript.linter.tslint.codestyle;

import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

public class TsLintImportCodeStyleAction extends AnAction {

  @Override
  public void update(AnActionEvent e) {
    final DataContext context = e.getDataContext();
    final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(context);
    final boolean enabledAndVisible = e.getProject() != null
                                      && psiFile != null
                                      && TslintUtil.isConfigFile(psiFile.getVirtualFile());
    e.getPresentation().setEnabledAndVisible(enabledAndVisible);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getProject();
    final PsiFile configPsi = CommonDataKeys.PSI_FILE.getData(e.getDataContext());
    if (configPsi == null || project == null) {
      return;
    }
    new TsLintCodeStyleImporter(true, true).importConfigFile(configPsi);
  }
}
