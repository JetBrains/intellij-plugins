/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.RemoteTestResultReceiver;
import com.google.jstestdriver.idea.execution.tree.RemoteTestListener;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.concurrency.FutureResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil.attachRunner;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * Encapsulates the execution state of the test runner. The IDE will create and run an instance of this class when the
 * user requests to run the tests. We in turn launch a new Java process which will execute the tests.
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
class TestRunnerState extends CommandLineState {

  private static final ExecutorService testResultReceiverExecutor =
      newSingleThreadExecutor(namedThreadFactory("remoteTestResultReceiver-%d"));
  private static final Logger logger = Logger.getInstance(TestRunnerState.class.getCanonicalName());

  // TODO(alexeagle): needs to be configurable?
  private static final int testResultPort = 10998;
  private final JstdRunConfiguration myRunConfiguration;
  private final Project myProject;
  private final List<VirtualFile> myConfigVirtualFiles;

  private static ThreadFactory namedThreadFactory(final String threadName) {
    return new ThreadFactory() {
      final AtomicInteger cnt = new AtomicInteger(0);
      @Override public Thread newThread(Runnable r) {
        int num = cnt.incrementAndGet();
        Thread thread = Executors.defaultThreadFactory().newThread(r);
        thread.setName(String.format(threadName, num));
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
          @Override
          public void uncaughtException(Thread t, Throwable e) {
            logger.error("Uncaught exception on " + t, e);
          }
        });
        return thread;
      }};
  }

  public TestRunnerState(JstdRunConfiguration runConfiguration, Project project,
                         ExecutionEnvironment env) {
    super(env);
    myRunConfiguration = runConfiguration;
    myProject = project;
    myConfigVirtualFiles = JstdClientCommandLineBuilder.INSTANCE.collectVirtualFiles(runConfiguration.getRunSettings(), project);
  }

  @Override
  @Nullable
  public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
    final TestConsoleProperties testConsoleProperties =
        new SMTRunnerConsoleProperties(
            new RuntimeConfigurationProducer.DelegatingRuntimeConfiguration<JstdRunConfiguration>(myRunConfiguration),
            "jsTestDriver",
            executor
        );

    FutureResult<ProcessData> processDataFuture = new FutureResult<ProcessData>();
    TestListenerContext context = new TestListenerContext(processDataFuture);
    RemoteTestListener listener = createRemoteTestListener(context);

    CountDownLatch receivingSocketOpen = new CountDownLatch(1);
    Future<?> testResultReceiverFuture = testResultReceiverExecutor.submit(
        new RemoteTestResultReceiver(listener, testResultPort, receivingSocketOpen));
    // TODO (ssimonchik) try get test results from process output stream without sockets
    try {
      receivingSocketOpen.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new ExecutionException("Thread has been interrupted unexpectedly", e);
    }
    if (testResultReceiverFuture.isDone()) {
      throw new ExecutionException("RemoteTestResultReceiver exits unexpectedly. See log for details");
    }

    ProcessHandler processHandler = startProcess();
    BaseTestsOutputConsoleView consoleView =
        attachRunner(myProject.getName(), processHandler, testConsoleProperties, getRunnerSettings(), getConfigurationSettings());
    processDataFuture.set(new ProcessData((SMTRunnerConsoleView) consoleView, processHandler));

    return new DefaultExecutionResult(context.consoleView(), context.processHandler(),
        createActions(context.consoleView(), context.processHandler()));
  }

  private RemoteTestListener createRemoteTestListener(TestListenerContext context) {
    VirtualFile virtualFile = null;
    if (myRunConfiguration.getRunSettings().isAllInDirectory()) {
      File directory = new File(myRunConfiguration.getRunSettings().getDirectory());
      virtualFile = LocalFileSystem.getInstance().findFileByIoFile(directory);
    }
    return new RemoteTestListener(context, virtualFile);
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    GeneralCommandLine commandLine = JstdClientCommandLineBuilder.INSTANCE.buildCommandLine(
        myRunConfiguration.getRunSettings(),
        testResultPort,
        myConfigVirtualFiles
    );
    logger.info("Running JSTestDriver: " + commandLine.getCommandLineString());
    return new OSProcessHandler(commandLine.createProcess(), "");
  }

  static class ProcessData {
    final SMTRunnerConsoleView consoleView;
    final ProcessHandler processHandler;

    public ProcessData(SMTRunnerConsoleView consoleView, ProcessHandler processHandler) {
      this.consoleView = consoleView;
      this.processHandler = processHandler;
    }
  }

}
