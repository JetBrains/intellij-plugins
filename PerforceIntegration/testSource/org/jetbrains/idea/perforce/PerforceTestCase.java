package org.jetbrains.idea.perforce;

import com.intellij.execution.process.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.rollback.RollbackProgressListener;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.RunAll;
import com.intellij.testFramework.TestApplicationManager;
import com.intellij.testFramework.TestLoggerFactory;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import com.intellij.testFramework.vcs.AbstractJunitVcsTestCase;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.TimeoutUtil;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.*;
import org.jetbrains.idea.perforce.application.annotation.PerforceFileAnnotation;
import org.jetbrains.idea.perforce.perforce.FormParser;
import org.jetbrains.idea.perforce.perforce.PerforceChangeListHelper;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.*;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.intellij.testFramework.UsefulTestCase.*;

public abstract class PerforceTestCase extends AbstractJunitVcsTestCase {
  private static final Logger LOG = Logger.getInstance(PerforceTestCase.class);

  protected static final String TEST_P4CONFIG = "testP4config";
  protected static final String DEFAULT_P4CONFIG = ".p4config";
  protected static final String P4_IGNORE_NAME = ".p4ignore";
  private static final int ourP4portInt = 5666;
  protected static final String ourP4port = String.valueOf(ourP4portInt);

  private Process myP4dProcess;
  protected TempDirTestFixture myTempDirFixture;
  protected File myClientRoot;
  protected File myP4IgnoreFile;
  protected File myP4ConfigFile;
  protected final Disposable myTestRootDisposable = Disposer.newDisposable();
  private static boolean ourP4StartupFailure;

  @Before
  public void before() throws Exception {
    enableDebugLogging();
    Clock.reset();

    myTempDirFixture = IdeaTestFixtureFactory.getFixtureFactory().createTempDirTestFixture();
    myTempDirFixture.setUp();
    TestApplicationManager.getInstance();

    String tempDir = myTempDirFixture.findOrCreateDir(FileUtil.sanitizeFileName(getTestName())).getPath();

    myClientBinaryPath = new File(PathManager.getHomePath(), getPerforceExecutableDir());
    assertTrue(myClientBinaryPath + " doesn't exist!", myClientBinaryPath.exists());
    myClientRoot = createSubDirectory(tempDir, "clientRoot");
    setupP4Ignore();
    launchP4Server(tempDir, myClientBinaryPath);
    setupP4Config();

    try {
      initProject(myClientRoot);
      watchRoot(tempDir);
    }
    catch (Throwable e) {
      try {
        stopPerforceServer();
      }
      finally {
        after();
      }
      throw new RuntimeException(e);
    }
  }

  protected String getPerforceExecutableDir() {
    return "/plugins/PerforceIntegration/testData/p4d/" + getPerforceVersion();
  }

  protected String getPerforceVersion() {
    return "2015.1";
  }

