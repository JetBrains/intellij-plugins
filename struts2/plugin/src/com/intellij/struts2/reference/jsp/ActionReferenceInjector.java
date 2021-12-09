/*
 * Copyright 2014 The authors
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
package com.intellij.struts2.reference.jsp;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.injection.ReferenceInjector;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.struts2.Struts2Icons;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

final class ActionReferenceInjector extends ReferenceInjector {
  private final PsiReferenceProvider myActionReferenceProvider = new ActionReferenceProvider();

  @Override
  public PsiReference @NotNull [] getReferences(@NotNull PsiElement element, @NotNull ProcessingContext context, @NotNull TextRange range) {
    if (!(element instanceof XmlAttributeValue)) {
      return PsiReference.EMPTY_ARRAY;
    }

    return myActionReferenceProvider.getReferencesByElement(element, context);
  }

  @NotNull
  @Override
  public String getId() {
    return "struts2-action";
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Struts 2 Action";
  }

  @NotNull
  @Override
  public Icon getIcon() {
    return Struts2Icons.Action;
  }
}
