// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.template;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiFile;
import com.jetbrains.plugins.jade.JadeBundle;
import com.jetbrains.plugins.jade.psi.JadeFileImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Created by fedorkorotkov.
 */
public final class JadeTemplateContextType extends TemplateContextType {
  private JadeTemplateContextType() {
    super(JadeBundle.message("pug.template.context.name"));
  }

  @Override
  public boolean isInContext(@NotNull PsiFile file, int offset) {
    return file instanceof JadeFileImpl;
  }
}
