package com.intellij.jps.flex.build;

import com.intellij.flex.build.FlexBuildTargetType;
import com.intellij.flex.build.FlexResourceBuildTargetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.incremental.BuilderService;
import org.jetbrains.jps.incremental.TargetBuilder;

import java.util.Arrays;
import java.util.List;

public class FlexBuilderService extends BuilderService {

  public List<? extends BuildTargetType<?>> getTargetTypes() {
    return Arrays.asList(FlexResourceBuildTargetType.PRODUCTION, FlexResourceBuildTargetType.TEST, FlexBuildTargetType.INSTANCE);
  }

  @NotNull
  public List<? extends TargetBuilder<?, ?>> createBuilders() {
    return Arrays.asList(new FlexResourceBuilder(), new FlexBuilder());
  }
}
