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
package com.intellij.protobuf.jvm;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiNamedElement;
import com.intellij.protobuf.lang.psi.PbDefinition;

/** Contextual information relevant to navigating from generated java code to .proto files. */
public class PbJavaGotoDeclarationContext {

  /** The element at the caret, resolved to its real definition. */
  public final PsiNamedElement resolvedElement;

  /**
   * The containing class or enum, generated from a {@link
   * PbDefinition}.
   */
  public final PsiClass javaClass;

  /** The outermost class of {@link this#javaClass} (could be the same). */
  public final PsiClass outerClass;

  public PbJavaGotoDeclarationContext(
      PsiNamedElement resolvedElement, PsiClass javaClass, PsiClass outerClass) {
    this.resolvedElement = resolvedElement;
    this.javaClass = javaClass;
    this.outerClass = outerClass;
  }

  @Override
  public String toString() {
    return String.format(
        "{\n\tElement: %s\n\tDefinition class: %s\n\tQualified Name: %s\n\tOuter class: %s\n}",
        resolvedElement,
        javaClass,
        javaClass.getQualifiedName() + "#" + resolvedElement.getName(),
        outerClass);
  }
}
