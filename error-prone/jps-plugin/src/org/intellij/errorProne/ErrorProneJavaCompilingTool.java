package org.intellij.errorProne;

import com.google.errorprone.ErrorProneOptions;
import com.google.errorprone.ErrorProneScanner;
import com.google.errorprone.ErrorReportingJavaCompiler;
import com.google.errorprone.Scanner;
import com.intellij.util.ArrayUtil;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.util.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.java.CannotCreateJavaCompilerException;
import org.jetbrains.jps.builders.java.JavaCompilingTool;

import javax.tools.*;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author nik
 */
public class ErrorProneJavaCompilingTool extends JavaCompilingTool {
  @NotNull
  @Override
  public String getId() {
    return "error-prone";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "error-prone compiler";
  }

  @NotNull
  @Override
  public JavaCompiler createCompiler() throws CannotCreateJavaCompilerException {
    return ToolProvider.getSystemJavaCompiler();
  }

  @NotNull
  @Override
  public List<File> getAdditionalClasspath() {
    return Collections.emptyList();
  }

  @Override
  public void prepareCompilationTask(@NotNull JavaCompiler.CompilationTask task, @NotNull Collection<String> options) {
    try {
      ErrorProneOptions errorProneOptions = ErrorProneOptions.processArgs(ArrayUtil.toStringArray(options));
      Context context = ((JavacTaskImpl)task).getContext();
      ErrorProneScanner scanner = new ErrorProneScanner(ErrorProneScanner.EnabledPredicate.DEFAULT_CHECKS);
      Method setDisabledChecks = ErrorProneScanner.class.getDeclaredMethod("setDisabledChecks", Set.class);
      setDisabledChecks.setAccessible(true);
      setDisabledChecks.invoke(scanner, errorProneOptions.getDisabledChecks());
      context.put(Scanner.class, scanner);
      ErrorReportingJavaCompiler.preRegister(context);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
