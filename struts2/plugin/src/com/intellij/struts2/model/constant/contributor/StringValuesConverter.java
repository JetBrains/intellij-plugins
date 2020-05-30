/*
 * Copyright 2009 The authors
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

package com.intellij.struts2.model.constant.contributor;

import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

/**
 * Resolves to list of given Strings.
 */
class StringValuesConverter extends ResolvingConverter.StringConverter {

  private final String[] values;

  StringValuesConverter(@NonNls final String... values) {
    Arrays.sort(values);
    this.values = values;
  }

  @Override
  public String fromString(final String s, final ConvertContext context) {
    return Arrays.binarySearch(values, s) > -1 ? s : null;
  }

  @Override
  @NotNull
  public Collection<String> getVariants(final ConvertContext context) {
    return Arrays.asList(values);
  }

}