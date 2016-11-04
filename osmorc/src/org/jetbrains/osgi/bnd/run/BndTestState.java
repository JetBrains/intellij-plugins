/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.osgi.bnd.run;

import aQute.bnd.build.ProjectLauncher;
import aQute.bnd.build.ProjectTester;
import aQute.bnd.build.Run;
import aQute.bnd.build.Workspace;
import aQute.bnd.service.EclipseJUnitTester;
import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.JavaTestLocator;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.SMCustomMessagesParsing;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.GeneralTestEventsProcessor;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.execution.testframework.sm.runner.events.*;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.io.URLUtil.SCHEME_SEPARATOR;
import static org.osmorc.i18n.OsmorcBundle.message;

public class BndTestState extends JavaCommandLineState {
  private static final String TEST_FRAMEWORK_NAME = "Bnd-OSGi-JUnit";
  private static final Logger LOG = Logger.getInstance(BndTestState.class);

  private final BndRunConfigurationBase.Test myConfiguration;
  private final ProjectTester myTester;
  private final ServerSocket mySocket;

  public BndTestState(@NotNull ExecutionEnvironment environment, @NotNull BndRunConfigurationBase.Test configuration) throws ExecutionException {
    super(environment);

    myConfiguration = configuration;

    final File runFile = new File(myConfiguration.bndRunFile);
    if (!runFile.isFile()) {
      throw new CantRunException(message("bnd.run.configuration.invalid", runFile));
    }

    try {
      String title = message("bnd.run.configuration.progress");
      myTester = ProgressManager.getInstance().run(new Task.WithResult<ProjectTester, Exception>(myConfiguration.getProject(), title, false) {
        @Override
        protected ProjectTester compute(@NotNull ProgressIndicator indicator) throws Exception {
          indicator.setIndeterminate(true);
          Run run = Workspace.getRun(runFile);
          if (run == null) throw new IllegalStateException("Failed to locate Bnd workspace at " + runFile.getParentFile().getParent());
          return run.getProjectTester();
        }
      });
    }
    catch (Throwable t) {
      LOG.info(t);
      throw new CantRunException(message("bnd.run.configuration.cannot.run", runFile, BndLaunchUtil.message(t)));
    }

    //noinspection InstanceofIncompatibleInterface
    if (!(myTester instanceof EclipseJUnitTester)) {
      throw new CantRunException(message("bnd.test.runner.unsupported", myTester.getClass().getName()));
    }

    try {
      mySocket = new ServerSocket(0);
      //noinspection CastToIncompatibleInterface
      ((EclipseJUnitTester)myTester).setPort(mySocket.getLocalPort());
    }
    catch (Exception e) {
      LOG.info(e);
      throw new CantRunException(message("bnd.test.cannot.run", e.getMessage()));
    }

    try {
      myTester.prepare();
    }
    catch (Exception e) {
      LOG.info(e);
      throw new CantRunException(message("bnd.run.configuration.cannot.run", runFile, e.getMessage()));
    }
  }

  @Override
  protected JavaParameters createJavaParameters() throws ExecutionException {
    ProjectLauncher launcher = myTester.getProjectLauncher();
    return BndLaunchUtil.createJavaParameters(myConfiguration, launcher);
  }

  @Nullable
  @Override
  protected ConsoleView createConsole(@NotNull Executor executor) throws ExecutionException {
    TestConsoleProperties consoleProperties = new MyTestConsoleProperties(this, executor);
    return SMTestRunnerConnectionUtil.createConsole(TEST_FRAMEWORK_NAME, consoleProperties);
  }

