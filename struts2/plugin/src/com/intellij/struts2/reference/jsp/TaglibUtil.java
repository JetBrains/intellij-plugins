/*
 * Copyright 2007 The authors
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
package com.intellij.struts2.reference.jsp;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Various utilities for taglibs.
 *
 * @author Yann CŽbron
 */
public final class TaglibUtil {

  /**
   * Checks whether the given attribute value is a dynamic expression.
   * Currently only checks for OGNL ("%{EXPRESSION}").
   *
   * @param attributeValue The attribute value to check.
   * @return true if yes, false otherwise.
   */
  public static boolean isDynamicExpression(@NotNull @NonNls final String attributeValue) {
    return attributeValue.startsWith("%{");
  }

}