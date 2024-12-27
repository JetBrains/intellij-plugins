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

package com.thoughtworks.gauge.findUsages.helper;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiMethodImpl;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.thoughtworks.gauge.findUsages.StepCollector;
import com.thoughtworks.gauge.helper.ModuleHelper;
import com.thoughtworks.gauge.language.psi.SpecPsiImplUtil;
import com.thoughtworks.gauge.language.psi.SpecStep;
import com.thoughtworks.gauge.language.psi.impl.ConceptStepImpl;
import com.thoughtworks.gauge.language.psi.impl.SpecStepImpl;
import com.thoughtworks.gauge.util.GaugeUtil;
import com.thoughtworks.gauge.util.StepUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ReferenceSearchHelper {

  public static final String UNKNOWN_SCOPE = "<unknown scope>";
  private final ModuleHelper helper;

  public ReferenceSearchHelper() {
    helper = new ModuleHelper();
  }

  public ReferenceSearchHelper(ModuleHelper helper) {
    this.helper = helper;
  }

  public boolean shouldFindReferences(@NotNull ReferencesSearch.SearchParameters searchParameters, PsiElement element) {
    return helper.isGaugeModule(element) &&
           !searchParameters.getScopeDeterminedByUser().getDisplayName().equals(UNKNOWN_SCOPE) &&
           GaugeUtil.isGaugeElement(element);
  }

  public @NotNull StepCollector getStepCollector(PsiElement element) {
    return new StepCollector(element.getProject());
  }

  public static @NotNull List<PsiElement> getPsiElements(StepCollector collector, PsiElement element) {
    List<PsiElement> elements = new ArrayList<>();
    if (element instanceof ConceptStepImpl) {
      elements = collector.get(getConceptStepText((ConceptStepImpl)element));
    }
    else if (element instanceof PsiMethodImpl) {
      for (String alias : StepUtil.getGaugeStepAnnotationValues((PsiMethod)element)) {
        elements.addAll(collector.get(getStepText(alias, element)));
      }
    }
    else if (element instanceof SpecStepImpl) {
      elements = collector.get(getStepText((SpecStepImpl)element));
      elements.addAll(collector.get(((SpecStepImpl)element).getName()));
    }
    return elements;
  }

  private static String getConceptStepText(ConceptStepImpl element) {
    String text = element.getStepValue().getStepText().trim();
    return !text.isEmpty() && text.charAt(0) == '*' || text.charAt(0) == '#' ? text.substring(1).trim() : text;
  }

  private static String getStepText(SpecStep elementToSearch) {
    return elementToSearch.getStepValue().getStepText().trim();
  }

  private static String getStepText(String text, PsiElement element) {
    String stepText = !text.isEmpty() && text.charAt(0) == '"' ? text.substring(1, text.length() - 1) : text;
    return SpecPsiImplUtil.getStepValueFor(element, stepText, false).getStepText();
  }
}