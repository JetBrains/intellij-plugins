// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.errorProne;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.serialization.JpsModelSerializerExtension;
import org.jetbrains.jps.model.serialization.JpsProjectExtensionSerializer;
import org.jetbrains.jps.model.serialization.java.compiler.JpsJavaCompilerOptionsSerializer;

import java.util.Collections;
import java.util.List;

public final class ErrorProneModelSerializerExtension extends JpsModelSerializerExtension {
  @Override
  public @NotNull List<? extends JpsProjectExtensionSerializer> getProjectExtensionSerializers() {
    return Collections.singletonList(new JpsJavaCompilerOptionsSerializer("ErrorProneCompilerSettings", ErrorProneJavaCompilingTool.COMPILER_ID));
  }
}