  private void initProject(final File root) {
    EdtTestUtil.runInEdtAndWait(() -> {
      try {
        initProject(root, getTestName());
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
      try {
        fillPerforceSettings(myClientBinaryPath);
        setupWorkspace();
        activateVCS("Perforce");
      }
      catch (Throwable e) {
        TestLoggerFactory.dumpLogToStdout(getTestStartedLogMessage());
        throw new RuntimeException(e);
      }
    });
  }

  private void setupP4Ignore() throws IOException {
    myP4IgnoreFile = new File(myClientRoot, P4_IGNORE_NAME);
    FileUtil.writeToFile(myP4IgnoreFile, createP4Ignore());
    AbstractP4Connection.setTestEnvironment(Map.of(P4ConfigFields.P4IGNORE.name(), myP4IgnoreFile.getAbsolutePath()), myTestRootDisposable);
    VfsUtil.markDirtyAndRefresh(false, false, false, VfsUtil.findFileByIoFile(myP4IgnoreFile, true));
  }

  private void setupP4Config() throws IOException {
    myP4ConfigFile = new File(myClientRoot, DEFAULT_P4CONFIG);
    FileUtil.writeToFile(myP4ConfigFile, createP4Config("test"));
    AbstractP4Connection.setTestEnvironment(Map.of(P4ConfigFields.P4CONFIG.name(), DEFAULT_P4CONFIG), myTestRootDisposable);
    VfsUtil.markDirtyAndRefresh(false, false, false, VfsUtil.findFileByIoFile(myP4ConfigFile, true));
  }

  protected void setupWorkspace() {
    setupClient(buildTestClientSpec());
  }

  private void fillPerforceSettings(final File p4Client) {
    PerforceSettings settings = PerforceSettings.getSettings(myProject);
    settings.useP4CONFIG = false;
    settings.pathToExec = new File(p4Client, executableName("p4")).getPath();
    settings.client = "test";
    settings.port = ourP4port;
  }

  private void launchP4Server(String tempDir, final File p4Client) throws IOException, InterruptedException {
    Assume.assumeTrue(!ourP4StartupFailure);

    final File p4dRoot = createSubDirectory(tempDir, "p4dRoot");
    final File serverBinary = new File(p4Client, executableName("p4d"));
    assertTrue(serverBinary + " doesn't exist!", serverBinary.exists());
    assertTrue("Can't execute " + serverBinary, serverBinary.canExecute());

    try {
      ProcessBuilder builder =
        new ProcessBuilder().directory(p4Client).command(serverBinary.getPath(), "-p", ourP4port, "-r", p4dRoot.toString());
      LOG.debug("p4 directory: " + builder.directory());
      LOG.debug(builder.command().toString());
      myP4dProcess = builder.start();
      OSProcessHandler handler = new OSProcessHandler(myP4dProcess, builder.command().toString());
      handler.addProcessListener(new ProcessAdapter() {
        @Override
        public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
          LOG.debug("P4D process " + event.getText());
        }

        @Override
        public void processTerminated(@NotNull ProcessEvent event) {
          LOG.debug("P4D terminated " + event.getExitCode());
        }
      });
      handler.startNotify();
    }
    catch (IOException e) {
      if (SystemInfo.isLinux) {
        System.out.println("ls output:");
        System.out.println(FileUtil.loadTextAndClose(
          new ProcessBuilder().command("ls", "-al", p4Client.getPath()).redirectErrorStream(true).start().getInputStream()));

        System.out.println("uname -a output:");
        System.out.println(FileUtil.loadTextAndClose(
          new ProcessBuilder().command("uname", "-a").redirectErrorStream(true).start().getInputStream()));

        System.out.println("file output:");
        System.out.println(FileUtil.loadTextAndClose(
          new ProcessBuilder().command("file", serverBinary.getPath()).redirectErrorStream(true).start().getInputStream()));
      }
      throw e;
    }
    ensureServerRunning();
    Disposer.register(myTestRootDisposable, () -> stopPerforceServer(true));
  }

  private static File createSubDirectory(String tempDir, String child) {
    final File p4dRoot = new File(tempDir, child);
    assertTrue(p4dRoot.mkdir());
    return p4dRoot;
  }

  private void watchRoot(String tempDir) throws IOException {
    final LocalFileSystem.WatchRequest request = LocalFileSystem.getInstance().addRootToWatch(new File(tempDir).getCanonicalPath(), true);
    if (request != null) {
      Disposer.register(myTestRootDisposable, () -> LocalFileSystem.getInstance().removeWatchedRoot(request));
    }
  }

  private void enableDebugLogging() {
    myTraceClient = true;
    LOG.info(getTestStartedLogMessage());
  }

  private String getTestStartedLogMessage() {
    return "Starting " + getClass().getName() + "." + getTestName();
  }

  protected String buildTestClientSpec() {
    return buildTestClientSpec("test", myClientRoot.toString(), "//test/...");
  }

  protected void addFile(@NotNull String subPath) {
    addFile(subPath, false);
  }

