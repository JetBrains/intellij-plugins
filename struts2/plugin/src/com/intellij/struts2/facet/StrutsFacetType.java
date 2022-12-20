/*
 * Copyright 2013 The authors
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

package com.intellij.struts2.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.struts2.Struts2Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Description-type for {@link StrutsFacet}.
 * Adds autodetection feature for struts.xml files found in project.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsFacetType extends FacetType<StrutsFacet, StrutsFacetConfiguration> {
  StrutsFacetType() {
    super(StrutsFacet.FACET_TYPE_ID, "Struts2", "Struts 2");
  }

  public static FacetType<StrutsFacet, StrutsFacetConfiguration> getInstance() {
    return findInstance(StrutsFacetType.class);
  }

  @Override
  public StrutsFacetConfiguration createDefaultConfiguration() {
    return new StrutsFacetConfiguration();
  }

  @Override
  public StrutsFacet createFacet(@NotNull final Module module,
                                 final String name,
                                 @NotNull final StrutsFacetConfiguration configuration,
                                 @Nullable final Facet underlyingFacet) {
    return new StrutsFacet(this, module, name, configuration, underlyingFacet);
  }

  @Override
  public boolean isSuitableModuleType(final ModuleType moduleType) {
    return moduleType instanceof JavaModuleType;
  }

  @Override
  public Icon getIcon() {
    return Struts2Icons.Action;
  }

  @Override
  public String getHelpTopic() {
    return "reference.settings.project.structure.facets.struts2.facet";
  }
}
