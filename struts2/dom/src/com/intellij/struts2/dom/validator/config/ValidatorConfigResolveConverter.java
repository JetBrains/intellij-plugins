/*
 * Copyright 2007 The authors
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

package com.intellij.struts2.dom.validator.config;

import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.Nullable;

/**
 * {@code field-validator/validator} "type".
 *
 * @author Yann C&eacute;bron
 */
public abstract class ValidatorConfigResolveConverter extends ResolvingConverter<ValidatorConfig> {

  @Override
  public String toString(@Nullable final ValidatorConfig validatorConfig, final ConvertContext context) {
    return validatorConfig != null ? validatorConfig.getName().getStringValue() : null;
  }

  @Override
  public String getErrorMessage(@Nullable final String s, final ConvertContext context) {
    return "Cannot resolve validator '" + s + "'";
  }

}