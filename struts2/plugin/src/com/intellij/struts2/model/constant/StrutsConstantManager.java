/*
 * Copyright 2010 The authors
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

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.xml.Converter;
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
    new ExtensionPointName<>("com.intellij.struts2.constantContributor");

  public static StrutsConstantManager getInstance(@NotNull final Project project) {
    return project.getService(StrutsConstantManager.class);
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
   * Returns the Converter for the given constant.
   *
   * @param context           Current context.
   * @param strutsConstantKey Key.
   * @param <T>               Converted value type.
   * @return {@code null} if no Converter could be determined.
   */
  @Nullable
  public abstract <T> Converter<T> findConverter(@NotNull final PsiElement context,
                                                 @NotNull final StrutsConstantKey<T> strutsConstantKey);

  /**
   * Determines the constant value for the given name.
   *
   * @param context           Current context.
   * @param strutsConstantKey Constant key.
   * @param <T>               Constant value type.
   * @return Converted value or {@code null} on errors.
   */
  @Nullable
  public abstract <T> T getConvertedValue(@NotNull final PsiElement context,
                                          @NotNull final StrutsConstantKey<T> strutsConstantKey);

}