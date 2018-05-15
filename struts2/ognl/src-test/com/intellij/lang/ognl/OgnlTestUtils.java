/*
 * Copyright 2018 The authors
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

package com.intellij.lang.ognl;

import org.jetbrains.annotations.NonNls;

/**
 * @author Yann C&eacute;bron
 */
public final class OgnlTestUtils {

  @NonNls
  public static final String OGNL_TEST_DATA = "/contrib/struts2/ognl/testData";

  private OgnlTestUtils() {
  }

  /**
   * Wraps text in OGNL expression holder for parsing.
   *
   * @param text Text.
   * @return OGNL expression.
   */
  public static String createExpression(final String text) {
    return OgnlLanguage.EXPRESSION_PREFIX + text + OgnlLanguage.EXPRESSION_SUFFIX;
  }
}
