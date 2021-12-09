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

import com.google.common.collect.Range;
import org.jetbrains.annotations.Nullable;

/** Common interface implemented by simple fields, map fields, and group fields. */
public interface PbField extends PbNamedElement, PbOptionOwner {

  /** An enum representing a field's canonical label, which might differ from the defined label. */
  enum CanonicalFieldLabel {
    OPTIONAL,
    REQUIRED,
    REPEATED
  }

  PbField[] EMPTY_ARRAY = new PbField[0];
  long MAX_FIELD_NUMBER = (1 << 29) - 1;
  long MAX_MESSAGE_SET_FIELD_NUMBER = 2147483646;
  Range<Long> NUMBERS_RESERVED_BY_PROTO = Range.closed(19000L, 19999L);

  /**
   * Returns the lexically-declared field label.
   *
   * <p>Callers should use {@link #getCanonicalLabel()}, {@link #isRequired()}, or {@link
   * #isRepeated()} methods instead. In most cases, the lexically-declared field label should match
   * these methods. But in the case of map fields, for instance, the label is ignored for all
   * purposes other than generating an annotation error, and the field's canonical label is always
   * <code>repeated</code>.
   *
   * @return the field label defined in proto source.
   */
  @Nullable
  PbFieldLabel getDeclaredLabel();

  /**
   * Returns the canonical field label.
   *
   * <p>The canonical usually matches {@link #getDeclaredLabel()}, but does not in the following
   * cases:
   *
   * <ul>
   *   <li>In proto3, a field's canonical label will be OPTIONAL when the field label is omitted
   *       from source.
   *   <li>Map fields always return a REPEATED canonical label, regardless of the source label.
   * </ul>
   *
   * @return the canonical field label.
   */
  CanonicalFieldLabel getCanonicalLabel();

  /** Returns <code>true</code> if this is a required field. */
  default boolean isRequired() {
    return CanonicalFieldLabel.REQUIRED.equals(getCanonicalLabel());
  }

  /** Returns <code>true</code> if this is a repeated field. */
  default boolean isRepeated() {
    return CanonicalFieldLabel.REPEATED.equals(getCanonicalLabel());
  }

  @Nullable
  PbTypeName getTypeName();

  @Nullable
  PbNumberValue getFieldNumber();

  /**
   * Returns the type that this field extends, or <code>null</code> if this field is not part of an
   * extend block or the extended type cannot be found.
   */
  @Nullable
  PbTypeName getExtendee();

  /**
   * Returns this field's parent <code>oneof</code> definition, or <code>null</code> if the field is
   * not part of a oneof definition.
   */
  @Nullable
  PbOneofDefinition getOneof();

  /** Returns <code>true</code> if this is an extension field. */
  default boolean isExtension() {
    return getExtendee() != null;
  }
}