  @NotNull
  @Override
  protected OSProcessHandler startProcess() throws ExecutionException {
    OSProcessHandler processHandler = super.startProcess();
    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(ProcessEvent event) {
        cleanup();
      }
    });
    return processHandler;
  }

  private void cleanup() {
    try {
      mySocket.close();
      FileUtil.delete(myTester.getReportDir());
    }
    catch (Exception e) {
      LOG.error(e);
    }
  }

  private static class MyTestConsoleProperties extends SMTRunnerConsoleProperties implements SMCustomMessagesParsing {
    private final ServerSocket mySocket;

    public MyTestConsoleProperties(@NotNull BndTestState runProfile, @NotNull Executor executor) {
      super(runProfile.myConfiguration, TEST_FRAMEWORK_NAME, executor);
      mySocket = runProfile.mySocket;
      setPrintTestingStartedTime(false);
    }

    @NotNull
    @Override
    public OutputToGeneralTestEventsConverter createTestEventsConverter(@NotNull String testFrameworkName,
                                                                        @NotNull TestConsoleProperties consoleProperties) {
      return new MyProcessOutputConsumer(testFrameworkName, consoleProperties, mySocket);
    }

    @Override
    public SMTestLocator getTestLocator() {
      return JavaTestLocator.INSTANCE;
    }
  }

  private static class MyProcessOutputConsumer extends OutputToGeneralTestEventsConverter {
    private final ServerSocket mySocket;
    private GeneralTestEventsProcessor myProcessor;
    private final Object myTestLock = new Object();
    private String myCurrentTest = null;

    public MyProcessOutputConsumer(@NotNull String testFrameworkName,
                                   @NotNull TestConsoleProperties consoleProperties,
                                   @NotNull ServerSocket socket) {
      super(testFrameworkName, consoleProperties);
      mySocket = socket;
    }

    @Override
    public void setProcessor(GeneralTestEventsProcessor processor) {
      myProcessor = processor;
      startProtocolListener();
    }

    private void startProtocolListener() {
      new Thread("Bnd test state") {
        @Override
        public void run() {
          try {
            Socket socket = mySocket.accept();
            try {
              BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
              try {
                String line;
                while ((line = reader.readLine()) != null) {
                  processEventLine(line);
                }
              }
              finally {
                reader.close();
              }
            }
            finally {
              socket.close();
            }
          }
          catch (IOException e) {
            LOG.debug(e);
          }
        }
      }.start();
    }

    @Override
    public void process(String text, Key outputType) {
      GeneralTestEventsProcessor processor = myProcessor;
      synchronized (myTestLock) {
        if (myCurrentTest != null) {
          processor.onTestOutput(new TestOutputEvent(myCurrentTest, text, outputType == ProcessOutputTypes.STDOUT));
        }
        else {
          processor.onUncapturedOutput(text, outputType);
        }
      }
    }

    @Override
    public void flushBufferBeforeTerminating() { }

    @Override
    public void dispose() {
      myProcessor = null;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static final class Proto {
      private static final String INIT      = "%TESTC  ";
      private static final String TREE      = "%TSTTREE";
      private static final String TEST      = "%TESTS  ";
      private static final String ERROR     = "%ERROR  ";
      private static final String FAILED    = "%FAILED ";
      private static final String TRACE     = "%TRACES ";
      private static final String TRACE_END = "%TRACEE ";
      private static final String TEST_END  = "%TESTE  ";
      private static final String DONE      = "%RUNTIME";
    }

    private static final int EVENT_TYPE_LEN = 8;

    private int myTestCount = 0;
    private String myCurrentSuite = null;
    private long myTestStarted = 0;
    private String myReason = null;
    private String myFailingTest = null;
    private List<String> myTrace = null;

    private void processEventLine(@NotNull String line) {
      if (LOG.isDebugEnabled()) LOG.debug(">> " + line);

      if (myTrace != null) {
        if (Proto.TRACE_END.equals(line)) {
          processTrace();
        }
        else {
          myTrace.add(line);
        }
        return;
      }

      if (line.length() >= EVENT_TYPE_LEN && line.charAt(0) == '%') {
        if (line.startsWith(Proto.INIT)) {
          processInit(line);
        }
        else if (line.startsWith(Proto.TREE)) {
          processTreeLine(line);
        }
        else if (line.startsWith(Proto.TEST)) {
          processTestStart(line);
        }
        else if (line.startsWith(Proto.FAILED)) {
          myReason = Proto.FAILED;
          myFailingTest = line;
        }
        else if (line.startsWith(Proto.ERROR)) {
          myReason = Proto.ERROR;
          myFailingTest = line;
        }
        else if (Proto.TRACE.equals(line)) {
          myTrace = ContainerUtil.newArrayListWithCapacity(20);
        }
        else if (line.startsWith(Proto.TEST_END)) {
          processTestEnd(line);
        }
        else if (line.startsWith(Proto.DONE)) {
          processDone();
        }
      }
    }

    private static void processInit(@NotNull String line) {
      int p = line.indexOf(' ', EVENT_TYPE_LEN);
      if (p < 0 || !" v2".equals(line.substring(p))) {
        LOG.warn("unsupported protocol: " + line);
      }
    }

    private void processTreeLine(@NotNull String line) {
      List<String> parts = StringUtil.split(line, ",");
      if (parts.size() == 4 && "false".equals(parts.get(2))) {
        Pair<String, String> names = parseTestName(parts.get(1), true);
        if (names != null) {
          myTestCount++;
        }
      }
    }

    private void processTestStart(@NotNull String line) {
      myTestStarted = System.currentTimeMillis();

      Pair<String, String> names = parseTestName(line, false);
      String testName = fullTestName(names, line);

      if (myTestCount > 0) {
        myProcessor.onTestsCountInSuite(myTestCount);
        myTestCount = -1;
      }

      if (names != null) {
        String suite = names.first;

        if (!suite.equals(myCurrentSuite)) {
          if (myCurrentSuite != null) {
            myProcessor.onSuiteFinished(new TestSuiteFinishedEvent(myCurrentSuite));
          }

          myProcessor.onSuiteStarted(new TestSuiteStartedEvent(suite, JavaTestLocator.SUITE_PROTOCOL + SCHEME_SEPARATOR + suite));
          myCurrentSuite = suite;
        }
      }

      GeneralTestEventsProcessor processor = myProcessor;
      synchronized (myTestLock) {
        myCurrentTest = testName;
        processor.onTestStarted(new TestStartedEvent(testName, JavaTestLocator.TEST_PROTOCOL + SCHEME_SEPARATOR + testName));
      }
    }

    private void processTestEnd(@NotNull String line) {
      long t = System.currentTimeMillis() - myTestStarted;
      String testName = fullTestName(parseTestName(line, false), line);

      GeneralTestEventsProcessor processor = myProcessor;
      synchronized (myTestLock) {
        processor.onTestFinished(new TestFinishedEvent(testName, t));
        myCurrentTest = null;
      }
    }

    private void processTrace() {
      if (myTrace != null) {
        StringBuilder header = new StringBuilder();
        StringBuilder stack = new StringBuilder();

        boolean inStack = false;
        for (String line : myTrace) {
          if (!inStack && line.startsWith("\tat ")) {
            inStack = true;
          }
          (inStack ? stack : header).append(line).append('\n');
        }

        Pair<String, String> pair = null;
        String message = header.toString();
        if (message.startsWith("org.junit.") || message.startsWith("junit.framework.")) {
          pair = matchComparison(message);
          if (pair != null) {
            myReason = Proto.FAILED;
          }
        }

        if (myFailingTest != null) {
          String testName = fullTestName(parseTestName(myFailingTest, false), myFailingTest);
          boolean testError = myReason != Proto.FAILED;
          String expected = pair != null ? pair.first : null;
          String actual = pair != null ? pair.second : null;
          myProcessor.onTestFailure(new TestFailedEvent(testName, message, stack.toString(), testError, actual, expected));
        }
        else {
          myProcessor.onError(message, stack.toString(), false);
        }
      }

      myTrace = null;
      myReason = null;
    }

    private void processDone() {
      if (myCurrentSuite != null) {
        myProcessor.onSuiteFinished(new TestSuiteFinishedEvent(myCurrentSuite));
        myCurrentSuite = null;
      }
    }

    @Nullable
    private static Pair<String, String> parseTestName(@NotNull String line, boolean fromStart) {
      int comma = fromStart ? 0 : line.indexOf(',', EVENT_TYPE_LEN);
      if (comma >= 0) {
        int paren = line.indexOf('(', comma + 1);
        if (paren > 0 && paren < line.length() - 1) {
          String test = line.substring(comma + 1, paren);
          String suite = line.substring(paren + 1, line.length() - 1);
          return pair(suite, test);
        }
      }

      return null;
    }

    @NotNull
    private static String fullTestName(@Nullable Pair<String, String> names, @NotNull String line) {
      return names != null ? names.first + '.' + names.second : line.substring(EVENT_TYPE_LEN);
    }

    private static final class Comparisons {
      private static final List<Pattern> PATTERNS = ContainerUtil.newArrayList(
        compile("\nExpected: is \"(.*)\"\n\\s*got: \"(.*)\"\n"),
        compile("\nExpected: is \"(.*)\"\n\\s*but: was \"(.*)\""),
        compile("\nExpected: (.*)\n\\s*got: (.*)"),
        compile(".*?\\s*expected same:<(.*)> was not:<(.*)>"),
        compile(".*?\\s*expected:<(.*?)> but was:<(.*?)>"),
        compile("\nExpected: \"(.*)\"\n\\s*but: was \"(.*)\""),
        compile("\\s*Expected: (.*)\\s*but: was (.*)")
      );

      private static Pattern compile(String regex) {
        return Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
      }
    }

    @Nullable
    private static Pair<String, String> matchComparison(@NotNull String message) {
      for (Pattern pattern : Comparisons.PATTERNS) {
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
          return pair(matcher.group(1).replaceAll("\\\\n", "\n"), matcher.group(2).replaceAll("\\\\n", "\n"));
        }
      }

      return null;
    }
  }
}