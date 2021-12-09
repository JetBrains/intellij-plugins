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
package com.intellij.protobuf.lang.completion;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ThreeState;
import com.intellij.protobuf.lang.psi.PbNumberValue;
import org.jetbrains.annotations.NotNull;

/** Allow skipping auto popup. */
public class PbCompletionConfidence extends CompletionConfidence {

  @Override
  @NotNull
  public ThreeState shouldSkipAutopopup(
      @NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {
    // IntelliJ can trigger an auto-popup when you type a numeric digit
    // https://github.com/JetBrains/intellij-community/commit/fe0ead80c0bd419fa4d7b6f6014f86fd10a154b4
    //
    // Skip if the digit is just part of a number (not part of something like "int6" -> "int64")
    if (contextElement.getParent() instanceof PbNumberValue) {
      return ThreeState.YES;
    }
    return ThreeState.UNSURE;
  }
}
