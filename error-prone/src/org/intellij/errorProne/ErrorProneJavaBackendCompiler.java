// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.errorProne;

import com.intellij.compiler.impl.javaCompiler.BackendCompiler;
import com.intellij.compiler.impl.javaCompiler.javac.JavacConfigurable;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.compiler.CompilerOptions;

import java.util.Collections;
import java.util.Set;

public final class ErrorProneJavaBackendCompiler implements BackendCompiler {
  public static final String COMPILER_ID = "error-prone";//duplicates ErrorProneJavaCompilingTool.COMPILER_ID from JPS part
  private final Project myProject;

  public ErrorProneJavaBackendCompiler(Project project) {
    myProject = project;
  }

  @Override
  public @NotNull String getId() {
    return COMPILER_ID;
  }

  @Override
  public @NotNull String getPresentableName() {
    return ErrorProneBundle.message("compiler.name.javac.with.error.prone");
  }

  @Override
  public @NotNull Configurable createConfigurable() {
    return new JavacConfigurable(myProject, ErrorProneCompilerConfiguration.getOptions(myProject));
  }

  @Override
  public @NotNull Set<FileType> getCompilableFileTypes() {
    return Collections.singleton(JavaFileType.INSTANCE);
  }

  @Override
  public @NotNull CompilerOptions getOptions() {
    return ErrorProneCompilerConfiguration.getOptions(myProject);
  }
}
