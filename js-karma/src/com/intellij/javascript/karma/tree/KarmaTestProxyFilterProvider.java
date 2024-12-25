// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.tree;

import com.intellij.execution.filters.AbstractFileHyperlinkFilter;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.testframework.sm.runner.TestProxyFilterProvider;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.filter.KarmaBrowserErrorFilter;
import com.intellij.javascript.karma.filter.KarmaSourceMapStacktraceFilter;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.testFramework.util.BrowserStacktraceFilters;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KarmaTestProxyFilterProvider implements TestProxyFilterProvider {

  private final Project myProject;
  private final KarmaServer myKarmaServer;

  public KarmaTestProxyFilterProvider(@NotNull Project project, @Nullable KarmaServer karmaServer) {
    myProject = project;
    myKarmaServer = karmaServer;
  }

  @Override
  public @Nullable Filter getFilter(@NotNull String nodeType, @NotNull String nodeName, @Nullable String nodeArguments) {
    if ("browser".equals(nodeType)) {
      String baseDir = myKarmaServer == null ? null : myKarmaServer.getServerSettings().getWorkingDirectorySystemDependent();
      AbstractFileHyperlinkFilter browserFilter = BrowserStacktraceFilters.createFilter(nodeName, myProject, baseDir);
      if (browserFilter != null) {
        return new KarmaSourceMapStacktraceFilter(myProject, baseDir, browserFilter);
      }
    }
    if ("browserError".equals(nodeType)) {
      return getBrowserErrorFilter();
    }
    return null;
  }

  private @Nullable String getBasePath() {
    if (myKarmaServer != null) {
      KarmaConfig karmaConfig = myKarmaServer.getKarmaConfig();
      if (karmaConfig != null) {
        return karmaConfig.getBasePath();
      }
      return myKarmaServer.getServerSettings().getWorkingDirectorySystemDependent();
    }
    return null;
  }

  private @Nullable Filter getBrowserErrorFilter() {
    String basePath = getBasePath();
    return basePath != null ? new KarmaBrowserErrorFilter(myProject, basePath) : null;
  }
}
