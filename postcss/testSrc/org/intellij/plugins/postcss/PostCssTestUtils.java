package org.intellij.plugins.postcss;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;

public final class PostCssTestUtils {
  private static final String DIR_MARKER = "org/intellij/plugins/postcss";

  @NotNull
  public static String getFullTestDataPath(@NotNull final Class<?> clazz) {
    final String classFullPath = getClassFullPath(clazz);
    final String packagePath = classFullPath.substring(0, classFullPath.lastIndexOf('/'));
    return FileUtil.join(getRootTestDataPath(), packagePath.substring(packagePath.indexOf(DIR_MARKER) + DIR_MARKER.length()));
  }

  @NotNull
  public static String getRootTestDataPath() {
    return FileUtil.join(IdeaTestExecutionPolicy.getHomePathWithPolicy(), "contrib", "postcss", "testData");
  }

  /**
   * Return relative path to the test data. Path is relative to the
   * {@link PathManager#getHomePath()}
   *
   * @return relative path to the test data.
   */
  @NotNull
  public static String getTestDataBasePath(@NotNull final Class<?> clazz) {
    return "/" + StringUtil.notNullize(FileUtil.getRelativePath(IdeaTestExecutionPolicy.getHomePathWithPolicy(), getFullTestDataPath(clazz), File.separatorChar));
  }

  @NotNull
  private static String getClassFullPath(@NotNull final Class<?> clazz) {
    String name = clazz.getSimpleName() + ".class";
    final URL url = clazz.getResource(name);
    return url.getPath();
  }


  private PostCssTestUtils() {
  }
}