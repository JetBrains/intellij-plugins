package com.jetbrains.lang.dart.ide.runner.test;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DartConsoleInputFilter implements InputFilter {
  private static String TEST_RUN_COMMAND = "pub.dart.snapshot run test:test";
  private static String NEWLINE = "\n"; // TODO Will this work on Windows?
  private static String PASS_CODE = "\u001B[32m";
  private static String FAIL_CODE = "\u001B[0;31m";
  private static Pattern TIME_FORMAT = Pattern.compile("\\d+:\\d\\d");

  private Project myProject;
  private boolean isFirstTime = true;
  private boolean isActive = false;
  private boolean didFail = false;
  private State myState = State.Init;
  private String myPass, myFail;

  private enum State {Init, Info, Timestamp, Pass, Fail, Message, Error}

  public DartConsoleInputFilter(Project project) {
    myProject = project; // TODO If not Dart project then disable this filter.
  }

  /**
   * @param text        the text to be filtered.
   * @param contentType the content type of filtered text
   * @return <tt>null</tt>, if there was no match, otherwise, a list of pairs like ('string to use', 'content type to use')
   */
  @Nullable
  public List<Pair<String, ConsoleViewContentType>> applyFilter(String text, ConsoleViewContentType contentType) {
    if (text == null) {
      return null;
    }
    if (isFirstTime) {
      if (text.indexOf(TEST_RUN_COMMAND) > 0) {
        isActive = true;
      }
    }
    if (isActive) {
      return accumulate(text, contentType);
    }
    else {
      return null;
    }
  }

  @Nullable
  private List<Pair<String, ConsoleViewContentType>> accumulate(@NotNull String text, ConsoleViewContentType contentType) {
    List<Pair<String, ConsoleViewContentType>> result = new ArrayList<Pair<String, ConsoleViewContentType>>();
    State currentState = myState, nextState = myState;
    boolean fastExit = false;
    switch (currentState) {
      case Init:
        if (NEWLINE.equals(text)) {
          nextState = State.Timestamp;
          fastExit = true;
        }
        break;
      case Info:
        break;
      case Timestamp:
        if (TIME_FORMAT.matcher(text).matches()) {
          nextState = State.Pass;
          // emit test-found string
        }
        break;
      case Pass:
        // fall through
      case Fail:
        String contentCode = contentType.toString();
        if (PASS_CODE.equals(contentCode)) {
          currentState = State.Pass;
          if (text.equals(myPass)) {
            nextState = State.Fail;
          } else {
            nextState = State.Message;
            myPass = text;
          }
          // emit test-pass string
        } else if (FAIL_CODE.equals(contentCode)) {
          currentState = State.Fail;
          if (text.equals(myFail)) {
            nextState = State.Message;
            didFail = true;
          } else {
            nextState = State.Error;
            myFail = text;
            // emit test-fail string
          }
        }
        break;
      case Message:
        nextState = State.Timestamp;
        if (didFail) {
          didFail = false;
          result.add(Pair.create("", contentType));
          return result; // EARLY EXIT
        }
        // emit test-message string
        break;
      case Error:
        break;
    }
    myState = nextState;
    if (fastExit) {
      return null;
    }
    Pair<String, ConsoleViewContentType> pair = Pair.create(text, contentType);
    result.add(pair);
    return result;
  }
}
