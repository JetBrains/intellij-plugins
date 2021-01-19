// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.web.WebFramework;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Angular2LangUtil {

  @NonNls public static final String ANGULAR_CORE_PACKAGE = "@angular/core";
  @NonNls public static final String ANGULAR_CLI_PACKAGE = "@angular/cli";
  @NonNls public static final String $IMPLICIT = "$implicit";

  @NotNull
  public static final WebFramework ANGULAR_FRAMEWORK = Objects.requireNonNull(WebFramework.get("angular2"));

  public static boolean isAngular2Context(@NotNull PsiElement context) {
    return ANGULAR_FRAMEWORK.isContext(context);
  }

  public static boolean isAngular2Context(@NotNull Project project, @NotNull VirtualFile context) {
    if (ApplicationManager.getApplication().isUnitTestMode()
        && "disabled".equals(System.getProperty("angular.js"))) {
      return false;
    }
    return ANGULAR_FRAMEWORK.isContext(context, project);
  }

}
