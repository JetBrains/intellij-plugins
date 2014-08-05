package org.jetbrains.jps.osmorc.build;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.TargetBuilder;

import java.io.IOException;
import java.util.Collections;

/**
 * @author michael.golubev
 */
public class OsmorcBuilder extends TargetBuilder<BuildRootDescriptor, OsmorcBuildTarget> {
  public static final String ID = "osgi";

  public OsmorcBuilder() {
    super(Collections.singletonList(OsmorcBuildTargetType.INSTANCE));
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return ID;
  }

  @Override
  public void build(@NotNull OsmorcBuildTarget target,
                    @NotNull DirtyFilesHolder<BuildRootDescriptor, OsmorcBuildTarget> holder,
                    @NotNull BuildOutputConsumer outputConsumer,
                    @NotNull CompileContext context) throws ProjectBuildException, IOException {
    new OsgiBuildSession().build(target, context);
  }
}
