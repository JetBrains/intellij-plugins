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

import org.jetbrains.annotations.Nullable;

/** An enum defining possible syntax levels. */
public enum SyntaxLevel {
  PROTO2("proto2"),
  PROTO3("proto3");

  @Nullable
  public static SyntaxLevel forString(String level) {
    for (SyntaxLevel possibility : SyntaxLevel.values()) {
      if (possibility.toString().equals(level)) {
        return possibility;
      }
    }
    return null;
  }

  private final String name;

  SyntaxLevel(String name) {
    this.name = name;
  }

  /** Returns the syntax name as it would appear in a syntax statement. */
  @Override
  public String toString() {
    return name;
  }
}
