package org.intellij.errorProne;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.compiler.impl.javaCompiler.BackendCompiler;
import com.intellij.compiler.server.BuildProcessParametersProvider;
import com.intellij.openapi.application.PluginPathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author nik
 */
public class ErrorProneClasspathProvider extends BuildProcessParametersProvider {
  private static final Logger LOG = Logger.getInstance(ErrorProneClasspathProvider.class);
  private final Project myProject;

  public ErrorProneClasspathProvider(Project project) {
    myProject = project;
  }

  @NotNull
  @Override
  public List<String> getLauncherClassPath() {
    BackendCompiler compiler = ((CompilerConfigurationImpl)CompilerConfiguration.getInstance(myProject)).getDefaultCompiler();
    if (compiler instanceof ErrorProneJavaBackendCompiler) {
      File pluginClassesRoot = new File(PathUtil.getJarPathForClass(getClass()));
      File libJar;
      if (pluginClassesRoot.isFile()) {
        libJar = new File(pluginClassesRoot.getParentFile(), "jps/error-prone-core-1.1.1.jar");
      }
      else {
        libJar = new File(PluginPathManager.getPluginHome("error-prone"), "lib/error-prone-core-1.1.1.jar");
      }
      LOG.assertTrue(libJar.exists(), "error-prone compiler jar not found: " + libJar.getAbsolutePath());
      return Collections.singletonList(libJar.getAbsolutePath());
    }
    return Collections.emptyList();
  }
}
