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

import com.google.common.primitives.UnsignedLong;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

/** A number value: either an integer or floating point number, "inf" or "nan". Can be negative. */
public interface ProtoNumberValue extends ProtoLiteral {

  /** Represents the number's format in proto source. */
  enum SourceType {
    INTEGER,
    FLOAT,
    INF,
    NAN
  }

  /** Represents the radix used to specify an integer. */
  enum IntegerFormat {
    OCT,
    DEC,
    HEX
  }

  /** Returns the child containing the number, without the optional negation. */
  PsiElement getNumberElement();

  /** Returns the {@link SourceType format} that this number was defined as in proto source. */
  @Nullable
  SourceType getSourceType();

  /** Returns the {@link IntegerFormat radix} that this integer was defined as in proto source. */
  @Nullable
  default IntegerFormat getIntegerFormat() {
    if (getSourceType() == SourceType.INTEGER) {
      PsiElement numberElement = getNumberElement();
      if (numberElement == null) {
        return null;
      }
      String numberText = numberElement.getText();
      int radix = ProtoNumberValueUtil.getRadix(numberText);
      return switch (radix) {
        case 8 -> IntegerFormat.OCT;
        case 10 -> IntegerFormat.DEC;
        case 16 -> IntegerFormat.HEX;
        default -> null;
      };
    }
    return null;
  }

  /**
   * Returns <code>true</code> if the number was defined as a negative value. For large unsigned
   * 64-bit values, the actual value of the returned UnsignedLong will be negative since Java does
   * not support unsigned values natively.
   *
   * @return whether the value was defined as a negative number.
   */
  boolean isNegative();

  @Override
  @Nullable
  default Object getValue() {
    return getNumber();
  }

  /**
   * Returns the value as a Long, or <code>null</code> if either SourceType is not INTEGER or the
   * value is larger than the bounds of a signed 64-bit integer.
   */
  @Nullable
  default Long getLongValue() {
    PsiElement numberElement = getNumberElement();
    if (numberElement == null) {
      return null;
    }
    if (getSourceType() == SourceType.INTEGER) {
      String numberText = numberElement.getText();
      String negativePrefix = isNegative() ? "-" : "";
      int radix = ProtoNumberValueUtil.getRadix(numberText);
      numberText = ProtoNumberValueUtil.trimRadixPrefix(numberText, radix);
      try {
        return Long.parseLong(negativePrefix + numberText, radix);
      } catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }

  /**
   * Returns the value as an UnsignedLong, or <code>null</code> if SourceType is not INTEGER, the
   * value was defined as a negative, or the value is larger than the bounds of an unsigned 64-bit
   * integer.
   */
  @Nullable
  default UnsignedLong getUnsignedLongValue() {
    if (isNegative()) {
      return null;
    }
    return getUnsignedLongComponent();
  }

  /**
   * Returns the integer component as an UnsignedLong, ignoring an optional negation sign. Returns
   * <code>null</code> if SourceType is not INTEGER or the value is larger than the bounds of an
   * unsigned 64-bit integer.
   */
  @Nullable
  default UnsignedLong getUnsignedLongComponent() {
    PsiElement numberElement = getNumberElement();
    if (numberElement == null) {
      return null;
    }
    if (getSourceType() == SourceType.INTEGER) {
      String numberText = numberElement.getText();
      int radix = ProtoNumberValueUtil.getRadix(numberText);
      numberText = ProtoNumberValueUtil.trimRadixPrefix(numberText, radix);
      try {
        return UnsignedLong.valueOf(numberText, radix);
      } catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }

  /**
   * Returns the value as a Double, or <code>null</code> if the SourceType is not FLOAT, INF, or
   * NAN, or the value is larger than the bounds of a Double.
   */
  @Nullable
  default Double getDoubleValue() {
    SourceType sourceType = getSourceType();
    if (sourceType == null) {
      return null;
    }
    return switch (sourceType) {
      case FLOAT -> {
        PsiElement numberElement = getNumberElement();
        if (numberElement == null) {
          yield null;
        }
        String numberText = numberElement.getText();
        String negativePrefix = isNegative() ? "-" : "";
        try {
          yield Double.parseDouble(negativePrefix + numberText);
        }
        catch (NumberFormatException e) {
          yield null;
        }
      }
      case INF -> isNegative() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
      case NAN -> Double.NaN;
      default -> null;
    };
  }

  /**
   * Returns a {@link Number} representation of the value. This will be a {@link Double} if the
   * value is a floating point number, a {@link Long} if the value is an integer within the range of
   * a 64-bit signed integer, or a {@link UnsignedLong} if the value is a non-negative integer
   * greater than the maximum value of a signed 64-bit integer. If the number cannot be represented
   * by any of Double, Long or UnsignedLong, <code>null</code> is returned.
   */
  @Nullable
  default Number getNumber() {
    SourceType sourceType = getSourceType();
    if (sourceType != null) {
      return switch (getSourceType()) {
        case FLOAT, INF, NAN -> getDoubleValue();
        case INTEGER -> {
          Long longValue = getLongValue();
          yield longValue != null ? longValue : getUnsignedLongValue();
        }
      };
    }
    return null;
  }

  /** Returns <code>true</code> if this value represents a valid double. */
  default boolean isValidDouble() {
    return getDoubleValue() != null;
  }

  /** Returns <code>true</code> if this value represents a valid int32. */
  default boolean isValidInt32() {
    Long value = getLongValue();
    return value != null && value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE;
  }

  /** Returns <code>true</code> if this value represents a valid uint32. */
  default boolean isValidUint32() {
    if (isNegative()) {
      return false;
    }
    Long value = getLongValue();
    return value != null && value >= 0 && value <= 0xFFFFFFFFL;
  }

  /** Returns <code>true</code> if this value represents a valid 64-bit signed integer. */
  default boolean isValidInt64() {
    return getLongValue() != null;
  }

  /** Returns <code>true</code> if this value represents a valid 64-bit unsigned integer. */
  default boolean isValidUint64() {
    return getUnsignedLongValue() != null;
  }

  /**
   * Returns <code>true</code> if this value represents a valid 65-bit signed integer.
   *
   * <p>Proto allows default values for float and double types to be set to -0xFFFFFFFFFFFFFFFF, a
   * value which is not a valid unsigned 64-bit int. The acceptance of the sign bit effectively
   * makes the value a signed 65-bit integer.
   */
  default boolean isValidInt65() {
    return getUnsignedLongComponent() != null;
  }
}