  protected void addFile(@NotNull String subPath, boolean withoutIgnoreChecking) {
    List<String> argList = new ArrayList<>();
    argList.add("add");
    if (withoutIgnoreChecking) {
      argList.add("-I");
    }
    argList.add(new File(myClientRoot, subPath).toString());

    verify(runP4WithClient(ArrayUtil.toStringArray(argList)));

    // IDE might've cached unversioned status for this file
    // We can't detect external 'p4 add' so let's pretend the user pressed 'Force Refresh'
    discardUnversionedCache();
  }

  protected void discardUnversionedCache() {
    getChangeListManager().waitUntilRefreshed();
    ((PerforceChangeProvider)PerforceVcs.getInstance(myProject).getChangeProvider()).discardCache();
  }

  protected ChangeListManagerImpl getChangeListManager() {
    return ChangeListManagerImpl.getInstanceImpl(myProject);
  }

  protected void rollbackModifiedWithoutCheckout(final VirtualFile file) throws InvocationTargetException, InterruptedException {
    final List<VcsException> exceptions = new ArrayList<>();
    //noinspection ConstantConditions
    ApplicationManager.getApplication().invokeAndWait(() -> {
      //noinspection ConstantConditions
      PerforceVcs.getInstance(myProject).getRollbackEnvironment().rollbackModifiedWithoutCheckout(Collections.singletonList(file),
                                                                                                  exceptions,
                                                                                                  RollbackProgressListener.EMPTY);
    });
    if (!exceptions.isEmpty()) {
      for(VcsException ex: exceptions) {
        ex.printStackTrace();
      }
      fail("Unexpected exception: " + exceptions.get(0).toString());
    }
  }

  protected P4Connection getConnection() {
    return PerforceConnectionManager.getInstance(myProject).getAllConnections().values().iterator().next();
  }

  protected long createChangeList(final String description, final List<String> files) {
    final ProcessOutput result;
    try {
      result = runP4(new String[]{"-c", "test", "change", "-i"},
                     PerforceChangeListHelper.createSpecification(description, -1, files, null, null, false, false));
    }
    catch (VcsException e) {
      throw new RuntimeException(e);
    }
    verify(result);
    return PerforceChangeListHelper.parseCreatedListNumber(result.getStdout());
  }

  protected void rollbackChange(final Change c) {
    rollbackChanges(Collections.singletonList(c));
  }
  protected void rollbackChanges(final List<Change> changes) {
    List<VcsException> exceptions = new ArrayList<>();
    //noinspection ConstantConditions
    ApplicationManager.getApplication().invokeAndWait(() -> {
        //noinspection ConstantConditions
        PerforceVcs.getInstance(myProject).getRollbackEnvironment().rollbackChanges(changes, exceptions,
                                                                                    RollbackProgressListener.EMPTY);
      });
    if (!exceptions.isEmpty()) {
      for(VcsException ex: exceptions) {
        ex.printStackTrace();
      }
      fail("Unexpected exception: " + exceptions.get(0).toString());
    }
  }

  protected void refreshInfoAndClient() {
    Map<VirtualFile,P4Connection> allConnections = PerforceConnectionManager.getInstance(myProject).getAllConnections();
    Map<P4Connection, ConnectionInfo> infoAndClient =
      PerforceInfoAndClient.calculateInfos(allConnections.values(), PerforceRunner.getInstance(myProject),
                                                   ClientRootsCache.getClientRootsCache(myProject));
    PerforceClientRootsChecker checker = new PerforceClientRootsChecker(infoAndClient, allConnections);
    assertFalse(checker.hasAnyErrors());
  }

  protected void goOffline() {
    try {
      ApplicationManager.getApplication().invokeAndWait(() -> PerforceSettings.getSettings(myProject).disable(true));
    }
    catch (Exception e) {
     throw new RuntimeException(e);
    }

    LOG.debug("goOffline result " + PerforceSettings.getSettings(myProject).ENABLED);

  }

