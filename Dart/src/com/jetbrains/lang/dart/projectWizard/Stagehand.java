package com.jetbrains.lang.dart.projectWizard;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

  static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.projectWizard.Stagehand");
  private static final List<StagehandTuple> EMPTY = new ArrayList<StagehandTuple>();

  private static final class PubRunner {

    private final String myWorkingDirectory;

    PubRunner() {
      myWorkingDirectory = null;
    }

    PubRunner(final VirtualFile workingDirectory) {
      myWorkingDirectory = workingDirectory.getCanonicalPath();
    }

    ProcessOutput runSync(int timeoutInSeconds, String... pubParameters) throws StagehandException {
      final GeneralCommandLine command = new GeneralCommandLine().withWorkDirectory(myWorkingDirectory);
      DartSdk sdk = DartSdk.getGlobalDartSdk();
      if (sdk == null) {
        throw new StagehandException(DartBundle.message("dart.pub.stagehand.exception.no.sdk"));
      }
      final File pubFile = new File(DartSdkUtil.getPubPath(sdk));
      command.setExePath(pubFile.getPath());
      command.addParameters(pubParameters);

      try {
        return new CapturingProcessHandler(command).runProcess(timeoutInSeconds * 1000, false);
      }
      catch (ExecutionException e) {
        throw new StagehandException(e);
      }
    }
  }

  public void generateInto(VirtualFile projectDirectory, String templateId) throws StagehandException {
    final ProcessOutput output = new PubRunner(projectDirectory).runSync(30, "global", "run", "stagehand", templateId);
    if (output.getExitCode() != 0) {
      throw new StagehandException(output.getStderr());
    }
  }

  public List<StagehandTuple> getAvailableTemplates() {
    try {
      final ProcessOutput output = new PubRunner().runSync(10, "global", "run", "stagehand", "--machine");
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
      LOG.info(e);
    }
    catch (JSONException e) {
      LOG.info(e);
    }

    return EMPTY;
  }


  public boolean isInstalled() {
    try {
      final ProcessOutput output = new PubRunner().runSync(10, "global", "list");
      if (output.getExitCode() != 0) {
        return false;
      }

      final List<String> lines = StringUtil.split(output.getStdout(), "\n");
      for (String line : lines) {
        if (line.startsWith("stagehand ")) {
          return true;
        }
      }
    }
    catch (StagehandException e) {
      // Log and fall through
      LOG.info(e);
    }

    return false;
  }

  public void install() {
    try {
      new PubRunner().runSync(60, "global", "activate", "stagehand");
    }
    catch (StagehandException e) {
      LOG.info(e);
    }
  }

  public void upgrade() {
    install();
  }
}
