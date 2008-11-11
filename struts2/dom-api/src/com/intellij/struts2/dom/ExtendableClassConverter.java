/*
 * Copyright 2008 The authors
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
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.CustomReferenceConverter;
import org.jetbrains.annotations.NotNull;

/**
 * Converter for "class" attribute.
 *
 * @author Yann C&eacute;bron
 */
public abstract class ExtendableClassConverter extends Converter<PsiClass> implements CustomReferenceConverter<PsiClass> {

  /**
   * Stores the reference type display name(s).
   */
  public static final Key<String[]> REFERENCES_TYPES = Key.create("STRUTS2_REFERENCE_TYPES");

  /**
   * References from this EP will be added automatically.
   */
  public static final ExtensionPointName<ExtendableClassConverterContributor> EP_NAME =
      new ExtensionPointName<ExtendableClassConverterContributor>("com.intellij.struts2.classContributor");


  /**
   * Contributes results to {@link ExtendableClassConverter}.
   *
   * @author Yann C&eacute;bron
   */
  public abstract static class ExtendableClassConverterContributor extends PsiReferenceProvider {

    /**
     * Is this contributor suitable in the current resolving context.
     *
     * @param convertContext Current context.
     * @return true if yes, false otherwise.
     */
    public abstract boolean isSuitable(@NotNull final ConvertContext convertContext);

    /**
     * Returns this contributor's type name for display in messages.
     *
     * @return Display name.
     */
    public abstract String getContributorType();

  }

}