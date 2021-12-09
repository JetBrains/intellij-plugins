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
package com.intellij.protobuf.lang.annotation;

import com.google.common.collect.ImmutableMap;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.protobuf.ide.PbCompositeModificationTracker;
import com.intellij.protobuf.lang.PbLangBundle;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;

import java.util.EnumMap;
import java.util.Map;

/** A helper class to track and annotate occurrences of special options ('default', 'json_name'). */
public class SpecialOptionTracker {
  private static final SpecialOptionTracker EMPTY = new SpecialOptionTracker();

  private final Map<SpecialOptionType, PbOptionName> firstOccurrences;

  private SpecialOptionTracker() {
    firstOccurrences = ImmutableMap.of();
  }

  private SpecialOptionTracker(PbOptionOwner owner) {
    firstOccurrences = new EnumMap<>(SpecialOptionType.class);
    for (PbOptionExpression option : owner.getOptions()) {
      SpecialOptionType specialType = option.getOptionName().getSpecialType();
      if (specialType != null && !firstOccurrences.containsKey(specialType)) {
        firstOccurrences.put(specialType, option.getOptionName());
      }
    }
  }

  /** Returns a tracker for the given {@link PbOptionOwner owner}. */
  static SpecialOptionTracker forOptionOwner(PbOptionOwner owner) {
    if (!(owner instanceof PbField)) {
      // Special options only apply to fields.
      return EMPTY;
    }
    return CachedValuesManager.getCachedValue(
        owner,
        () ->
            Result.create(
              new SpecialOptionTracker(owner), PbCompositeModificationTracker.byElement(owner)));
  }

  /** Possibly annotates the given {@link PbOptionName} if it's a duplicate. */
  void annotateOptionName(PbOptionName name, AnnotationHolder holder) {
    SpecialOptionType specialType = name.getSpecialType();
    if (specialType == null) {
      return;
    }
    PbOptionName firstOccurrence = firstOccurrences.get(specialType);
    if (firstOccurrence == null || name.equals(firstOccurrence)) {
      return;
    }

    // The occurrence is non-null and is not our option, so ours is a duplicate.
    holder.newAnnotation(
        HighlightSeverity.ERROR, PbLangBundle.message("special.option.multiple.times", name.getText()))
        .range(name)
        .create();
  }
}
