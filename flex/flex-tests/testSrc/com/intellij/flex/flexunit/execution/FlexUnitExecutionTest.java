// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.flexunit.execution;

import com.intellij.codeInsight.JavaCodeInsightTestCase;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.Printable;
import com.intellij.execution.testframework.Printer;
import com.intellij.execution.testframework.sm.runner.states.TestStateInfo;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.flex.util.FlexUnitLibs;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfigurationType;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ThrowableRunnable;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.ui.UIUtil;
import net.n3.nanoxml.IXMLElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class FlexUnitExecutionTest extends JavaCodeInsightTestCase implements FlexUnitLibs {

  public static class FlexUnitExecutionWebTest extends FlexUnitExecutionTest {
    public FlexUnitExecutionWebTest() {
      super(TargetPlatform.Web);
    }
  }

  public static class FlexUnitExecutionDesktopTest extends FlexUnitExecutionTest {
    public FlexUnitExecutionDesktopTest() {
      super(TargetPlatform.Desktop);
    }
  }

  private static final long COMPILATION_TIMEOUT = 60; // seconds
  private static final long STARTUP_TIMEOUT = 60; // seconds
  private static final long EXECUTION_TIMEOUT = 60 * 5; // seconds

  private static final String FLEX3_SDK_HOME = "c:\\Programs\\flex_sdk_3.3.0.4852";
  private static final String FLEX4_SDK_HOME = "c:\\Programs\\flex_sdk_4.6.0.23201";
  private static final String FLEX_SDK_HOME = FLEX4_SDK_HOME;

  private static final Map<FlexUnitRunnerParameters.OutputLogLevel, String> LOG_MESSAGES =
    new HashMap<FlexUnitRunnerParameters.OutputLogLevel, String>() {
      {
        put(FlexUnitRunnerParameters.OutputLogLevel.Fatal, "Fatal_message");
        put(FlexUnitRunnerParameters.OutputLogLevel.Error, "Error_message");
        put(FlexUnitRunnerParameters.OutputLogLevel.Warn, "Warning_message");
        put(FlexUnitRunnerParameters.OutputLogLevel.Info, "Info_message");
        put(FlexUnitRunnerParameters.OutputLogLevel.Debug, "Debug_message");
      }
    };

  private static final String BASE_PATH = "/execute/";

  private static final boolean BLOCK_PORT_843 = true;

  private volatile boolean myStopBlocking;
  private boolean myUseMxmlcCompc;
  private boolean myUseBuiltInCompiler;
  private boolean myUseFcsh;

  private final TargetPlatform myTargetPlatform;

  @SuppressWarnings("JUnitTestCaseWithNonTrivialConstructors")
  protected FlexUnitExecutionTest(final TargetPlatform platform) {
    myTargetPlatform = platform;
  }

  @Override
  protected void doCommitModel(@NotNull ModifiableRootModel rootModel) {
    super.doCommitModel(rootModel);

    FlexTestUtils.addFlexUnitLib(getClass(), getTestName(false), getModule(), getTestDataPath(), FLEX_UNIT_0_9_SWC, FLEX_UNIT_4_SWC);
  }

  @NotNull
  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("flexUnit");
  }

  @Override
  protected void setUp() {
    UIUtil.invokeAndWaitIfNeeded((Runnable)() -> {
      try {
        super.setUp();
        CompilerProjectExtension.getInstance(myProject).setCompilerOutputUrl(createOutputFolder().getUrl());
      }
      catch (Throwable e) {
        e.printStackTrace();
        assertTrue(false);
      }
    });

    if (BLOCK_PORT_843) {
      ApplicationManager.getApplication().executeOnPooledThread(() -> {
        ServerSocket socket = null;
        myStopBlocking = false;
        try {
          socket = new ServerSocket(843);
          socket.setSoTimeout(50);
          while (!myStopBlocking) {
            try {
              socket.accept();
            }
            catch (SocketTimeoutException e) {
              // continue looping
            }
          }
        }
        catch (IOException e) {
          fail(e.getMessage());
        }
        finally {
          if (socket != null) {
            try {
              socket.close();
            }
            catch (IOException e) {
              // ignore
            }
          }
        }
      });
    }

    FlexCompilerProjectConfiguration c = FlexCompilerProjectConfiguration.getInstance(myProject);
    myUseMxmlcCompc = c.USE_MXMLC_COMPC;
    myUseBuiltInCompiler = c.USE_BUILT_IN_COMPILER;
    myUseFcsh = c.USE_FCSH;

    c.USE_MXMLC_COMPC = true;
    c.USE_FCSH = false;
    c.USE_BUILT_IN_COMPILER = false;
  }

  @NotNull
  private VirtualFile createOutputFolder() {
    final File outputFolder = new File(myProject.getBasePath(), "out");
    if (!outputFolder.isDirectory() && !outputFolder.mkdirs()) {
      fail("Failed to create output folder: " + outputFolder);
    }
    return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(outputFolder);
  }

  @Override
  protected void runBareRunnable(@NotNull ThrowableRunnable<Throwable> runnable) throws Throwable {
    runnable.run();
  }

  @Override
  protected void setUpJdk() {
    final Sdk sdk = FlexTestUtils.createSdk(FLEX4_SDK_HOME, "4.6.0", getTestRootDisposable());
    FlexTestUtils.modifyBuildConfiguration(myModule, configuration -> FlexTestUtils.setSdk(configuration, sdk));
  }

  @Override
  protected VirtualFile configureByFiles(final File projectRoot, final VirtualFile @NotNull [] vFiles) {
    final Ref<VirtualFile> result = new Ref<>();
    UIUtil.invokeAndWaitIfNeeded((Runnable)() -> {
      try {
        result.set(super.configureByFiles(projectRoot, vFiles));
      }
      catch (IOException e) {
        LOG.error(e);
      }
    });
    return result.get();
  }

  protected void doDebugTest(FlexUnitRunnerParameters.Scope testScope, String testClassOrPackage, String testMethod) throws Exception {
    doTest(true, testScope, testClassOrPackage, testMethod, null, BASE_PATH + getTestName(false) + ".as");
  }

  protected void doRunTest(FlexUnitRunnerParameters.Scope testScope, String testClassOrPackage, @Nullable String testMethod)
    throws Exception {
    doTest(false, testScope, testClassOrPackage, testMethod, null, BASE_PATH + getTestName(false) + ".as");
  }

  private AbstractTestProxy doTest(boolean debugNotRun,
                                   FlexUnitRunnerParameters.Scope testScope,
                                   String testClassOrPackage,
                                   @Nullable String testMethod,
                                   @Nullable String projectRoot,
                                   String... files) throws Exception {
    return doTest(debugNotRun, testScope, testClassOrPackage, testMethod, projectRoot, null, files);
  }

  private AbstractTestProxy doTest(boolean debugNotRun,
                                   FlexUnitRunnerParameters.Scope testScope,
                                   String testClassOrPackage,
                                   @Nullable String testMethod,
                                   @Nullable String projectRoot,
                                   @Nullable FlexUnitRunnerParameters.OutputLogLevel outputLogLevel,
                                   String... files) throws Exception {
    configureByFiles(projectRoot, files);
    final Ref<IXMLElement> expected = new Ref<>();
    UIUtil.invokeAndWaitIfNeeded((Runnable)() -> WriteAction.run(() -> {
      try {
        Collection<IXMLElement> collection = JSTestUtils.extractXml(myEditor.getDocument(), "testResults");
        assertEquals("Invalid expected structure", 1, collection.size());
        expected.set(collection.iterator().next());
      }
      catch (Exception e) {
        e.printStackTrace();
        fail(e.getMessage());
      }
    }));

    UIUtil.invokeAndWaitIfNeeded((Runnable)() -> WriteAction.run(
      () -> FlexTestUtils.modifyBuildConfiguration(myModule, configuration -> configuration.setTargetPlatform(myTargetPlatform))));

    final RunnerAndConfigurationSettings runnerAndConfigurationSettings =
      RunManager.getInstance(myProject).createConfiguration("test", FlexUnitRunConfigurationType.class);
    final FlexUnitRunConfiguration flexUnitRunConfig = (FlexUnitRunConfiguration)runnerAndConfigurationSettings.getConfiguration();
    final FlexUnitRunnerParameters params = flexUnitRunConfig.getRunnerParameters();

    params.setModuleName(myModule.getName());
    params.setBCName(FlexBuildConfigurationManager.getInstance(myModule).getBuildConfigurations()[0].getName());
    params.setOutputLogLevel(outputLogLevel);
    params.setScope(testScope);
    switch (testScope) {
      case Class:
        params.setClassName(testClassOrPackage);
        break;
      case Method:
        params.setClassName(testClassOrPackage);
        params.setMethodName(testMethod);
        break;
      case Package:
        params.setPackageName(testClassOrPackage);
        break;
      default:
        fail("Unknown scope: " + testScope);
    }

    flexUnitRunConfig.checkConfiguration();

    final ProgramRunner runner = new FlexUnitTestRunner();
    final Executor executor = debugNotRun ? DefaultDebugExecutor.getDebugExecutorInstance() : DefaultRunExecutor.getRunExecutorInstance();
    final ExecutionEnvironment env = new ExecutionEnvironment(executor, runner, runnerAndConfigurationSettings, getProject());

    final Semaphore compilation = new Semaphore();
    compilation.down();

    final Semaphore execution = new Semaphore();
    execution.down();

    final Semaphore startup = new Semaphore();

    final ProcessListener listener = new ProcessListener() {
      @Override
      public void startNotified(@NotNull ProcessEvent event) {
        startup.up();
      }

      @Override
      public void processTerminated(@NotNull ProcessEvent event) {
        execution.up();
      }

      @Override
      public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
        System.out.println("FlexUnit: " + event.getText());
      }
    };

    final Ref<ExecutionConsole> executionConsole = new Ref<>();
    ApplicationManager.getApplication().invokeLater(() -> {
      try {
        env.setCallback(new ProgramRunner.Callback() {
          @Override
          public void processStarted(RunContentDescriptor descriptor) {
            compilation.up();
            startup.down();
            descriptor.getProcessHandler().addProcessListener(listener);
            executionConsole.set(descriptor.getExecutionConsole());
          }
        });
        runner.execute(env);
      }
      catch (Throwable t) {
        t.printStackTrace();
        fail(t.getMessage());
        compilation.up();
        startup.up();
        execution.up();
      }
    });

    if (!compilation.waitFor(COMPILATION_TIMEOUT * 1000)) {
      fail("Compilation did not succeed in " +
           COMPILATION_TIMEOUT +
           " seconds. There was an error or it took too long\n");
    }

    if (!startup.waitFor(STARTUP_TIMEOUT * 1000)) {
      fail("Process was not started in " + STARTUP_TIMEOUT + " seconds");
    }

    if (!execution.waitFor(EXECUTION_TIMEOUT * 1000)) {
      fail("Execution did not finish in " + EXECUTION_TIMEOUT + " seconds");
    }

    Thread.sleep(200); // give tests tree some time to stabilize

    final AbstractTestProxy testRoot = ((SMTRunnerConsoleView)executionConsole.get()).getResultsViewer().getRoot();
    checkResults(expected.get(), testRoot);
    if (outputLogLevel == null) {
      checkOutput(testRoot, outputLogLevel);
    }
    return testRoot;
  }

  private static void checkResults(IXMLElement expectedRoot, AbstractTestProxy actualRoot) {
    assertEquals("Wrong tests run status", expectedRoot.getAttribute("status", null), getStatusTitle(actualRoot.getMagnitude()));
    assertEquals("Wrong tests suites count", expectedRoot.getChildrenCount(), actualRoot.getChildren().size());
    for (int i = 0; i < actualRoot.getChildren().size(); i++) {
      final AbstractTestProxy actualSuite = actualRoot.getChildren().get(i);
      //assertTrue(actualSuite.getName() + " expected to be suite", actualSuite.);
      IXMLElement expectedSuite = getChild(expectedRoot, actualSuite.getName());
      if (expectedSuite == null) {
        fail("Unexpected suite: " + actualSuite.getName());
      }
      assertEquals("Wrong expected node", "suite", expectedSuite.getName());
      assertEquals("Wrong test suite status", expectedSuite.getAttribute("status", null), getStatusTitle(actualSuite.getMagnitude()));

      assertEquals("Wrong tests count in suite " + actualSuite.getName(), expectedSuite.getChildrenCount(),
                   actualSuite.getChildren().size());
      for (int j = 0; j < actualSuite.getChildren().size(); j++) {
        final AbstractTestProxy actualTest = actualSuite.getChildren().get(j);
        //assertTrue(actualSuite.getName() + " expected to be suite", actualSuite.);
        IXMLElement expectedTest = getChild(expectedSuite, actualTest.getName());
        if (expectedTest == null) {
          fail("Unexpected test: " + actualTest.getName());
        }
        assertEquals("Wrong expected node", "test", expectedTest.getName());
        assertEquals("Wrong test " + actualSuite.getName() + "." + actualTest.getName() + "() status",
                     expectedTest.getAttribute("status", null), getStatusTitle(actualTest.getMagnitude()));

        assertEquals("Test children not allowed", 0, expectedTest.getChildrenCount());
        assertEmpty("Test children not expected", actualTest.getChildren());
      }
    }
  }

  @Nullable
  private static IXMLElement getChild(IXMLElement parent, String name) {
    for (Object o : parent.getChildren()) {
      if (name.equals(((IXMLElement)o).getAttribute("name", null))) {
        return (IXMLElement)o;
      }
    }
    return null;
  }

  private static String getStatusTitle(int i) {
    for (TestStateInfo.Magnitude magnitude : TestStateInfo.Magnitude.values()) {
      if (magnitude.getValue() == i) {
        return magnitude.getTitle();
      }
    }
    fail("Unknown expected status value: " + i);
    return null;
  }

  private static void checkOutput(AbstractTestProxy testProxy, @Nullable FlexUnitRunnerParameters.OutputLogLevel logLevel) {
    final StringBuilder stdout = new StringBuilder();
    testProxy.printOn(new Printer() {
      @Override
      public void print(String text, ConsoleViewContentType contentType) {
        if (contentType == ConsoleViewContentType.NORMAL_OUTPUT) {
          stdout.append(text);
        }
        else if (contentType != ConsoleViewContentType.ERROR_OUTPUT && contentType != ConsoleViewContentType.SYSTEM_OUTPUT) {
          assert false;
        }
      }

      @Override
      public void onNewAvailable(@NotNull Printable printable) {
        printable.printOn(this);
      }

      @Override
      public void printHyperlink(String text, HyperlinkInfo info) {
      }

      @Override
      public void mark() {
      }
    });

    if (logLevel == null) {
      Assert.assertEquals("Test std output should be empty but was '" + stdout + "'", 0, stdout.length());
    }
    else {
      for (FlexUnitRunnerParameters.OutputLogLevel level : FlexUnitRunnerParameters.OutputLogLevel.values()) {
        String message = LOG_MESSAGES.get(level);
        if (message != null) {
          if (level.compareTo(logLevel) <= 0) {
            Assert.assertTrue("Expected message '" + message + "' was not found in test output '" + stdout + "'",
                              stdout.indexOf(message) != -1);
          }
          else {
            Assert.assertEquals("Message '" + message + "' was not expected in test output '" + stdout + "'", stdout.indexOf(message), -1);
          }
        }
      }
    }
  }

  @Override
  protected void tearDown() {
    FlexCompilerProjectConfiguration c = FlexCompilerProjectConfiguration.getInstance(myProject);
    c.USE_MXMLC_COMPC = myUseMxmlcCompc;
    c.USE_FCSH = myUseFcsh;
    c.USE_BUILT_IN_COMPILER = myUseBuiltInCompiler;

    UIUtil.invokeAndWaitIfNeeded((Runnable)() -> {
      if (BLOCK_PORT_843) {
        myStopBlocking = true;
      }
      try {
        super.tearDown();
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testFlexUnit1() throws Exception {
    doRunTest(FlexUnitRunnerParameters.Scope.Package, "", null);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testFlexUnit1Class() throws Exception {
    doRunTest(FlexUnitRunnerParameters.Scope.Class, "FlexUnit1Class", null);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testFlexUnit1Method() throws Exception {
    doRunTest(FlexUnitRunnerParameters.Scope.Method, "FlexUnit1Method", "testRed");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testFlexUnit1Empty() throws Exception {
    doRunTest(FlexUnitRunnerParameters.Scope.Package, "", null);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testFlexUnit4() throws Exception {
    doRunTest(FlexUnitRunnerParameters.Scope.Package, "", null);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testFlexUnit4Class() throws Exception {
    doRunTest(FlexUnitRunnerParameters.Scope.Class, "FlexUnit4Class", null);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testFlexUnit4Method() throws Exception {
    doRunTest(FlexUnitRunnerParameters.Scope.Method, "FlexUnit4Method", "red");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testFlexUnit4TestIgnored1() throws Exception {
    doRunTest(FlexUnitRunnerParameters.Scope.Package, "", null);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testFlexUnit4TestIgnored2() throws Exception {
    doRunTest(FlexUnitRunnerParameters.Scope.Package, "", null);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testFlexUnit4TestIgnored3() throws Exception {
    doRunTest(FlexUnitRunnerParameters.Scope.Package, "", null);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testPackaged1() throws Exception {
    doTest(false, FlexUnitRunnerParameters.Scope.Class, "test.TestFoo", null, BASE_PATH, BASE_PATH + "test/testFoo.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testPackaged2() throws Exception {
    doTest(false, FlexUnitRunnerParameters.Scope.Class, "test.TestFoo", null, BASE_PATH, BASE_PATH + "test/testFoo.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testPackaged3() throws Exception {
    doTest(false, FlexUnitRunnerParameters.Scope.Class, "testSuite.TestFoo", null, BASE_PATH, BASE_PATH + "testSuite/testFoo.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testPackaged4() throws Exception {
    doTest(false, FlexUnitRunnerParameters.Scope.Class, "testSuite.TestFoo", null, BASE_PATH, BASE_PATH + "testSuite/testFoo.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testPackaged5() throws Exception {
    doTest(false, FlexUnitRunnerParameters.Scope.Class, "tests.TestFoo", null, BASE_PATH, BASE_PATH + "tests/testFoo.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testPackaged6() throws Exception {
    doTest(true, FlexUnitRunnerParameters.Scope.Class, "tests.TestFoo", null, BASE_PATH, BASE_PATH + "tests/testFoo.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testSuite1() throws Exception {
    doTest(true, FlexUnitRunnerParameters.Scope.Class, "Suite1", null, BASE_PATH + "suites", BASE_PATH + "suites/Suite1.as",
           BASE_PATH + "suites/rawTests/testFoo.as", BASE_PATH + "suites/rawTests/testBar.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testOldStyleSuiteFlexUnit1() throws Exception {
    doTest(true, FlexUnitRunnerParameters.Scope.Class, "OldStyleSuite", null, BASE_PATH + "suites", BASE_PATH + "suites/OldStyleSuite.as",
           BASE_PATH + "suites/rawTests/testFoo.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testOldStyleSuiteFlexUnit4() throws Exception {
    doTest(true, FlexUnitRunnerParameters.Scope.Class, "OldStyleSuite", null, BASE_PATH + "suites", BASE_PATH + "suites/OldStyleSuite.as",
           BASE_PATH + "suites/rawTests/testFoo.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testFlunitSuite() throws Exception {
    doTest(true, FlexUnitRunnerParameters.Scope.Class, "FlunitSuite", null, BASE_PATH + "suites", BASE_PATH + "suites/FlunitSuite.as",
           BASE_PATH + "suites/rawTests/FlunitTest.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testAllInPackageWithSuite() throws Exception {
    doTest(true, FlexUnitRunnerParameters.Scope.Package, "", null, BASE_PATH + "suites", BASE_PATH + "suites/AllInPackageWithSuite.as",
           BASE_PATH + "suites/Suite1.as", BASE_PATH + "suites/Suite2.as", BASE_PATH + "suites/FlunitSuite.as",
           BASE_PATH + "suites/rawTests/testFoo.as", BASE_PATH + "suites/rawTests/testBar.as", BASE_PATH + "suites/rawTests/FlunitTest.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testClassWithCustomRunner() throws Exception {
    doTest(true, FlexUnitRunnerParameters.Scope.Class, getTestName(false), null, BASE_PATH, BASE_PATH + getTestName(false) + ".as",
           BASE_PATH + "CustomRunner.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testClassWithCustomRunnerInPackage() throws Exception {
    doTest(true, FlexUnitRunnerParameters.Scope.Package, "", null, null, BASE_PATH + "ClassWithCustomRunner.as",
           BASE_PATH + "CustomRunner.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testMethodWithCustomRunner() throws Exception {
    doTest(true, FlexUnitRunnerParameters.Scope.Method, getTestName(false), "foo2", BASE_PATH, BASE_PATH + getTestName(false) + ".as",
           BASE_PATH + "CustomRunner.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testImplicitRunners() throws Exception {
    doTest(true, FlexUnitRunnerParameters.Scope.Class, getTestName(false), null, null, BASE_PATH + getTestName(false) + ".as",
           BASE_PATH + getTestName(false) + "2.as", BASE_PATH + getTestName(false) + "3.as", BASE_PATH + "CustomRunner.as");
  }

  private void doTestLogOutput(boolean debugNotRun, @Nullable FlexUnitRunnerParameters.OutputLogLevel logLevel) throws Exception {
    AbstractTestProxy testRoot =
      doTest(debugNotRun, FlexUnitRunnerParameters.Scope.Class, "LogOutput", null, null, logLevel, BASE_PATH + "LogOutput.as");
    checkOutput(testRoot, logLevel);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testLogOutputNone1() throws Exception {
    doTestLogOutput(true, null);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testLogOutputFatal1() throws Exception {
    doTestLogOutput(true, FlexUnitRunnerParameters.OutputLogLevel.Fatal);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testLogOutputError1() throws Exception {
    doTestLogOutput(true, FlexUnitRunnerParameters.OutputLogLevel.Error);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testLogOutputWarn1() throws Exception {
    doTestLogOutput(true, FlexUnitRunnerParameters.OutputLogLevel.Warn);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testLogOutputDebug1() throws Exception {
    doTestLogOutput(true, FlexUnitRunnerParameters.OutputLogLevel.Debug);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testLogOutputInfo1() throws Exception {
    doTestLogOutput(true, FlexUnitRunnerParameters.OutputLogLevel.Info);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testLogOutputNone4() throws Exception {
    doTestLogOutput(true, null);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testLogOutputFatal4() throws Exception {
    doTestLogOutput(true, FlexUnitRunnerParameters.OutputLogLevel.Fatal);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testLogOutputError4() throws Exception {
    doTestLogOutput(true, FlexUnitRunnerParameters.OutputLogLevel.Error);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testLogOutputWarn4() throws Exception {
    doTestLogOutput(true, FlexUnitRunnerParameters.OutputLogLevel.Warn);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testLogOutputDebug4() throws Exception {
    doTestLogOutput(true, FlexUnitRunnerParameters.OutputLogLevel.Debug);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testLogOutputInfo4() throws Exception {
    doTestLogOutput(true, FlexUnitRunnerParameters.OutputLogLevel.Info);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testUnicodeBreaks() throws Exception {
    doTest(true, FlexUnitRunnerParameters.Scope.Class, getTestName(false), null, null, BASE_PATH + getTestName(false) + ".as");
  }
}
