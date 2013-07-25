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
package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.psi.CfmlImplicitVariable;
import com.intellij.coldFusion.model.psi.CfmlPsiUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.StandardPatterns.string;

/**
 * @author vnikolaenko
 */
public class CfmlReferenceContributor extends PsiReferenceContributor {
  public static final PsiElementPattern.Capture<PsiComment> CFMLVARIABLE_COMMENT =
    psiElement(PsiComment.class).inFile(psiElement(CfmlFile.class)).withText(string().contains(CfmlFile.CFMLVARIABLE_MARKER));

  public static final PsiElementPattern.Capture<PsiComment> CFMLJAVALOADER_COMMENT =
    psiElement(PsiComment.class).inFile(psiElement(CfmlFile.class)).withText(string().contains(CfmlFile.CFMLJAVALOADER_MARKER));
  /*
  public static final PsiElementPattern.Capture<PsiComment> CFMLVARIABLE_COMMENT =
    psiElement(PsiComment.class).inFile(psiElement(CfmlFile.class)).withText(string().contains(CfmlFile.CFMLVARIABLE_MARKER));
    */

  private class VariableReferenceProvider extends PsiReferenceProvider {
    @NotNull
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
      final String text = element.getText();
      TextRange range = CfmlPsiUtil.findRange(text, "name=\"", "\"");
      if (range == null) {
        return PsiReference.EMPTY_ARRAY;
      }

      final String name = range.substring(text);
      final CfmlImplicitVariable variable = ((CfmlFile)element.getContainingFile()).findImplicitVariable(name);
      if (variable == null) {
        return PsiReference.EMPTY_ARRAY;
      }

      PsiReferenceBase<PsiComment> ref =
        new PsiReferenceBase<PsiComment>((PsiComment)element, TextRange.from(range.getStartOffset(), name.length())) {
          public PsiElement resolve() {
            return variable;
          }

          @NotNull
          public Object[] getVariants() {
            return EMPTY_ARRAY;
          }
        };
      final List<PsiReference> result = new SmartList<PsiReference>();
      result.add(ref);
      return result.toArray(PsiReference.EMPTY_ARRAY);
    }
  }

  public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {
    registerImplicitVariableProvider(registrar);
  }

  private void registerImplicitVariableProvider(PsiReferenceRegistrar registrar) {
    // reference to java types
    registrar.registerReferenceProvider(CFMLVARIABLE_COMMENT, new PsiReferenceProvider() {
      @NotNull
      public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
        return getReferencesToJavaTypes(element);
      }
    }, PsiReferenceRegistrar.DEFAULT_PRIORITY);

    registrar.registerReferenceProvider(CFMLVARIABLE_COMMENT, new VariableReferenceProvider(), PsiReferenceRegistrar.DEFAULT_PRIORITY);
    registrar.registerReferenceProvider(CFMLJAVALOADER_COMMENT, new VariableReferenceProvider(), PsiReferenceRegistrar.DEFAULT_PRIORITY);
  }

  public static PsiReference[] getReferencesToJavaTypes(PsiElement element) {
    final String text = element.getText();
    TextRange range = findTypeNameRange(text);
    if (range == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    final JavaClassReferenceProvider provider = new JavaClassReferenceProvider();
    return provider.getReferencesByString(range.substring(text), element, range.getStartOffset());
  }

  @Nullable
  public static TextRange findTypeNameRange(@NotNull String text) {
    return CfmlPsiUtil.findRange(text, "type=\"", "\"");
  }
}
