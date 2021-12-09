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

package com.intellij.struts2.dom;

import org.jetbrains.annotations.NonNls;

/**
 * DOM-related constants.
 *
 * @author Yann C&eacute;bron
 */
public final class StrutsDomConstants {

  private StrutsDomConstants() {
  }

  /**
   * DOM-Namespace-Key for struts.xml
   */
  @NonNls
  public static final String STRUTS_NAMESPACE_KEY = "struts namespace";

  /**
   * DOM-Namespace-Key for validation.xml
   */
  @NonNls
  public static final String VALIDATOR_NAMESPACE_KEY = "validator namespace";

  /**
   * DOM-Namespace-Key for validator-config.xml
   */
  @NonNls
  public static final String VALIDATOR_CONFIG_NAMESPACE_KEY = "validator config namespace";
}