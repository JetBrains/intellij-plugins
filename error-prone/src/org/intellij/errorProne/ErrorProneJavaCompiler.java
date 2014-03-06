package org.intellij.errorProne;

import com.intellij.compiler.OutputParser;
import com.intellij.compiler.impl.javaCompiler.BackendCompiler;
import com.intellij.compiler.impl.javaCompiler.ModuleChunk;
import com.intellij.compiler.impl.javaCompiler.javac.JavacConfigurable;
import com.intellij.compiler.impl.javaCompiler.javac.JavacConfiguration;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * @author nik
 */
public class ErrorProneJavaCompiler implements BackendCompiler {
  private Project myProject;

  public ErrorProneJavaCompiler(Project project) {
    myProject = project;
  }

  @NotNull
  @Override
  public String getId() {
    return "error-prone";
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return "Javac with error-prone";
  }

  @NotNull
  @Override
  public Configurable createConfigurable() {
    return new JavacConfigurable(JavacConfiguration.getOptions(myProject, JavacConfiguration.class));
  }

  @NotNull
  @Override
  public Set<FileType> getCompilableFileTypes() {
    return Collections.<FileType>singleton(StdFileTypes.JAVA);
  }

  @Nullable
  @Override
  public OutputParser createErrorParser(@NotNull String outputDir, Process process) {
    return null;
  }

  @Nullable
  @Override
  public OutputParser createOutputParser(@NotNull String outputDir) {
    return null;
  }

  @Override
  public boolean checkCompiler(CompileScope scope) {
    return true;
  }

  @NotNull
  @Override
  public Process launchProcess(@NotNull ModuleChunk chunk, @NotNull String outputDir, @NotNull CompileContext compileContext)
    throws IOException {
    throw new UnsupportedOperationException("Obsolete 'in-process' mode is not supported by error-prone compiler");
  }

  @Override
  public void compileFinished() {
  }
}
