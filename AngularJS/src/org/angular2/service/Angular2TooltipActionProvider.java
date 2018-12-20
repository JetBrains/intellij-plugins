// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.service;

import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings;
import com.intellij.lang.typescript.compiler.languageService.codeFixes.TypeScriptAnnotationTooltipActionProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.settings.AngularSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Provides ide quick fixes for service-related errors
 */
public class Angular2TooltipActionProvider extends TypeScriptAnnotationTooltipActionProvider {
  @Override
  protected boolean isAcceptableContext(@NotNull PsiFile psiFile) {
    Project project = psiFile.getProject();

    return psiFile.getLanguage() instanceof Angular2HtmlLanguage &&
           AngularSettings.get(project).isUseService() &&
           TypeScriptCompilerSettings.getSettings(project).useService();
  }
}
