/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.gencodeutils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;

import java.util.Arrays;

import static com.google.common.truth.Truth.assertWithMessage;

/**
 * Expectation for going from source code -> other location in source code (e.g., java parameter
 * reference to the parameter declaration).
 */
final class SourceTargetExpectation extends ReferenceGotoExpectation {

  SourceTargetExpectation(String srcReference, GotoExpectationMarker expectation) {
    super(srcReference, expectation);
  }

  @Override
  public void assertCorrectTarget(PsiElement[] targets) {
    PsiNamedElement element = getNamedElement(targets);
    assertWithMessage(String.format("%s goes to named element", this)).that(element).isNotNull();
    assertWithMessage(String.format("%s file match", this))
        .that(element.getContainingFile().getName())
        .isEqualTo(expectation.expectedFile);
    String elementName = element.getName();
    assertWithMessage(String.format("%s element match", this))
        .that(elementName)
        .isEqualTo(expectation.expectedElementName);
  }

  private static PsiNamedElement getNamedElement(PsiElement[] elements) {
    return Arrays.stream(elements)
        .filter(PsiNamedElement.class::isInstance)
        .map(PsiNamedElement.class::cast)
        .findFirst()
        .orElse(null);
  }
}
