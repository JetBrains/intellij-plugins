package com.intellij.jps.flex.build;

import com.intellij.flex.model.module.JpsFlexModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.incremental.resources.ResourceBuilderExtension;
import org.jetbrains.jps.model.module.JpsModule;

public class FlexResourceBuilderExtension extends ResourceBuilderExtension {
  public boolean skipStandardResourceCompiler(final @NotNull JpsModule module) {
    return module.getModuleType() == JpsFlexModuleType.INSTANCE;
  }
}
