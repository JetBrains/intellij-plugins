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
package com.intellij.protobuf.lang.psi;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.util.TextRange;
import com.intellij.protobuf.ide.PbCompositeModificationTracker;
import com.intellij.protobuf.lang.psi.util.PbPsiImplUtil;
import com.intellij.protobuf.lang.util.ProtoString;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;

/** A shared interface implemented by elements that represent a proto-formatted string part. */
public interface ProtoStringPart extends PsiElement {

  default @NotNull ProtoString getParsedString() {
    return CachedValuesManager.getCachedValue(
        this,
        () ->
            Result.create(ProtoString.parse(getText()), PbCompositeModificationTracker.byElement(this)));
  }

  default boolean isUnterminated() {
    return getParsedString().isUnterminated();
  }

  default ImmutableList<TextRange> getInvalidEscapeRanges() {
    return getParsedString().getInvalidEscapeRanges();
  }

  default @NotNull TextRange getTextRangeNoQuotes() {
    return PbPsiImplUtil.getTextRangeNoQuotes(this);
  }
}
