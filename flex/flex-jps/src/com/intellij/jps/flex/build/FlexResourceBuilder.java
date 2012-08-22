package com.intellij.jps.flex.build;

import com.intellij.jps.flex.model.module.JpsFlexModuleType;
import com.intellij.jps.flex.model.bc.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.incremental.BuilderCategory;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ModuleLevelBuilder;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.model.library.JpsLibrary;
import org.jetbrains.jps.model.library.sdk.JpsSdk;
import org.jetbrains.jps.model.module.JpsModule;

public class FlexResourceBuilder extends ModuleLevelBuilder {

  private static final @NonNls String BUILDER_NAME = "flex-resource";

  protected FlexResourceBuilder() {
    super(BuilderCategory.SOURCE_PROCESSOR);
  }

  public String getName() {
    return BUILDER_NAME;
  }

  public String getDescription() {
    return "Flex Resource Builder";
  }

  public ExitCode build(final CompileContext context, final ModuleChunk chunk) throws ProjectBuildException {
    //todo implement
    return ExitCode.OK;
  }
}
