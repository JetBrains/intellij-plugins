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

package com.intellij.struts2.dom.validator;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.validator.config.ValidatorConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Project-service for accessing validation.xml files and various utility methods.
 *
 * @author Yann C&eacute;bron
 */
public abstract class ValidatorManager {

  public static ValidatorManager getInstance(@NotNull final Project project) {
    return project.getService(ValidatorManager.class);
  }

  /**
   * Checks whether the given file is a valid {@code validation.xml} file.
   *
   * @param xmlFile File to check.
   * @return {@code true} if yes, {@code false} otherwise.
   */
  public abstract boolean isValidatorsFile(@NotNull XmlFile xmlFile);

  /**
   * Gets the available validators from {@code validators.xml}.
   *
   * @param module Current module to search within.
   * @return All available validators.
   */
  public abstract List<ValidatorConfig> getValidators(@NotNull final Module module);

  /**
   * Locates the validator-config.xml for the given module.
   * <p/>
   * Resolves either to
   * <ol>
   * <li>validators.xml in source root path</li>
   * <li>com/opensymphony/xwork2/validator/validators/default.xml from xwork.jar</li>
   * </ol>
   *
   * @param module Module to search within
   * @return configuration file or {@code null} if none was found.
   */
  @Nullable
  public abstract XmlFile getValidatorConfigFile(@NotNull final Module module);

  /**
   * Checks whether the given file is a custom validators.xml.
   *
   * @param psiFile from {@link #getValidatorConfigFile(Module)}.
   * @return true if not default.
   */
  public abstract boolean isCustomValidatorConfigFile(@NotNull PsiFile psiFile);

  /**
   * Finds all corresponding {@code ActionClass-[ActionPath-]validation.xml} files.
   *
   * @param clazz Class.
   * @return Empty list if none found.
   */
  @NotNull
  public abstract List<XmlFile> findValidationFilesFor(@NotNull final PsiClass clazz);

}