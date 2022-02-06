/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessNotCreatedException;
import com.intellij.execution.util.ExecUtil;
import com.intellij.ide.impl.TrustedProjects;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.process.InterruptibleProcess;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.Consumer;
import com.intellij.util.EmptyConsumer;
import com.intellij.util.MemoryDumpHelper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.perforce.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public abstract class AbstractP4Connection implements P4Connection {
  private static final Logger LOG = Logger.getInstance(AbstractP4Connection.class);
  public static final int TIMEOUT_EXIT_CODE = -2;
  public static final String CONNECT_REFUSED = "Connect to server failed";
  private volatile boolean myNotConnected = false;
  private static Map<String, String> ourTestEnvironment = Collections.emptyMap();
  private static Consumer<? super String> ourCommandCallback = EmptyConsumer.getInstance();

  @Override
  public ExecResult runP4CommandLine(final PerforceSettings settings,
                                     final String[] strings,
                                     final StringBuffer stringBuffer) throws VcsException {
    final ExecResult result = new ExecResult();
    try {
      runP4Command(settings, strings, result, stringBuffer);
    }
    catch (PerforceTimeoutException | InterruptedException | IOException e) {
      throw new VcsException(e);
    }
    return result;
  }

  @Override
  public ExecResult runP4CommandLine(final PerforceSettings settings,
                                     final String[] conArgs,
                                     final String[] p4args,
                                     final StringBuffer stringBuffer) throws VcsException {
    final ExecResult result = new ExecResult();
    try {
      runP4CommandImpl(settings, conArgs, p4args, result, stringBuffer);
    } catch (RuntimeException e) {
      throw new VcsException(e);
    }
    return result;
  }

  protected void runP4CommandImpl(PerforcePhysicalConnectionParametersI parameters,
                                  String[] connArgs,
                                  String[] p4args,
                                  ExecResult retVal,
                                  StringBuffer inputStream) {
    try {
      runCmdLine(parameters, connArgs, p4args, retVal, inputStream);
    }
    catch (ProcessCanceledException e) {
      throw e;
    }
    catch (Exception e) {
      //noinspection InstanceofCatchParameter
      if (e instanceof ProcessNotCreatedException) {
        String message = e.getMessage();
        if (message != null && message.contains("No such file or directory")) {
          retVal.setException(new ProcessNotCreatedException(
            PerforceBundle.message("dialog.message.invalid.perforce.executable.name", e.getMessage()), e.getCause(), ((ProcessNotCreatedException)e).getCommandLine()));
          return;
        }
      }


      retVal.setException(e);
    }
  }

  private void runCmdLine(PerforcePhysicalConnectionParametersI perforceSettings,
                          String[] connArgs,
                          @NonNls String[] p4args,
                          ExecResult retVal,
                          StringBuffer inputData) throws Exception {
    ProgressManager.checkCanceled();
    Project project = perforceSettings.getProject();

    if (!project.isDefault() && !TrustedProjects.isTrusted(project)) {
      throw new IllegalStateException("Shouldn't be possible to run a P4 command in the safe mode");
    }

    File cwd = getWorkingDirectory();
    GeneralCommandLine cmd = fillCmdLine(perforceSettings, connArgs, p4args);
    cmd.setWorkDirectory(cwd);
    setEnvironment(cwd, cmd.getEnvironment());

    final CommandDebugInfoWrapper debugInfoWrapper = new CommandDebugInfoWrapper(cmd);
    final Tracer tracer = new Tracer(project, p4args.length > 0 ? p4args[0] : "", debugInfoWrapper);

    debugCmd(cwd, debugInfoWrapper, cmd.getEnvironment());

    int rc = -42;
    Process proc = null;
    MyInterruptibleProcess worker = null;
    PerforceProcessWaiter processWaiter = null;
    String processList = null;
    try {
      tracer.start();
      proc = cmd.createProcess();
      if (inputData != null) {
        passInputToProcess(inputData.toString(), proc, perforceSettings);
      }

      worker = new MyInterruptibleProcess(project, proc, perforceSettings.getServerTimeout());

      processWaiter = new PerforceProcessWaiter();
      worker.setOnBeforeInterrupt(processWaiter::cancelListeners);
      rc = processWaiter.execute(worker, perforceSettings.getServerTimeout());
    }
    catch (ExecutionException e) {
      throw new RuntimeException(e.getCause());
    }
    catch (TimeoutException e) {
      rc = TIMEOUT_EXIT_CODE;
    }
    finally {
      if (rc == TIMEOUT_EXIT_CODE && ApplicationManager.getApplication().isUnitTestMode()) {
        processList = ProcessHandle.allProcesses().map(h -> h.pid() + ": " + h.info()).collect(Collectors.joining("\n"));
      }

      tracer.stop();
      if (worker != null) {
        worker.closeProcess();
      } else if (proc != null) {
        InterruptibleProcess.close(proc);
      }
    }

    if (rc == 0) {
      retVal.setExitCode(worker.getExitCode());
      retVal.setOutputGobbler(processWaiter.getInStreamListener());
      retVal.setErrorGobbler(processWaiter.getErrStreamListener());

      // checked here, since in PerforceRunner we deal with more high-level commands (that require authentication)
      // but this class can be used solely, without PerforceRunner wrapping
      if (worker.getExitCode() != 0 && retVal.getException() == null && retVal.getStderr().contains(CONNECT_REFUSED)) {
        notConnected();
        return;
      }
      connected();
    }
    else if (rc == TIMEOUT_EXIT_CODE) {
      retVal.setOutputGobbler(processWaiter.getInStreamListener());
      retVal.setErrorGobbler(processWaiter.getErrStreamListener());
      LOG.info("Perforce real timeout: " + perforceSettings.getServerTimeout());
      LOG.info("stdout: " + retVal.getStdout());
      LOG.info("stderr: " + retVal.getStderr());
      retVal.setStdout("");
      retVal.setStderr(PerforceBundle.message("exception.text.perforce.integration.disconnected"));
      if (LOG.isDebugEnabled()) {
        LOG.debug("process list:\n" + processList);
      }
      if (ApplicationManager.getApplication().isUnitTestMode()) {
        System.out.println("======================");
        GeneralCommandLine sysInfo = new GeneralCommandLine(SystemInfo.isWindows ? "systeminfo" : SystemInfo.isMac ? "vm_stat" : "free").withRedirectErrorStream(true);
        System.out.println("system info:\n" + ExecUtil.execAndGetOutput(sysInfo).getStdout());

        Path dumpPath = FileUtil.createTempFile(new File(System.getProperty("teamcity.build.tempDir", System.getProperty("java.io.tmpdir"))), "perforce", ".hprof.zip", false, false).toPath();
        MemoryDumpHelper.captureMemoryDumpZipped(dumpPath);
        System.out.println("Captured " + dumpPath);
      }
      processWaiter.clearGobblers();
      throw new PerforceTimeoutException();
    }
    else {
      LOG.info("Perforce unreal timeout: " + perforceSettings.getServerTimeout() + "; rc=" + rc);
      notConnected();
      perforceSettings.disable();
      processWaiter.clearGobblers();
      throw new PerforceTimeoutException();
    }
  }

  @TestOnly
  public static void setTestEnvironment(Map<String, String> env, Disposable parentDisposable) {
    if (!ourTestEnvironment.isEmpty()) {
      ourTestEnvironment.putAll(env);
      return;
    }

    ourTestEnvironment = env;
    Disposer.register(parentDisposable, new Disposable() {
      @Override
      public void dispose() {
        //noinspection AssignmentToStaticFieldFromInstanceMethod
        ourTestEnvironment = Collections.emptyMap();
      }
    });
  }

  public static Map<String, String> getTestEnvironment() {
    return ourTestEnvironment;
  }

  @TestOnly
  public static List<String> dumpCommands(Disposable parentDisposable) {
    CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
    setCommandCallback(list::add, parentDisposable);
    return list;
  }

  @TestOnly
  public static void setCommandCallback(Consumer<? super String> callback, Disposable parentDisposable) {
    assert ourCommandCallback == EmptyConsumer.getInstance();
    ourCommandCallback = callback;
    Disposer.register(parentDisposable, new Disposable() {
      @Override
      public void dispose() {
        //noinspection AssignmentToStaticFieldFromInstanceMethod
        ourCommandCallback = EmptyConsumer.getInstance();
      }
    });
  }

  private static GeneralCommandLine fillCmdLine(PerforcePhysicalConnectionParametersI perforceSettings, String[] connArgs, String[] p4args) {
    GeneralCommandLine cmd = new GeneralCommandLine(perforceSettings.getPathToExec());
    String cmdName = p4args.length == 0 ? null : p4args[0];
    if (ourCommandCallback != EmptyConsumer.getInstance()) {
      ourCommandCallback.consume(StringUtil.join(p4args, " "));
    }
    boolean addZProg = "true".equals(System.getProperty("perforce.specify.zprog", "true"));
    if (addZProg) {
      cmd.addParameter("-zprog=IntelliJ_IDEA_" + cmdName);
    }
    cmd.addParameters(connArgs);
    cmd.addParameters(p4args);
    if (perforceSettings.getPathToIgnore() != null) {
      cmd.withEnvironment(P4ConfigHelper.P4_IGNORE, perforceSettings.getPathToIgnore());
    }
    return cmd;
  }

  private static void setEnvironment(File cwd, Map<String, String> env) {
    // On Unix, Perforce relies on the "PWD" variable to determine its current working directory
    // for finding .p4config.  We need to make sure it matches the directory we want to use.
    // (JetBrains bugs: IDEADEV-7445, etc.)
    env.put("PWD", cwd.getAbsolutePath());
    env.putAll(ourTestEnvironment);
  }

  private static void passInputToProcess(String inputData, Process proc, final PerforcePhysicalConnectionParametersI perforceSettings) throws IOException {
    final OutputStream outputStream = proc.getOutputStream();

    String charsetName = perforceSettings.getCharsetName();
    byte[] bytes;
    try {
      //noinspection SSBasedInspection
      bytes = !PerforceSettings.getCharsetNone().equals(charsetName) ? inputData.getBytes(charsetName) : inputData.getBytes(
        StandardCharsets.UTF_8);
    }
    catch (UnsupportedEncodingException e) {
      LOG.info(e);
      //noinspection SSBasedInspection
      bytes = inputData.getBytes(StandardCharsets.UTF_8);
    }
    outputStream.write(bytes);
    // must close or p4 won't read input
    outputStream.close();
  }

  private static final class CommandDebugInfoWrapper {
    private final GeneralCommandLine myCmd;
    private String myPresentation;

    private CommandDebugInfoWrapper(GeneralCommandLine cmd) {
      myCmd = cmd;
    }

    String getPresentation() {
      if (myPresentation == null) {
        StringBuilder presentation = new StringBuilder();

        String option = P4ConfigFields.P4PASSWD.getFlag();
        List<String> args = myCmd.getParametersList().getList();
        for (int i = 0; i < args.size(); i++) {
          final String s = args.get(i);
          presentation.append(" ").append(s);

          if (option.equals(s.trim())) {
            if ((i + 1) < args.size()) {
              presentation.append(" *****");  // hide password
              //noinspection AssignmentToForLoopParameter
              ++ i;
            }
          }
        }

        myPresentation = presentation.toString();
      }
      return myPresentation;
    }
  }

  private void debugCmd(File cwd, CommandDebugInfoWrapper wrapper, Map<String, String> env) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("[Perf Execute:] " + wrapper.getPresentation() + "\n [cwd] " + cwd + "\n env=" + env + "\n connection=" + this);
    }
  }

  private static final class MyInterruptibleProcess extends InterruptibleProcess {
    private final boolean myNeedStallDialog;
    private final Project myProject;
    // todo think & refactor
    // to be able to notify gobblers that streams are closed by parent
    private Runnable myOnBeforeInterrupt;

    private MyInterruptibleProcess(final Project project, final Process process, final long timeout) {
      super(process, timeout, TimeUnit.MILLISECONDS);
      myProject = project;
      myNeedStallDialog = SwingUtilities.isEventDispatchThread() || ProgressManager.getInstance().hasModalProgressIndicator();
    }

    @Override
    public void closeProcess() {
      if (myOnBeforeInterrupt != null) {
        myOnBeforeInterrupt.run();
      }
      super.closeProcess();
    }

    public void setOnBeforeInterrupt(Runnable onBeforeInterrupt) {
      myOnBeforeInterrupt = onBeforeInterrupt;
    }

    @Override
    protected int processTimeout() {
      if (myProject.isDisposed()) {
        return TIMEOUT_EXIT_CODE;
      }
      return Messages
        .showOkCancelDialog(myProject, PerforceBundle.message("confirmation.text.perforce.server.not.responding.disable.integration"),
                            PerforceBundle.message("dialog.title.perforce"),
                            PerforceBundle.message("button.text.wait.more"),
                            PerforceBundle.message("button.text.resent.and.disable.integration"),
                            Messages.getQuestionIcon());
    }

    @Override
    protected int processTimeoutInEDT() {
      if (!myNeedStallDialog || myProject.isDisposed() || ApplicationManager.getApplication().isUnitTestMode()) {
        return TIMEOUT_EXIT_CODE;
      }
      return super.processTimeoutInEDT();
    }
  }

  private static class Tracer {
    private final PerforceManager myPm;
    @NotNull
    private final P4Command myCommand;
    private final CommandDebugInfoWrapper myWrapper;
    private Object myContext;

    Tracer(final Project project, final String commandName, final CommandDebugInfoWrapper wrapper) {
      myPm = PerforceManager.getInstance(project);
      myCommand = P4Command.getInstance(commandName);
      myWrapper = wrapper;
    }

    void start() {
      if (myPm.isTraceEnabled()) {
        myContext = myPm.traceEnter(myCommand, myWrapper.getPresentation());
      }
    }

    void stop() {
      if (myPm.isTraceEnabled()) {
        if (myContext == null) {
          LOG.info("Tracing problem: no enter was registered for " + myWrapper.getPresentation());
          return;
        }
        myPm.traceExit(myContext, myCommand, myWrapper.getPresentation());
      }
    }
  }

  public void notConnected() {
    myNotConnected = true;
  }

  public void connected() {
    myNotConnected = false;
  }

  @Override
  public boolean isConnected() {
    return ! myNotConnected;
  }

}
