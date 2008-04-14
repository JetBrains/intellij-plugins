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

package com.intellij.struts2.dom.struts.impl;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlElement;
import com.intellij.struts2.dom.struts.action.ActionClassConverter;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.DomJavaUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann CŽbron
 */
public class ActionClassConverterImpl extends ActionClassConverter {

  private static ActionClassConverterContributor[] ADDITIONAL_CONTRIBUTORS = new ActionClassConverterContributor[0];

  /**
   * Adds the given providers.
   *
   * @param additionalProviders Additional providers to query.
   */
  public static void addAdditionalContributors(final ActionClassConverterContributor[] additionalProviders) {
    ADDITIONAL_CONTRIBUTORS = ArrayUtil.mergeArrays(ADDITIONAL_CONTRIBUTORS,
                                                    additionalProviders,
                                                    ActionClassConverterContributor.class);
  }

  public PsiClass fromString(@Nullable @NonNls final String s, final ConvertContext context) {
    if (s == null) {
      return null;
    }

    // resolve JAVA-class directly
    final PsiClass psiClass = DomJavaUtil.findClass(s, context.getFile(), context.getModule(), null);
    if (psiClass != null) {
      return psiClass;
    }

    // first match in additional providers
    final XmlElement element = context.getReferenceXmlElement();
    if (element == null) {
      return null;
    }

    for (final ActionClassConverterContributor actionClassConverterContributor : ADDITIONAL_CONTRIBUTORS) {
      if (actionClassConverterContributor.isSuitable(context)) {
        final PsiReference[] add = actionClassConverterContributor.getReferencesByElement(element, new ProcessingContext());
        if (add.length == 1 && add[0].resolve() != null) {
          return (PsiClass) add[0].resolve();
        }
      }
    }

    return null;
  }

  public String toString(@Nullable final PsiClass psiClass, final ConvertContext context) {
    return psiClass != null ? psiClass.getQualifiedName() : null;
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<PsiClass> psiClassGenericDomValue,
                                         final PsiElement element,
                                         final ConvertContext context) {

    // 1. "normal" JAVA classes
    final GlobalSearchScope scope = getResolveScope(psiClassGenericDomValue);
    final JavaClassReferenceProvider javaClassReferenceProvider = new JavaClassReferenceProvider(scope);

    javaClassReferenceProvider.setOption(JavaClassReferenceProvider.INSTANTIATABLE, Boolean.TRUE);
    javaClassReferenceProvider.setOption(JavaClassReferenceProvider.CONCRETE, Boolean.TRUE);
    javaClassReferenceProvider.setOption(JavaClassReferenceProvider.NOT_INTERFACE, Boolean.TRUE);
    javaClassReferenceProvider.setSoft(true);
    PsiReference[] javaClassReferences = javaClassReferenceProvider.getReferencesByElement(element);


    @NonNls String[] referenceTypes = new String[]{"class"};
    
    // 2. additional resolvers (currently Spring only)
    for (final ActionClassConverterContributor actionClassConverterContributor : ADDITIONAL_CONTRIBUTORS) {
      if (actionClassConverterContributor.isSuitable(context)) {
        final PsiReference[] additionalReferences = actionClassConverterContributor.getReferencesByElement(element, new ProcessingContext());
        javaClassReferences = ArrayUtil.mergeArrays(javaClassReferences, additionalReferences, PsiReference.class);
        referenceTypes = ArrayUtil.append(referenceTypes, actionClassConverterContributor.getContributorType());
      }
    }

    psiClassGenericDomValue.putUserData(REFERENCES_TYPES, referenceTypes);
    return javaClassReferences;
  }

  @Nullable
  private static GlobalSearchScope getResolveScope(final GenericDomValue genericdomvalue) {
    final Module module = genericdomvalue.getModule();
    return module != null ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module) : null;
  }

}