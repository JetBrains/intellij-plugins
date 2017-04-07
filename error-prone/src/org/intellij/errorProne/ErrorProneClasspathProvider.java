package org.intellij.errorProne;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.compiler.impl.javaCompiler.BackendCompiler;
import com.intellij.compiler.server.BuildProcessParametersProvider;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileFilters;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.ObjectUtils;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author nik
 */
public class ErrorProneClasspathProvider extends BuildProcessParametersProvider {
  private static final Logger LOG = Logger.getInstance(ErrorProneClasspathProvider.class);
  public static final String ERROR_PRONE_VERSION = "2.0.19";//must be consistent with library/error-prone.xml
  private static final String VERSION_PROPERTY = "idea.error.prone.version";//duplicates ErrorProneJavaCompilingTool.VERSION_PROPERTY
  private final Project myProject;

  public ErrorProneClasspathProvider(Project project) {
    myProject = project;
  }

  public static File getCompilerFilesDir() {
    return new File(PathManager.getSystemPath(), "download-cache/error-prone/" + ERROR_PRONE_VERSION);
  }

  public static File[] getJarFiles(File dir) {
    return ObjectUtils.notNull(dir.listFiles(FileFilters.filesWithExtension("jar")), ArrayUtilRt.EMPTY_FILE_ARRAY);
  }

  @NotNull
  @Override
  public List<String> getVMArguments() {
    if (isErrorProneCompilerSelected(myProject)) {
      File libDir = getCompilerFilesDir();
      File[] jars = getJarFiles(libDir);
      LOG.assertTrue(jars.length > 0, "error-prone compiler jars not found in directory: " + libDir.getAbsolutePath());
      List<String> classpath = new ArrayList<>();
      for (File file : jars) {
        classpath.add(file.getAbsolutePath());
      }
      List<String> arguments = new ArrayList<>();
      arguments.add("-Xbootclasspath/a:" + StringUtil.join(classpath, File.pathSeparator));
      StreamEx.of(jars).map(ErrorProneClasspathProvider::readVersion).nonNull().findFirst().ifPresent(
        version -> arguments.add("-D" + VERSION_PROPERTY + "=" + version)
      );
      return arguments;
    }
    return Collections.emptyList();
  }

  private static String readVersion(File jarFile) {
    try {
      try (FileSystem zipFS = FileSystems.newFileSystem(jarFile.toPath(), null)) {
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
