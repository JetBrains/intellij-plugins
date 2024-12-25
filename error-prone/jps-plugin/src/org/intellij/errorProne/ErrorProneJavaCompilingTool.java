// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

  @Override
  public @NotNull String getId() {
    return COMPILER_ID;
  }

  @Override
  public @NotNull String getDescription() {
    String version = System.getProperty(VERSION_PROPERTY);
    return "error-prone compiler " + (version != null ? version : "(unknown version)");
  }

  @Override
  public @NotNull JavaCompiler createCompiler() throws CannotCreateJavaCompilerException {
    return new JavacCompilerTool().createCompiler();
  }

  @Override
  public boolean isCompilerTreeAPISupported() {
    return true;
  }

  @Override
  public @NotNull List<File> getAdditionalClasspath() {
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
      else if (option.equals("-processorpath")) {
        iterator.remove();
        if (iterator.hasNext()) {
          processorPathOption = iterator.next();
          iterator.remove();
        }
      }
    }

    String compilerPath = getCompilerPath(processorPathOption);

    options.add("-XDcompilePolicy=simple");
    options.add("-processorpath");
    options.add(compilerPath);
    options.add(("-Xplugin:ErrorProne " + StringUtil.join(errorProneOptions, " ")).trim());
  }

  protected @NotNull String getCompilerPath(String processorPathOption) {
    String compilerPathProperty = System.getProperty(COMPILER_PATH_PROPERTY);
    LOG.assertTrue(compilerPathProperty != null);
    if (processorPathOption == null || processorPathOption.isEmpty()) {
      return compilerPathProperty;
    }
    StringBuilder compilerPath = new StringBuilder(compilerPathProperty);
    if (!compilerPathProperty.endsWith(File.pathSeparator)) {
      compilerPath.append(File.pathSeparator);
    }
    compilerPath.append(processorPathOption);
    return compilerPath.toString();
  }
}
