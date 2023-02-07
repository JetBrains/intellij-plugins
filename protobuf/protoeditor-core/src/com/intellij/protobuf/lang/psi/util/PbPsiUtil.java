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
package com.intellij.protobuf.lang.psi.util;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.lang.descriptor.Descriptor;
import com.intellij.protobuf.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Static utility functions for working with proto PSI elements. */
public final class PbPsiUtil {

  /** An empty qualified name (""). */
  public static final QualifiedName EMPTY_QUALIFIED_NAME = QualifiedName.fromComponents();

  /** Returns <code>true</code> if the given element is a PbStatement. */
  public static boolean isStatement(PsiElement element) {
    return element instanceof PbStatement;
  }

  /** Returns <code>true</code> if the given element is a PbDefinition. */
  public static boolean isDefinition(PsiElement element) {
    return element instanceof PbDefinition;
  }

  /** Returns <code>true</code> if the given element is a type (message, enum or group). */
  public static boolean isTypeElement(PsiElement element) {
    return element instanceof PbNamedTypeElement;
  }

  /** Returns <code>true</code> if the given element is a message (message or group). */
  public static boolean isMessageElement(PsiElement element) {
    return element instanceof PbMessageType;
  }

  /** Returns <code>true</code> if the given element is a group type */
  private static boolean isGroupElement(PsiElement element) {
    return element instanceof PbGroupDefinition;
  }

  /** Returns <code>true</code> if the given element is a package. */
  public static boolean isPackageElement(PsiElement element) {
    return element instanceof PbPackageName;
  }

  /** Returns <code>true</code> if the element is an extend definition */
  public static boolean isExtendElement(PsiElement element) {
    return element instanceof PbExtendDefinition;
  }

  /** Returns <code>true</code> if the element is an enum definition */
  public static boolean isEnumElement(PsiElement element) {
    return element instanceof PbEnumDefinition;
  }

  /** Returns <code>true</code> if the element is a oneof definition */
  public static boolean isOneofElement(PsiElement element) {
    return element instanceof PbOneofDefinition;
  }

  /** Returns the descriptor option with the given name if it exists, or else <code>null</code>. */
  @Nullable
  public static PbOptionExpression getDescriptorOption(PbOptionOwner owner, String name) {
    Descriptor descriptor = Descriptor.locate(owner.getPbFile());
    if (descriptor == null) {
      return null;
    }
    QualifiedName fqn = owner.getDescriptorOptionsTypeName(descriptor).append(name);
    for (PbOptionExpression option : owner.getOptions()) {
      PbField optionField =
          resolveRefToType(option.getOptionName().getEffectiveReference(), PbField.class);
      if (optionField != null && fqn.equals(optionField.getQualifiedName())) {
        return option;
      }
    }
    return null;
  }

  /**
   * Returns the value of the named descriptor option as a boolean.
   *
   * @param owner the {@link PbOptionOwner}
   * @param optionName the option name
   * @return {@link Boolean#TRUE} if the option is set to "true" (case-sensitive), {@link
   *     Boolean#FALSE} if the option is set to any other value, or <code>null</code> if the option
   *     is not set.
   */
  @Nullable
  public static Boolean getBooleanDescriptorOption(PbOptionOwner owner, String optionName) {
    PbOptionExpression option = getDescriptorOption(owner, optionName);
    if (option == null) {
      return null;
    }
    ProtoBooleanValue booleanValue = option.getBooleanValue();
    if (booleanValue == null) {
      return Boolean.FALSE;
    }
    Boolean value = booleanValue.getBooleanValue();
    if (value == null) {
      return Boolean.FALSE;
    }
    return value;
  }

  /** Returns true if the given option refers to the descriptor option field with the given name. */
  public static Boolean isDescriptorOption(PbOptionExpression option, String name) {
    PbOptionName optionName = option.getOptionName();
    PbOptionOwner optionOwner = PsiTreeUtil.getParentOfType(option, PbOptionOwner.class);
    if (optionOwner == null) {
      return false;
    }
    Descriptor descriptor = Descriptor.locate(option.getPbFile());
    if (descriptor == null) {
      return false;
    }
    PbField optionField =
        PbPsiUtil.resolveRefToType(optionName.getEffectiveReference(), PbField.class);
    if (optionField == null) {
      return false;
    }
    QualifiedName optionQualifiedName = optionField.getQualifiedName();
    if (optionQualifiedName == null) {
      return false;
    }
    QualifiedName descriptorTypeName = optionOwner.getDescriptorOptionsTypeName(descriptor);
    return optionQualifiedName.equals(descriptorTypeName.append(name));
  }

