package org.intellij.errorProne;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.lang.JavaVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.impl.java.JavacCompilerTool;
import org.jetbrains.jps.builders.java.CannotCreateJavaCompilerException;
import org.jetbrains.jps.builders.java.JavaCompilingTool;

import javax.tools.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author nik
 */
public class ErrorProneJavaCompilingTool extends JavaCompilingTool {
  private static final Logger LOG = Logger.getInstance(ErrorProneJavaCompilingTool.class);
  public static final String COMPILER_ID = "error-prone";//duplicates ErrorProneJavaBackendCompiler.COMPILER_ID from IDE part
  private static final String VERSION_PROPERTY = "idea.error.prone.version";//duplicates ErrorProneClasspathProvider.VERSION_PROPERTY
  private static final String COMPILER_PATH_PROPERTY = "idea.error.prone.compiler.path";//duplicates ErrorProneClasspathProvider.COMPILER_PATH_PROPERTY

  @NotNull
  @Override
  public String getId() {
    return COMPILER_ID;
  }

  @NotNull
  @Override
  public String getDescription() {
    String version = System.getProperty(VERSION_PROPERTY);
    return "error-prone compiler " + (version != null ? version : "(unknown version)");
  }

  @NotNull
  @Override
  public JavaCompiler createCompiler() throws CannotCreateJavaCompilerException {
    if (isUnderJava9()) {
      return new JavacCompilerTool().createCompiler();
    }
    try {
      return (JavaCompiler)Class.forName("com.google.errorprone.ErrorProneJavaCompiler").newInstance();
    }
    catch (Exception e) {
      throw new CannotCreateJavaCompilerException(e.getMessage());
    }
  }

  private static boolean isUnderJava9() {
    return JavaVersion.current().feature >= 9;
  }

  @Override
  public boolean isCompilerTreeAPISupported() {
    return isUnderJava9();
  }

  @NotNull
  @Override
  public List<File> getAdditionalClasspath() {
    return Collections.emptyList();
  }

  @Override
  public void preprocessOptions(List<String> options) {
    if (isUnderJava9()) {
      //when running under Java 9 Error Prone should register itself as a plugin, see http://errorprone.info/docs/installation#command-line
      Iterator<String> iterator = options.iterator();
      List<String> errorProneOptions = new ArrayList<>();
      while (iterator.hasNext()) {
        String option = iterator.next();
        if (option.startsWith("-Xep")) {
          iterator.remove();
          errorProneOptions.add(option);
        }
      }
      String compilerPath = System.getProperty(COMPILER_PATH_PROPERTY);
      LOG.assertTrue(compilerPath != null);
      options.add("-processorpath");
      options.add(compilerPath);
      options.add(("-Xplugin:ErrorProne " + StringUtil.join(errorProneOptions, " ")).trim());
    }
  }
}
