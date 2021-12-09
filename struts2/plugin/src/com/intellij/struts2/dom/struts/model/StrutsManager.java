/*
 * Copyright 2019 The authors
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

package com.intellij.struts2.dom.struts.model;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.facet.ui.StrutsFileSet;
import com.intellij.util.xml.model.impl.BaseDomModelFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Project-service for accessing {@link StrutsModel} and various utility methods.
 *
 * @author Yann C&eacute;bron
 */
public abstract class StrutsManager {

  public static StrutsManager getInstance(@NotNull final Project project) {
    return project.getService(StrutsManager.class);
  }

  /**
   * Checks whether the given file is a valid {@code struts.xml} file.
   *
   * @param xmlFile File to check.
   * @return {@code true} if yes, {@code false} otherwise.
   */
  public abstract boolean isStruts2ConfigFile(@NotNull XmlFile xmlFile);

  /**
   * Gets the model using the given file.
   *
   * @param file File to resolve context.
   * @return {@code null} if no model available.
   * @see com.intellij.util.xml.model.impl.DomModelFactory#getModelByConfigFile(XmlFile)
   */
  @Nullable
  public abstract StrutsModel getModelByFile(@NotNull final XmlFile file);

  /**
   * Gets all models.
   *
   * @param module Module.
   * @return List of all models.
   * @see BaseDomModelFactory#getAllModels(UserDataHolder)
   */
  @NotNull
  public abstract List<StrutsModel> getAllModels(@NotNull Module module);

  /**
   * Gets the combined model.
   *
   * @param module Module.
   * @return Combined model.
   * @see BaseDomModelFactory#getCombinedModel(UserDataHolder)
   */
  @Nullable
  public abstract StrutsModel getCombinedModel(@Nullable final Module module);

  /**
   * @param psiElement To determine module.
   * @return Combined model
   * @see #getCombinedModel(Module)
   */
  @Nullable
  public abstract StrutsModel getCombinedModel(@NotNull final PsiElement psiElement);

  /**
   * Gets all configured filesets from current facet setup.
   *
   * @param module Module to get filesets for.
   * @return All configured filesets (can be empty).
   */
  @NotNull
  public abstract Set<StrutsFileSet> getAllConfigFileSets(@NotNull final Module module);
}