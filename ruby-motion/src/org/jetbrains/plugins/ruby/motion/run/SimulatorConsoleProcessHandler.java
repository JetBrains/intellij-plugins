/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.AnsiEscapeDecoder;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.process.UnixProcessManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.TimeoutUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.cidr.execution.ExecutionResult;
import com.jetbrains.cidr.execution.ProcessHandlerWithPID;
import com.jetbrains.cidr.execution.debugger.CidrDebuggerLog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.console.RubyConsoleProcessHandler;
import org.jetbrains.plugins.ruby.console.RubyLanguageConsole;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtil;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dennis.Ushakov
 */
public class SimulatorConsoleProcessHandler extends RubyConsoleProcessHandler implements ProcessHandlerWithPID,
                                                                                         ProcessHandlerWithDetachSemaphore,
                                                                                         AnsiEscapeDecoder.ColoredChunksAcceptor {
  private static final Pattern PROMPT = Pattern.compile("(\\(\\S*\\)[>?]).*");
  private final ExecutionResult<Integer> myPIDResult = new ExecutionResult<>();
  private final MotionSimulatorRunExtension.MotionProcessOutputReaders myReaders;
  private String myLastMatched;
  private boolean mySimulateStarted = false;
  private final boolean isOSX;
  private Semaphore myDetachSemaphore;

  public SimulatorConsoleProcessHandler(@NotNull final Module module,
                                        Process process,
                                        String commandLine,
                                        MotionSimulatorRunExtension.MotionProcessOutputReaders readers) {
    super(process, commandLine);
    myReaders = readers;
    myReaders.setHandler(this);
    isOSX = RubyMotionUtil.getInstance().isOSX(module);
  }

  @Override
  public void setConsole(@NotNull RubyLanguageConsole console) {
    super.setConsole(console);
    console.setPrompt("(main)>");
  }

  @Override
  public void coloredChunksAvailable(@NotNull List<Pair<String, Key>> textChunks) {
    super.coloredChunksAvailable(preprocessText(textChunks));
  }

  @NotNull
  private List<Pair<String, Key>> preprocessText(@NotNull List<Pair<String, Key>> textChunks) {
    final List<Pair<String, Key>> textToProcess = ContainerUtil.newArrayList();
    for (final Pair<String, Key> chunk : textChunks) {
      final String text = chunk.getFirst();
      final Key attributes = chunk.getSecond();
      mySimulateStarted |= text.contains(isOSX ? "Run" : "Simulate");
      final String[] lines = text.split("\r");
      findPid(lines, mySimulateStarted, myPIDResult);
      if (attributes != ProcessOutputTypes.STDOUT) {
        textToProcess.add(Pair.create(text, attributes));
        continue;
      }
      for (final String line : lines) {
        if (StringUtil.isEmpty(line.trim())) continue;
        if (StringUtil.equals(myLastMatched, line)) continue;

        final Matcher matcher = PROMPT.matcher(line);
        if (matcher.matches()) {
          final String prompt = matcher.group(1);
          getConsole().setPrompt(prompt);
          myLastMatched = line;
        } else {
          textToProcess.add(Pair.create(line, attributes));
          myLastMatched = null;
        }
      }
    }
    return textToProcess;
  }

  static void findPid(String[] lines, final boolean simulateStarted, final ExecutionResult<Integer> pidResult) {
    if (simulateStarted && !pidResult.isDone()) {
      for (String line : lines) {
        if (line.contains(".app")) {
          findPid(line.trim(), pidResult);
        }
      }
    }
  }

  @Override
  protected void destroyProcessImpl() {
    try {
      if (myDetachSemaphore != null) myDetachSemaphore.acquire();
    } catch (InterruptedException e) {
      CidrDebuggerLog.LOG.info(e);
    }
    super.destroyProcessImpl();
    closeReaders();
  }

  private void closeReaders() {
    ApplicationManager.getApplication().executeOnPooledThread(() -> myReaders.close());
  }

  @Override
  protected void detachProcessImpl() {
    super.detachProcessImpl();
    closeReaders();
  }

  private static void findPid(String line, final ExecutionResult<Integer> pidResult) {
    final int i = line.lastIndexOf('/');
    final String appName = i > 0 ? line.substring(i) : null;
    if (appName == null) return;
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      // let's wait for an app for 3 minutes with 100ms interval
      for (int i1 = 0; i1 < 3 * 60 * 10; i1++) {
        UnixProcessManager.processPSOutput(UnixProcessManager.getPSCmd(false, true), s -> {
          final Scanner scanner = new Scanner(s);
          final int ppid = scanner.nextInt();
          final int pid = scanner.nextInt();
          final String command = scanner.nextLine();
          if (command.contains(appName) && !pidResult.isDone()) {
            pidResult.set(pid);
            return true;
          }
          return false;
        });
        if (pidResult.isDone()) {
          return;
        }
        TimeoutUtil.sleep(100);
      }
      CidrDebuggerLog.LOG.error("Failed to find pid for: " + appName);
    });
  }

  @Override
  public int getPID() throws ExecutionException {
    return myPIDResult.get();
  }

  @Override
  public int getPID(long timeout) throws ExecutionException, TimeoutException {
    return myPIDResult.get(timeout, TimeUnit.MILLISECONDS);
  }

  @Override
  public void setDetachSemaphore(Semaphore detachSemaphore) {
    myDetachSemaphore = detachSemaphore;
  }
}
