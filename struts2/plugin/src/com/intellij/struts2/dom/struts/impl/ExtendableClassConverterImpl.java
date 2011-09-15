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

package com.intellij.struts2.dom.struts.impl;

import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlElement;
import com.intellij.struts2.dom.ExtendableClassConverter;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomJavaUtil;
import com.intellij.util.xml.ExtendClass;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 */
public class ExtendableClassConverterImpl extends ExtendableClassConverter {

  public PsiClass fromString(@Nullable @NonNls final String s, final ConvertContext context) {
    if (s == null) {
      return null;
    }

    // resolve JAVA-class directly
    final PsiClass psiClass = DomJavaUtil.findClass(s, context.getInvocationElement());
    if (psiClass != null) {
      return psiClass;
    }

    // first match in additional providers
    final XmlElement element = context.getReferenceXmlElement();
    assert element != null;

    for (final ExtendableClassConverterContributor contributor : Extensions.getExtensions(EP_NAME)) {
      if (contributor.isSuitable(context)) {
        final PsiReference[] add = contributor.getReferencesByElement(element, new ProcessingContext());
        if (add.length == 1) {
          final PsiElement resolveElement = add[0].resolve();
          if (resolveElement != null) {
            return (PsiClass) resolveElement;
          }
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
    final ExtendClass extendClass = psiClassGenericDomValue.getAnnotation(ExtendClass.class);
    assert extendClass != null : psiClassGenericDomValue + " must be annotated with @ExtendClass";

    // 1. "normal" JAVA classes
    final GlobalSearchScope scope = getResolveScope(psiClassGenericDomValue);
    final JavaClassReferenceProvider classReferenceProvider =
        new JavaClassReferenceProvider() {
          @Override
          public GlobalSearchScope getScope(final Project project) {
            return scope;
          }
        };
    if (extendClass.instantiatable()) {
      classReferenceProvider.setOption(JavaClassReferenceProvider.INSTANTIATABLE, Boolean.TRUE);
    }
    if (!extendClass.allowAbstract()) {
      classReferenceProvider.setOption(JavaClassReferenceProvider.CONCRETE, Boolean.TRUE);
    }
    final boolean allowInterface = extendClass.allowInterface();
    if (!allowInterface) {
      classReferenceProvider.setOption(JavaClassReferenceProvider.NOT_INTERFACE, Boolean.TRUE);
    }
    if (StringUtil.isNotEmpty(extendClass.value())) {
      classReferenceProvider.setOption(JavaClassReferenceProvider.EXTEND_CLASS_NAMES,
                                       new String[]{extendClass.value()});
    }
    classReferenceProvider.setSoft(true);
    PsiReference[] javaClassReferences = classReferenceProvider.getReferencesByElement(element);


    @NonNls String[] referenceTypes = allowInterface ? new String[]{"class", "interface"} : new String[]{"class"};

    // 2. additional resolvers (currently Spring only)
    for (final ExtendableClassConverterContributor contributor : Extensions.getExtensions(EP_NAME)) {
      if (contributor.isSuitable(context)) {
        final PsiReference[] additionalReferences = contributor.getReferencesByElement(element,
                                                                                       new ProcessingContext());
        javaClassReferences = ArrayUtil.mergeArrays(javaClassReferences, additionalReferences, PsiReference.ARRAY_FACTORY);
        referenceTypes = ArrayUtil.append(referenceTypes,
                                          contributor.getContributorType(),
                                          ArrayUtil.STRING_ARRAY_FACTORY);
      }
    }

    psiClassGenericDomValue.putUserData(REFERENCES_TYPES, referenceTypes);
    return javaClassReferences;
  }

  @Nullable
  private static GlobalSearchScope getResolveScope(final GenericDomValue genericdomvalue) {
    final Module module = genericdomvalue.getModule();
    return module != null ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false) : null;
  }

}