  protected void goOnline() {
    try {
      ApplicationManager.getApplication().invokeAndWait(() -> PerforceSettings.getSettings(myProject).enable());
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    LOG.debug("goOnline result " + PerforceSettings.getSettings(myProject).ENABLED);
  }

  protected static String createP4Config(String client) {
    return "P4CLIENT=" + client + System.lineSeparator() +
           "P4PORT=localhost:" + ourP4port + System.lineSeparator();
  }

  protected void unsetUseP4Config() {
    AbstractP4Connection.getTestEnvironment().remove(P4ConfigFields.P4CONFIG.name());
  }

  protected void openForEdit(final VirtualFile fileToEdit) {
    final VcsException[] exc = new VcsException[1];
    try {
      ApplicationManager.getApplication().invokeAndWait(() -> {
          try {
            PerforceVcs.getInstance(myProject).getEditFileProvider().editFiles(new VirtualFile[]{fileToEdit});
          }
          catch (VcsException e) {
            exc[0] = e;
          }
        });
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    if (exc[0] != null) {
      throw new RuntimeException(exc[0]);
    }
  }

  protected void setP4ConfigRoots(VirtualFile... roots) {
    assertTrue(roots.length > 0);
    List<VcsDirectoryMapping> mappings = new ArrayList<>();
    for (VirtualFile root : roots) {
      String fileName = TEST_P4CONFIG;
      if (root.findChild(fileName) == null) {
        VirtualFile p4Config = createFileInCommand(root, fileName, createP4Config("test"));
        addFile("//" + VfsUtilCore.getRelativePath(p4Config, myWorkingCopyDir, '/'));
      }
      mappings.add(createMapping(root));
    }

    submitDefaultList("p4config");
    refreshVfs();
    refreshChanges();
    assertEmpty(getChangeListManager().getAllChanges());

    setUseP4Config();
    setVcsMappings(mappings);
    refreshVfs();
    refreshChanges();
  }

  protected static VcsDirectoryMapping createMapping(VirtualFile root) {
    return new VcsDirectoryMapping(root.getPath(), "Perforce");
  }

  protected void setUseP4Config() {
    setUseP4Config(TEST_P4CONFIG);
  }
  protected void setUseP4Config(@NotNull String p4ConfigName) {
    getChangeListManager().waitUntilRefreshed();
    PerforceSettings.getSettings(myProject).useP4CONFIG = true;
    AbstractP4Connection.setTestEnvironment(Map.of(P4ConfigFields.P4CONFIG.getName(), p4ConfigName), myTestRootDisposable);
    PerforceConnectionManager.getInstance(myProject).updateConnections();
  }

  protected static String createP4Ignore() {
    return P4_IGNORE_NAME + System.lineSeparator() +
           DEFAULT_P4CONFIG + System.lineSeparator();
  }

  protected void ignoreTestP4ConfigFiles() {
    ignoreFiles(System.lineSeparator() + TEST_P4CONFIG);
  }

  protected void ignoreFiles(@NotNull String fileEntries) {
    try {
      FileUtil.appendToFile(myP4IgnoreFile, fileEntries);
    }
    catch (IOException ignore) {
    }
    refreshChanges();
  }

  private static String executableName(final String rawName) {
    return SystemInfo.isWindows ? rawName + ".exe" : SystemInfo.isMac ? rawName + "mac" : rawName;
  }

  private void ensureServerRunning() throws InterruptedException {
    final long start = System.currentTimeMillis();
    while (System.currentTimeMillis() - start < 10000 && !pingPerforceServer()) {
      //noinspection BusyWait
      Thread.sleep(50);
    }
    if (!myP4dProcess.isAlive()) {
      ourP4StartupFailure = true;
      throw new IllegalStateException("Perforce server couldn't be started: " + myP4dProcess.exitValue());
    }
  }

  private static int killProcessTreeIfNeeded(Process process) {
    try {
      return process.exitValue();
      // already terminated normally
    }
    catch (IllegalThreadStateException e) {
      LOG.debug("killing p4 server");
      try {
        OSProcessUtil.killProcessTree(process);
      }
      catch (Throwable e1) {
        e1.printStackTrace();
      }
      return 666;
    }
  }

  protected void stopPerforceServer() {
    stopPerforceServer(false);
  }
  private void stopPerforceServer(boolean fromDisposer) {
    LOG.debug("stopping p4 server");
    final String p4Path = PerforceSettings.getSettings(myProject).pathToExec;
    try {
      final Process stopProcess = new ProcessBuilder().command(p4Path, "-c", "test", "-p", ourP4port, "admin", "stop").start();
      final InputStream stdOut = stopProcess.getInputStream();
      final InputStream stdErr = stopProcess.getErrorStream();
      final long start = System.currentTimeMillis();
      while (System.currentTimeMillis() - start < 5000) {
        DebugUtil.sleep(50);
        if (!pingPerforceServer()) break;
      }

      final String out = StreamUtil.readText(new InputStreamReader(stdOut, Charset.defaultCharset()));
      final String err = StreamUtil.readText(new InputStreamReader(stdErr, Charset.defaultCharset()));
      final int rc = killProcessTreeIfNeeded(stopProcess);
      if (StringUtil.isNotEmpty(out) || StringUtil.isNotEmpty(err) || rc != 0) {
        LOG.debug("rc = " + rc + ", out = " + out + ", err = " + err);
      }

    }
    catch (IOException e) {
      if (fromDisposer) {
        e.printStackTrace();
      } else {
        throw new RuntimeException(e);
      }
    }
    finally {
      killProcessTreeIfNeeded(myP4dProcess);
      LOG.debug("after stopping p4 server");
    }
  }

  private boolean pingPerforceServer() {
    ProcessOutput infoOutput = runP4(new String[]{"info"}, null);
    int exitCode = infoOutput.getExitCode();
    if (exitCode != 0) {
      LOG.debug("p4 server ping failed: \n" + infoOutput.getStderr());
      LOG.debug("no p4 server ping, alive=" + myP4dProcess.isAlive());
      return false;
    }

    LOG.debug("p4 server ping success: \n" + infoOutput.getStdout());
    return true;
  }

  protected void forceDisableMoveCommand() {
    ClientVersion.DISABLE_MOVE_IN_TESTS = true;
    Disposer.register(myTestRootDisposable, () -> ClientVersion.DISABLE_MOVE_IN_TESTS = false);
  }

  @After
  public void after() throws Exception {
    RunAll.runAll(
      () -> {
        if (myProject != null) {
          getChangeListManager().waitUntilRefreshed();
        }
      },
      () -> Disposer.dispose(myTestRootDisposable),
      () -> {
        Ref<Exception> eRef = Ref.create();
        ApplicationManager.getApplication().invokeAndWait(() -> {
          try {
            tearDownProject();
          }
          catch (Exception e) {
            eRef.set(e);
          }
        });
        if (!eRef.isNull()) {
          throw eRef.get();
        }
      },
      () -> {
        long start = System.currentTimeMillis();
        while (myP4dProcess != null && myP4dProcess.isAlive() && System.currentTimeMillis() - start < 10_000) {
          TimeoutUtil.sleep(50); // maybe it's finishing?
        }
        if (myP4dProcess != null && myP4dProcess.isAlive()) {
          throw new AssertionError("Perforce server still alive!");
        }
      },
      () -> {
        if (myTempDirFixture != null) {
          myTempDirFixture.tearDown();
          myTempDirFixture = null;
        }
      }
    );
  }

  protected ProcessOutput runP4(String[] commandLine, @Nullable String stdin) {
    final List<String> arguments = new ArrayList<>();
    Collections.addAll(arguments, "-p", ourP4port);
    Collections.addAll(arguments, commandLine);
    return runP4Bare(ArrayUtilRt.toStringArray(arguments), stdin);
  }

  protected ProcessOutput runP4Bare(String[] commandLine, @Nullable String stdin) {
    final List<String> arguments = new ArrayList<>();
    Collections.addAll(arguments, commandLine);
    try {
      final Map<String, String> env = Map.of("PATH", myClientBinaryPath.getPath(), "PWD", myClientRoot.getPath());
      return createClientRunner(env).runClient(SystemInfo.isMac ? "p4mac" : "p4", stdin, myClientRoot,
                                               ArrayUtilRt.toStringArray(arguments));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected ProcessOutput runP4WithClient(String... commandLine) {
    List<String> arguments = new ArrayList<>();
    arguments.add("-c");
    arguments.add("test");
    Collections.addAll(arguments, commandLine);
    return runP4(ArrayUtilRt.toStringArray(arguments), null);
  }

  protected static String buildTestClientSpec(String clientName, String root, String depotMapping) {
    return buildTestClientSpecCore(clientName, root) + "\t//depot/... " + depotMapping + System.lineSeparator();
  }

  @NotNull
  protected static String buildTestClientSpecCore(String clientName, String root) {
    String sep = System.lineSeparator();
    return "Client:\t" + clientName + sep +
               "Root:\t" + root + sep +
               "View:" + sep;
  }

  protected void setupClient(String spec) {
    verify(runP4(new String[] { "client", "-i" }, spec));
    PerforceManager.getInstance(myProject).configurationChanged();
  }

  protected void verifyOpened(final String path, final String changeType) {
    ProcessOutput result = runP4WithClient("opened", new File(myClientRoot, path).toString());
    verify(result);
    final String stdout = result.getStdout();
    assertTrue("Unexpected 'p4 opened' result: " + stdout,
                      StringUtil.startsWithConcatenation(stdout, "//depot/", path, "#1 - ", changeType));
  }

  protected void submitFile(final String... depotPaths) {
    submitFileWithClient("test", depotPaths);
  }

  protected void submitFileWithClient(String client, String... depotPaths) {
    StringBuilder submitSpec = new StringBuilder("Change:\tnew\r\n");
    submitSpec.append("Description:\r\n\ttest\r\n");
    submitSpec.append("Files:\r\n");
    for(String depotPath: depotPaths) {
      submitSpec.append("\t").append(depotPath).append("\r\n");
    }
    verify(runP4(new String[] { "-c", client, "submit", "-i"}, submitSpec.toString()));
  }

  protected List<String> getFilesInDefaultChangelist() {
    ProcessOutput result = runP4WithClient("change", "-o");
    verify(result);
    final Map<String,List<String>> map = FormParser.execute(result.getStdout(), PerforceRunner.CHANGE_FORM_FIELDS);
    final List<String> strings = map.get(PerforceRunner.FILES);
    if (strings != null) {
      return strings;
    }
    return Collections.emptyList();
  }

  protected List<String> getFilesInList(final long number) {
    ProcessOutput result = runP4WithClient("change", "-o", String.valueOf(number));
    verify(result);
    final Map<String,List<String>> map = FormParser.execute(result.getStdout(), PerforceRunner.CHANGE_FORM_FIELDS);
    final List<String> strings = map.get(PerforceRunner.FILES);
    if (strings != null) {
      return strings;
    }
    return Collections.emptyList();
  }

  protected void editListDescription(final long number, final String description) throws VcsException {
    ProcessOutput result = runP4WithClient("change", "-o", String.valueOf(number));
    verify(result);
    final Map<String,List<String>> map = FormParser.execute(result.getStdout(), PerforceRunner.CHANGE_FORM_FIELDS);

    final String specification =
      PerforceChangeListHelper.createSpecification(description, number, map.get(PerforceRunner.FILES), "test", "test", true, false);
    result = runP4(new String[] {"-c", "test", "change", "-i"}, specification);
    verify(result);
  }

  @Nullable
  protected String getListDescription(final long number) {
    ProcessOutput result = runP4WithClient("change", "-o", String.valueOf(number));
    verify(result);
    final Map<String,List<String>> map = FormParser.execute(result.getStdout(), PerforceRunner.CHANGE_FORM_FIELDS);
    final List<String> strings = map.get(PerforceRunner.DESCRIPTION);
    if (strings != null && (! strings.isEmpty())) {
      final StringBuilder sb = new StringBuilder();
      for (String string : strings) {
        if (sb.length() != 0) {
          sb.append('\n');
        }
        sb.append(string);
      }
      return sb.toString();
    }
    return null;
  }

  protected void enableSilentOperation(final VcsConfiguration.StandardConfirmation op) {
    setStandardConfirmation("Perforce", op, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
  }

  protected void ensureNoEnvP4Config() {
    String p4ConfigValue = runP4(new String[]{"set", "P4CONFIG"}, null).getStdout();
    if (StringUtil.isNotEmpty(p4ConfigValue)) {
      runP4(new String[] { "set", "P4CONFIG=" }, null);
      assertEmpty(runP4(new String[]{"set", "P4CONFIG"}, null).getStdout());
    }
  }

  protected void submitDefaultList(String desc) {
    verify(runP4WithClient("submit", "-d", desc));
    refreshVfs();
    getChangeListManager().waitUntilRefreshed();
  }

  protected void submitList(long number) {
    verify(runP4WithClient("submit", "-c", String.valueOf(number)));
  }

  protected List<VcsFileRevision> getFileHistory(VirtualFile file)  {
    try {
      //noinspection ConstantConditions
      return PerforceVcs.getInstance(myProject).getVcsHistoryProvider().createSessionFor(VcsUtil.getFilePath(file)).getRevisionList();
    }
    catch (VcsException e) {
      throw new RuntimeException(e);
    }
  }

  protected void refreshChanges() {
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    getChangeListManager().ensureUpToDate();
    VcsException exception = getChangeListManager().getUpdateException();
    if (exception != null) {
      throw new RuntimeException(exception);
    }
  }

  @NotNull
  protected Change getSingleChange() {
    return assertOneElement(getChangeListManager().getDefaultChangeList().getChanges());
  }

  protected void editExternally(VirtualFile file, String text) {
    File ioFile = VfsUtilCore.virtualToIoFile(file);
    assertTrue(ioFile.setWritable(true));
    try {
      FileUtil.writeToFile(ioFile, text);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    assertTrue(ioFile.setLastModified(ioFile.lastModified() + 10000));
    refreshVfs();
  }

  protected void rollbackMissingFileDeletion(final LocallyDeletedChange change) {
    ArrayList<VcsException> exceptions = new ArrayList<>();
    //noinspection ConstantConditions
    ApplicationManager.getApplication().invokeAndWait(() -> {
        //noinspection ConstantConditions
        PerforceVcs.getInstance(myProject).getRollbackEnvironment().rollbackMissingFileDeletion(Collections.singletonList(change.getPath()),
                                                                                                exceptions,
                                                                                                RollbackProgressListener.EMPTY);
      });
    assertEmpty(exceptions);
  }

  protected void assertChangesViewEmpty() {
    assertEmpty(getChangeListManager().getAllChanges());
    assertEmpty(getChangeListManager().getUnversionedFiles());
    assertEmpty(getChangeListManager().getDeletedFiles());
    assertEmpty(getChangeListManager().getModifiedWithoutEditing());
  }

  protected List<Long> getLists() {
    final ProcessOutput result = runP4WithClient("changes", "-s", "pending");
    verify(result);
    final String[] strings = result.getStdout().split("\n");
    final List<Long> numbers = new ArrayList<>();
    for (String string : strings) {
      if (string.length() > 0) {
        long number = PerforceChangeListHelper.parseCreatedListNumber(string);
        assert number != -1;
        if (! numbers.contains(number)) {
          numbers.add(number);
        }
      }
    }
    return numbers;
  }

  protected void linkJob(final long number, final String name) {
    final ProcessOutput result = runP4WithClient("fix", "-c", String.valueOf(number), name);
    verify(result);
  }

  protected void setupTwoClients(VirtualFile dir1, VirtualFile dir2) {
    unsetUseP4Config();
    ignoreTestP4ConfigFiles();
    createIOFile(dir1, TEST_P4CONFIG, createP4Config("test"));
    createIOFile(dir2, TEST_P4CONFIG, createP4Config("dir2"));
    getChangeListManager().waitUntilRefreshed();

    verify(runP4(new String[] { "client", "-i" }, buildTestClientSpec("test", dir1.getPath(), "//test/...")));
    verify(runP4(new String[] { "client", "-i" }, buildTestClientSpec("dir2", dir2.getPath(), "//dir2/...")));
    setUseP4Config();
    setVcsMappings(createMapping(dir1), createMapping(dir2));
    refreshChanges();

    assertEmpty(getChangeListManager().getAllChanges());
    assertEmpty(getChangeListManager().getUnversionedFiles());
  }

  protected void checkNativeList(final long number, final String comment) {
    final String nativeDescription = getListDescription(number);
    assert nativeDescription != null;

    assert nativeDescription.trim().equals(comment.trim());
  }

  protected void withP4SetVariable(String variable, String value, Runnable action) {
    String oldP4Config = runP4(new String[]{"set", variable}, null).getStdout();
    if (!oldP4Config.isEmpty()) {
      runP4(new String[]{"set", variable + "="}, null);
      oldP4Config = runP4(new String[]{"set", variable}, null).getStdout();
      assertEquals("", oldP4Config);
    }

    try {
      assertEquals("", runP4(new String[]{"set", variable + "=" + value}, null).getStderr());

      String check = runP4(new String[]{"set", variable}, null).getStdout();
      assertTrue(check.contains(value));

      action.run();
    }
    finally {
      runP4(new String[]{"set", variable + "="}, null);
    }

  }

  protected void moveToChangelist(final long newListNumber, final String filePath) {
    verify(runP4WithClient("reopen", "-c", String.valueOf(newListNumber), filePath));
  }

  protected void moveToDefaultChangelist(final String filePath) {
    verify(runP4WithClient("reopen", "-c", "default", filePath));
  }

  protected PerforceFileAnnotation createTestAnnotation(VirtualFile file) throws VcsException {
    return (PerforceFileAnnotation)createTestAnnotation(PerforceVcs.getInstance(myProject).getAnnotationProvider(), file);
  }

  @Override
  protected void renameFileInCommand(VirtualFile file, String newName) {
    waitForVfsRefreshToCalmDown();
    super.renameFileInCommand(file, newName);
  }

  @Override
  protected void moveFileInCommand(VirtualFile file, VirtualFile newParent) {
    waitForVfsRefreshToCalmDown();
    super.moveFileInCommand(file, newParent);
  }

  // because of IDEA-182560
  private void waitForVfsRefreshToCalmDown() {
    for (int i = 0; i < 5; i++) {
      ((VcsAnnotationLocalChangesListenerImpl)ProjectLevelVcsManager.getInstance(myProject).getAnnotationLocalChangesListener()).calmDown();

      getChangeListManager().waitUntilRefreshed();
    }
  }

  protected boolean createIOFile(@NotNull String name, @NotNull String content) {
    return createIOFile(myWorkingCopyDir, name, content);
  }

  protected boolean createIOFile(@NotNull VirtualFile parent, @NotNull String name, @NotNull String content) {
    File file = new File(VfsUtilCore.virtualToIoFile(parent), name);
    try {
      FileUtil.writeToFile(file, content);
    }
    catch (IOException ignore) {
      return false;
    }
    return file.exists();
  }
}
