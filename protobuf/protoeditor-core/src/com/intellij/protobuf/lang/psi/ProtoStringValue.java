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

import com.intellij.openapi.util.TextRange;
import com.intellij.protobuf.ide.PbCompositeModificationTracker;
import com.intellij.protobuf.lang.psi.util.PbPsiImplUtil;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** A shared interface implemented by elements that represent a proto-formatted string. */
public interface ProtoStringValue extends ProtoLiteral {

  /** Returns the list of constituent {@link ProtoStringPart} elements. */
  @NotNull
  List<? extends ProtoStringPart> getStringParts();

  @NotNull
  @Override
  default String getValue() {
    return CachedValuesManager.getCachedValue(
        this,
        () -> {
          StringBuilder builder = new StringBuilder();
          for (ProtoStringPart part : getStringParts()) {
            builder.append(part.getParsedString());
          }
          return Result.create(builder.toString(), PbCompositeModificationTracker.byElement(this));
        });
  }

  @NotNull
  @Override
  default String getAsString() {
    return getValue();
  }

  /**
   * Returns a TextRange containing the string value without starting and ending quotes. If this
   * ProtoStringValue is a combination of multiple StringParts, intermediate quotes will still be
   * included. If the quotes are incomplete (missing end quote), the start and end offset will be
   * the same.
   */
  @NotNull
  default TextRange getTextRangeNoQuotes() {
    return PbPsiImplUtil.getTextRangeNoQuotes(this);
  }
}
