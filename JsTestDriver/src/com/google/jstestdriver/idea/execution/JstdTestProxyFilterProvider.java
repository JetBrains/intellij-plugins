package com.google.jstestdriver.idea.execution;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.testframework.sm.runner.TestProxyFilterProvider;
import com.intellij.javascript.testFramework.util.BrowserStacktraceFilter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
* @author Sergey Simonchik
*/
public class JstdTestProxyFilterProvider implements TestProxyFilterProvider {

  private static final String DEFAULT_PATH_PREFIX = "/test/";

  private final Project myProject;

  public JstdTestProxyFilterProvider(@NotNull Project project) {
    myProject = project;
  }

  @Nullable
  @Override
  public Filter getFilter(@NotNull String nodeType, @NotNull String nodeName, @Nullable String nodeArguments) {
    if ("browser".equals(nodeType) && nodeArguments != null) {
      final File basePath = new File(nodeArguments);
      if (basePath.isAbsolute() && basePath.isDirectory()) {
        Function<String, File> fileFinder = path -> findFileByPath(basePath, path);
        return new BrowserStacktraceFilter(myProject, nodeName, fileFinder);
      }
    }
    if ("browserError".equals(nodeType) && nodeArguments != null) {
      File basePath = new File(nodeArguments);
      if (basePath.isDirectory() && basePath.isAbsolute()) {
        return new JsErrorFilter(myProject, basePath);
      }
    }
    return null;
  }

  @Nullable
  private static File findFileByPath(@NotNull File basePath, @NotNull String urlStr) {
    File file = findFileByBasePath(basePath, urlStr);
    if (file != null) {
      return file;
    }
    try {
      URL url = new URL(urlStr);
      String path = url.getPath();
      path = StringUtil.trimStart(path, DEFAULT_PATH_PREFIX);
      return findFileByBasePath(basePath, path);
    } catch (MalformedURLException ignored) {
    }
    return null;
  }

  @Nullable
  private static File findFileByBasePath(@NotNull File basePath, @NotNull String subPath) {
    File file = new File(basePath, subPath);
    if (!file.isFile()) {
      File absoluteFile = new File(subPath);
      if (absoluteFile.isAbsolute() && absoluteFile.isFile()) {
        file = absoluteFile;
      }
    }
    return file.isFile() ? file : null;
  }

}
