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

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.AnnotationSession;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.protobuf.ide.highlighter.PbSyntaxHighlighter;
import com.intellij.protobuf.ide.highlighter.PbTextSyntaxHighlighter;
import com.intellij.protobuf.lang.PbLangBundle;
import com.intellij.protobuf.lang.annotation.OptionOccurrenceTracker.Occurrence;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.util.BuiltInType;
import com.intellij.protobuf.lang.util.ValueTester;
import com.intellij.protobuf.lang.util.ValueTester.ValueTesterType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/** An error {@link Annotator} for text format elements. */
public class PbTextAnnotator implements Annotator {

  private static final Key<Map<PsiElement, Boolean>> RESERVED_FIELDS_KEY =
      Key.create("RESERVED_FIELDS_KEY");

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull final AnnotationHolder holder) {
    // We always annotate strings.
    if (element instanceof PbTextStringPart part) {
      SharedAnnotations.annotateStringPart(part, holder, getInvalidEscapeAttributes(part));
      return;
    }

    // Additional annotations are only performed if this text message is bound to a schema.
    if (!fullAnnotation(element)) {
      return;
    }

    // Don't perform any annotations if the element exists within a reserved field.
    if (isReservedFieldOrDescendant(element, holder)) {
      return;
    }

    element.accept(
        new PbTextVisitor() {
          @Override
          public void visitField(@NotNull PbTextField field) {
            annotateField(field, holder);
          }

          @Override
          public void visitFieldName(@NotNull PbTextFieldName name) {
            annotateFieldName(name, holder);
          }

          @Override
          public void visitSymbolPath(@NotNull PbTextSymbolPath path) {
            SharedAnnotations.annotateSymbolPath(path, holder);
          }

          @Override
          public void visitExtensionName(@NotNull PbTextExtensionName name) {
            annotateExtensionName(name, holder);
          }

          @Override
          public void visitDomain(@NotNull PbTextDomain domain) {
            annotateDomain(domain, holder);
          }

          @Override
          public void visitStringPart(@NotNull PbTextStringPart part) {
            SharedAnnotations.annotateStringPart(part, holder, getInvalidEscapeAttributes(part));
          }
        });
  }

  /*
   * Returns true if the given element is a PbTextField with a reserved name, descends from one.
   */
  private boolean isReservedFieldOrDescendant(PsiElement element, AnnotationHolder holder) {
    if (element == null) {
      return false;
    }
    PbTextField containingField =
        PsiTreeUtil.getParentOfType(element, PbTextField.class, /* strict= */ false);
    if (containingField == null) {
      return false;
    }
    PbTextFieldName containingFieldName = containingField.getFieldName();

    // Reserved names don't apply to extension fields.
    if (containingFieldName.getExtensionName() != null) {
      return false;
    }

    // First check to see whether we've already determined reserved status for this field.
    AnnotationSession session = holder.getCurrentAnnotationSession();
    Map<PsiElement, Boolean> reservedFields = session.getUserData(RESERVED_FIELDS_KEY);
    Boolean cached = reservedFields != null ? reservedFields.get(containingField) : null;
    if (cached != null) {
      return cached;
    }

    PbTextMessage containingMessage =
        PsiTreeUtil.getParentOfType(containingField, PbTextMessage.class);
    if (containingMessage == null) {
      return isReservedFieldOrDescendant(containingField.getParent(), holder);
    }
    PbMessageType declaredMessage = containingMessage.getDeclaredMessage();
    if (declaredMessage == null) {
      return isReservedFieldOrDescendant(containingField.getParent(), holder);
    }

    // Now we can check whether this field's name is reserved in the containing message.
    boolean reserved = declaredMessage.isReservedFieldName(containingFieldName.getText());

    // If this field wasn't reserved, we delegate to a recursive check of its parent.
    if (!reserved) {
      reserved = isReservedFieldOrDescendant(containingField.getParent(), holder);
    }

    // Cache the determined value and return.
    if (reservedFields == null) {
      reservedFields = new HashMap<>();
      session.putUserData(RESERVED_FIELDS_KEY, reservedFields);
    }
    reservedFields.put(containingField, reserved);

    return reserved;
  }

  private boolean fullAnnotation(PsiElement element) {
    PsiFile file = element.getContainingFile();
    if (file instanceof PbFile) {
      return true;
    }
    if (file instanceof PbTextFile) {
      return ((PbTextFile) file).isBound();
    }
    return false;
  }

  private TextAttributesKey getInvalidEscapeAttributes(PsiElement element) {
    if (element.getContainingFile() instanceof PbFile) {
      return PbSyntaxHighlighter.INVALID_STRING_ESCAPE;
    } else {
      return PbTextSyntaxHighlighter.INVALID_STRING_ESCAPE;
    }
  }

  private static void annotateField(@NotNull PbTextField field, @NotNull AnnotationHolder holder) {
    PbTextFieldName name = field.getFieldName();

    BuiltInType type = name.getDeclaredBuiltInType();
    if (type != null) {
      annotateBuiltInValue(field, type, holder);
    } else {
      PbNamedTypeElement namedType = name.getDeclaredNamedType();
      if (namedType instanceof PbMessageType) {
        annotateMessageValue(field, holder);
      } else if (namedType instanceof PbEnumDefinition) {
        annotateEnumValue(field, holder);
      }
    }

    PbTextValueList valueList = field.getValueList();
    if (valueList != null) {

      // Ensure that the field name is followed by a ':' if this is not a message type field.
      // A quirk of text format is that message fields don't require a colon. So, for example:
      //   intfield: 1
      //   msgfield { foo: 2 }
      //
      // But this also translates into value lists:
      //   intlist: [1, 2]
      //   msglist [ {foo: 1}, {foo: 2} ]
      //
      // The grammar permits value list fields without a colon since it does not have the field type
      // context.
      if (!(name.getDeclaredNamedType() instanceof PbMessageType)) {
        PsiElement next = name.getNextSibling();
        if (next == null || !":".equals(next.getText())) {
          TextRange range = TextRange.from(name.getTextOffset() + name.getTextLength(), 1);
          holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("expected.colon.after.non.message.field"))
              .range(range)
              .create();
        }
      }

      // Other value list annotations
      annotateValueList(field, valueList, holder);
    }
  }

  // Annotate built-in type values
  private static void annotateBuiltInValue(
      PbTextField field, BuiltInType type, AnnotationHolder holder) {
    ValueTester tester = type.getValueTester(ValueTesterType.TEXT);
    for (PbTextElement value : field.getValues()) {
      if (value instanceof PbTextLiteral) {
        String error = tester.testValue((PbTextLiteral) value);
        if (error != null) {
          holder.newAnnotation(HighlightSeverity.ERROR, error)
              .range(value.getTextRange())
              .create();
        }
      } else {
        // Probably a message, which is not a literal.
        // We can pass null to the tester to get an appropriate error message.
        String error = tester.testValue(null);
        if (error != null) {
          holder.newAnnotation(HighlightSeverity.ERROR, error)
              .range(value)
              .create();
        }
      }
    }
  }

  // Annotate message values
  private static void annotateMessageValue(PbTextField field, AnnotationHolder holder) {
    for (PbTextElement value : field.getValues()) {
      if (!(value instanceof PbTextMessage)) {
        // Message options must be set to message values.
        holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("message.value.expected"))
            .range(value.getTextRange())
            .create();
      }
    }
  }

  // Annotate a possible enum value.
  private static void annotateEnumValue(PbTextField field, AnnotationHolder holder) {
    for (PbTextElement value : field.getValues()) {
      if (value instanceof PbTextIdentifierValue) {
        SharedAnnotations.annotateEnumOptionValue((PbTextIdentifierValue) value, holder);
      } else if (value instanceof PbTextNumberValue && ((PbTextNumberValue) value).isValidInt32()) {
        SharedAnnotations.annotateEnumOptionValue((PbTextNumberValue) value, holder);
      } else {
        holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("enum.value.expected"))
            .range(value.getTextRange())
            .create();
      }
    }
  }

  // Annotate a value list
  private static void annotateValueList(
      PbTextField field, PbTextValueList valueList, AnnotationHolder holder) {
    PbField declaredField = field.getFieldName().getDeclaredField();
    if (declaredField != null && isIncorrectValueListUsage(field, declaredField)) {
      // Non-repeated fields cannot be initialized with value lists.
      holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("non.repeated.value.list", declaredField.getName()))
          .range(valueList)
          .create();
    }
  }

  private static boolean isIncorrectValueListUsage(PbTextField field, PbField declaredField) {
    return field != null
        && declaredField != null
        && !declaredField.isRepeated()
        && field.getValueList() != null;
  }

  private static void annotateFieldName(
      @NotNull PbTextFieldName name, @NotNull AnnotationHolder holder) {
    // Check whether this is an extension field. If so, annotateExtensionName will handle
    // the annotations.
    if (name.getExtensionName() == null) {
      // This is a normal field name.
      PsiElement identifier = name.getNameIdentifier();
      switch (SharedAnnotations.getReferenceState(name.getEffectiveReference())) {
        case VALID:
        case NULL:
          break;
        case UNRESOLVED:
        default:
          TextRange range = identifier != null ? identifier.getTextRange() : name.getTextRange();
          String symbolName = identifier != null ? identifier.getText() : name.getText();
          holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("cannot.resolve.field", symbolName))
              .range(range)
              .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
              .create();
      }
    }

    // Generate occurrence annotations if we're not already generating an invalid value list usage
    // error.
    PbField declaredField = name.getDeclaredField();
    PbTextField field = PsiTreeUtil.getParentOfType(name, PbTextField.class);
    if (!isIncorrectValueListUsage(field, declaredField)) {
      annotateOptionOccurrences(name, holder);
    }
  }

  private static void annotateExtensionName(
      @NotNull PbTextExtensionName name, @NotNull AnnotationHolder holder) {
    if (name.isAnyTypeUrl()) {
      annotateAnyValue(name, holder);
    } else {
      ProtoSymbolPath symbolPath = name.getSymbolPath();
      if (symbolPath == null) {
        return;
      }
      PbTextMessage message = PsiTreeUtil.getParentOfType(name, PbTextMessage.class);
      PbMessageType qualifierType = message != null ? message.getDeclaredMessage() : null;
      String referenceString = symbolPath.getQualifiedName().toString();

      SharedAnnotations.annotateExtensionName(
          symbolPath,
          name.getEffectiveReference(), // We use the effective reference to handle message sets.
          referenceString,
          qualifierType,
          holder,
          /* allowMembers= */ false);
    }
  }

  private static void annotateAnyValue(
      @NotNull PbTextExtensionName name, @NotNull AnnotationHolder holder) {

    // First ensure that the declared type is a message.
    ProtoSymbolPath symbolPath = name.getSymbolPath();
    if (symbolPath == null) {
      return;
    }
    PsiReference ref = symbolPath.getReference();
    if (ref != null) {
      PsiElement resolved = ref.resolve();
      if (resolved != null && !(resolved instanceof PbMessageType)) {
        holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("message.type.expected"))
            .range(symbolPath.getSymbol())
            .create();
      }
    }

    // Also ensure that the parent field is an instance of google.protobuf.Any
    PbTextField thisField = PsiTreeUtil.getParentOfType(name, PbTextField.class);
    if (thisField == null) {
      return;
    }
    PbTextMessage parentMessage = PsiTreeUtil.getParentOfType(thisField, PbTextMessage.class);
    if (parentMessage == null || !isAnyType(parentMessage.getDeclaredMessage())) {
      holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("any.value.parent.field"))
          .range(name)
          .create();
    }
  }

  private static boolean isAnyType(PbNamedTypeElement type) {
    return AnyType.forElement(type) != null;
  }

  private static void annotateDomain(
      @NotNull PbTextDomain domain, @NotNull AnnotationHolder holder) {
    String domainText = domain.getDomainName();
    if (!"type.googleapis.com".equals(domainText) && !"type.googleprod.com".equals(domainText)) {
      holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("invalid.any.type.domain"))
          .range(domain)
          .create();
    }
  }

  /** Returns the element to annotate for a field name. */
  private static PsiElement getFieldNameAnnotationElement(PbTextFieldName name) {
    PbTextExtensionName extension = name.getExtensionName();
    if (extension != null) {
      if (extension.isAnyTypeUrl()) {
        return extension;
      }
      PbTextSymbolPath path = extension.getSymbolPath();
      if (path != null) {
        return path.getSymbol();
      }
      return extension;
    } else {
      return name;
    }
  }

  private static void annotateOptionOccurrences(PbTextFieldName name, AnnotationHolder holder) {
    PbTextMessage message = PsiTreeUtil.getParentOfType(name, PbTextMessage.class);
    if (message == null) {
      return;
    }
    OptionOccurrenceTracker tracker = OptionOccurrenceTracker.forMessage(message);
    if (tracker == null) {
      return;
    }

    for (Occurrence occurrence : tracker.getOccurrences(name)) {
      occurrence.annotate(holder, getFieldNameAnnotationElement(name));
    }
  }
}
