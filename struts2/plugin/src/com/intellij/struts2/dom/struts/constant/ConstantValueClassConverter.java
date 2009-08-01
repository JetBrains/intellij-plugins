/*
 * Copyright 2009 The authors
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

package com.intellij.struts2.dom.struts.constant;

import com.intellij.jpa.model.xml.impl.converters.ClassConverterBase;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Resolves to Shortcut-name, JAVA-Class or result from {@link ConstantValueConverterClassContributor}.
 */
class ConstantValueClassConverter extends ClassConverterBase {

  private final String baseClass;
  private final Map<String, String> shortCutToPsiClassMap;
  private final boolean hasShortCuts;

  ConstantValueClassConverter(@NonNls final String baseClass,
                                      final Map<String, String> shortCutToPsiClassMap) {

    this.baseClass = baseClass;
    this.shortCutToPsiClassMap = shortCutToPsiClassMap;
    this.hasShortCuts = !shortCutToPsiClassMap.isEmpty();
  }

  public PsiClass fromString(@Nullable @NonNls final String s, final ConvertContext convertContext) {
    if (s == null) {
      return null;
    }

    // 1. via shortcut
    if (hasShortCuts) {
      final String shortCutClassName = shortCutToPsiClassMap.get(s);

      if (StringUtil.isNotEmpty(shortCutClassName)) {
        return super.fromString(shortCutClassName, convertContext);
      }
    }

    // 2. first non-null result from extension point contributor (currently only Spring)
    for (final ConstantValueConverterClassContributor converterClassContributor : Extensions.getExtensions(
        ConstantValueConverter.EP_NAME)) {
      final PsiClass contributorClass = converterClassContributor.fromString(s, convertContext);
      if (contributorClass != null) {
        return contributorClass;
      }
    }

    // 3. via JAVA-class
    return super.fromString(s, convertContext);
  }

  @NotNull
  public Set<String> getAdditionalVariants(@NotNull final ConvertContext context) {
    return shortCutToPsiClassMap.keySet();
  }

  protected void setJavaClassReferenceProviderOptions(final JavaClassReferenceProvider javaClassReferenceProvider,
                                                      final ConvertContext convertContext) {
    super.setJavaClassReferenceProviderOptions(javaClassReferenceProvider, convertContext);
    javaClassReferenceProvider.setOption(JavaClassReferenceProvider.CONCRETE, Boolean.TRUE);
    javaClassReferenceProvider.setOption(JavaClassReferenceProvider.NOT_INTERFACE, Boolean.TRUE);
    javaClassReferenceProvider.setOption(JavaClassReferenceProvider.EXTEND_CLASS_NAMES, new String[]{baseClass});
  }
}
