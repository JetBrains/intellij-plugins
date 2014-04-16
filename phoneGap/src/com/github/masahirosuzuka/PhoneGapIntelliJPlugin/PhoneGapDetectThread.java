package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;

/**
 * Created by Masahiro Suzuka on 2014/04/16.
 */
public class PhoneGapDetectThread implements Runnable, ProcessListener {

  private static Project project = null;

  public PhoneGapDetectThread(final Project project) {
    this.project = project;
  }

  @Override
  public void run() {
    final GeneralCommandLine generalCommandLine = new GeneralCommandLine(PhoneGapSettings.PHONEGAP_PATH, "--version");
    generalCommandLine.setWorkDirectory(project.getBasePath());
    try {
      final OSProcessHandler handler = new OSProcessHandler(generalCommandLine);
      handler.addProcessListener(this);
    } catch (Exception e) {
      e.printStackTrace();
    } finally { }
  }

  @Override
  public void startNotified(ProcessEvent processEvent) {

  }

  @Override
  public void processTerminated(ProcessEvent processEvent) {
    int exitCode = processEvent.getExitCode();
    if (exitCode != 0) {
      //System.out.println("PhoneGap not detected");
    } else {
      //System.out.println("PhoneGap detected");
    }
  }

  @Override
  public void processWillTerminate(ProcessEvent processEvent, boolean b) {

  }

  @Override
  public void onTextAvailable(ProcessEvent processEvent, Key key) {

  }
}
