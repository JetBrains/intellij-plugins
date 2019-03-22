// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
    super(ID, "ruby_motion", "RubyMotion");
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
