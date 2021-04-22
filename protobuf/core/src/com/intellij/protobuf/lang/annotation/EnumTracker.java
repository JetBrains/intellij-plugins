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

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.protobuf.lang.PbLangBundle;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;

import java.util.*;

/**
 * Visits statements in an enum definition to discover duplicate names, numbers, overlapping
 * reserved ranges, and usages of reserved names and numbers.
 *
 * @see MessageFieldTracker
 */
final class EnumTracker {
  private static final String ALLOW_ALIAS = "allow_alias";

  private final Multimap<PbElement, ProblemAnnotation> queuedAnnotations =
      ArrayListMultimap.create();

  /** Annotates any problems that are discovered for an enum value. */
  static void annotateEnumValue(PbEnumValue value, AnnotationHolder holder) {
    annotateElement(value, holder);
  }

  /** Annotates any problems that are discovered for a reserved statement. */
  static void annotateReservedStatement(
      PbEnumReservedStatement reservedStatement, AnnotationHolder holder) {
    annotateElement(reservedStatement, holder);
  }

  /** Annotates any problems that are discovered for an option statement. */
  static void annotateOptionExpression(
      PbOptionExpression optionExpression, AnnotationHolder holder) {
    annotateElement(optionExpression, holder);
  }

  private static void annotateElement(PbElement element, AnnotationHolder holder) {
    PbEnumDefinition enumDefinition = PsiTreeUtil.getParentOfType(element, PbEnumDefinition.class);
    if (enumDefinition == null) {
      return;
    }
    EnumTracker tracker = getTracker(enumDefinition);
    Collection<ProblemAnnotation> problems = tracker.queuedAnnotations.get(element);
    for (ProblemAnnotation problem : problems) {
      holder.newAnnotation(HighlightSeverity.ERROR, problem.message).range(problem.annotationElement).create();
    }
  }

  private static EnumTracker getTracker(PbEnumDefinition enumDefinition) {
    return CachedValuesManager.getCachedValue(
        enumDefinition,
        () -> {
          EnumTracker tracker = new EnumTracker();
          tracker.trackProblems(enumDefinition);
          return CachedValueProvider.Result.create(
              tracker, PsiModificationTracker.MODIFICATION_COUNT);
        });
  }

  private void trackProblems(PbEnumDefinition enumDefinition) {
    new ProblemsVisitor().visit(enumDefinition);
  }

  private void queueError(PbElement element, PsiElement annotationElement, String message) {
    queuedAnnotations.put(element, new ProblemAnnotation(annotationElement, message));
  }

  private static class ProblemAnnotation {
    final PsiElement annotationElement;
    final String message;

    ProblemAnnotation(PsiElement annotationElement, String message) {
      this.annotationElement = annotationElement;
      this.message = message;
    }
  }

  /** Visits an enum body to find problems, and queues them in the {@link EnumTracker}. */
  private final class ProblemsVisitor {

    private final RangeSet<Integer> reservedNumbers = TreeRangeSet.create();
    private final Set<String> reservedNames = new HashSet<>();
    private final Map<Integer, String> numberToName = new HashMap<>();
    private final Map<String, PbEnumValue> canonicalNameToValue = new HashMap<>();
    private boolean hasAliases = false;

    void visit(PbEnumDefinition enumDefinition) {
      visitReserved(enumDefinition);
      visitValues(enumDefinition);
      visitOptions(enumDefinition);
    }

    private void visitReserved(PbEnumDefinition enumDefinition) {
      PbEnumBody body = enumDefinition.getBody();
      if (body == null) {
        return;
      }
      for (PbEnumReservedStatement reservedStatement : body.getEnumReservedStatementList()) {
        for (PbEnumReservedRange reservedRange : reservedStatement.getEnumReservedRangeList()) {
          visitReservedRange(reservedStatement, reservedRange);
        }
        for (PbStringValue reservedName : reservedStatement.getStringValueList()) {
          visitReservedName(reservedStatement, reservedName);
        }
      }
    }

    private void visitValues(PbEnumDefinition enumDefinition) {
      for (PbEnumValue value : enumDefinition.getEnumValues()) {
        String name = value.getName();
        if (name != null) {
          visitEnumValueName(enumDefinition, value, name);
        }
        PbNumberValue numberValue = value.getNumberValue();
        if (numberValue != null) {
          visitEnumValueNumber(enumDefinition, value, name, numberValue);
        }
      }
    }

