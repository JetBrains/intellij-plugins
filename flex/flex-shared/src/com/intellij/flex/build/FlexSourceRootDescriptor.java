package com.intellij.flex.build;

import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.impl.BuildRootDescriptorImpl;

import java.io.File;

class FlexSourceRootDescriptor extends BuildRootDescriptorImpl {
  FlexSourceRootDescriptor(final BuildTarget target, final File root) {
    super(target, root);
  }
}
