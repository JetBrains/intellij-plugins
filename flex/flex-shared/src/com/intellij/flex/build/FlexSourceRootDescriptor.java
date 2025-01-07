// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.build;

import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.impl.BuildRootDescriptorImpl;

import java.io.File;

final class FlexSourceRootDescriptor extends BuildRootDescriptorImpl {
  FlexSourceRootDescriptor(BuildTarget<?> target, File root) {
    super(target, root);
  }
}