    private void visitOptions(PbEnumDefinition enumDefinition) {
      PbEnumBody body = enumDefinition.getBody();
      if (body == null) {
        return;
      }
      for (PbOptionExpression option : body.getOptions()) {
        if (!ALLOW_ALIAS.equals(option.getOptionName().getText())) {
          continue;
        }
        ProtoBooleanValue booleanValue = option.getBooleanValue();
        Boolean aliasesAllowed = booleanValue != null ? booleanValue.getBooleanValue() : null;
        if (aliasesAllowed == null) {
          continue;
        }
        if (aliasesAllowed) {
          if (!hasAliases) {
            queueError(option, option, PbLangBundle.message("enum.allow.alias.unnecessary"));
          }
        } else {
          queueError(option, option, PbLangBundle.message("enum.allow.alias.no.effect"));
        }
      }
    }

    private void visitReservedRange(
        PbEnumReservedStatement reservedStatement, PbEnumReservedRange reservedRange) {
      Long from = reservedRange.getFrom();
      if (from == null) {
        return;
      }
      Integer fromInt = from.intValue();
      Long to = reservedRange.getTo();
      Integer toInt = to != null ? to.intValue() : fromInt;
      if (to != null && toInt <= fromInt) {
        queueError(
            reservedStatement,
            reservedRange,
            PbLangBundle.message("enum.reserved.end.greater.than.start"));
      }
      Range<Integer> newRange = Range.closed(fromInt, toInt);
      Optional<Range<Integer>> overlappingRange = findPreviousOverlappingReservedRange(newRange);
      if (overlappingRange.isPresent()) {
        queueError(
            reservedStatement,
            reservedRange,
            PbLangBundle.message(
                "enum.reserved.range.overlaps.existing",
                newRange.lowerEndpoint(),
                newRange.upperEndpoint(),
                overlappingRange.get().lowerEndpoint(),
                overlappingRange.get().upperEndpoint()));
        return;
      }
      reservedNumbers.add(newRange);
    }

    private Optional<Range<Integer>> findPreviousOverlappingReservedRange(Range<Integer> newRange) {
      RangeSet<Integer> intersection = reservedNumbers.subRangeSet(newRange);
      if (intersection.isEmpty()) {
        return Optional.empty();
      }
      return reservedNumbers.asRanges().stream()
          .filter(range -> range.isConnected(newRange))
          .findFirst();
    }

    private void visitReservedName(
        PbEnumReservedStatement reservedStatement, PbStringValue reservedName) {
      String name = reservedName.getAsString();
      if (!reservedNames.add(name)) {
        queueError(
            reservedStatement,
            reservedName,
            PbLangBundle.message("enum.reserved.name.multiple.times", name));
      }
    }

    private void visitEnumValueNumber(
        PbEnumDefinition enumDefinition,
        PbEnumValue value,
        String name,
        PbNumberValue valueNumber) {
      if (!valueNumber.isValidInt32()) {
        queueError(value, valueNumber, PbLangBundle.message("integer.value.out.of.range"));
        return;
      }
      Long numberAsLong = valueNumber.getLongValue();
      Preconditions.checkNotNull(numberAsLong);
      int number = numberAsLong.intValue();

      if (numberToName.containsKey(number) && !allowsAliases(enumDefinition)) {
        String earlierValue = numberToName.get(number);
        queueError(
            value,
            valueNumber,
            PbLangBundle.message("enum.number.already.used", number, earlierValue));
      } else if (reservedNumbers.contains(number)) {
        queueError(
            value, valueNumber, PbLangBundle.message("enum.uses.reserved.number", name, number));
      }
      if (numberToName.containsKey(number)) {
        hasAliases = true;
      } else {
        numberToName.put(number, name);
      }
    }

