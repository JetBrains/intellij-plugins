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

package com.intellij.struts2.dom.validator.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Condition;
import com.intellij.struts2.dom.validator.ValidatorManager;
import com.intellij.struts2.dom.validator.config.ValidatorConfig;
import com.intellij.struts2.dom.validator.config.ValidatorConfigResolveConverter;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 */
public class ValidatorConfigResolveConverterImpl extends ValidatorConfigResolveConverter {

  @Override
  @NotNull
  public Collection<? extends ValidatorConfig> getVariants(final ConvertContext context) {
    final Module module = context.getModule();
    if (module == null) {
      return Collections.emptyList();
    }

    final ValidatorManager validatorManager = ValidatorManager.getInstance(module.getProject());
    return validatorManager.getValidators(module);
  }

  @Override
  public ValidatorConfig fromString(@Nullable @NonNls final String name, final ConvertContext context) {
    if (name == null) {
      return null;
    }

    return ContainerUtil.find(getVariants(context), (Condition<ValidatorConfig>)validatorConfig -> Objects
      .equals(name, validatorConfig.getName().getStringValue()));
  }

}