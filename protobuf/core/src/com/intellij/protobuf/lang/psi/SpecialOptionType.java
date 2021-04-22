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

import java.util.function.BiPredicate;

/**
 * Special built-in options.
 *
 * <p>Most options (such as <code>deprecated</code>) are defined in descriptor.proto. However, the
 * options listed below are specially handled by the compiler and are not included in the
 * descriptor.
 */
public enum SpecialOptionType {
  FIELD_DEFAULT((name, owner) -> "default".equals(name.getText()) && owner instanceof PbField),
  FIELD_JSON_NAME((name, owner) -> "json_name".equals(name.getText()) && owner instanceof PbField);

  private final BiPredicate<PbOptionName, PbOptionOwner> check;

  SpecialOptionType(BiPredicate<PbOptionName, PbOptionOwner> check) {
    this.check = check;
  }

  public boolean isInstance(PbOptionName name, PbOptionOwner owner) {
    return check.test(name, owner);
  }
}
