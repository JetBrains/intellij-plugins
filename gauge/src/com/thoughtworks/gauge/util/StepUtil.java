/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.thoughtworks.gauge.GaugeBootstrapService;
import com.thoughtworks.gauge.GaugeConstants;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.StepValue;
import com.thoughtworks.gauge.connection.GaugeConnection;
import com.thoughtworks.gauge.language.psi.SpecPsiImplUtil;
import com.thoughtworks.gauge.language.psi.SpecStep;
import com.thoughtworks.gauge.language.psi.impl.ConceptConceptImpl;
import com.thoughtworks.gauge.language.psi.impl.ConceptStepImpl;
import com.thoughtworks.gauge.language.psi.impl.SpecStepImpl;
import com.thoughtworks.gauge.reference.ReferenceCache;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public final class StepUtil {
  private static final Logger LOG = Logger.getInstance(StepUtil.class);
  private static final HashMap<String, StepValue> stepValueCache = new HashMap<>();

  public static PsiElement findStepImpl(SpecStep step, Module module) {
    if (module == null) {
      return null;
    }
    GaugeBootstrapService bootstrapService = GaugeBootstrapService.getInstance(module.getProject());

    ReferenceCache cache = bootstrapService.getReferenceCache(module);
    PsiElement reference = cache.searchReferenceFor(step);
    if (reference == null) {
      reference = findStepReference(step, module);
    }
    return reference;
  }

  private static PsiElement findStepReference(SpecStep step, Module module) {
    Collection<PsiMethod> stepMethods = getStepMethods(module);
    PsiMethod method = findStepImplementationMethod(stepMethods, step, module);
    PsiElement referenceElement;
    if (method == null) {
      referenceElement = searchConceptsForImpl(step, module);
      if (referenceElement != null) {
        referenceElement = new ConceptStepImpl(referenceElement.getNode(), true);
      }
    }
    else {
      referenceElement = method;
    }
    addReferenceToCache(step, referenceElement, module);
    return referenceElement;
  }

  private static void addReferenceToCache(SpecStep step, PsiElement referenceElement, Module module) {
    GaugeBootstrapService bootstrapService = GaugeBootstrapService.getInstance(module.getProject());
    bootstrapService.getReferenceCache(module).addStepReference(step, referenceElement);
  }

  private static PsiElement searchConceptsForImpl(SpecStep step, Module module) {
    try {
      VirtualFile[] conceptFiles = findConceptFiles(module);
      if (conceptFiles.length > 0) {
        PsiElement reference;
        for (VirtualFile file : conceptFiles) {
          reference = searchConceptReference(file, step, module);
          if (reference != null) {
            return reference;
          }
        }
      }
    }
    catch (Exception e) {
      LOG.debug(e);
      return null;
    }
    return null;
  }

  private static PsiElement searchConceptReference(VirtualFile virtualFile, SpecStep step, Module module) {
    PsiFile psiFile = PsiManager.getInstance(module.getProject()).findFile(virtualFile);
    if (psiFile == null || !psiFile.isValid()) {
      return null;
    }
    for (PsiElement psiElement : psiFile.getChildren()) {
      if (psiElement.getClass().equals(ConceptConceptImpl.class)) {
        StepValue conceptStepValue = ((ConceptConceptImpl)psiElement).getStepValue();

        if (step.getStepValue().getStepText().equals(conceptStepValue.getStepText())) {
          return psiElement;
        }
      }
    }
    return null;
  }

  private static VirtualFile[] findConceptFiles(Module module) {
    Collection<VirtualFile> conceptFiles = FilenameIndex.getAllFilesByExt(module.getProject(), GaugeConstants.CONCEPT_EXTENSION);
    return conceptFiles.toArray(VirtualFile.EMPTY_ARRAY);
  }

  private static PsiMethod findStepImplementationMethod(Collection<PsiMethod> stepMethods, SpecStep step, Module module) {
    String stepText = step.getStepValue().getStepText();
    for (PsiMethod stepMethod : stepMethods) {
      if (isMatch(stepMethod, stepText, module)) {
        return stepMethod;
      }
    }
    return null;
  }

  public static boolean isMatch(PsiMethod stepMethod, String stepText, Module module) {
    List<String> annotationValues = getGaugeStepAnnotationValues(stepMethod);
    for (String value : annotationValues) {
      if (SpecPsiImplUtil.getStepValueFor(module, stepMethod, value, false).getStepText().equals(stepText)) {
        return true;
      }
    }
    return false;
  }

  public static StepValue getStepValue(final GaugeConnection connection, final String text, Boolean hasInlineTable) {
    String stepText = hasInlineTable ? text + " <table>" : text;
    StepValue value = stepValueCache.get(stepText);
    if (value == null || value.getStepText().isEmpty()) {
      value = connection.getStepValue(text, hasInlineTable);
      stepValueCache.put(stepText, value);
    }
    return value;
  }

  public static List<String> getGaugeStepAnnotationValues(PsiMethod stepMethod) {
    final PsiModifierList modifierList = stepMethod.getModifierList();
    final PsiAnnotation[] annotations = modifierList.getAnnotations();
    List<String> values = new ArrayList<>();
    for (PsiAnnotation annotation : annotations) {
      values.addAll(getGaugeStepAnnotationValues(annotation));
    }
    return values;
  }

  static List<String> getGaugeStepAnnotationValues(PsiAnnotation annotation) {
    List<String> values = new ArrayList<>();
    if (!isGaugeAnnotation(annotation)) {
      return values;
    }
    PsiAnnotationMemberValue attributeValue = annotation.findAttributeValue("value");
    Object value =
      JavaPsiFacade.getInstance(annotation.getProject()).getConstantEvaluationHelper().computeConstantExpression(attributeValue);
    if (value instanceof String) {
      values.add((String)value);
    }
    else if (attributeValue instanceof PsiArrayInitializerMemberValue) {
      PsiAnnotationMemberValue[] memberValues = ((PsiArrayInitializerMemberValue)attributeValue).getInitializers();
      for (PsiAnnotationMemberValue memberValue : memberValues) {
        Object val =
          JavaPsiFacade.getInstance(annotation.getProject()).getConstantEvaluationHelper().computeConstantExpression(memberValue);
        if (val instanceof String) {
          values.add((String)val);
        }
      }
    }
    return values;
  }

  private static boolean isGaugeAnnotation(PsiAnnotation annotation) {
    return Step.class.getCanonicalName().equals(annotation.getQualifiedName());
  }

  public static Collection<PsiMethod> getStepMethods(Module module) {
    final PsiClass step = JavaPsiFacade.getInstance(module.getProject())
      .findClass("com.thoughtworks.gauge.Step", GlobalSearchScope.allScope(module.getProject()));
    if (step != null) {
      GaugeBootstrapService bootstrapService = GaugeBootstrapService.getInstance(module.getProject());

      Collection<PsiMethod> methods = new ArrayList<>();
      for (Module m : bootstrapService.getSubModules(module)) {
        if (m.isDisposed()) continue;

        methods.addAll(
          AnnotatedElementsSearch.searchPsiMethods(step, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(m, true)).findAll());
      }
      return methods;
    }
    return new ArrayList<>();
  }

  public static boolean isImplementedStep(SpecStep step, Module module) {
    return findStepImpl(step, module) != null;
  }

  public static boolean isStep(@Nullable PsiElement element) {
    return element instanceof SpecStepImpl;
  }

  public static boolean isConcept(@Nullable PsiElement element) {
    return element instanceof ConceptStepImpl;
  }

  public static boolean isMethod(PsiElement element) {
    return element instanceof PsiMethod;
  }
}
