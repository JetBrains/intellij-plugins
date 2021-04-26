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
package com.intellij.protobuf.lang.psi.impl;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.protobuf.lang.psi.PbElement;
import org.jetbrains.annotations.NotNull;

interface PbOverridableElement extends PbElement {
  Key<PsiElement> PARENT_OVERRIDE = Key.create("PARENT_OVERRIDE");
  Key<PsiElement> NAVIGATION_ELEMENT_OVERRIDE = Key.create("NAVIGATION_ELEMENT_OVERRIDE");
  Key<TextRange> TEXT_RANGE_OVERRIDE = Key.create("TEXT_RANGE_OVERRIDE");

  /**
   * Override the value of {@link #getParent()}.
   *
   * @param parent the override {@link #getParent()} result.
   */
  default void setParentOverride(PsiElement parent) {
    putCopyableUserData(PARENT_OVERRIDE, parent);
  }

  /**
   * Override the value of {@link #getNavigationElement()}.
   *
   * @param navigationElement the override {@link #getNavigationElement()} result.
   */
  default void setNavigationElementOverride(PsiElement navigationElement) {
    putCopyableUserData(NAVIGATION_ELEMENT_OVERRIDE, navigationElement);
  }

  /**
   * Override the value of {@link #getTextRange()}.
   *
   * @param textRange the override {@link #getTextRange()} result.
   */
  default void setTextRangeOverride(TextRange textRange) {
    putCopyableUserData(TEXT_RANGE_OVERRIDE, textRange);
  }

  default PsiElement getParentOverride() {
    return this.getCopyableUserData(PARENT_OVERRIDE);
  }

  default PsiElement getNavigationElementOverride() {
    return this.getCopyableUserData(NAVIGATION_ELEMENT_OVERRIDE);
  }

  default TextRange getTextRangeOverride() {
    return this.getCopyableUserData(TEXT_RANGE_OVERRIDE);
  }

  /**
   * Copy overrides set on this element to the specified overridable element.
   *
   * @param other the other element
   */
  default void copyOverridesTo(@NotNull PbOverridableElement other) {
    other.setParentOverride(getParentOverride());
    other.setNavigationElementOverride(getNavigationElementOverride());
    other.setTextRangeOverride(getTextRangeOverride());
  }
}
