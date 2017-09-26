package org.intellij.errorProne;

import com.intellij.compiler.impl.javaCompiler.BackendCompiler;
import com.intellij.compiler.impl.javaCompiler.javac.JavacConfigurable;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

/**
 * @author nik
 */
public class ErrorProneJavaBackendCompiler implements BackendCompiler {
  public static final String COMPILER_ID = "error-prone";//duplicates ErrorProneJavaCompilingTool.COMPILER_ID from JPS part
  private final Project myProject;

  public ErrorProneJavaBackendCompiler(Project project) {
    myProject = project;
  }

  @NotNull
  @Override
  public String getId() {
    return COMPILER_ID;
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return "Javac with error-prone";
  }

  @NotNull
  @Override
  public Configurable createConfigurable() {
    return new JavacConfigurable(myProject, ErrorProneCompilerConfiguration.getOptions(myProject));
  }

  @NotNull
  @Override
  public Set<FileType> getCompilableFileTypes() {
    return Collections.singleton(StdFileTypes.JAVA);
  }
}
