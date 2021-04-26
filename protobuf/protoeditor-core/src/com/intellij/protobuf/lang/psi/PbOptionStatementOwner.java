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
import java.util.Objects;
import java.util.stream.Collectors;

/** A {@link PbOptionOwner} whose options are represented as {@link PbOptionStatement}s. */
public interface PbOptionStatementOwner extends PbOptionOwner {
  @NotNull
  List<PbOptionStatement> getOptionStatements();

  @NotNull
  @Override
  default List<PbOptionExpression> getOptions() {
    return getOptionStatements()
        .stream()
        .map(PbOptionStatement::getOptionExpression)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
