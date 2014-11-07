package com.jetbrains.lang.dart.projectWizard;


import com.intellij.execution.ExecutionException;
import com.intellij.execution.Output;
import com.intellij.execution.OutputListener;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class Stagehand {

  public static class StagehandTuple {
    public final String myId;
    public final String myDescription;
    public final String myEntrypoint;

    public StagehandTuple(String id, String description, String entrypoint) {
      this.myId = id;
      this.myDescription = description;
      this.myEntrypoint = entrypoint;
    }

    @Override
    public String toString() {
      return StringUtil.join("[", myId, ",", myDescription, ",", myEntrypoint, "]");
    }
  }

  public static class StagehandException extends Exception {
    public StagehandException(String message) {
      super(message);
    }

    public StagehandException(Throwable t) {
      super(t);
    }
  }

  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.projectWizard.Stagehand");
  private static final List<StagehandTuple> EMPTY = new ArrayList<StagehandTuple>();

  private static final class PubRunner {

    final CountDownLatch myLatch = new CountDownLatch(1);
    private final File myWorkingDirectory;

    PubRunner() {
      this(null);
    }

    PubRunner(final File workingDirectory) {
      myWorkingDirectory = workingDirectory;
    }

    Output runSync(String... pubParameters) throws StagehandException {
      final GeneralCommandLine command = new GeneralCommandLine().withWorkDirectory(myWorkingDirectory);
      DartSdk sdk = DartSdk.getGlobalDartSdk();
      if (sdk == null) {
        throw new StagehandException(DartBundle.message("dart.pub.stagehand.exception.no.sdk"));
      }
      final File pubFile = new File(DartSdkUtil.getPubPath(sdk));
      command.setExePath(pubFile.getPath());
      command.addParameters(pubParameters);
      final OSProcessHandler processHandler;
      try {
        processHandler = new OSProcessHandler(command);
      }
      catch (ExecutionException e) {
        throw new StagehandException(e);
      }
      final OutputListener outputListener = new OutputListener();
      processHandler.addProcessListener(outputListener);
      processHandler.addProcessListener(new ProcessAdapter() {
        @Override
        public void processTerminated(final ProcessEvent event) {
          myLatch.countDown();
        }
      });

      processHandler.startNotify();

      try {
        myLatch.await();
      }
      catch (InterruptedException e) {
        // Ignore
      }

      return outputListener.getOutput();
    }
  }


  public void generateInto(File projectDirectory, String templateId) throws StagehandException {
    final Output output = new PubRunner(projectDirectory).runSync("global", "run", "stagehand", templateId);
    if (output.getExitCode() != 0) {
      throw new StagehandException(output.getStderr());
    }
  }


  public List<StagehandTuple> getAvailableTemplates() {

    try {
      final Output output = new PubRunner().runSync("global", "run", "stagehand", "--machine");
      int exitCode = output.getExitCode();

      if (exitCode != 0) {
        return EMPTY;
      }

      // [{"name":"consoleapp","description":"A minimal command-line application."}, {"name": ..., }]
      JSONArray arr = new JSONArray(output.getStdout());
      List<StagehandTuple> result = new ArrayList<Stagehand.StagehandTuple>();

      for (int i = 0; i < arr.length(); i++) {
        JSONObject obj = arr.getJSONObject(i);

        result.add(new StagehandTuple(
          obj.getString("name"),
          obj.getString("description"),
          obj.optString("entrypoint")));
      }

      return result;
    }
    catch (StagehandException e) {
      LOG.error(e);
    }
    catch (JSONException e) {
      LOG.error(e);
    }

    return EMPTY;
  }


  public boolean isInstalled() {

    try {
      final Output output = new PubRunner().runSync("global", "list");
      if (output.getExitCode() != 0) {
        return false;
      }

      String[] lines = output.getStdout().split("\n");
      for (String line : lines) {
        if (line.startsWith("stagehand ")) {
          return true;
        }
      }
    }
    catch (StagehandException e) {
      // Log and fall through
      LOG.error(e);
    }

    return false;
  }

  public void install() {
    try {
      new PubRunner().runSync("global", "activate", "stagehand");
    }
    catch (StagehandException e) {
      LOG.error(e);
    }
  }

  public void upgrade() {
    install();
  }
}
