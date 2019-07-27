/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package jetbrains.communicator.idea.sendMessage;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import icons.IdeTalkCoreIcons;
import jetbrains.communicator.core.transport.StacktraceEvent;
import jetbrains.communicator.idea.BaseIncomingLocalMessage;
import jetbrains.communicator.util.CommunicatorStrings;

import javax.swing.*;

/**
 * @author Kir
 */
public class IncomingStacktraceMessage extends BaseIncomingLocalMessage {
  private final String myStacktrace;

  public IncomingStacktraceMessage(StacktraceEvent event) {
    super(event.getMessage(), event.getWhen());
    myStacktrace = event.getStacktrace();
  }

  @Override
  public boolean containsString(String searchString) {
    return super.containsString(searchString) || CommunicatorStrings.containedIn(myStacktrace, searchString);
  }

  @Override
  protected void outputMessage(ConsoleView consoleView) {
    if (!com.intellij.openapi.util.text.StringUtil.isEmptyOrSpaces(getComment())) {
      printComment(consoleView);
      consoleView.print("-----------------------------------------------------\n", ConsoleViewContentType.SYSTEM_OUTPUT);
    }
    consoleView.print(myStacktrace, ConsoleViewContentType.ERROR_OUTPUT);
    consoleView.print("\n", ConsoleViewContentType.NORMAL_OUTPUT);
  }

  @Override
  public String getTitle() {
    return CommunicatorStrings.getMsg("stacktrace");
  }

  @Override
  protected Icon getIcon() {
    return IdeTalkCoreIcons.Stacktrace;
  }
}
