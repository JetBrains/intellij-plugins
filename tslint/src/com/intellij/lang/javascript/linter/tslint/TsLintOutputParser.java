package com.intellij.lang.javascript.linter.tslint;

import com.google.gson.*;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.idea.RareLogger;
import com.intellij.lang.javascript.linter.JSLinterError;
import com.intellij.lang.javascript.linter.JSLinterErrorBase;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.util.text.CharSequenceReader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Irina.Chernushina on 6/4/2015.
 */
public class TsLintOutputParser implements ProcessListener {
  private static final Logger LOG = Logger.getInstance(TsLintConfiguration.LOG_CATEGORY);
  private static final Logger RARE_LOGGER = RareLogger.wrap(LOG, false);
  private final String myPath;
  private final boolean myZeroBasedRowCol;
  private final ArrayList<JSLinterError> myErrors;
  private final StringBuilder myGlobalError;
  private final StringBuilder myText;

  public TsLintOutputParser(String path, boolean zeroBasedRowCol) {
    myPath = path;
    myZeroBasedRowCol = zeroBasedRowCol;
    myErrors = new ArrayList<>();
    myGlobalError = new StringBuilder();
    myText = new StringBuilder();
  }

  @Override
  public void startNotified(ProcessEvent event) {
  }

  @Override
  public void processTerminated(ProcessEvent event) {
  }

  @Override
  public void processWillTerminate(ProcessEvent event, boolean willBeDestroyed) {
  }

  @Override
  public void onTextAvailable(ProcessEvent event, Key outputType) {
    final String text = event.getText().trim();
    if (outputType == ProcessOutputTypes.STDERR) {
      myGlobalError.append(text).append("\n");
    } else if (outputType == ProcessOutputTypes.STDOUT) {
      myText.append(text).append("\n");
    }
  }

  public void process() {
    final JsonElement root;
    try {
      root = new JsonParser().parse(new CharSequenceReader(myText));
    } catch (JsonIOException e) {
      RARE_LOGGER.info(e.getClass().getName() + " when parsing tslint output: " + myText, e);
      return;
    } catch (JsonSyntaxException e) {
      RARE_LOGGER.info(e.getClass().getName() + " when parsing tslint output: " + myText, e);
      return;
    }
    if (root instanceof JsonNull) return;
    if (! root.isJsonArray()) {
      logError("root element is not array");
      return;
    }
    final JsonArray array = root.getAsJsonArray();
    final int size = array.size();
    for (int i = 0; i < size; i++) {
      final JsonElement element = array.get(i);
      if (! element.isJsonObject()) {
        logError("element under root is not object");
        return;
      }
      final JsonObject object = element.getAsJsonObject();
      processError(object);
    }
  }

  private void processError(JsonObject object) {
    final JsonElement name = object.get("name");
    if (name == null) {
      logError("no name for error object");
      return;
    }
    final JsonElement failure = object.get("failure");
    if (failure == null || ! (failure.isJsonPrimitive() && failure.getAsJsonPrimitive().isString())) {
      logError("no failure for error object");
      return;
    }
    final JsonElement startPosition = object.get("startPosition");
    if (startPosition == null || ! startPosition.isJsonObject()) {
      logError("no startPosition for error object");
      return;
    }
    final JsonElement endPosition = object.get("endPosition");
    if (endPosition == null || ! endPosition.isJsonObject()) {
      logError("no endPosition for error object");
      return;
    }
    final JsonElement ruleName = object.get("ruleName");
    if (ruleName == null || ! (ruleName.isJsonPrimitive() && ruleName.getAsJsonPrimitive().isString())) {
      logError("no rule name for error object");
      return;
    }
    final Pair<Integer, Integer> start = parseLineColumn(startPosition.getAsJsonObject());
    final Pair<Integer, Integer> end = parseLineColumn(endPosition.getAsJsonObject());
    if (start == null || end == null) return;

    myErrors.add(new TsLinterError(start.getFirst(), start.getSecond(), failure.getAsString(), ruleName.getAsString(),
                                   end.getFirst(), end.getSecond()));
  }

  private Pair<Integer, Integer> parseLineColumn(JsonObject position) {
    final JsonElement line = position.get("line");
    if (line == null || ! (line.isJsonPrimitive() && line.getAsJsonPrimitive().isNumber())) {
      logError("no line for position");
      return null;
    }
    final JsonElement character = position.get("character");
    if (character == null || ! (character.isJsonPrimitive() && character.getAsJsonPrimitive().isNumber())) {
      logError("no line for position");
      return null;
    }
    if (myZeroBasedRowCol) return Pair.create(line.getAsJsonPrimitive().getAsInt(), character.getAsJsonPrimitive().getAsInt());
    return Pair.create(line.getAsJsonPrimitive().getAsInt() + 1, character.getAsJsonPrimitive().getAsInt() + 1);
  }


  private void logError(final String error) {
    LOG.info("Error when parsing tslint file: " + error + ", file: " + myPath + ", text: " + myText);
  }

  public JSLinterErrorBase getGlobalError() {
    return myGlobalError.length() == 0 ? null : new JSLinterError(0,0, myGlobalError.toString(), null);
  }

  public List<JSLinterError> getErrors() {
    return myErrors;
  }
}
