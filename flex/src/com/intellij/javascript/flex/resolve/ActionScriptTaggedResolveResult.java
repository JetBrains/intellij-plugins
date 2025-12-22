// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.resolve;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;


public final class ActionScriptTaggedResolveResult implements Comparable<ActionScriptTaggedResolveResult> {

  public final @NotNull ResolveResult result;
  public final @NotNull EnumSet<ResolveResultTag> tags;
  public final int completeMatchLevel;

  ActionScriptTaggedResolveResult(@NotNull ResolveResult result, @NotNull EnumSet<ResolveResultTag> tags, int completeMatchLevel) {
    this.result = result;
    this.tags = tags;
    this.completeMatchLevel = completeMatchLevel;
  }

  @Override
  public int compareTo(@NotNull ActionScriptTaggedResolveResult o) {
    final int priorityTo = comparePriorityTo(o);
    if (priorityTo != 0) return priorityTo;
    if (tags.contains(ResolveResultTag.CURRENT_FILE) && !o.tags.contains(ResolveResultTag.CURRENT_FILE)) return -1;
    if (!tags.contains(ResolveResultTag.CURRENT_FILE) && o.tags.contains(ResolveResultTag.CURRENT_FILE)) return 1;
    return 0;
  }

  int comparePriorityTo(@NotNull ActionScriptTaggedResolveResult o) {
    return comparePriorities(
      tags, result.isValidResult(), completeMatchLevel,
      o.tags, o.result.isValidResult(), o.completeMatchLevel
    );
  }

  private static final Integer POSITIVE = 1;
  private static final Integer NEGATIVE = -1;
  private static final Pair<ResolveResultTag, Integer> MATCH_LEVEL_PLACEHOLDER = new Pair<>(null, null);
  private static final Pair<ResolveResultTag, Integer> IS_VALID_RESOLVE_PLACEHOLDER = new Pair<>(null, null);
  private static final Pair<ResolveResultTag, Integer> SAME_CLASS_CONSTRUCTOR_DEFINITION_PLACEHOLDER = new Pair<>(null, null);
  private static final List<Pair<ResolveResultTag, Integer>> TAGS_SIGNIFICANCE_ORDER =
    List.of(Pair.create(ResolveResultTag.PARTIAL, NEGATIVE),
            IS_VALID_RESOLVE_PLACEHOLDER,
            Pair.create(ResolveResultTag.DEFINITION_IN_CLASS_NOT_CONSTRUCTOR, NEGATIVE),
            MATCH_LEVEL_PLACEHOLDER,
            Pair.create(ResolveResultTag.IS_ASSIGNMENT, NEGATIVE),
            SAME_CLASS_CONSTRUCTOR_DEFINITION_PLACEHOLDER,
            Pair.create(ResolveResultTag.SELF_DEFINITION, POSITIVE),
            Pair.create(ResolveResultTag.CONTEXT_MATCHES, POSITIVE));

  private static int comparePriorities(
    @NotNull EnumSet<ResolveResultTag> tags1, boolean isValid1, int matchLevel1,
    @NotNull EnumSet<ResolveResultTag> tags2, boolean isValid2, int matchLevel2) {
    for (Pair<ResolveResultTag, Integer> pair : TAGS_SIGNIFICANCE_ORDER) {
      if (pair == IS_VALID_RESOLVE_PLACEHOLDER) {
        if (isValid1 && !isValid2) return -1;
        if (!isValid1 && isValid2) return 1;
      }
      else if (pair == MATCH_LEVEL_PLACEHOLDER) {
        if (matchLevel1 < matchLevel2) return -1;
        if (matchLevel1 > matchLevel2) return 1;
      }
      else {
        if (pair != SAME_CLASS_CONSTRUCTOR_DEFINITION_PLACEHOLDER) {
          int compare = compareByTag(pair, tags1, tags2);
          if (compare != 0) return compare;
        }
      }
    }

    return 0;
  }

  private static int compareByTag(@NotNull Pair<ResolveResultTag, Integer> pair,
                                  @NotNull EnumSet<ResolveResultTag> tags1,
                                  @NotNull EnumSet<ResolveResultTag> tags2) {
    if (!tags1.contains(pair.first) && tags2.contains(pair.first)) return pair.second;
    if (tags1.contains(pair.first) && !tags2.contains(pair.first)) return -pair.second;
    return 0;
  }

  public boolean hasTag(ResolveResultTag tag) {
    return tags.contains(tag);
  }

  public enum ResolveResultTag {
    PARTIAL, IS_ASSIGNMENT,
    CURRENT_FILE, CONTEXT_MATCHES, SELF_DEFINITION, DEFINITION_IN_CLASS_NOT_CONSTRUCTOR,
  }
}
