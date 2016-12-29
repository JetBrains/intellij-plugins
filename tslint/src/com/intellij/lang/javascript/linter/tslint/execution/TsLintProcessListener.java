package com.intellij.lang.javascript.linter.tslint.execution;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.idea.RareLogger;
import com.intellij.lang.javascript.linter.JSLinterError;
import com.intellij.lang.javascript.linter.JSLinterErrorBase;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.CharSequenceReader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Irina.Chernushina on 6/4/2015.
 */
public class TsLintProcessListener implements ProcessListener {
  private static final Logger LOG = Logger.getInstance(TsLintConfiguration.LOG_CATEGORY);
  private static final Logger RARE_LOGGER = RareLogger.wrap(LOG, false);
  private final String myPath;
  private final boolean myZeroBasedRowCol;
  private final ArrayList<JSLinterError> myErrors;
  private final StringBuilder myGlobalError;
  private final StringBuilder myText;

  public TsLintProcessListener(String path, boolean zeroBasedRowCol) {
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
    }
    else if (outputType == ProcessOutputTypes.STDOUT) {
      myText.append(text).append("\n");
    }
  }

  public void process() {
    if (StringUtil.isEmptyOrSpaces(myText)) return;
    try {
      JsonElement root = new JsonParser().parse(new CharSequenceReader(myText));
      myErrors.addAll(new TsLintOutputJsonParser(myPath, root, myZeroBasedRowCol).getErrors());
    }
    catch (JsonIOException | JsonSyntaxException e) {
      RARE_LOGGER.info(e.getClass().getName() + " when parsing tslint output: " + myText, e);
    }
  }

  public JSLinterErrorBase getGlobalError() {
    return myGlobalError.length() == 0 ? null : new JSLinterError(0, 0, myGlobalError.toString(), null);
  }

  public List<JSLinterError> getErrors() {
    return myErrors;
  }
}
