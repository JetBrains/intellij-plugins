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

package com.thoughtworks.gauge.language.psi;

import com.intellij.lang.ASTNode;
import com.thoughtworks.gauge.StepValue;
import com.thoughtworks.gauge.language.psi.impl.ConceptConceptImpl;

import static com.thoughtworks.gauge.language.psi.SpecPsiImplUtil.getStepValueFor;

public final class ConceptPsiImplUtil {

  private ConceptPsiImplUtil() {
  }

  public static StepValue getStepValue(ConceptStep element) {
    ASTNode step = element.getNode();
    String stepText = step.getText().trim();
    int newLineIndex = stepText.indexOf("\n");
    int endIndex = newLineIndex == -1 ? stepText.length() : newLineIndex;
    ConceptTable inlineTable = element.getTable();
    int index = 0;
    if (stepText.trim().charAt(0) == '#') {
      index = 1;
    }
    stepText = stepText.substring(index, endIndex).trim();
    return getStepValueFor(element, stepText, inlineTable != null);
  }

  public static StepValue getStepValue(ConceptConceptImpl conceptConcept) {
    String conceptHeadingText = conceptConcept.getConceptHeading().getText();
    conceptHeadingText = conceptHeadingText.trim().split("\n")[0];
    String text = conceptHeadingText.trim().replaceFirst("#", "");
    return getStepValueFor(conceptConcept, text, false);
  }
}
