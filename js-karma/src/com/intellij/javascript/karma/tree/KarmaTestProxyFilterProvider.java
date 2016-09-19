package com.intellij.javascript.karma.tree;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.testframework.sm.runner.TestProxyFilterProvider;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.testFramework.util.BrowserStacktraceFilters;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    KarmaConfig config = myKarmaServer.getKarmaConfig();
    String baseDir = config != null ? config.getBasePath() : null;
    if ("browser".equals(nodeType)) {
      return BrowserStacktraceFilters.createFilter(nodeName, myProject, baseDir);
    }
    if ("browserError".equals(nodeType)) {
      return getBrowserErrorFilter();
    }
    return null;
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
