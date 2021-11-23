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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.protobuf.ide.PbCompositeModificationTracker;
import com.intellij.protobuf.lang.PbLangBundle;
import com.intellij.protobuf.lang.descriptor.Descriptor;
import com.intellij.protobuf.lang.descriptor.DescriptorOptionType;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.QualifiedName;
import org.jetbrains.annotations.NotNull;

/** Annotations specific to proto3 syntax level. */
public class Proto3Annotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull final AnnotationHolder holder) {
    // Only operate on proto3 files.
    if (!(element instanceof PbElement)
        || ((PbElement) element).getPbFile().getSyntaxLevel() != SyntaxLevel.PROTO3) {
      return;
    }

    element.accept(
        new PbVisitor() {
          @Override
          public void visitEnumValue(@NotNull PbEnumValue value) {
            annotateEnumValue(value, holder);
          }

          @Override
          public void visitExtendDefinition(@NotNull PbExtendDefinition extendDefinition) {
            annotateExtendDefinition(extendDefinition, holder);
          }

          @Override
          public void visitExtensionsStatement(@NotNull PbExtensionsStatement statement) {
            annotateExtensionsStatement(statement, holder);
          }

          @Override
          public void visitField(@NotNull PbField field) {
            annotateField(field, holder);
          }

          @Override
          public void visitImportStatement(@NotNull PbImportStatement statement) {
            annotateImportStatement(statement, holder);
          }

          @Override
          public void visitGroupDefinition(@NotNull PbGroupDefinition group) {
            annotateGroupDefinition(group, holder);
          }

          @Override
          public void visitOptionExpression(@NotNull PbOptionExpression option) {
            annotateOptionExpression(option, holder);
          }

          @Override
          public void visitOptionName(@NotNull PbOptionName name) {
            annotateOptionName(name, holder);
          }
        });
  }

  /*
   * The first enum value must be 0.
   */
  private static void annotateEnumValue(PbEnumValue value, AnnotationHolder holder) {
    PbEnumDefinition enumDefinition = PsiTreeUtil.getParentOfType(value, PbEnumDefinition.class);
    if (enumDefinition == null) {
      return;
    }
    PbEnumValue firstValue = enumDefinition.getEnumValues().stream().findFirst().orElse(null);
    if (!value.equals(firstValue)) {
      return;
    }
    PbNumberValue numberValue = value.getNumberValue();
    if (numberValue == null) {
      return;
    }
    Long enumNumber = numberValue.getLongValue();
    if (enumNumber != null && enumNumber != 0) {
      holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("proto3.first.enum.value.zero"))
          .range(value)
          .create();
    }
  }

  /*
   * Extend definitions are only allowed for extending descriptor options.
   */
  private static void annotateExtendDefinition(
      PbExtendDefinition extendDefinition, AnnotationHolder holder) {
    Descriptor descriptor = Descriptor.locate(extendDefinition.getPbFile());
    if (descriptor == null) {
      return;
    }
    PbTypeName extendee = extendDefinition.getTypeName();
    if (extendee == null) {
      return;
    }
    PbSymbol resolved =
        PbPsiUtil.resolveRefToType(extendee.getEffectiveReference(), PbSymbol.class);
    if (resolved == null) {
      return;
    }
    QualifiedName extendeeQualifiedName = resolved.getQualifiedName();
    if (extendeeQualifiedName == null) {
      return;
    }
    for (DescriptorOptionType optionType : DescriptorOptionType.values()) {
      if (extendeeQualifiedName.equals(optionType.forDescriptor(descriptor))) {
        // This is a valid extension.
        return;
      }
    }
    holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("proto3.extensions"))
        .range(extendee.getSymbolPath().getSymbol())
        .create();
  }

  /*
   * Message extension ranges are not allowed.
   */
  private static void annotateExtensionsStatement(
      PbExtensionsStatement statement, AnnotationHolder holder) {
    holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("proto3.extension.ranges"))
        .range(statement)
        .create();
  }

  /*
   * Fields cannot be marked required.
   * Field names must be unique after converting to lowercase and removing underscores.
   * Enum types must be defined in proto3 files.
   */
  private static void annotateField(PbField field, AnnotationHolder holder) {
    PbFieldLabel label = field.getDeclaredLabel();
    if (label != null && "required".equals(label.getText())) {
      holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("proto3.required.fields"))
          .range(label)
          .create();
    }

    PsiElement nameIdentifier = field.getNameIdentifier();
    PbSymbolOwner message = field.getSymbolOwner();
    if (nameIdentifier != null && message != null) {
      Multimap<String, PbField> fieldNameMap = getProto3NameToFieldMap(message);
      String convertedName = toLowerWithoutUnderscores(nameIdentifier.getText());
      if (fieldNameMap.get(convertedName).size() > 1) {
        holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("proto3.field.name.uniqueness"))
            .range(nameIdentifier)
            .create();
      }
    }

    PbTypeName fieldType = field.getTypeName();
    if (fieldType != null) {
      PbNamedTypeElement enumDef =
          PbPsiUtil.resolveRefToType(fieldType.getEffectiveReference(), PbEnumDefinition.class);
      if (enumDef != null) {
        PbFile definingFile = enumDef.getPbFile();
        if (definingFile.getSyntaxLevel() != SyntaxLevel.PROTO3) {
          holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("proto3.enums"))
              .range(fieldType.getSymbolPath().getSymbol())
              .create();
        }
      }
    }
  }

  /*
   * Group fields are not allowed in proto3.
   */
  private static void annotateGroupDefinition(PbGroupDefinition group, AnnotationHolder holder) {
    holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("proto3.group.fields"))
        .range(group)
        .create();
  }

  /*
   * Weak imports are not allowed.
   */
  private static void annotateImportStatement(
      PbImportStatement statement, AnnotationHolder holder) {
    PsiElement label = statement.getImportLabel();
    if (label != null && statement.isWeak()) {
      holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("proto3.weak.imports"))
          .range(label)
          .create();
    }
  }

  /*
   * Default values are not supported.
   */
  private static void annotateOptionExpression(PbOptionExpression option, AnnotationHolder holder) {
    if (option.getOptionName().getSpecialType() == SpecialOptionType.FIELD_DEFAULT) {
      holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("proto3.default.values"))
          .range(option)
          .create();
    }
  }

  /*
   * The 'cc_api_compatibility' option is not supported.
   * The 'message_set_wire_format' is not supported.
   */
  private static void annotateOptionName(PbOptionName optionName, AnnotationHolder holder) {
    PbOptionExpression optionExpression =
        PsiTreeUtil.getParentOfType(optionName, PbOptionExpression.class);
    if (optionExpression == null) {
      return;
    }

    if (PbPsiUtil.isDescriptorOption(optionExpression, "cc_api_compatibility")) {
      PbIdentifierValue identifierValue = optionExpression.getIdentifierValue();
      if (identifierValue == null) {
        return;
      }
      PbEnumValue enumValue =
          PbPsiUtil.resolveRefToType(identifierValue.getReference(), PbEnumValue.class);
      if (enumValue == null) {
        return;
      }
      if (!"NO_COMPATIBILITY".equals(enumValue.getName())) {
        holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("proto3.cc_api_compatibility"))
            .range(optionName)
            .create();
      }
    } else if (PbPsiUtil.isDescriptorOption(optionExpression, "message_set_wire_format")) {
      ProtoBooleanValue booleanValue = optionExpression.getBooleanValue();
      if (booleanValue != null && Boolean.TRUE.equals(booleanValue.getBooleanValue())) {
        holder.newAnnotation(HighlightSeverity.ERROR, PbLangBundle.message("proto3.messageset"))
            .range(optionName)
            .create();
      }
    }
  }

  private static Multimap<String, PbField> getProto3NameToFieldMap(PbSymbolOwner message) {
    return CachedValuesManager.getCachedValue(
        message,
        () -> {
          Multimap<String, PbField> messageFieldNameMap = ArrayListMultimap.create();
          for (PbField field : message.getSymbols(PbField.class)) {
            String fieldName = field.getName();
            if (fieldName != null) {
              messageFieldNameMap.put(toLowerWithoutUnderscores(fieldName), field);
            }
          }
          return Result.create(messageFieldNameMap, PbCompositeModificationTracker.byElement(message));
        });
  }

  // Adapted from ToLowercaseWithoutUnderscores(...) in descriptor.cc.
  private static String toLowerWithoutUnderscores(String name) {
    StringBuilder newName = new StringBuilder();
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (c != '_') {
        if (c >= 'A' && c <= 'Z') {
          newName.append((char) (c - 'A' + 'a'));
        } else {
          newName.append(c);
        }
      }
    }
    return newName.toString();
  }
}
