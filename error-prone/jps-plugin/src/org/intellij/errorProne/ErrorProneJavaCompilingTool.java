package org.intellij.errorProne;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.java.CannotCreateJavaCompilerException;
import org.jetbrains.jps.builders.java.JavaCompilingTool;

import javax.tools.*;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author nik
 */
public class ErrorProneJavaCompilingTool extends JavaCompilingTool {
  public static final String COMPILER_ID = "error-prone";

  @NotNull
  @Override
  public String getId() {
    return COMPILER_ID;
  }

  @NotNull
  @Override
  public String getDescription() {
    return "error-prone compiler";
  }

  @NotNull
  @Override
  public JavaCompiler createCompiler() throws CannotCreateJavaCompilerException {
    return new ErrorProneJavaCompiler();
  }

  @NotNull
  @Override
  public List<File> getAdditionalClasspath() {
    return Collections.emptyList();
  }

  @Override
  public void prepareCompilationTask(@NotNull JavaCompiler.CompilationTask task, @NotNull Collection<String> options) {
  }
}
