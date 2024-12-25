// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.execution;

import com.intellij.execution.Executor;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.actions.AbstractRerunFailedTestsAction;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.execution.testframework.sm.runner.TestProxyFilterProvider;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.javascript.karma.tree.KarmaTestProxyFilterProvider;
import com.intellij.javascript.testFramework.util.BrowserStacktraceFilters;
import com.intellij.javascript.testing.JsTestConsoleProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KarmaConsoleProperties extends JsTestConsoleProperties {
  private static final String FRAMEWORK_NAME = "KarmaJavaScriptTestRunner";

  private final KarmaTestProxyFilterProvider myFilterProvider;

  public KarmaConsoleProperties(@NotNull KarmaRunConfiguration configuration, Executor executor, @NotNull KarmaTestProxyFilterProvider filterProvider) {
    super(configuration, FRAMEWORK_NAME, executor);
    myFilterProvider = filterProvider;
    setUsePredefinedMessageFilter(true);
    setIfUndefined(TestConsoleProperties.HIDE_PASSED_TESTS, false);
    setIfUndefined(TestConsoleProperties.HIDE_IGNORED_TEST, true);
    setIfUndefined(TestConsoleProperties.SCROLL_TO_SOURCE, true);
    setIfUndefined(TestConsoleProperties.SELECT_FIRST_DEFECT, true);
    setIdBasedTestTree(true);
    setPrintTestingStartedTime(false);
    addStackTraceFilters(filterProvider);
  }

  private void addStackTraceFilters(@NotNull KarmaTestProxyFilterProvider filterProvider) {
    Filter chromeStacktraceFilter = filterProvider.getFilter("browser", BrowserStacktraceFilters.CHROME, null);
    if (chromeStacktraceFilter != null) {
      addStackTraceFilter(chromeStacktraceFilter);
    }
    Filter phantomStacktraceFilter = filterProvider.getFilter("browser", BrowserStacktraceFilters.PHANTOM_JS, null);
    if (phantomStacktraceFilter != null) {
      addStackTraceFilter(phantomStacktraceFilter);
    }
  }

  @Override
  public SMTestLocator getTestLocator() {
    return KarmaTestLocationProvider.INSTANCE;
  }

  @Override
  public TestProxyFilterProvider getFilterProvider() {
    return myFilterProvider;
  }

  @Override
  public @Nullable AbstractRerunFailedTestsAction createRerunFailedTestsAction(ConsoleView consoleView) {
    return new KarmaRerunFailedTestAction((SMTRunnerConsoleView)consoleView, this);
  }
}
