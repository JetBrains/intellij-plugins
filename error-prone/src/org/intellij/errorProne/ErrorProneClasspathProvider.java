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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author nik
 */
public class ErrorProneClasspathProvider extends BuildProcessParametersProvider {
  private static final Logger LOG = Logger.getInstance(ErrorProneClasspathProvider.class);
  public static final String ERROR_PRONE_VERSION = "2.0.19";//must be consistent with library/error-prone.xml
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
      return Collections.singletonList("-Xbootclasspath/a:" + StringUtil.join(classpath, File.pathSeparator));
    }
    return Collections.emptyList();
  }

  static boolean isErrorProneCompilerSelected(@NotNull Project project) {
    BackendCompiler compiler = ((CompilerConfigurationImpl)CompilerConfiguration.getInstance(project)).getDefaultCompiler();
    return compiler instanceof ErrorProneJavaBackendCompiler;
  }
}
