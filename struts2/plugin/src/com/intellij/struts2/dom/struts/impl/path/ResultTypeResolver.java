/*
 * Copyright 2008 The authors
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
package com.intellij.struts2.dom.struts.impl.path;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * @author Yann C&eacute;bron
 * @deprecated will be removed
 */
public class ResultTypeResolver {

  @NonNls
  private static final String[] RESULT_TYPES_CHAIN_REDIRECT = new String[]{"chain", "redirect-action", "redirectAction"};

  /**
   * Is the given result type pointing to an action.
   * <p/>
   * TODO: hardcoded list.
   *
   * @param resultType Result tag's "type" attribute value.
   * @return true/false.
   */
  public static boolean isChainOrRedirectType(@Nullable final String resultType) {
    if (resultType == null) {
      return false;
    }

    return Arrays.binarySearch(RESULT_TYPES_CHAIN_REDIRECT, resultType) >= 0;
  }

}