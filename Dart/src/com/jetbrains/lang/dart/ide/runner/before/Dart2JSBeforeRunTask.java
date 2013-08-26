package com.jetbrains.lang.dart.ide.runner.before;

import com.intellij.execution.BeforeRunTask;
import com.intellij.openapi.util.Key;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by fedorkorotkov.
 */
public class Dart2JSBeforeRunTask extends BeforeRunTask<Dart2JSBeforeRunTask> {
  public static final String DART_INPUT_FILE = "DART_INPUT_FILE";
  public static final String DART_OUTPUT_FILE = "DART_OUTPUT_FILE";
  public static final String DART_CHECKED_MODE = "DART_CHECKED_MODE";
  public static final String DART_MINIFY_MODE = "DART_MINIFY_MODE";

  protected String myInputFilePath = null;
  protected String myOutputFilePath = null;
  protected boolean myCheckedMode = false;
  protected boolean myMinifyMode = false;

  protected Dart2JSBeforeRunTask(@NotNull Key<Dart2JSBeforeRunTask> providerId) {
    super(providerId);
  }

  @Override
  public void writeExternal(Element element) {
    super.writeExternal(element);
    if (myInputFilePath != null) {
      element.setAttribute(DART_INPUT_FILE, myInputFilePath);
    }
    if (myOutputFilePath != null) {
      element.setAttribute(DART_OUTPUT_FILE, myOutputFilePath);
    }
    element.setAttribute(DART_CHECKED_MODE, Boolean.toString(myCheckedMode));
    element.setAttribute(DART_MINIFY_MODE, Boolean.toString(myMinifyMode));
  }

  @Override
  public void readExternal(Element element) {
    super.readExternal(element);
    setInputFilePath(element.getAttributeValue(DART_INPUT_FILE));
    setOutputFilePath(element.getAttributeValue(DART_OUTPUT_FILE));
    setCheckedMode(Boolean.valueOf(element.getAttributeValue(DART_CHECKED_MODE)));
    setMinifyMode(Boolean.valueOf(element.getAttributeValue(DART_MINIFY_MODE)));
  }

  @Nullable
  public String getInputFilePath() {
    return myInputFilePath;
  }

  public void setInputFilePath(@Nullable String inputFilePath) {
    myInputFilePath = inputFilePath;
  }

  @Nullable
  public String getOutputFilePath() {
    return myOutputFilePath;
  }

  public void setOutputFilePath(String outputFilePath) {
    myOutputFilePath = outputFilePath;
  }

  public boolean isCheckedMode() {
    return myCheckedMode;
  }

  public void setCheckedMode(boolean checkedMode) {
    myCheckedMode = checkedMode;
  }

  public boolean isMinifyMode() {
    return myMinifyMode;
  }

  public void setMinifyMode(boolean minifyMode) {
    myMinifyMode = minifyMode;
  }
}
