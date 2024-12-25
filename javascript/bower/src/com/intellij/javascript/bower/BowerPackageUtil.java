package com.intellij.javascript.bower;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.json.psi.JsonFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

public final class BowerPackageUtil {

  private static final String BOWER_JSON = "bower.json";
  private static final Logger LOG = Logger.getInstance(BowerPackageUtil.class);

  public static @Nullable JsonFile getContainingBowerJsonFile(@NotNull PsiElement element) {
    PsiFile file = element.getContainingFile();
    if (file instanceof JsonFile && BOWER_JSON.equals(file.getName())) {
      return (JsonFile)file;
    }
    return null;
  }

  public static @NotNull BowerPackageInfo loadPackageInfo(@Nullable ProgressIndicator indicator,
                                                          @NotNull BowerSettings settings,
                                                          @NotNull String packageName) throws ExecutionException {
    ProcessOutput output = BowerCommandLineUtil.runBowerCommand(indicator, settings, "info", packageName, "--json");
    JsonReader reader = new JsonReader(new StringReader(output.getStdout()));
    try {
      reader.setLenient(false);
      JsonParser parser = new JsonParser();
      JsonElement rootElement = parser.parse(reader);
      if (!rootElement.isJsonObject()) {
        throw new RuntimeException("[loading bower package info] Top level element should be an object");
      }
      return parsePackageInfo(rootElement.getAsJsonObject());
    }
    finally {
      try {
        reader.close();
      }
      catch (IOException e) {
        LOG.warn(e);
      }
    }
  }

  private static BowerPackageInfo parsePackageInfo(@NotNull JsonObject root) {
    JsonArray versionsArray = JsonUtil.getChildAsArray(root, "versions");
    List<String> versions = toVersions(versionsArray);
    JsonObject latestObject = JsonUtil.getChildAsObject(root, "latest");
    return new BowerPackageInfo(versions, latestObject);
  }

  private static @NotNull List<String> toVersions(@Nullable JsonArray versionsArray) {
    if (versionsArray == null) {
      return Collections.emptyList();
    }
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    for (JsonElement element : versionsArray) {
      String version = JsonUtil.getString(element);
      if (version != null) {
        builder.add(version);
      }
    }
    return builder.build();
  }
}
