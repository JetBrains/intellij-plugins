/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.resolve.directive;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.protobuf.lang.PbTextLanguage;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/** A reference contributor that provides references for directive comments. */
public class SchemaDirectiveReferenceContributor extends PsiReferenceContributor {

  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(
        PlatformPatterns.psiComment().withLanguage(PbTextLanguage.INSTANCE),
        new PsiReferenceProvider() {
          @Override
          public @NotNull PsiReference[] getReferencesByElement(
              @NotNull PsiElement element, @NotNull ProcessingContext context) {
            return getReferencesFromComment((PsiComment) element);
          }
        });
  }

  private static PsiReference[] getReferencesFromComment(PsiComment comment) {
    SchemaDirective schemaDirective = SchemaDirective.find(comment.getContainingFile());
    if (schemaDirective == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    SchemaComment schemaComment = schemaDirective.getSchemaComment(comment);
    if (schemaComment == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    return schemaComment.getAllReferences().toArray(PsiReference.EMPTY_ARRAY);
  }
}
