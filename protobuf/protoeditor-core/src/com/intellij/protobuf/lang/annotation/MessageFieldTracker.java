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
 * Visits statements in a message type to track field numbers, reserved, and extension statements.
 * In the process, discovers problems like duplicate field numbers, use of reserved or extension
 * numbers, reserving a value twice, etc.
 *
 * <p>Problems from the message visit are queued until a user calls an annotate function on a child
 * of the message ({@link #annotateField}, etc.).
 *
 * <p>The annotate call must happen on a child of the message because IntelliJ does not run
 * annotation passes for a parent element that has descendants with attached error elements:
 * https://github.com/JetBrains/intellij-community/blob/d62bc71fb8f908682ab51a7f8921ac100e86cf0e/platform/analysis-impl/src/com/intellij/codeInsight/daemon/impl/GeneralHighlightingPass.java#L355
 */
final class MessageFieldTracker {

  private final Multimap<PbElement, ProblemAnnotation> queuedAnnotations =
      ArrayListMultimap.create();

  /** Annotates any problems that are discovered for a field. */
  static void annotateField(PbField field, AnnotationHolder holder) {
    annotateElement(field, holder);
  }

  /** Annotates any problems that are discovered for a reserved statement. */
  static void annotateReservedStatement(
      PbReservedStatement reservedStatement, AnnotationHolder holder) {
    annotateElement(reservedStatement, holder);
  }

  /** Annotates any problems that are discovered for an extensions statement. */
  static void annotateExtensionsStatement(
      PbExtensionsStatement extensionsStatement, AnnotationHolder holder) {
    annotateElement(extensionsStatement, holder);
  }

  private static void annotateElement(PbElement element, AnnotationHolder holder) {
    PbMessageType messageType = PsiTreeUtil.getParentOfType(element, PbMessageType.class);
    if (messageType == null) {
      return;
    }
    MessageFieldTracker tracker = getTracker(messageType);
    Collection<ProblemAnnotation> problems = tracker.queuedAnnotations.get(element);
    for (ProblemAnnotation problem : problems) {
      holder.newAnnotation(
          HighlightSeverity.ERROR, problem.message).range(
          problem.annotationElement).create();
    }
  }

  private static MessageFieldTracker getTracker(PbMessageType messageType) {
    return CachedValuesManager.getCachedValue(
        messageType,
        () -> {
          MessageFieldTracker tracker = new MessageFieldTracker();
          tracker.trackProblems(messageType);
          return CachedValueProvider.Result.create(
              tracker, PsiModificationTracker.MODIFICATION_COUNT);
        });
  }

  private void trackProblems(PbMessageType messageType) {
    new ProblemsVisitor().visit(messageType);
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

  /** Visits a message body to find problems, and queues them in the {@link MessageFieldTracker}. */
  private final class ProblemsVisitor {

    private final RangeSet<Long> reservedFieldNumbers = TreeRangeSet.create();
    private final RangeSet<Long> extensionFieldNumbers = TreeRangeSet.create();
    private final Set<String> reservedNames = new HashSet<>();
    private final Map<Long, String> fieldNumberToField = new HashMap<>();

    void visit(PbMessageType messageType) {
      visitReserved(messageType);
      visitExtensions(messageType);
      visitFields(messageType);
    }

    private void visitReserved(PbMessageType messageType) {
      for (PbReservedStatement reservedStatement :
          messageType.getBody().getReservedStatementList()) {
        for (PbReservedRange reservedRange : reservedStatement.getReservedRangeList()) {
          visitReservedRange(reservedStatement, reservedRange);
        }
        for (PbStringValue reservedName : reservedStatement.getStringValueList()) {
          visitReservedName(reservedStatement, reservedName);
        }
      }
    }

    private void visitExtensions(PbMessageType messageType) {
      for (PbExtensionsStatement extensionsStatement :
          messageType.getBody().getExtensionsStatementList()) {
        for (PbExtensionRange extensionRange : extensionsStatement.getExtensionRangeList()) {
          visitExtensionRange(extensionsStatement, extensionRange);
        }
      }
    }

    private void visitFields(PbMessageType messageType) {
      for (PbSymbol symbol : messageType.getSymbols()) {
        if (!(symbol instanceof PbField)) {
          continue;
        }
        PbField field = (PbField) symbol;
        String fieldName = field.getName();
        if (fieldName != null) {
          visitFieldName(field, fieldName);
        }
        PbNumberValue fieldNumberValue = field.getFieldNumber();
        if (fieldNumberValue != null) {
          visitFieldNumber(field, fieldName, fieldNumberValue);
        }
      }
    }

    private void visitReservedRange(
        PbReservedStatement reservedStatement, PbReservedRange reservedRange) {
      Long from = reservedRange.getFrom();
      if (from == null) {
        return;
      }
      Long to = reservedRange.getTo();
      if (to == null) {
        to = from;
      }
      if (from <= 0 || to <= 0) {
        queueError(
            reservedStatement,
            reservedRange,
            PbLangBundle.message("reserved.number.must.be.positive"));
        return;
      }
      if (to < from) {
        return;
      }
      Range<Long> newRange = Range.closed(from, to);
      Optional<Range<Long>> overlappingRange = findPreviousOverlappingReservedRange(newRange);
      if (overlappingRange.isPresent()) {
        queueError(
            reservedStatement,
            reservedRange,
            PbLangBundle.message(
                "reserved.range.overlaps.existing",
                newRange.lowerEndpoint(),
                newRange.upperEndpoint(),
                overlappingRange.get().lowerEndpoint(),
                overlappingRange.get().upperEndpoint()));
        return;
      }
      reservedFieldNumbers.add(newRange);
    }

    private Optional<Range<Long>> findPreviousOverlappingReservedRange(Range<Long> newRange) {
      return findPreviousOverlappingRange(reservedFieldNumbers, newRange);
    }

    private Optional<Range<Long>> findPreviousOverlappingExtensionRange(Range<Long> newRange) {
      return findPreviousOverlappingRange(extensionFieldNumbers, newRange);
    }

    private Optional<Range<Long>> findPreviousOverlappingRange(
        RangeSet<Long> ranges, Range<Long> newRange) {
      RangeSet<Long> intersection = ranges.subRangeSet(newRange);
      if (intersection.isEmpty()) {
        return Optional.empty();
      }
      return ranges.asRanges().stream().filter(range -> range.isConnected(newRange)).findFirst();
    }

    private void visitReservedName(
        PbReservedStatement reservedStatement, PbStringValue reservedName) {
      String name = reservedName.getAsString();
      if (!reservedNames.add(name)) {
        queueError(
            reservedStatement,
            reservedName,
            PbLangBundle.message("reserved.name.multiple.times", name));
      }
    }

    private void visitExtensionRange(
        PbExtensionsStatement extensionsStatement, PbExtensionRange extensionRange) {
      Long from = extensionRange.getFrom();
      if (from == null) {
        return;
      }
      Long declaredTo = extensionRange.getTo();
      Long to = declaredTo == null ? from : declaredTo;
      if (from <= 0 || to <= 0) {
        queueError(
            extensionsStatement,
            extensionRange,
            PbLangBundle.message("extension.number.must.be.positive"));
        return;
      }
      if (declaredTo != null && to < from) {
        queueError(
            extensionsStatement,
            extensionRange,
            PbLangBundle.message("extension.end.greater.than.start"));
        return;
      }
      long max = extensionRange.getMaxValue();
      if (from > max || to > max) {
        queueError(
            extensionsStatement,
            extensionRange,
            PbLangBundle.message("extension.number.greater.than.max", max));
        return;
      }
      Range<Long> newRange = Range.closed(from, to);
      Optional<Range<Long>> overlappingExtensionRange =
          findPreviousOverlappingExtensionRange(newRange);
      if (overlappingExtensionRange.isPresent()) {
        queueError(
            extensionsStatement,
            extensionRange,
            PbLangBundle.message(
                "extension.range.overlaps.existing",
                newRange.lowerEndpoint(),
                newRange.upperEndpoint(),
                overlappingExtensionRange.get().lowerEndpoint(),
                overlappingExtensionRange.get().upperEndpoint()));
        return;
      }
      Optional<Range<Long>> overlappingReservedRange =
          findPreviousOverlappingReservedRange(newRange);
      if (overlappingReservedRange.isPresent()) {
        queueError(
            extensionsStatement,
            extensionRange,
            PbLangBundle.message(
                "extension.range.overlaps.reserved.range",
                newRange.lowerEndpoint(),
                newRange.upperEndpoint(),
                overlappingReservedRange.get().lowerEndpoint(),
                overlappingReservedRange.get().upperEndpoint()));
        return;
      }
      extensionFieldNumbers.add(newRange);
    }

    private void visitFieldNumber(PbField field, String fieldName, PbNumberValue fieldNumberValue) {
      if (!fieldNumberValue.isValidInt32()) {
        queueError(field, fieldNumberValue, PbLangBundle.message("integer.value.out.of.range"));
        return;
      }
      long fieldNumber = Preconditions.checkNotNull(fieldNumberValue.getLongValue());
      long maxFieldNumber = getMaxFieldNumber(field);
      if (fieldNumber <= 0) {
        queueError(field, fieldNumberValue, PbLangBundle.message("field.number.must.be.positive"));
      } else if (PbField.NUMBERS_RESERVED_BY_PROTO.contains(fieldNumber)) {
        queueError(
            field,
            fieldNumberValue,
            PbLangBundle.message(
                "field.number.in.proto.reserved.range",
                PbField.NUMBERS_RESERVED_BY_PROTO.lowerEndpoint(),
                PbField.NUMBERS_RESERVED_BY_PROTO.upperEndpoint()));
      } else if (fieldNumber > maxFieldNumber) {
        queueError(
            field,
            fieldNumberValue,
            PbLangBundle.message("field.number.greater.than.max", PbField.MAX_FIELD_NUMBER));
      } else if (!field.isExtension()) {
        // Only perform field number checks for non-extension fields.
        // TODO(volkman): perform field number checks against the extended message.
        if (fieldNumberToField.containsKey(fieldNumber)) {
          String earlierField = fieldNumberToField.get(fieldNumber);
          queueError(
              field,
              fieldNumberValue,
              PbLangBundle.message("field.number.already.used", fieldNumber, earlierField));
        } else if (reservedFieldNumbers.contains(fieldNumber)) {
          queueError(
              field,
              fieldNumberValue,
              PbLangBundle.message("field.uses.reserved.number", fieldName, fieldNumber));
        } else if (extensionFieldNumbers.contains(fieldNumber)) {
          queueError(
              field,
              fieldNumberValue,
              PbLangBundle.message("field.uses.extension.number", fieldName, fieldNumber));
        }
      }
      if (!field.isExtension()) {
        fieldNumberToField.putIfAbsent(fieldNumber, fieldName);
      }
    }

    private void visitFieldName(PbField field, String fieldName) {
      // We don't apply reserved name checks to extension fields.
      if (field.getExtendee() != null) {
        return;
      }
      if (reservedNames.contains(fieldName)) {
        PsiElement annotationElement = PbAnnotator.getSymbolNameAnnotationElement(field);
        queueError(
            field, annotationElement, PbLangBundle.message("field.uses.reserved.name", fieldName));
      }
    }

    private long getMaxFieldNumber(PbField field) {
      PbMessageType parent;
      PbTypeName extendee = field.getExtendee();
      if (extendee != null) {
        parent = PbPsiUtil.resolveRefToType(extendee.getEffectiveReference(), PbMessageType.class);
      } else {
        parent = PsiTreeUtil.getParentOfType(field, PbMessageType.class);
      }
      if (parent != null && parent.isMessageSet()) {
        return PbField.MAX_MESSAGE_SET_FIELD_NUMBER;
      }
      return PbField.MAX_FIELD_NUMBER;
    }
  }
}
