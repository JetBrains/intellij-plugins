/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.intention;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.language.psi.ConceptDynamicArg;
import com.thoughtworks.gauge.language.psi.SpecDynamicArg;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public final class ConvertToStaticArgIntention extends ConvertArgTypeIntentionBase {
  @Override
  public @Nls @NotNull String getText() {
    return GaugeBundle.message("gauge.convert.to.static.parameter");
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
    if (!element.isWritable()) return false;
    return PsiTreeUtil.getParentOfType(element, SpecDynamicArg.class) != null
           || PsiTreeUtil.getParentOfType(element, ConceptDynamicArg.class) != null;
  }

  @Override
  protected @NotNull String getReplacementString(String paramText) {
    return "\"" + paramText + "\"";
  }
}
