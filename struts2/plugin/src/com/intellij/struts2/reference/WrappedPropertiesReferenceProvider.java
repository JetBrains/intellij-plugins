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

package com.intellij.struts2.reference;

import com.intellij.psi.CommonReferenceProviderTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Reference to .properties keys (for I18N).
 * <p/>
 * Can be switched off globally in S2 facet settings.
 *
 * @author Yann C&eacute;bron
 */
public class WrappedPropertiesReferenceProvider extends PsiReferenceProvider {

  private final PsiReferenceProvider propertiesProvider;

  public WrappedPropertiesReferenceProvider() {
    propertiesProvider = CommonReferenceProviderTypes.PROPERTIES_FILE_KEY_PROVIDER.getProvider();
  }

  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement psiElement,
                                                         @NotNull final ProcessingContext processingContext) {
    final StrutsFacet facet = StrutsFacet.getInstance(psiElement);
    return facet != null && !facet.getConfiguration().isPropertiesKeysDisabled() ?
        propertiesProvider.getReferencesByElement(psiElement, processingContext) : PsiReference.EMPTY_ARRAY;
  }

}