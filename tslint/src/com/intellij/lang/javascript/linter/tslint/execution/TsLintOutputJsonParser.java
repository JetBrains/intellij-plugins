// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.execution;


import com.google.gson.*;
import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.javascript.service.protocol.LocalFilePath;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.lang.javascript.linter.tslint.highlight.TsLintFixInfo.createTsLintFixInfo;

public final class TsLintOutputJsonParser {

  private static final Logger LOG = TslintUtil.LOG;
  public static final String FIX_PROPERTY = "fix";

  @Nullable
  private final String myPath;
  private final boolean myMyZeroBasedRowCol;

  @NotNull
  private final List<TsLinterError> myErrors;
  @NotNull
  private final Gson myGson;

  public TsLintOutputJsonParser(@Nullable String path, final JsonElement root, boolean zeroBasedRowCol, @NotNull Gson gson) {
    myPath = path;
    myMyZeroBasedRowCol = zeroBasedRowCol;
    myGson = gson;

    if (root instanceof JsonNull || !root.isJsonArray()) {
      logError("root element is not array");
      myErrors = ContainerUtil.emptyList();
    }
    else {
      final JsonArray array = root.getAsJsonArray();
      final int size = array.size();
      ArrayList<TsLinterError> errors = new ArrayList<>();
      for (int i = 0; i < size; i++) {
        final JsonElement element = array.get(i);
        if (!element.isJsonObject()) {
          logError("element under root is not object");
        }
        else {
          final JsonObject object = element.getAsJsonObject();
          errors.addAll(processError(object));
        }
      }
      myErrors = errors;
    }
  }

  private List<TsLinterError> processError(JsonObject object) {
    List<TsLinterError> result = new ArrayList<>();
    final JsonElement name = object.get("name");
    if (name == null) {
      logError("no name for error object");
      return result;
    }
    final JsonElement failure = object.get("failure");
    if (failure == null || !(failure.isJsonPrimitive() && failure.getAsJsonPrimitive().isString())) {
      logError("no failure for error object");
      return result;
    }
    final JsonElement startPosition = object.get("startPosition");
    if (startPosition == null || !startPosition.isJsonObject()) {
      logError("no startPosition for error object");
      return result;
    }
    final JsonElement endPosition = object.get("endPosition");
    if (endPosition == null || !endPosition.isJsonObject()) {
      logError("no endPosition for error object");
      return result;
    }
    final JsonElement ruleName = object.get("ruleName");
    if (ruleName == null || !(ruleName.isJsonPrimitive() && ruleName.getAsJsonPrimitive().isString())) {
      logError("no rule name for error object");
      return result;
    }
    String severityStr = JsonUtil.getChildAsString(object, "ruleSeverity");
    final Pair<Integer, Integer> start = parseLineColumn(startPosition.getAsJsonObject());
    final Pair<Integer, Integer> end = parseLineColumn(endPosition.getAsJsonObject());
    if (start == null || end == null) return result;

    JsonElement element = object.get(FIX_PROPERTY);

    LocalFilePath localfilePath = myGson.getAdapter(LocalFilePath.class).fromJsonTree(name);
    String filePath = LocalFilePath.getPath(localfilePath);
    result.add(new TsLinterError(StringUtil.isEmpty(filePath) ? myPath : filePath,
                                 start.getFirst(),
                                 start.getSecond(),
                                 end.getFirst(),
                                 end.getSecond(),
                                 failure.getAsString(), //NON-NLS
                                 ruleName.getAsString(),
                                 StringUtil.equalsIgnoreCase(severityStr, "warning"),
                                 createTsLintFixInfo(element)));

    return result;
  }

  private Pair<Integer, Integer> parseLineColumn(JsonObject position) {
    final JsonElement line = position.get("line");
    if (line == null || !(line.isJsonPrimitive() && line.getAsJsonPrimitive().isNumber())) {
      logError("no line for position");
      return null;
    }
    final JsonElement character = position.get("character");
    if (character == null || !(character.isJsonPrimitive() && character.getAsJsonPrimitive().isNumber())) {
      logError("no character for position");
      return null;
    }
    if (myMyZeroBasedRowCol) return Pair.create(line.getAsJsonPrimitive().getAsInt(), character.getAsJsonPrimitive().getAsInt());
    return Pair.create(line.getAsJsonPrimitive().getAsInt() + 1, character.getAsJsonPrimitive().getAsInt() + 1);
  }

  @NotNull
  public List<TsLinterError> getErrors() {
    return myErrors;
  }

  private static void logError(String s) {
    LOG.info("TSLint result parsing: " + s);
  }

  public static boolean isVersionZeroBased(SemVer tsLintVersion) {
    return tsLintVersion != null && (tsLintVersion.getMajor() < 2 ||
                                     tsLintVersion.getMajor() == 2 && tsLintVersion.getMinor() <= 1);
  }
}
