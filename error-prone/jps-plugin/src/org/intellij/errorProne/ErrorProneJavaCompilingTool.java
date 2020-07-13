package org.intellij.errorProne;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
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
    return new JavacCompilerTool().createCompiler();
  }

  @Override
  public boolean isCompilerTreeAPISupported() {
    return true;
  }

  @NotNull
  @Override
  public List<File> getAdditionalClasspath() {
    return Collections.emptyList();
  }

  @Override
  public void preprocessOptions(List<String> options) {
    //Error Prone should register itself as a plugin, see http://errorprone.info/docs/installation#command-line
    Iterator<String> iterator = options.iterator();
    List<String> errorProneOptions = new ArrayList<>();
    String processorPathOption = null;
    while (iterator.hasNext()) {
      String option = iterator.next();
      if (option.startsWith("-Xep")) {
        iterator.remove();
        errorProneOptions.add(option);
      }
      if (option.equals("-processorpath")) {
        iterator.remove();
        if (iterator.hasNext()) {
          processorPathOption = iterator.next();
          iterator.remove();
        }
      }
    }

    String compilerPathProperty = System.getProperty(COMPILER_PATH_PROPERTY);
    LOG.assertTrue(compilerPathProperty != null);
    StringBuilder compilerPath = new StringBuilder(compilerPathProperty);
    if (processorPathOption != null) {
      compilerPath.append(File.pathSeparator).append(processorPathOption);
    }

    options.add("-XDcompilePolicy=simple");
    options.add("-processorpath");
    options.add(compilerPath.toString());
    options.add(("-Xplugin:ErrorProne " + StringUtil.join(errorProneOptions, " ")).trim());
  }
}
