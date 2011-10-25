package com.intellij.lang.javascript.flex.actions;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import com.intellij.util.text.StringTokenizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class ExternalTask {
  protected final Project myProject;
  protected final Sdk myFlexSdk;

  private Process myProcess;
  private boolean myFinished;
  private String myCommandLine = "";
  protected List<String> myMessages = new ArrayList<String>();
  private int myExitCode = -1;

  public ExternalTask(final Project project, final Sdk flexSdk) {
    myProject = project;
    myFlexSdk = flexSdk;
  }

  public void start() {
    final List<String> command = createCommandLine();
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

  protected abstract List<String> createCommandLine();

  protected boolean checkMessages() {
    return myMessages.isEmpty();
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

  public static boolean runWithProgress(final ExternalTask task, final String progressTitle, final String frameTitle) {
    return ProgressManager.getInstance().runProcessWithProgressSynchronously(createRunnable(task), progressTitle, true, task.myProject)
           && checkMessages(task, frameTitle);
  }

  public static void runInBackground(final ExternalTask task,
                                     final String progressTitle,
                                     final @Nullable Runnable onSuccess,
                                     final @Nullable Consumer<List<String>> onFailure) {
    ProgressManager.getInstance().run(new Task.Backgroundable(task.myProject, progressTitle, true) {
      public void run(@NotNull final ProgressIndicator indicator) {
        createRunnable(task).run();
      }

      public void onSuccess() {
        if (task.checkMessages()) {
          if (onSuccess != null) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
              public void run() {
                onSuccess.run();
              }
            });
          }
        }
        else if (onFailure != null) {
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              onFailure.consume(task.getMessages());
            }
          });
        }
      }
    });
  }

  private static boolean checkMessages(final ExternalTask task, final String frameTitle) {
    final List<String> messages = task.getMessages();
    if (task.checkMessages()) {
      return true;
    }
    else {
      final String message = messages.isEmpty() ? FlexBundle.message("unexpected.empty.adt.output") : StringUtil.join(messages, "\n");
      Messages.showErrorDialog(task.myProject, message, frameTitle);
    }
    return false;
  }

  private static Runnable createRunnable(final ExternalTask task) {
    return new Runnable() {
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
  }
}
