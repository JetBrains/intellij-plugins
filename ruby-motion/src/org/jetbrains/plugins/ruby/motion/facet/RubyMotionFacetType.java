/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
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
package org.jetbrains.plugins.ruby.motion.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import icons.RubyIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.RModuleUtil;

import javax.swing.*;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionFacetType extends FacetType<RubyMotionFacet, RubyMotionFacetConfiguration> {
  public static final FacetTypeId<RubyMotionFacet> ID = new FacetTypeId<>("ruby_motion");

  public static RubyMotionFacetType getInstance() {
    return findInstance(RubyMotionFacetType.class);
  }

  private RubyMotionFacetType() {
    super(ID, "ruby_motion", "RubyMotion", null);
  }

  @Override
  public RubyMotionFacetConfiguration createDefaultConfiguration() {
    return new RubyMotionFacetConfiguration();
  }

  @Override
  public RubyMotionFacet createFacet(@NotNull final Module module,
                              final String name,
                              @NotNull final RubyMotionFacetConfiguration configuration,
                              @Nullable final Facet underlyingFacet) {
    return new RubyMotionFacet(this, module, name, configuration, underlyingFacet);
  }

  @Override
  public boolean isSuitableModuleType(ModuleType moduleType) {
    return RModuleUtil.getInstance().isRubyModuleType(moduleType);
  }

  @Override
  public Icon getIcon() {
    // fix me
    return RubyIcons.Ruby.Rubygems;
  }
}
