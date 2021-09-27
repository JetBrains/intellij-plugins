/*
 * Copyright 2021 Google LLC
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
package com.intellij.protobuf.lang.names;

import com.intellij.protobuf.lang.psi.PbIdentifierValue;
import com.intellij.protobuf.lang.psi.PbNumberValue;
import com.intellij.protobuf.lang.psi.PbOptionExpression;
import com.intellij.protobuf.lang.psi.ProtoStringValue;

import java.util.Optional;

/**
 * Utility methods for implementations of {@link NameGeneratorContributer}
 */
public class NameGeneratorUtils {
  /** Convert the provided {@link PbOptionExpression} to an {@link Integer} if possible. */
  public static Optional<Integer> parseIntOption(PbOptionExpression optionExpression) {
    return Optional.ofNullable(optionExpression.getNumberValue())
      .map(PbNumberValue::getLongValue)
      .map(Long::intValue);
  }

  /** Convert the provided {@link PbOptionExpression} to a {@link String} if possible. */
  public static Optional<String> parseStringOption(PbOptionExpression optionExpression) {
    return Optional.ofNullable(optionExpression.getStringValue())
      .map(ProtoStringValue::getAsString);
  }

  /** Convert the provided {@link PbOptionExpression} to a {@link Boolean} if possible. */
  public static Optional<Boolean> parseBoolOption(PbOptionExpression optionExpression) {
    return Optional.ofNullable(optionExpression.getIdentifierValue())
      .map(PbIdentifierValue::getBooleanValue);
  }
}
