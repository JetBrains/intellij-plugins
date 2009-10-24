/*
 * Copyright 2009 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2.model.jam;

import com.intellij.patterns.PsiClassPattern;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.PsiPackage;
import com.intellij.semantic.SemContributor;
import com.intellij.semantic.SemRegistrar;
import com.intellij.struts2.model.jam.convention.JamParentPackage;
import com.intellij.struts2.model.jam.convention.JamResultPath;

import static com.intellij.patterns.PsiJavaPatterns.psiClass;

/**
 * Registers JAM.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsSemContributor extends SemContributor {

  public void registerSemProviders(final SemRegistrar registrar) {
    final PsiClassPattern psiClassPattern = psiClass().nonAnnotationType();

    // @ParentPackage
    JamParentPackage.META_CLASS.register(registrar, psiClassPattern.withAnnotation(JamParentPackage.ANNOTATION_NAME));
    JamParentPackage.META_PACKAGE.register(registrar, PsiJavaPatterns.psiElement(PsiPackage.class));

    // @ResultPath
    JamResultPath.META_CLASS.register(registrar, psiClassPattern.withAnnotation(JamResultPath.ANNOTATION_NAME));
    JamResultPath.META_PACKAGE.register(registrar, PsiJavaPatterns.psiElement(PsiPackage.class));
  }

}