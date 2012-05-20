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

package com.intellij.lang.ognl.psi;

/**
 * All reserved keyword literals.
 *
 * @author Yann C&eacute;bron
 */
public final class OgnlKeyword {

  private OgnlKeyword() {
  }

  public static final String SHL = "shl";
  public static final String SHR = "shr";
  public static final String USHR = "ushr";

  public static final String AND = "and";
  public static final String BAND = "band";
  public static final String OR = "or";
  public static final String BOR = "bor";
  public static final String XOR = "xor";
  public static final String EQ = "eq";
  public static final String NEQ = "neq";
  public static final String LT = "lt";
  public static final String LTE = "lte";
  public static final String GT = "gt";
  public static final String GTE = "gte";
  public static final String NOT_IN = "not in";
  public static final String IN = "in";
  public static final String NEW = "new";

  public static final String TRUE = "true";
  public static final String FALSE = "false";
  public static final String NULL = "null";
  public static final String INSTANCEOF = "instanceof";

}