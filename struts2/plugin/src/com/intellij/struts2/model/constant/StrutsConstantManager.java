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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Provides access to all defined constant configuration properties.
 *
 * @author Yann C&eacute;bron
 */
public abstract class StrutsConstantManager {

  /**
   * EP for contributing plugin-specific {@link StrutsConstant}s.
   */
  public static final ExtensionPointName<StrutsConstantContributor> EP_NAME =
      new ExtensionPointName<StrutsConstantContributor>("com.intellij.struts2.constantContributor");

  public static StrutsConstantManager getInstance(final Project project) {
    return ServiceManager.getService(project, StrutsConstantManager.class);
  }

  /**
   * Returns all defined constants for the given module.
   *
   * @param module Module.
   * @return Constants.
   */
  @NotNull
  public abstract List<StrutsConstant> getConstants(@NotNull final Module module);

  /**
   * Returns the constant for the given name.
   *
   * @param module Module.
   * @param name   Constant name.
   * @return Definition or {@code null} if unknown.
   */
  @Nullable
  public abstract StrutsConstant findByName(@NotNull final Module module,
                                            @NotNull @NonNls final String name);

  /**
   * Determines the constant value for the given name.
   *
   * @param context Current context.
   * @param name    Constant name.
   * @return Value or {@code null} if no value could be determined.
   */
  @Nullable
  public abstract String getValue(@NotNull final PsiFile context,
                                  @NotNull @NonNls final String name);
}
