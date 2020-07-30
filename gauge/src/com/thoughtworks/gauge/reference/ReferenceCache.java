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

package com.thoughtworks.gauge.reference;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.thoughtworks.gauge.language.psi.SpecStep;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ReferenceCache {
  private static final Logger LOG = Logger.getInstance(ReferenceCache.class);

  private final Map<String, PsiStepReferenceCache> stepReferences;

  public ReferenceCache() {
    this.stepReferences = new ConcurrentHashMap<>();
  }

  public PsiElement searchReferenceFor(SpecStep step) {
    try {
      String stepValueText = step.getStepValue().getStepText();
      PsiStepReferenceCache element = stepReferences.get(stepValueText);
      if (isValid(element)) {
        return element.getPsiElement();
      }
      stepReferences.remove(stepValueText);
    }
    catch (Exception e) {
      LOG.debug(e);
      return null;
    }
    return null;
  }

  public void addStepReference(SpecStep step, PsiElement referenceElement) {
    if (isValidPsiElement(referenceElement)) {
      stepReferences.put(step.getStepValue().getStepText(), new PsiStepReferenceCache(referenceElement.getText(), referenceElement));
    }
  }

  private static boolean isValidPsiElement(PsiElement psiElement) {
    return psiElement != null && psiElement.isValid();
  }

  private static boolean isValid(PsiStepReferenceCache element) {
    if (element == null || element.getPsiElement() == null) {
      return false;
    }
    PsiElement psiElement = element.getPsiElement();
    return psiElement.isValid() && element.getText().equals(psiElement.getText());
  }

  private static class PsiStepReferenceCache {
    private final String text;
    private final PsiElement psiElement;

    private PsiStepReferenceCache(String text, PsiElement psiElement) {
      this.text = text;
      this.psiElement = psiElement;
    }

    public String getText() {
      return text;
    }

    public PsiElement getPsiElement() {
      return psiElement;
    }
  }
}
