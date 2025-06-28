// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.projectWizard;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// TODO: Rename this and related classes away from "stagehand",
//   as templates are now retrieved and bootstrapped with `dart create`.
public class Stagehand {
  public static class StagehandDescriptor {
    public final @NotNull @NonNls String myId;
    public final @NotNull @NlsSafe String myLabel;
    public final @NotNull @NlsSafe String myDescription;
    public final @Nullable @NonNls String myEntrypoint;

    public StagehandDescriptor(@NotNull @NonNls String id,
                               @NotNull @NlsSafe String label,
                               @NotNull @NlsSafe String description,
                               @Nullable @NonNls String entrypoint) {
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

  private static final Logger LOG = Logger.getInstance(Stagehand.class);
  private static final List<StagehandDescriptor> EMPTY = new ArrayList<>();

  private static ProcessOutput runDartCreate(@NotNull String sdkRoot,
                                             @Nullable String workingDirectory,
                                             int timeoutInSeconds,
                                             String... parameters) throws ExecutionException {
    final GeneralCommandLine command = new GeneralCommandLine()
      .withExePath(DartSdkUtil.getDartExePath(sdkRoot))
      .withWorkDirectory(workingDirectory);

    command.addParameter("create");
    command.addParameters(parameters);

    return new CapturingProcessHandler(command).runProcess(timeoutInSeconds * 1000, false);
  }

  public void generateInto(final @NotNull String sdkRoot,
                           final @NotNull VirtualFile projectDirectory,
                           final @NotNull String templateId) throws ExecutionException {
    ProcessOutput output = runDartCreate(sdkRoot, projectDirectory.getParent().getPath(), 30, "--force", "--no-pub", "--template",
                                         templateId, projectDirectory.getName());

    if (output.getExitCode() != 0) {
      throw new ExecutionException(output.getStderr());
    }
  }

  public List<StagehandDescriptor> getAvailableTemplates(final @NotNull String sdkRoot) {
    try {
      ProcessOutput output = runDartCreate(sdkRoot, null, 10, "--list-templates");

      int exitCode = output.getExitCode();

      if (exitCode != 0) {
        return EMPTY;
      }

      // [{"name":"consoleapp", "label":"Console App", "description":"A minimal command-line application."}, {"name": ..., }]
      JSONArray arr = new JSONArray(output.getStdout());
      List<StagehandDescriptor> result = new ArrayList<>();

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
    catch (ExecutionException | JSONException e) {
      LOG.info(e);
    }

    return EMPTY;
  }
}
