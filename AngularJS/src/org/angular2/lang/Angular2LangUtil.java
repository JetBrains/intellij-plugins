// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.angular2.Angular2Framework;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public final class Angular2LangUtil {

  @NonNls public static final String ANGULAR_CORE_PACKAGE = "@angular/core";
  @NonNls public static final String ANGULAR_CLI_PACKAGE = "@angular/cli";
  @NonNls public static final String $IMPLICIT = "$implicit";
  @NonNls public static final String EVENT_EMITTER = "EventEmitter";
  @NonNls public static final String OUTPUT_CHANGE_SUFFIX = "Change";

  public static boolean isAngular2Context(@NotNull PsiElement context) {
    return Angular2Framework.getInstance().isContext(context);
  }

  public static boolean isAngular2Context(@NotNull Project project, @NotNull VirtualFile context) {
    return Angular2Framework.getInstance().isContext(context, project);
  }

}
