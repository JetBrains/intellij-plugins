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
package com.intellij.coldFusion.UI.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.coldFusion.CfmlBundle;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 17.02.2009
 */
public abstract class CfmlInspectionBase extends LocalInspectionTool {
  @NotNull
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new PsiElementVisitor() {
      public void visitElement(final PsiElement element) {
        registerProblems(element, holder);
      }
    };
  }

  protected abstract void registerProblems(final PsiElement element, final ProblemsHolder holder);

  @Nls
  @NotNull
  public String getGroupDisplayName() {
    return CfmlBundle.message("cfml.inspections.group");
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }

  public boolean isEnabledByDefault() {
    return true;
  }
}
