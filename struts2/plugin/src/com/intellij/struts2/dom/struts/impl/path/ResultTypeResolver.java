/*
 * Copyright 2011 The authors
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

import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Determines result-type "kind".
 *
 * @author Yann C&eacute;bron
 */
public final class ResultTypeResolver {

  private ResultTypeResolver() {
  }

  @NonNls
  private static final String[] RESULT_TYPES_CHAIN_REDIRECT = new String[]{"chain", "redirect-action", "redirectAction"};

  @NonNls
  private static final String[] RESULT_TYPES_DISPATCH = new String[]{"dispatcher", "plainText", "redirect"};

  /**
   * Is the given result type pointing to an action.
   *
   * @param resultType Result tag's "type" attribute value.
   * @return true/false.
   */
  public static boolean isChainOrRedirectType(@NotNull final String resultType) {
    return Arrays.binarySearch(RESULT_TYPES_CHAIN_REDIRECT, resultType) >= 0;
  }

  /**
   * Is the given result type pointing to an "dispatch"-type result (web-resource).
   *
   * @param resultType Result tag's "type" attribute value.
   * @return true/false.
   */
  public static boolean isDispatchType(@NotNull final String resultType) {
    return Arrays.binarySearch(RESULT_TYPES_DISPATCH, resultType) >= 0;
  }

  /**
   * Is the given resultType handled by one of the built-in/contributed {@link StrutsResultContributor}s.
   *
   * @param resultType Result-Type.
   * @return {@code true} if supported, {@code false} otherwise.
   */
  public static boolean hasResultTypeContributor(@Nullable final String resultType) {
    if (resultType == null) {
      return false;
    }

    // check "builtin"
    if (isDispatchType(resultType) ||
        isChainOrRedirectType(resultType)) {
      return true;
    }

    // find extensions
    return ContainerUtil.find(StrutsResultContributor.EP_NAME.getExtensionList(), strutsResultContributor -> strutsResultContributor.matchesResultType(resultType)) != null;
  }

}