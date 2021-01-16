/*
 * Copyright 2010 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Provides reference functionality for static String values.
 *
 * @author Yann C&eacute;bron
 */
public class StaticStringValuesReferenceProvider extends PsiReferenceProvider {

  private final boolean allowOtherValues;
  private final String[] values;

  /**
   * Creates a reference provider with the given values for autocompletion.
   * Other values will *not* be highlighted as errors.
   *
   * @param values Autocompletion values.
   */
  public StaticStringValuesReferenceProvider(@NonNls final String @NotNull ... values) {
    this(true, values);
  }

  /**
   * Creates a reference provider with the given values for autocompletion and optional error highlighting.
   *
   * @param allowOtherValues Set to false to enable error highlighting.
   * @param values           Autocompletion values.
   */
  public StaticStringValuesReferenceProvider(final boolean allowOtherValues, @NonNls final String @NotNull ... values) {
    this.allowOtherValues = allowOtherValues;
    Arrays.sort(values); // make sure Arrays.binarySearch() works later on..
    this.values = values;
  }

  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement element,
                                                         @NotNull final ProcessingContext context) {
    return new PsiReference[]{new PsiReferenceBase<>((XmlAttributeValue)element) {
      @Override
      public PsiElement resolve() {
        final String myValue = myElement.getValue();
        if (allowOtherValues ||
            TaglibUtil.isDynamicExpression(myValue)) {
          return myElement;
        }

        return Arrays.binarySearch(values, myValue) > -1 ? myElement : null;
      }

      @Override
      public Object @NotNull [] getVariants() {
        return values;
      }
    }};
  }

}