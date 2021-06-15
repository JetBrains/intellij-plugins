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

import com.intellij.protobuf.lang.psi.ProtoLiteral;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

/**
 * A simple functional interface that tests an option value for validity against this BuiltInType.
 */
public interface ValueTester {
  /** The various modes of value testers. */
  enum ValueTesterType {
    DEFAULT,
    OPTION,
    TEXT
  }

  /**
   * Tests whether the given value can be assigned to the tester's associated {@link BuiltInType}.
   *
   * @param value the value to test
   * @return an error message if the value is not assignable, or <code>null</code> if it is
   */
  @Nls
  @Nullable
  String testValue(@Nullable ProtoLiteral value);
}
