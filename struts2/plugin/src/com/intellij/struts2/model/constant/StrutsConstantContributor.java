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

import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Contributes (plugin-specific) entries for {@code <constant>}.
 *
 * @author Yann C&eacute;bron
 */
public interface StrutsConstantContributor {

  /**
   * Returns whether the contributor is available for the given module.
   *
   * @param module Module.
   * @return {@code true} if contributor is available.
   */
  boolean isAvailable(@NotNull final Module module);

  /**
   * Returns the definitions for the given module.
   *
   * @param module Module.
   * @return Definitions, can be empty.
   */
  @NotNull
  List<StrutsConstant> getStrutsConstantDefinitions(@NotNull Module module);

}