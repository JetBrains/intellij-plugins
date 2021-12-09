/*
 * Copyright 2011 The authors
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

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.struts2.StrutsIcons;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * UI Form tags "theme" attribute.
 * <p/>
 * TODO: real resolving to available themes
 *
 * @author Yann C&eacute;bron
 */
public class ThemeReferenceProvider extends PsiReferenceProvider {

  @NonNls
  private static final Object[] DEFAULT_THEMES = new Object[]{
    LookupElementBuilder.create("simple").withIcon(StrutsIcons.THEME),
    LookupElementBuilder.create("xhtml").withIcon(StrutsIcons.THEME),
    LookupElementBuilder.create("ajax").withIcon(StrutsIcons.THEME)
  };

  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
    return new PsiReference[]{new PsiReferenceBase<>((XmlAttributeValue)element) {
      @Override
      public PsiElement resolve() {
        return myElement;
      }

      @Override
      public Object @NotNull [] getVariants() {
        return DEFAULT_THEMES;
      }
    }};
  }

}
