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

import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.psi.PbNamedElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.QualifiedName;

import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

/** Expectation for going from source code -> proto. */
final class ProtoTargetExpectation extends ReferenceGotoExpectation {

  // Marker to use if a reference should resolve to the file itself and not a symbol in the file.
  private static final String FILE_TARGET = "<file>";

  ProtoTargetExpectation(String srcReference, GotoExpectationMarker expectation) {
    super(srcReference, expectation);
  }

  @Override
  public void assertCorrectTarget(PsiElement[] targets) {
    if (expectation.expectedElementName.equals(FILE_TARGET)) {
      PbFile file = getProtoFile(targets);
      assertWithMessage(String.format("%s file match", this))
          .that(file.getName())
          .isEqualTo(expectation.expectedFile);
      return;
    }
    PbNamedElement protoElement = getProtoElement(targets);
    assertWithMessage(String.format("%s goes to proto element", this))
        .that(protoElement)
        .isNotNull();
    PbFile file = protoElement.getPbFile();
    assertWithMessage(String.format("%s file match", this))
        .that(file.getName())
        .isEqualTo(expectation.expectedFile);
    QualifiedName filePackage = file.getPackageQualifiedName();
    QualifiedName elementName = protoElement.getQualifiedName();
    assertThat(elementName).isNotNull();
    assertWithMessage(String.format("%s element match", this))
        .that(elementName.removeHead(filePackage.getComponentCount()).toString())
        .isEqualTo(expectation.expectedElementName);
  }

  private static PbNamedElement getProtoElement(PsiElement[] elements) {
    return Arrays.stream(elements)
        .filter(PbNamedElement.class::isInstance)
        .map(PbNamedElement.class::cast)
        .findFirst()
        .orElse(null);
  }

  private static PbFile getProtoFile(PsiElement[] elements) {
    return Arrays.stream(elements)
        .filter(PbFile.class::isInstance)
        .map(PbFile.class::cast)
        .findFirst()
        .orElse(null);
  }
}
