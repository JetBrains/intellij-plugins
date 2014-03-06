package org.intellij.errorProne;

import com.google.errorprone.ErrorProneOptions;
import com.google.errorprone.ErrorProneScanner;
import com.google.errorprone.ErrorReportingJavaCompiler;
import com.google.errorprone.Scanner;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.util.Context;

import javax.lang.model.SourceVersion;
import javax.tools.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author nik
 */
public class ErrorProneJavaCompiler implements JavaCompiler {
  private final JavaCompiler myJavacTool;

  public ErrorProneJavaCompiler() {
    myJavacTool = ToolProvider.getSystemJavaCompiler();
  }

  @Override
  public CompilationTask getTask(Writer out,
                                 JavaFileManager fileManager,
                                 DiagnosticListener<? super JavaFileObject> diagnosticListener,
                                 Iterable<String> options,
                                 Iterable<String> classes,
                                 Iterable<? extends JavaFileObject> compilationUnits) {
    ErrorProneOptions errorProneOptions = ErrorProneOptions.processArgs(ArrayUtil.toStringArray(ContainerUtil.toCollection(options)));
    List<String> remainingOptions = Arrays.asList(errorProneOptions.getRemainingArgs());
    CompilationTask task = myJavacTool.getTask(out, fileManager, diagnosticListener, remainingOptions, classes, compilationUnits);
    Context context = ((JavacTaskImpl)task).getContext();
    try {
      ErrorProneScanner scanner = new ErrorProneScanner(ErrorProneScanner.EnabledPredicate.DEFAULT_CHECKS);
      Method setDisabledChecks = ErrorProneScanner.class.getDeclaredMethod("setDisabledChecks", Set.class);
      setDisabledChecks.setAccessible(true);
      setDisabledChecks.invoke(scanner, errorProneOptions.getDisabledChecks());
      context.put(Scanner.class, scanner);
    }
    catch (InvocationTargetException e) {
      throw new RuntimeException(e.getTargetException());
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    ErrorReportingJavaCompiler.preRegister(context);
    return task;
  }

  @Override
  public StandardJavaFileManager getStandardFileManager(DiagnosticListener<? super JavaFileObject> diagnosticListener,
                                                        Locale locale,
                                                        Charset charset) {
    return myJavacTool.getStandardFileManager(diagnosticListener, locale, charset);
  }

  @Override
  public int isSupportedOption(String option) {
    int numberOfArgs = myJavacTool.isSupportedOption(option);
    if (numberOfArgs != -1) return numberOfArgs;

    try {
      Field prefixField = ErrorProneOptions.class.getDeclaredField("DISABLE_FLAG_PREFIX");
      prefixField.setAccessible(true);
      return option.startsWith((String)prefixField.get(null)) ? 0 : -1;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int run(InputStream in, OutputStream out, OutputStream err, String... arguments) {
    return myJavacTool.run(in, out, err, arguments);
  }

  @Override
  public Set<SourceVersion> getSourceVersions() {
    Set<SourceVersion> filtered = EnumSet.noneOf(SourceVersion.class);
    for (SourceVersion version : myJavacTool.getSourceVersions()) {
      if (version.compareTo(SourceVersion.RELEASE_6) >= 0) {
        filtered.add(version);
      }
    }
    return filtered;
  }
}
