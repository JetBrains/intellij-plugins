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

/**
 * Pair of source reference and test expectation for goto target. Checks the expectation given the
 * actual targets.
 */
public abstract class ReferenceGotoExpectation {

  private final String srcReference;
  final GotoExpectationMarker expectation;

  ReferenceGotoExpectation(String srcReference, GotoExpectationMarker expectation) {
    this.srcReference = srcReference;
    this.expectation = expectation;
  }

  public static ReferenceGotoExpectation create(
      String srcReference, GotoExpectationMarker expectation) {
    if (expectation.expectedFile.endsWith(".proto")) {
      return new ProtoTargetExpectation(srcReference, expectation);
    } else {
      return new SourceTargetExpectation(srcReference, expectation);
    }
  }

  public abstract void assertCorrectTarget(PsiElement[] targets);

  @Override
  public String toString() {
    return String.format(
        "%s -> %s: %s %s",
        srcReference,
        expectation.expectedFile,
        expectation.expectedElementName,
        expectation.rangeString());
  }
}