  /** Returns <code>true</code> if this is an auto-generated MapEntry message. */
  public static boolean isGeneratedMapEntry(PbElement element) {
    if (element instanceof PbMessageType && !element.isPhysical()) {
      Boolean mapEntryOption =
          getBooleanDescriptorOption(((PbMessageType) element).getBody(), "map_entry");
      return Boolean.TRUE.equals(mapEntryOption);
    }
    return false;
  }

  /**
   * Returns the {@link PbSymbolOwner} ancestor of this element. Note that this should be used in
   * place of <code>PsiTreeUtil.getParentOfType(element, PbSymbolOwner.class)</code> due to special
   * handling for packages.
   *
   * @param element The child element
   * @return The symbol owner parent
   */
  @Nullable
  public static PbSymbolOwner getSymbolOwner(PsiElement element) {
    PbSymbolOwner owner = PsiTreeUtil.getParentOfType(element, PbSymbolOwner.class);
    // If the parent is the file, we possibly need to artificially return
    // the most-qualified package name element as the symbol parent by delegating to
    // PbFile.getPrimarySymbolOwner().
    if (owner instanceof PbFile) {
      return ((PbFile) owner).getPrimarySymbolOwner();
    }
    return owner;
  }

  @Nullable
  public static <T> T resolveRefToType(PsiReference ref, Class<T> type) {
    return streamRefToType(ref, type).findFirst().orElse(null);
  }

  public static <T> List<T> multiResolveRefToType(PsiReference ref, Class<T> type) {
    return streamRefToType(ref, type).collect(Collectors.toList());
  }

  private static <T> Stream<T> streamRefToType(PsiReference ref, Class<T> type) {
    if (ref instanceof PsiPolyVariantReference polyRef) {
      return Arrays.stream(polyRef.multiResolve(false))
          .map(ResolveResult::getElement)
          .filter(type::isInstance)
          .map(type::cast);
    } else if (ref != null) {
      PsiElement resolved = ref.resolve();
      if (type.isInstance(resolved)) {
        return Stream.of(type.cast(resolved));
      }
    }
    return Stream.empty();
  }

  /**
   * Test if the given field is either an extension or a direct child of the given type.
   *
   * @param field the field to check
   * @param type the extended or parent type to check
   * @return true if the field is in an extension or child of the given type.
   */
  public static boolean fieldIsExtensionOrMember(PbField field, PbNamedTypeElement type) {
    return type.equals(field.getSymbolOwner()) || fieldIsExtension(field, type);
  }

  /**
   * Test if the given field is in an extension of the given type.
   *
   * @param field the field to check
   * @param type the extended type to check
   * @return true if the field is in an extension of the given type.
   */
  public static boolean fieldIsExtension(PbField field, PbNamedTypeElement type) {
    PbTypeName extendeeType = field.getExtendee();
    if (extendeeType == null) {
      return false;
    }
    PsiReference ref = extendeeType.getEffectiveReference();
    return ref != null && type.equals(ref.resolve());
  }

  /** Returns <code>true</code> if given field is a message type */
  public static boolean fieldIsMessage(PbField field) {
    PbTypeName fieldType = field.getTypeName();
    if (fieldType == null) {
      return false;
    }
    PsiReference ref = fieldType.getEffectiveReference();
    return ref != null && isMessageElement(ref.resolve());
  }

  /** Returns <code>true</code> if given field is a group type */
  public static boolean fieldIsGroup(PbField field) {
    PbTypeName fieldType = field.getTypeName();
    if (fieldType == null) {
      return false;
    }
    PsiReference ref = fieldType.getEffectiveReference();
    return ref != null && isGroupElement(ref.resolve());
  }

  public static boolean isFirstInside(@NotNull PsiElement element, @NotNull PsiElement ancestor) {
    TextRange ancestorTextRange = ancestor.getTextRange();
    TextRange elementTextRange = element.getTextRange();
    if (ancestorTextRange == null || elementTextRange == null) {
      return false;
    }
    return ancestorTextRange.getStartOffset() == elementTextRange.getStartOffset();
  }

  static boolean isElementType(PsiElement element, IElementType type) {
    return element != null && type.equals(element.getNode().getElementType());
  }

  static boolean isWhitespace(PsiElement element) {
    return isElementType(element, TokenType.WHITE_SPACE);
  }

  static boolean isBlockComment(PsiElement element) {
    return isElementType(element, ProtoTokenTypes.BLOCK_COMMENT);
  }

  static boolean isLineComment(PsiElement element) {
    return isElementType(element, ProtoTokenTypes.LINE_COMMENT);
  }

  static boolean isComment(PsiElement element) {
    return isLineComment(element) || isBlockComment(element);
  }

  private PbPsiUtil() {}
}
