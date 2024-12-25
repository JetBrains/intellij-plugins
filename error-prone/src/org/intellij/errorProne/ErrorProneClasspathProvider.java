// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.errorProne;

import com.intellij.ReviseWhenPortedToJDK;
import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.compiler.impl.javaCompiler.BackendCompiler;
import com.intellij.compiler.server.BuildManager;
import com.intellij.compiler.server.BuildProcessParametersProvider;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdkVersion;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileFilters;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.ObjectUtils;
import com.intellij.util.text.VersionComparatorUtil;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class ErrorProneClasspathProvider extends BuildProcessParametersProvider {
  private static final Logger LOG = Logger.getInstance(ErrorProneClasspathProvider.class);
  private static final String VERSION_PROPERTY = "idea.error.prone.version";//duplicates ErrorProneJavaCompilingTool.VERSION_PROPERTY
  private static final String COMPILER_PATH_PROPERTY = "idea.error.prone.compiler.path";//duplicates ErrorProneJavaCompilingTool.COMPILER_PATH_PROPERTY
  private final Project myProject;

  public ErrorProneClasspathProvider(Project project) {
    myProject = project;
  }

  public static File getCompilerFilesDir(String version) {
    return new File(getDownloadCacheDir(), version);
  }

  private static @NotNull File getDownloadCacheDir() {
    return new File(PathManager.getSystemPath(), "download-cache/error-prone");
  }

  private static File[] getLatestCompilerJars() {
    File[] files = getDownloadCacheDir().listFiles();
    if (files != null && files.length > 0) {
      return StreamEx.of(files).map(dir -> new Pair<>(dir, getJarFiles(dir)))
        .filter(pair -> pair.second.length > 0)
        .max(Comparator.comparing(pair -> pair.getFirst().getName(), VersionComparatorUtil.COMPARATOR))
        .map(pair -> pair.getSecond())
        .orElse(new File[0]);
    }
    return new File[0];
  }

  public static File[] getJarFiles(File dir) {
    return ObjectUtils.notNull(dir.listFiles(FileFilters.filesWithExtension("jar")), ArrayUtilRt.EMPTY_FILE_ARRAY);
  }

  @Override
  public @NotNull List<String> getVMArguments() {
    if (isErrorProneCompilerSelected(myProject)) {
      File[] jars = getLatestCompilerJars();
      LOG.assertTrue(jars.length > 0, "error-prone compiler jars not found in directory: " + getDownloadCacheDir());
      List<String> bootClasspath = new ArrayList<>();
      List<String> classpath = new ArrayList<>();
      for (File file : jars) {
        if (file.getName().startsWith("javac-")) {
          bootClasspath.add(file.getAbsolutePath());
        }
        else {
          classpath.add(file.getAbsolutePath());
        }
      }
      //in order to run Error Prone we should add special arguments to JVM command line, see http://errorprone.info/docs/installation#command-line
      List<String> arguments = new ArrayList<>();
      arguments.add("-D" + COMPILER_PATH_PROPERTY + "=" + StringUtil.join(classpath, File.pathSeparator));
      if (!BuildManager.getBuildProcessRuntimeSdk(myProject).second.isAtLeast(JavaSdkVersion.JDK_1_9)) {
        arguments.add("-Xbootclasspath/p:" + StringUtil.join(bootClasspath, File.pathSeparator));
      }
      StreamEx.of(jars).map(ErrorProneClasspathProvider::readVersion).nonNull().findFirst().ifPresent(
        version -> arguments.add("-D" + VERSION_PROPERTY + "=" + version)
      );
      return arguments;
    }
    return Collections.emptyList();
  }

  @ReviseWhenPortedToJDK("13")
  private static String readVersion(File jarFile) {
    try {
      try (@SuppressWarnings({"RedundantCast", "RedundantSuppression"}) FileSystem zipFS = FileSystems.newFileSystem(jarFile.toPath(), (ClassLoader)null)) {
        Path propertiesPath = zipFS.getPath("META-INF/maven/com.google.errorprone/error_prone_core/pom.properties");
        if (Files.exists(propertiesPath)) {
          Properties properties = new Properties();
          try (InputStream input = Files.newInputStream(propertiesPath)) {
            properties.load(input);
            return properties.getProperty("version");
          }
        }
      }
    }
    catch (IOException e) {
      LOG.debug(e);
    }
    return null;
  }

  static boolean isErrorProneCompilerSelected(@NotNull Project project) {
    BackendCompiler compiler = ((CompilerConfigurationImpl)CompilerConfiguration.getInstance(project)).getDefaultCompiler();
    return compiler instanceof ErrorProneJavaBackendCompiler;
  }
}
