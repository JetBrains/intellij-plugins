/*
 * Copyright 2015 The authors
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

package com.intellij.struts2.dom.inspection;

import com.intellij.compiler.options.ValidationConfiguration;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.util.InspectionValidator;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Base-class for validators.
 *
 * @author Yann C&eacute;bron
 */
abstract class ValidatorBase extends InspectionValidator {

  protected ValidatorBase(@NonNls @NotNull String id, @NotNull String description,
                          @NotNull String progressIndicatorText) {
    super(id, description, progressIndicatorText);
  }

  protected final boolean isEnabledForModule(final Module module) {
    return ValidationConfiguration.getInstance(module.getProject()).isSelected(getId());
  }

  @Override
  public final boolean isAvailableOnScope(@NotNull final CompileScope scope) {
    for (final Module module : scope.getAffectedModules()) {
      if (isEnabledForModule(module)) {
        return true;
      }
    }

    return false;
  }

}