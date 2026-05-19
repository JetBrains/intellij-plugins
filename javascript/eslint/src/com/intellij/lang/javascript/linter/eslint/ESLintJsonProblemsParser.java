package com.intellij.lang.javascript.linter.eslint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ESLintJsonProblemsParser {
  private static final Logger LOG = Logger.getInstance(ESLintJsonProblemsParser.class);
  private final JsonObject myObject;
  private final List<@InspectionMessage String> myFileLevelErrors;
  private final @NotNull List<EslintError> myErrors;
  public static final String UNKNOWN_SEVERITY = "Unknown message severity: %s\nbody:\n%s";

  private ESLintJsonProblemsParser(final @NotNull JsonObject object) {
    myObject = object;
    myFileLevelErrors = new SmartList<>();
    myErrors = new SmartList<>();
  }

  public static ESLintJsonProblemsParser parse(final @NotNull JsonObject object) {
    final ESLintJsonProblemsParser parser = new ESLintJsonProblemsParser(object);
    parser.parse();
    return parser;
  }

  public @Nullable @InspectionMessage String getFileLevelError() {
    return myFileLevelErrors.isEmpty()
           ? null
           : myFileLevelErrors.stream().filter(it -> it != null).collect(Collectors.joining("\n")); //NON-NLS
  }

  public @NotNull List<EslintError> getErrors() {
    return myErrors;
  }

  private void addFileLevelError(final @NotNull @InspectionMessage String message) {
    LOG.info(message);
    myFileLevelErrors.add(message);
  }

  private void parse() {
    final String parseProblem = parseBody();
    if (parseProblem != null) myFileLevelErrors.add(parseProblem);
  }

  private @Nullable @InspectionMessage String parseBody() {
    final JsonObject body = JsonUtil.getChildAsObject(myObject, "body");
    JsonArray results = JsonUtil.getChildAsArray(body, "results");
    if (results == null) {
      return EslintBundle.message("eslint.inspections.error.unexpected.language.service.response", myObject.toString());
    }
    if (results.isEmpty()) return null;
    final JsonObject object = ObjectUtils.tryCast(results.get(0), JsonObject.class);
    if (object == null) {
      return EslintBundle.message("eslint.inspections.error.unexpected.language.service.response", myObject.toString());
    }
    final JsonElement messages = object.get("messages");
    if (messages == null) return null;
    final JsonArray values = ObjectUtils.tryCast(messages, JsonArray.class);
    if (values == null) return EslintBundle.message("eslint.inspections.error.messages.not.array", messages.toString());
    if (values.isEmpty()) return null;

    final Stream.Builder<JsonElement> builder = Stream.builder();
    values.forEach(builder);
    myErrors.addAll(builder.build().map(message -> parseMessage(message)).filter(item -> item != null).toList());
    return null;
  }

  private @Nullable EslintError parseMessage(final @NotNull JsonElement message) {
    final JsonObject messageObject = ObjectUtils.tryCast(message, JsonObject.class);
    if (messageObject == null) {
      addFileLevelError(EslintBundle.message("eslint.inspections.error.can.not.parse.message", message.toString()));
      return null;
    }
    final @NlsSafe String messageText = JsonUtil.getChildAsString(messageObject, "message");
    if (messageText == null) {
      LOG.debug("Message without the text: " + message.toString());
      return null; // skip, nothing to report
    }
    final String ruleId = JsonUtil.getChildAsString(messageObject, "ruleId");
    final int line = JsonUtil.getChildAsInteger(messageObject, "line", 0);
    final int column = JsonUtil.getChildAsInteger(messageObject, "column", 0);
    final int endLine = JsonUtil.getChildAsInteger(messageObject, "endLine", -1);
    final int endColumn = JsonUtil.getChildAsInteger(messageObject, "endColumn", -1);
    if (JsonUtil.getChildAsBoolean(messageObject, "fatal", false) && line <= 0 && column <= 0) {
      addFileLevelError(messageText);
      return null;
    }
    final HighlightSeverity hs = readSeverity(messageObject);
    return hs == null
           ? null
           : new EslintError(line, column, endLine, endColumn, messageText, ruleId, hs, parseFixInfo(messageObject),
                             parseSuggestions(messageObject));
  }

  private static @Nullable EslintError.FixInfo parseFixInfo(@Nullable JsonObject object) {
    @NlsSafe String description = JsonUtil.getChildAsString(object, "desc");
    JsonObject fixObject = JsonUtil.getChildAsObject(object, "fix");
    String text = JsonUtil.getChildAsString(fixObject, "text");
    JsonArray range = JsonUtil.getChildAsArray(fixObject, "range");
    if (text == null || range == null || range.size() != 2) {
      return null;
    }
    JsonElement first = range.get(0);
    JsonElement second = range.get(1);
    if (first instanceof JsonPrimitive && ((JsonPrimitive)first).isNumber()
        && second instanceof JsonPrimitive && ((JsonPrimitive)second).isNumber()) {
      return new EslintError.FixInfo(description, first.getAsInt(), second.getAsInt(), text);
    }
    return null;
  }

  private static @NotNull List<EslintError.FixInfo> parseSuggestions(@Nullable JsonObject object) {
    JsonArray suggestions = JsonUtil.getChildAsArray(object, "suggestions");
    if (suggestions == null) return Collections.emptyList();

    List<EslintError.FixInfo> result = new SmartList<>();
    suggestions.forEach(elt -> ContainerUtil.addIfNotNull(result, elt instanceof JsonObject ? parseFixInfo((JsonObject)elt) : null));
    return result;
  }

  private @Nullable HighlightSeverity readSeverity(@NotNull JsonObject message) {
    HighlightSeverity hs = null;
    final JsonElement severityP = message.get("severity");
    if (severityP.isJsonPrimitive() && severityP.getAsJsonPrimitive().isNumber()) {
      final int intV = severityP.getAsJsonPrimitive().getAsInt();
      if (intV == 0) {
        return null;
      }
      else if (intV == 1) {
        hs = HighlightSeverity.WARNING;
      }
      else if (intV == 2) {
        hs = HighlightSeverity.ERROR;
      }
      else {
        addFileLevelError(EslintBundle.message("eslint.inspections.error.unknown.message.severity", intV, message.toString()));
      }
    }
    else {
      if (severityP.isJsonPrimitive() && severityP.getAsJsonPrimitive().isString()) {
        final String stringSeverity = StringUtil.unquoteString(severityP.getAsJsonPrimitive().getAsString());
        if ("error".equals(stringSeverity)) {
          hs = HighlightSeverity.ERROR;
        }
        else if ("warning".equals(stringSeverity)) {
          hs = HighlightSeverity.WARNING;
        }
        else {
          addFileLevelError(
            EslintBundle.message("eslint.inspections.error.unknown.message.severity", stringSeverity, message.toString()));
        }
      }
      else {
        addFileLevelError(
          EslintBundle.message("eslint.inspections.error.unknown.message.severity", severityP.toString(), message.toString()));
      }
    }
    return hs;
  }
}
