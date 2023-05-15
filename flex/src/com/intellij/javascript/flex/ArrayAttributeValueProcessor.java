/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.javascript.flex;

import org.jetbrains.annotations.NotNull;

public abstract class ArrayAttributeValueProcessor {
  public void process(@NotNull String value) {
    int length = value.length();
    if (length < 3) {
      return;
    }
    if (value.charAt(0) != '[') {
      return;
    }

    int start = 1;
    while (start < length) {
      int end = value.indexOf(',', start);
      if (end < 0) {
        end = value.indexOf(']', start);
        if (end < 0) {
          end = length;
        }
      }
      while (start < length && Character.isWhitespace(value.charAt(start))) {
        start++;
      }
      int end1 = end;
      while (end1 >= 1 && Character.isWhitespace(value.charAt(end1 - 1))) {
        end1--;
      }
      processElement(start, end1);
      start = end + 1;
    }
  }

  protected abstract void processElement(int start, int end);
}
