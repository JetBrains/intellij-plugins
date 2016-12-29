package org.intellij.errorProne;

import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.java.CannotCreateJavaCompilerException;
import org.jetbrains.jps.builders.java.JavaCompilingTool;

import javax.tools.JavaCompiler;
import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author nik
 */
public class ErrorProneJavaCompilingTool extends JavaCompilingTool {
  public static final String COMPILER_ID = "error-prone";//duplicates ErrorProneJavaBackendCompiler.COMPILER_ID from IDE part

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
    if (!SystemInfo.isJavaVersionAtLeast("1.8")) {
      throw new CannotCreateJavaCompilerException("Error-prone compiler requires JDK 1.8 to run");
    }
    try {
      return (JavaCompiler)Class.forName("com.google.errorprone.ErrorProneJavaCompiler").newInstance();
    }
    catch (Exception e) {
      throw new CannotCreateJavaCompilerException(e.getMessage());
    }
  }

  @NotNull
  @Override
  public List<File> getAdditionalClasspath() {
    return Collections.emptyList();
  }
}
