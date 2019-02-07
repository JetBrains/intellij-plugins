/*
 * Copyright 2012 The authors
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

package com.intellij.struts2.model.jam;

import com.intellij.openapi.project.Project;
import com.intellij.patterns.PsiClassPattern;
import com.intellij.patterns.PsiJavaElementPattern;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.PsiPackage;
import com.intellij.semantic.SemContributor;
import com.intellij.semantic.SemRegistrar;
import com.intellij.struts2.model.jam.convention.JamDefaultInterceptorRef;
import com.intellij.struts2.model.jam.convention.JamInterceptorRef;
import com.intellij.struts2.model.jam.convention.JamParentPackage;
import com.intellij.struts2.model.jam.convention.JamResultPath;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PsiJavaPatterns.psiClass;

/**
 * Registers JAM.
 *
 * @author Yann C&eacute;bron
 */
final class StrutsSemContributor extends SemContributor {
  private static final PsiJavaElementPattern.Capture<PsiPackage> PSI_PACKAGE_CAPTURE =
      PsiJavaPatterns.psiElement(PsiPackage.class);

  private static final PsiClassPattern PSI_CLASS_PATTERN = psiClass().nonAnnotationType();

  @Override
  public void registerSemProviders(@NotNull final SemRegistrar registrar, @NotNull Project project) {
    // @DefaultInterceptorRef
    JamDefaultInterceptorRef.META_PACKAGE.register(registrar, PSI_PACKAGE_CAPTURE);

    // @InterceptorRef(s)
    JamInterceptorRef.META_CLASS
        .register(registrar, PSI_CLASS_PATTERN.withAnnotation(JamInterceptorRef.ANNOTATION_NAME));
    JamInterceptorRef.META_CLASS_LIST
        .register(registrar, PSI_CLASS_PATTERN.withAnnotation(JamInterceptorRef.ANNOTATION_NAME_LIST));

    // @ParentPackage
    JamParentPackage.META_CLASS.register(registrar, PSI_CLASS_PATTERN.withAnnotation(JamParentPackage.ANNOTATION_NAME));
    JamParentPackage.META_PACKAGE.register(registrar, PSI_PACKAGE_CAPTURE);

    // @ResultPath
    JamResultPath.META_CLASS.register(registrar, PSI_CLASS_PATTERN.withAnnotation(JamResultPath.ANNOTATION_NAME));
    JamResultPath.META_PACKAGE.register(registrar, PSI_PACKAGE_CAPTURE);
  }

}