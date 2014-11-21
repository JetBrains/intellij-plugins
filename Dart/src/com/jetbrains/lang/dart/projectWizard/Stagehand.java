package com.jetbrains.lang.dart.projectWizard;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Stagehand {

  public static class StagehandDescriptor {
    public final String myId;
    public final String myDescription;
    public final String myEntrypoint;
    public final String myLabel;

    public StagehandDescriptor(String id, String label, String description, String entrypoint) {
      myId = id;
      myLabel = label;
      myDescription = description;
      myEntrypoint = entrypoint;
    }

    @Override
    public String toString() {
      return StringUtil.join("[", myId, ",", myLabel, ",", myDescription, ",", myEntrypoint, "]");
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
  private static final List<StagehandDescriptor> EMPTY = new ArrayList<StagehandDescriptor>();

  private static final class PubRunner {

    private final String myWorkingDirectory;

    PubRunner() {
      myWorkingDirectory = null;
    }

    PubRunner(final VirtualFile workingDirectory) {
      myWorkingDirectory = workingDirectory.getCanonicalPath();
    }

    ProcessOutput runSync(final @NotNull String sdkRoot, int timeoutInSeconds, String... pubParameters) throws StagehandException {
      final GeneralCommandLine command = new GeneralCommandLine().withWorkDirectory(myWorkingDirectory);

      final File pubFile = new File(DartSdkUtil.getPubPath(sdkRoot));
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

  public void generateInto(@NotNull final String sdkRoot,
                           @NotNull final VirtualFile projectDirectory,
                           @NotNull final String templateId) throws StagehandException {
    final ProcessOutput output = new PubRunner(projectDirectory).runSync(sdkRoot, 30, "global", "run", "stagehand", templateId);
    if (output.getExitCode() != 0) {
      throw new StagehandException(output.getStderr());
    }
  }

  public List<StagehandDescriptor> getAvailableTemplates(@NotNull final String sdkRoot) {
    try {
      final ProcessOutput output = new PubRunner().runSync(sdkRoot, 10, "global", "run", "stagehand", "--machine");
      int exitCode = output.getExitCode();

      if (exitCode != 0) {
        return EMPTY;
      }

      // [{"name":"consoleapp", "label":"Console App", "description":"A minimal command-line application."}, {"name": ..., }]
      JSONArray arr = new JSONArray(output.getStdout());
      List<StagehandDescriptor> result = new ArrayList<StagehandDescriptor>();

      for (int i = 0; i < arr.length(); i++) {
        JSONObject obj = arr.getJSONObject(i);

        result.add(new StagehandDescriptor(
          obj.getString("name"),
          obj.getString("label"),
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

  public void install(@NotNull final String sdkRoot) {
    try {
      new PubRunner().runSync(sdkRoot, 60, "global", "activate", "stagehand");
    }
    catch (StagehandException e) {
      LOG.info(e);
    }
  }
}
