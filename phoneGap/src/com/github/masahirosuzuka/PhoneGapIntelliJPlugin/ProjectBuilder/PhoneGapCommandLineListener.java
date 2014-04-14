package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ProjectBuilder;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;

/**
 * Created by Masahiro Suzuka on 2014/04/13.
 */
public class PhoneGapCommandLineListener implements ProcessListener{

  private Project project;

  public PhoneGapCommandLineListener(final Project project){
    this.project = project;
  }

  @Override
  public void startNotified(ProcessEvent processEvent) {

  }

  @Override
  public void processTerminated(ProcessEvent processEvent) {
    String workDir = project.getBasePath();

    // move ".cordova" "marges" "node_modules" "platforms" "plugins" "www"
    GeneralCommandLine commandLine1 = new GeneralCommandLine("mv", "temp/.cordova", "./.cordova");
    commandLine1.setWorkDirectory(workDir);

    GeneralCommandLine commandLine2 = new GeneralCommandLine("mv", "temp/merges", "./merges");
    commandLine2.setWorkDirectory(workDir);

    GeneralCommandLine commandLine3 = new GeneralCommandLine("mv", "temp/node_modules", "./node_modules");
    commandLine3.setWorkDirectory(workDir);

    GeneralCommandLine commandLine4 = new GeneralCommandLine("mv", "temp/platforms", "./platforms");
    commandLine4.setWorkDirectory(workDir);

    GeneralCommandLine commandLine5 = new GeneralCommandLine("mv", "temp/plugins", "./plugins");
    commandLine5.setWorkDirectory(workDir);

    GeneralCommandLine commandLine6 = new GeneralCommandLine("mv", "temp/www", "./www");
    commandLine6.setWorkDirectory(workDir);

    GeneralCommandLine commandLine7 = new GeneralCommandLine("mv", "temp/hooks", "./hooks");
    commandLine7.setWorkDirectory(workDir);

    try {
      commandLine1.createProcess();
      commandLine2.createProcess();
      commandLine3.createProcess();
      commandLine4.createProcess();
      commandLine5.createProcess();
      commandLine6.createProcess();
      commandLine7.createProcess();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void processWillTerminate(ProcessEvent processEvent, boolean b) {

  }

  @Override
  public void onTextAvailable(ProcessEvent processEvent, Key key) {
    System.out.println(processEvent.getText());
  }
}
