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

import com.intellij.psi.PsiElement;
import com.intellij.protobuf.lang.util.BuiltInType;
import com.intellij.protobuf.lang.util.ValueTester;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public interface PbOptionNameBase extends PbElement {
  /**
   * Returns the qualifier PbOptionName element, if it exists.
   *
   * <ul>
   *   <li> For an OptionName of "(foo).bar", the qualifier is the OptionName representing "(foo)".
   *   <li> For an OptionName of "bar", the qualifier is <code>null</code>.
   * </ul>
   *
   * @return PbOptionName qualifier.
   */
  @Nullable
  PbOptionName getQualifier();

  /**
   * If this option name refers to an extension, returns the associated {@link PbExtensionName}
   * element. Otherwise, <code>null</code> is returned. Extension options are surrounded by
   * parenthesis. For example, "(foo)".
   *
   * @return The associated {@link PbExtensionName}, or <code>null</code> if this is not an
   *     extension option.
   */
  @Nullable
  PbExtensionName getExtensionName();

  /**
   * If this option name refers to a field, and not an extension, this method returns the symbol
   * containing the field name. Otherwise, if this is an extension option, <code>null</code> is
   * returned. For example: for the option "deprecated", this method returns a symbol containing
   * "deprecated", but for "(foo)", this method returns <code>null</code>.
   *
   * @return The associated option name symbol, or <code>null</code> if this is an extension option.
   */
  @Nullable
  PsiElement getSymbol();

  /**
   * Returns this option's qualifier type.
   *
   * <p>This method behavior differs from {@link #getQualifier()} when no PbOptionName qualifier
   * exists. In that case, this method will return the appropriate qualifier type from <code>
   * descriptor.proto</code>, if the descriptor can be found. For example, for a field option named
   * "deprecated", this method will return the <code>proto2.FieldOptions</code> type.
   *
   * @return This option's qualifier type, possibly from <code>descriptor.proto</code>. If the
   *     descriptor cannot be found, and {@link #getQualifier()} returns <code>null</code>, this
   *     method will return <code>null</code>.
   */
  @Nullable
  PbMessageType getQualifierType();

  /** Returns <code>true</code> if this is a special option name which should be highlighted. */
  default boolean isSpecial() {
    return getSpecialType() != null;
  }

  /**
   * Returns the {@link SpecialOptionType} that this option represents, or <code>null</code> if this
   * is not a special option.
   */
  @Nullable
  SpecialOptionType getSpecialType();

  /**
   * If this option's type is a built-in, this method returns the associated {@link BuiltInType}
   * instance.
   *
   * @return a {@link BuiltInType}, or <code>null</code> if the option type is not a built-in.
   */
  @Nullable
  BuiltInType getBuiltInType();

  /**
   * If this option's type is a built-in, this method returns a {@link ValueTester} instance to
   * apply to assigned values.
   *
   * @return a {@link ValueTester}, or <code>null</code> if the option type is not a built-in.
   */
  @Nullable
  ValueTester getBuiltInValueTester();

  /**
   * Returns the {@link PbNamedTypeElement type} of this option if it is a message or enum.
   *
   * @return the option type, or <code>null</code> if the option is not a message or enum value.
   */
  @Nullable
  PbNamedTypeElement getNamedType();
}
