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
package com.intellij.protobuf.jvm.names;

/** Utilities for mapping from proto names to various other forms of names useful for Java. */
public class NameUtils {

  public static String underscoreToCamelCase(String input) {
    return underscoresToCamelCase(input, false);
  }

  public static String underscoreToCapitalizedCamelCase(String input) {
    return underscoresToCamelCase(input, true);
  }

  // This is basically copied from the protobuf compiler to be as similar as possible
  // https://github.com/google/protobuf/blob/3.0.x/src/google/protobuf/compiler/java/java_helpers.cc#L118
  private static String underscoresToCamelCase(String input, boolean capNextLetter) {
    if (input.isEmpty()) {
      return input;
    }
    StringBuilder result = new StringBuilder(input.length());
    // Try not to be dependent on locale, so explicitly check for a-zA-Z.
    for (int i = 0; i < input.length(); i++) {
      if ('a' <= input.charAt(i) && input.charAt(i) <= 'z') {
        if (capNextLetter) {
          result.append((char) (input.charAt(i) + ('A' - 'a')));
        } else {
          result.append(input.charAt(i));
        }
        capNextLetter = false;
      } else if ('A' <= input.charAt(i) && input.charAt(i) <= 'Z') {
        if (i == 0 && !capNextLetter) {
          // Force first letter to lower-case unless explicitly told to
          // capitalize it.
          result.append((char) (input.charAt(i) + ('a' - 'A')));
        } else {
          // Capital letters after the first are left as-is.
          result.append(input.charAt(i));
        }
        capNextLetter = false;
      } else if ('0' <= input.charAt(i) && input.charAt(i) <= '9') {
        result.append(input.charAt(i));
        capNextLetter = true;
      } else {
        capNextLetter = true;
      }
    }
    // Add a trailing "_" if the name should be altered.
    if (input.charAt(input.length() - 1) == '#') {
      result.append('_');
    }
    return result.toString();
  }
}
