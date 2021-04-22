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

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A symbol that contributes additional synthetic symbols into the tree.
 *
 * <p>For example, a Group itself is a message-type definition, but also contributes a field. A map
 * is a field, but contributes a MapEntry message.
 */
public interface PbSymbolContributor extends PbSymbol {

  /**
   * Returns additional {@link PbSymbol} elements that will be placed in the tree as siblings of
   * this element.
   *
   * @return additional sibling elements.
   */
  @NotNull
  List<PbSymbol> getAdditionalSiblings();
}
