// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.codeInsight.intention.FileModifier;
import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.angular2.cli.AngularCliUtil;
import org.angular2.cli.actions.AngularCliAddDependencyAction;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AngularCliAddQuickFix implements LocalQuickFix, HighPriorityAction {
  private final VirtualFile myPackageJson;
  private final String myPackageName;
  private final String myVersionSpec;
  private final boolean myReinstall;

  public AngularCliAddQuickFix(@NotNull VirtualFile packageJson, @NotNull String packageName,
                               @NotNull String versionSpec, boolean reinstall) {
    myPackageJson = packageJson;
    myPackageName = packageName;
    myVersionSpec = versionSpec;
    myReinstall = reinstall;
  }

  @Override
  public @Nls @NotNull String getName() {
    return Angular2Bundle.message(myReinstall ? "angular.quickfix.json.ng-add.name.reinstall"
                                              : "angular.quickfix.json.ng-add.name.run",
                                  myPackageName);
  }

  @Override
  public @Nls @NotNull String getFamilyName() {
    return Angular2Bundle.message("angular.quickfix.json.ng-add.family");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    if (AngularCliUtil.hasAngularCLIPackageInstalled(project, myPackageJson)) {
      AngularCliAddDependencyAction.runAndShowConsoleLater(
        project, myPackageJson.getParent(), myPackageName, myVersionSpec.trim(), !myReinstall);
    }
    else {
      AngularCliUtil.notifyAngularCliNotInstalled(project, myPackageJson.getParent(),
                                                  Angular2Bundle.message("angular.quickfix.json.ng-add.error.cant-run"));
    }
  }

  @Override
  public @Nullable FileModifier getFileModifierForPreview(@NotNull PsiFile target) {
    return null;
  }
}