    private void visitEnumValueName(
        PbEnumDefinition enumDefinition, PbEnumValue value, String name) {
      String canonicalName = getCanonicalName(enumDefinition.getName(), name);
      if (reservedNames.contains(name)) {
        queueError(
            value,
            value.getNameIdentifier(),
            PbLangBundle.message("enum.uses.reserved.name", name));
      } else if (canonicalNameToValue.containsKey(canonicalName)) {
        PbEnumValue prevValue = canonicalNameToValue.get(canonicalName);
        // This check doesn't apply if the two values are aliases of one another.
        if (!valuesAreAliases(value, prevValue)) {
          queueError(
              value,
              value.getNameIdentifier(),
              PbLangBundle.message(
                  "enum.canonical.name.conflict", name, prevValue.getName(), canonicalName));
        }
      }
      canonicalNameToValue.putIfAbsent(canonicalName, value);
    }
  }

  private static boolean valuesAreAliases(PbEnumValue first, PbEnumValue second) {
    PbNumberValue firstNumber = first.getNumberValue();
    PbNumberValue secondNumber = second.getNumberValue();
    Long firstLong = firstNumber != null ? firstNumber.getLongValue() : null;
    Long secondLong = secondNumber != null ? secondNumber.getLongValue() : null;
    if (firstLong == null || secondLong == null) {
      return false;
    }
    return firstLong.equals(secondLong);
  }

  private static boolean allowsAliases(PbEnumDefinition enumDefinition) {
    PbEnumBody body = enumDefinition.getBody();
    if (body == null) {
      return false;
    }
    return Boolean.TRUE.equals(PbPsiUtil.getBooleanDescriptorOption(body, ALLOW_ALIAS));
  }

  // Adapted From descriptor.cc
  private static String getCanonicalName(String enumName, String valueName) {
    // Check that enum labels are still unique when we remove the enum prefix from
    // values that have it.
    //
    // This will fail for something like:
    //
    //   enum MyEnum {
    //     MY_ENUM_FOO = 0;
    //     FOO = 1;
    //   }
    //
    // By enforcing this reasonable constraint, we allow code generators to strip
    // the prefix and/or PascalCase it without creating conflicts.  This can lead
    // to much nicer language-specific enums like:
    //
    //   enum NameType {
    //     FirstName = 1,
    //     LastName = 2,
    //   }
    //
    // Instead of:
    //
    //   enum NameType {
    //     NAME_TYPE_FIRST_NAME = 1,
    //     NAME_TYPE_LAST_NAME = 2,
    //   }
    if (enumName == null) {
      return valueName;
    }
    String withPrefixStripped = stripPrefix(enumName, valueName);
    return toPascalCase(withPrefixStripped);
  }

  // Adapted from descriptor.cc.
  //
  // Tries to remove the enum prefix from this enum value.
  // If this is not possible, returns the input verbatim.
  private static String stripPrefix(String enumName, String valueName) {
    // We can't just lowercase and strip str and look for a prefix.
    // We need to properly recognize the difference between:
    //
    //   enum Foo {
    //     FOO_BAR_BAZ = 0;
    //     FOO_BARBAZ = 1;
    //   }
    //
    // This is acceptable (though perhaps not advisable) because even when
    // we PascalCase, these two will still be distinct (BarBaz vs. Barbaz).

    // Skip past prefix in valueName if we can.
    int i = 0;
    int j = 0;
    while (i < valueName.length() && j < enumName.length()) {
      if (valueName.charAt(i) == '_') {
        i++;
        continue;
      }
      if (enumName.charAt(j) == '_') {
        j++;
        continue;
      }
      if (Character.toLowerCase(valueName.charAt(i++))
          != Character.toLowerCase(enumName.charAt(j++))) {
        return valueName;
      }
    }

    // If we didn't make it through the prefix, we've failed to strip the
    // prefix.
    if (j < enumName.length()) {
      return valueName;
    }

    // Skip underscores between prefix and further characters.
    while (i < valueName.length() && valueName.charAt(i) == '_') {
      i++;
    }

    // Enum label can't be the empty string.
    if (i == valueName.length()) {
      return valueName;
    }

    // We successfully stripped the prefix.
    return valueName.substring(i);
  }

  // Adapted from descriptor.cc.
  private static String toPascalCase(String input) {
    boolean nextUpper = true;
    StringBuilder result = new StringBuilder(input.length());

    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (c == '_') {
        nextUpper = true;
      } else {
        if (nextUpper) {
          result.append(Character.toUpperCase(c));
        } else {
          result.append(Character.toLowerCase(c));
        }
        nextUpper = false;
      }
    }
    return result.toString();
  }
}
