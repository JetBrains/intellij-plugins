/*
 * Copyright 2015 The authors
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
 *
 */

package com.intellij.struts2.dom;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.ExtendClass;
import org.jetbrains.annotations.NotNull;

/**
 * Converter for "class" attribute. Must be used in conjunction with {@link ExtendClass} to
 * determine valid references (interface allowed, non-public class allowed, ..).
 *
 * @author Yann C&eacute;bron
 */
public abstract class ExtendableClassConverter extends Converter<PsiClass>
    implements CustomReferenceConverter<PsiClass> {

  /**
   * Stores the reference type display name(s).
   */
  public static final Key<String[]> REFERENCES_TYPES = Key.create("STRUTS2_REFERENCE_TYPES");

  /**
   * References from this EP will be added automatically.
   */
  public static final ExtensionPointName<ExtendableClassConverterContributor> EP_NAME =
    new ExtensionPointName<>("com.intellij.struts2.classContributor");


  /**
   * Contributes results to {@link ExtendableClassConverter}.
   * <p/>
   * If the provided reference implements {@link com.intellij.codeInspection.LocalQuickFixProvider}
   * its fixes will be added automatically.
   *
   * @author Yann C&eacute;bron
   */
  public abstract static class ExtendableClassConverterContributor {

    /**
     * Returns this contributor's type name for display in messages.
     *
     * @return Type name.
     */
    @NotNull
    public abstract String getTypeName();

    /**
     * Is this contributor suitable in the current resolving context.
     *
     * @param convertContext Current context.
     * @return true if yes, false otherwise.
     */
    public abstract boolean isSuitable(@NotNull final ConvertContext convertContext);

    /**
     * Creates references if this contributor is suitable.
     *
     * @param convertContext Current context.
     * @param psiElement     Reference element.
     * @param extendClass    Extend class definition for this element.
     * @return References.
     */
    public abstract PsiReference @NotNull [] getReferences(@NotNull final ConvertContext convertContext,
                                                           @NotNull final PsiElement psiElement,
                                                           @NotNull final ExtendClass extendClass);

  }

}