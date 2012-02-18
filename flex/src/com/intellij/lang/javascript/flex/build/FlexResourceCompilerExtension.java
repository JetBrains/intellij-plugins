package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.impl.resourceCompiler.ResourceCompilerExtension;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;

public class FlexResourceCompilerExtension extends ResourceCompilerExtension {

  public boolean skipStandardResourceCompiler(final @NotNull Module module) {
    if (ModuleType.get(module) == FlexModuleType.getInstance()) {
      return true;
    }
    return super.skipStandardResourceCompiler(module);
  }
}
