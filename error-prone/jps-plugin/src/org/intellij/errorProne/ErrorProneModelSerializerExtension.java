package org.intellij.errorProne;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.serialization.JpsModelSerializerExtension;
import org.jetbrains.jps.model.serialization.JpsProjectExtensionSerializer;
import org.jetbrains.jps.model.serialization.java.compiler.JpsJavaCompilerOptionsSerializer;

import java.util.Collections;
import java.util.List;

public class ErrorProneModelSerializerExtension extends JpsModelSerializerExtension {
  @NotNull
  @Override
  public List<? extends JpsProjectExtensionSerializer> getProjectExtensionSerializers() {
    return Collections.singletonList(new JpsJavaCompilerOptionsSerializer("ErrorProneCompilerSettings", ErrorProneJavaCompilingTool.COMPILER_ID));
  }
}
