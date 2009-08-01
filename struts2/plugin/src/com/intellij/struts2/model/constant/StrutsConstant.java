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

package com.intellij.struts2.model.constant;

import com.intellij.util.xml.Converter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines a Struts constant with corresponding converter.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsConstant {

  @NotNull
  @NonNls
  private final String name;

  @Nullable
  private final Converter converter;

  public StrutsConstant(@NotNull @NonNls final String name, @Nullable final Converter converter) {
    this.name = name;
    this.converter = converter;
  }

  @NotNull
  @NonNls
  public String getName() {
    return name;
  }

  @Nullable
  public Converter getConverter() {
    return converter;
  }

}