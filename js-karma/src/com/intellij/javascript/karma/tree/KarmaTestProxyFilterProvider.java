package com.intellij.javascript.karma.tree;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.testframework.sm.runner.TestProxyFilterProvider;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.nodejs.NodeStackTraceFilter;
import com.intellij.javascript.testFramework.util.BrowserStacktraceFilter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class KarmaTestProxyFilterProvider implements TestProxyFilterProvider {

  private final Project myProject;
  private final KarmaServer myKarmaServer;

  public KarmaTestProxyFilterProvider(@NotNull Project project, @NotNull KarmaServer karmaServer) {
    myProject = project;
    myKarmaServer = karmaServer;
  }

  @Nullable
  @Override
  public Filter getFilter(@NotNull String nodeType, @NotNull String nodeName, @Nullable String nodeArguments) {
    if ("browser".equals(nodeType)) {
      return getBrowserFilter(nodeName);
    }
    if ("browserError".equals(nodeType)) {
      return getBrowserErrorFilter();
    }
    return null;
  }

  @NotNull
  private Filter getBrowserFilter(@NotNull String browserName) {
    if (StringUtil.startsWithIgnoreCase(browserName, "PhantomJS")) {
      return new NodeStackTraceFilter(myProject);
    }
    Function<String, File> fileFinder = s -> {
      File file = new File(s);
      if (file.isFile() && file.isAbsolute()) {
        return file;
      }
      KarmaConfig karmaConfig = myKarmaServer.getKarmaConfig();
      if (karmaConfig != null) {
        String basePath = karmaConfig.getBasePath();
        File baseDir = new File(basePath);
        if (baseDir.isDirectory()) {
          file = new File(baseDir, s);
          if (file.isFile()) {
            return file;
          }
        }
      }
      return null;
    };
    return new BrowserStacktraceFilter(myProject, browserName, fileFinder);
  }

  @Nullable
  private Filter getBrowserErrorFilter() {
    KarmaConfig karmaConfig = myKarmaServer.getKarmaConfig();
    if (karmaConfig != null) {
      return new KarmaBrowserErrorFilter(myProject, karmaConfig);
    }
    return null;
  }

}
