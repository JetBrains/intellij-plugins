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
package com.intellij.protobuf.lang.util;

import com.google.common.base.Ascii;
import com.intellij.protobuf.lang.PbLangBundle;
import com.intellij.protobuf.lang.psi.ProtoBooleanValue;
import com.intellij.protobuf.lang.psi.ProtoIdentifierValue;
import com.intellij.protobuf.lang.psi.ProtoNumberValue;
import com.intellij.protobuf.lang.psi.ProtoNumberValue.IntegerFormat;
import com.intellij.protobuf.lang.psi.ProtoNumberValue.SourceType;
import com.intellij.protobuf.lang.psi.ProtoStringValue;
import com.intellij.protobuf.lang.util.ValueTester.ValueTesterType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Represents a built-in type. */
@SuppressWarnings("ImmutableEnumChecker")
public enum BuiltInType {
  STRING(new StringTester()),
  BYTES(new StringTester()),
  BOOL(new BooleanTester()),
  DOUBLE(new FloatTester()),
  FLOAT(new FloatTester()),
  UINT32(new IntegerTester(ProtoNumberValue::isValidUint32, true)),
  UINT64(new IntegerTester(ProtoNumberValue::isValidUint64, true)),
  FIXED32(new IntegerTester(ProtoNumberValue::isValidUint32, true)),
  FIXED64(new IntegerTester(ProtoNumberValue::isValidUint64, true)),
  INT32(new IntegerTester(ProtoNumberValue::isValidInt32, false)),
  INT64(new IntegerTester(ProtoNumberValue::isValidInt64, false)),
  SINT32(new IntegerTester(ProtoNumberValue::isValidInt32, false)),
  SINT64(new IntegerTester(ProtoNumberValue::isValidInt64, false)),
  SFIXED32(new IntegerTester(ProtoNumberValue::isValidInt32, false)),
  SFIXED64(new IntegerTester(ProtoNumberValue::isValidInt64, false));

  private static final Map<String, BuiltInType> TYPES_BY_NAME =
      Collections.unmodifiableMap(
          Stream.of(values()).collect(Collectors.toMap(BuiltInType::getName, Function.identity())));

  private final ValueTesterFactory factory;

  BuiltInType(ValueTesterFactory factory) {
    this.factory = factory;
  }

  /** Returns <code>true</code> if the given name represents a built-in type. */
  public static boolean isBuiltInType(String name) {
    return getType(name) != null;
  }

  /** Returns the collection of valid {@link BuiltInType} instances. */
  public static Collection<BuiltInType> getTypes() {
    return Arrays.asList(values());
  }

  /**
   * Returns the {@link BuiltInType} of the given name.
   *
   * @param name type name
   * @return The {@link BuiltInType} instance of the given name, or <code>null</code>.
   */
  public static @Nullable BuiltInType getType(String name) {
    if (name == null) {
      return null;
    }
    return TYPES_BY_NAME.get(name);
  }

  /** Returns the type name. */
  public @NotNull String getName() {
    return Ascii.toLowerCase(this.name());
  }

  /** Returns a {@link ValueTester} to be used in the specified type context. */
  public @NotNull ValueTester getValueTester(ValueTesterType type) {
    return factory.getValueTester(type);
  }

  private interface ValueTesterFactory {
    ValueTester getValueTester(ValueTesterType type);
  }

  private static class IntegerTester implements ValueTesterFactory {
    private final boolean unsigned;
    private final Predicate<ProtoNumberValue> validityCheck;

    private IntegerTester(Predicate<ProtoNumberValue> validityCheck, boolean unsigned) {
      this.validityCheck = validityCheck;
      this.unsigned = unsigned;
    }

    @Override
    public @Nls @NotNull ValueTester getValueTester(ValueTesterType type) {
      return value -> {
        if (!(value instanceof ProtoNumberValue number)) {
          return PbLangBundle.message("integer.value.expected");
        }
        if (number.getSourceType() != SourceType.INTEGER) {
          return PbLangBundle.message("integer.value.expected");
        }
        // Provide a nice message for negative values used with unsigned types
        if (unsigned && number.isNegative()) {
          return PbLangBundle.message("unsigned.value.cannot.be.negative");
        }
        if (!validityCheck.test(number)) {
          return PbLangBundle.message("integer.value.out.of.range");
        }
        return null;
      };
    }
  }

  private static class FloatTester implements ValueTesterFactory {
    @Override
    public @Nls @NotNull ValueTester getValueTester(ValueTesterType type) {
      return value -> {
        if (value instanceof ProtoIdentifierValue identifierValue) {
          // getAsNumber() might return null, but the following instanceof check will catch it.
          value = identifierValue.getAsNumber();
        }
        if (!(value instanceof ProtoNumberValue number)) {
          return PbLangBundle.message("floating.point.value.expected");
        }
        SourceType sourceType = number.getSourceType();
        if (sourceType == null) {
          return PbLangBundle.message("floating.point.value.expected");
        }
        return switch (number.getSourceType()) {
          case INTEGER -> {
            // text format integers must be decimal when used as floating point values.
            if (type == ValueTesterType.TEXT
                && number.getIntegerFormat() != IntegerFormat.DEC) {
              yield PbLangBundle.message("integer.value.must.be.decimal");
            }
            boolean valid;
            if (number.isNegative()) {
              valid =
                type == ValueTesterType.DEFAULT
                ? number.isValidInt65()
                : number.isValidInt64();
            }
            else {
              valid = number.isValidUint64();
            }
            yield valid ? null : PbLangBundle.message("integer.value.out.of.range");
          }
          case FLOAT -> number.isValidDouble()
                        ? null
                        : PbLangBundle.message("invalid.floating.point.value");
          case INF -> type == ValueTesterType.OPTION
                      ? PbLangBundle.message("not.allowed.as.option.value", "inf")
                      : null;
          case NAN -> type == ValueTesterType.OPTION
                      ? PbLangBundle.message("not.allowed.as.option.value", "nan")
                      : null;
        };
      };
    }
  }

  private static class StringTester implements ValueTesterFactory {
    @Override
    public @Nls @NotNull ValueTester getValueTester(ValueTesterType type) {
      return value -> {
        if (!(value instanceof ProtoStringValue)) {
          return PbLangBundle.message("string.value.expected");
        }
        return null;
      };
    }
  }

  private static class BooleanTester implements ValueTesterFactory {
    @Override
    public @Nls @NotNull ValueTester getValueTester(ValueTesterType type) {
      return value -> {
        if (!(value instanceof ProtoBooleanValue)) {
          return PbLangBundle.message("boolean.value.expected");
        }
        Boolean booleanValue = ((ProtoBooleanValue) value).getBooleanValue();
        if (booleanValue == null) {
          return PbLangBundle.message("boolean.value.expected");
        }
        return null;
      };
    }
  }
}
