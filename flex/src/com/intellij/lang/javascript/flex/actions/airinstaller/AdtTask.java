package com.intellij.lang.javascript.flex.actions.airinstaller;

import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.StringTokenizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class AdtTask {

  private final Project myProject;
  private final Sdk myFlexSdk;

  private Process myProcess;
  private boolean myFinished;
  private String myCommandLine = "";
  private List<String> myMessages = new ArrayList<String>();
  private int myExitCode = -1;

  public AdtTask(Project project, Sdk flexSdk) {
    myProject = project;
    myFlexSdk = flexSdk;
  }

  public static boolean runWithProgress(final AdtTask task, final String progressTitle, final String frameTitle) {
    final Runnable process = new Runnable() {
      public void run() {
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        if (indicator != null) {
          indicator.setIndeterminate(true);
        }

        task.start();
        while (!task.isFinished()) {
          if (indicator != null && indicator.isCanceled()) {
            task.cancel();
            break;
          }
          try {
            Thread.sleep(200);
          }
          catch (InterruptedException e) {/*ignore*/}
        }
      }
    };

    final boolean ok = ProgressManager.getInstance().runProcessWithProgressSynchronously(process, progressTitle, true, task.myProject);

    if (ok) {
      final List<String> messages = task.getMessages();
      if (task.checkMessages(messages)) {
        return true;
      }
      else {
        Messages.showErrorDialog(task.myProject, StringUtil.join(messages, "\n"), frameTitle);
      }
    }

    return false;
  }

  protected boolean checkMessages(final List<String> messages) {
    return messages.isEmpty();
  }

  public void start() {
    final List<String> command = FlexSdkUtils.getCommandLineForSdkTool(myProject, myFlexSdk, null, "com.adobe.air.ADT", "adt.jar");
    appendAdtOptions(command);
    final ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);

    myCommandLine = StringUtil.join(command, " ");

    try {
      myProcess = processBuilder.start();
      readInputStream();
    }
    catch (IOException e) {
      myFinished = true;
      myMessages.add(e.getMessage());
    }
  }

  public boolean isFinished() {
    return myFinished;
  }

  public void cancel() {
    if (myProcess != null) {
      myProcess.destroy();
      try {
        myExitCode = myProcess.exitValue();
      }
      catch (IllegalThreadStateException e) {/*ignore*/}
    }

    myFinished = true;
  }

  public String getCommandLine() {
    return myCommandLine;
  }

  public List<String> getMessages() {
    return myMessages;
  }

  public int getExitCode() {
    return myExitCode;
  }

  protected abstract void appendAdtOptions(final List<String> command);

  private void readInputStream() {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        boolean usageStarted = false;
        final InputStreamReader reader = new InputStreamReader(myProcess.getInputStream());

        try {
          char[] buf = new char[4096];
          int read;
          while ((read = reader.read(buf, 0, buf.length)) >= 0) {
            if (!usageStarted) {
              final String output = new String(buf, 0, read);
              final StringTokenizer tokenizer = new StringTokenizer(output, "\r\n");

              while (tokenizer.hasMoreElements()) {
                final String message = tokenizer.nextElement();
                if (!StringUtil.isEmptyOrSpaces(message)) {
                  if (message.startsWith("usage:")) {
                    usageStarted = true;
                    break;
                  }

                  if (message.trim().endsWith("password:")) {
                    final OutputStream outputStream = myProcess.getOutputStream();
                    outputStream.write("\n".getBytes());
                    outputStream.flush();
                  }
                  else {
                    myMessages.add(message);
                  }
                }
              }
            }
          }
        }
        catch (IOException e) {
          myMessages.add(e.getMessage());
        }
        finally {
          cancel();

          try {
            reader.close();
          }
          catch (IOException e) {/*ignore*/}
        }
      }
    });
  }

  public static void appendSigningOptions(List<String> command, AirInstallerParametersBase parameters) {
    if (parameters.KEY_ALIAS.length() > 0) {
      command.add("-alias");
      command.add(parameters.KEY_ALIAS);
    }

    command.add("-storetype");
    command.add(parameters.KEYSTORE_TYPE);

    command.add("-keystore");
    command.add(parameters.KEYSTORE_PATH);

    if (parameters.getKeystorePassword().length() > 0) {
      command.add("-storepass");
      command.add(parameters.getKeystorePassword());
    }

    if (parameters.getKeyPassword().length() > 0) {
      command.add("-keypass");
      command.add(parameters.getKeyPassword());
    }

    if (parameters.PROVIDER_CLASS.length() > 0) {
      command.add("-providerName");
      command.add(parameters.PROVIDER_CLASS);
    }

    if (parameters.TSA.length() > 0) {
      command.add("-tsa");
      command.add(parameters.TSA);
    }
  }

  public static void appendPaths(final List<String> command, final AirInstallerParametersBase parameters) {
    command.add(parameters.INSTALLER_FILE_LOCATION + File.separatorChar + parameters.INSTALLER_FILE_NAME);
    command.add(parameters.AIR_DESCRIPTOR_PATH);

    for (AirInstallerParametersBase.FilePathAndPathInPackage path : parameters.FILES_TO_PACKAGE) {
      final String fullPath = FileUtil.toSystemIndependentName(path.FILE_PATH.trim());
      String relPathInPackage = FileUtil.toSystemIndependentName(path.PATH_IN_PACKAGE.trim());
      if (relPathInPackage.startsWith("/")) {
        relPathInPackage = relPathInPackage.substring(1);
      }

      if (fullPath.endsWith("/" + relPathInPackage)) {
        command.add("-C");
        command.add(FileUtil.toSystemDependentName(fullPath.substring(0, fullPath.length() - relPathInPackage.length())));
        command.add(FileUtil.toSystemDependentName(relPathInPackage));
      }
      else {
        command.add("-e");
        command.add(FileUtil.toSystemDependentName(fullPath));
        command.add(relPathInPackage);
      }
    }
  }
}
