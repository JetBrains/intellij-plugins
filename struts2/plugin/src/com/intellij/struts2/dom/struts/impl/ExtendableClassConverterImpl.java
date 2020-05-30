/*
 * Copyright 2013 The authors
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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlElement;
import com.intellij.struts2.dom.ExtendableClassConverter;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 */
public class ExtendableClassConverterImpl extends ExtendableClassConverter {

  @Override
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

    final ExtendClass extendClass = getExtendsAnnotation(context.getInvocationElement());

    for (final ExtendableClassConverterContributor contributor : EP_NAME.getExtensionList()) {
      if (contributor.isSuitable(context)) {
        final PsiReference[] add = contributor.getReferences(context, element, extendClass);
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

  @Override
  public String toString(@Nullable final PsiClass psiClass, final ConvertContext context) {
    return psiClass != null ? psiClass.getQualifiedName() : null;
  }

  @Override
  public PsiReference @NotNull [] createReferences(final GenericDomValue<PsiClass> psiClassGenericDomValue,
                                                   final PsiElement element,
                                                   final ConvertContext context) {
    final ExtendClass extendClass = getExtendsAnnotation(psiClassGenericDomValue);

    // 1. "normal" JAVA classes
    final GlobalSearchScope scope = getResolveScope(psiClassGenericDomValue);
    final JavaClassReferenceProvider classReferenceProvider =
        new JavaClassReferenceProvider() {
          @Override
          public GlobalSearchScope getScope(final Project project) {
            return scope;
          }
        };
    PsiClassConverter.createJavaClassReferenceProvider(psiClassGenericDomValue, extendClass, classReferenceProvider);
    PsiReference[] javaClassReferences = classReferenceProvider.getReferencesByElement(element);

    final boolean allowInterface = extendClass.allowInterface();
    @NonNls String[] referenceTypes = allowInterface ? new String[]{"class", "interface"} : new String[]{"class"};

    // 2. additional resolvers
    for (final ExtendableClassConverterContributor contributor : EP_NAME.getExtensionList()) {
      if (contributor.isSuitable(context)) {
        final PsiReference[] additionalReferences = contributor.getReferences(context, element, extendClass);
        javaClassReferences = ArrayUtil.mergeArrays(javaClassReferences,
                                                    additionalReferences,
                                                    PsiReference.ARRAY_FACTORY);
        referenceTypes = ArrayUtil.append(referenceTypes,
                                          contributor.getTypeName(),
                                          ArrayUtil.STRING_ARRAY_FACTORY);
      }
    }

    psiClassGenericDomValue.putUserData(REFERENCES_TYPES, referenceTypes);
    return javaClassReferences;
  }

  @NotNull
  private static ExtendClass getExtendsAnnotation(final DomElement psiClassDomElement) {
    final ExtendClass extendClass = psiClassDomElement.getAnnotation(ExtendClass.class);
    assert extendClass != null : psiClassDomElement + " must be annotated with @ExtendClass";
    return extendClass;
  }

  @Nullable
  private static GlobalSearchScope getResolveScope(final GenericDomValue genericdomvalue) {
    final Module module = genericdomvalue.getModule();
    return module != null ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false) : null;
  }

}