/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package jetbrains.communicator.idea.sendMessage;

import com.intellij.execution.ui.ConsoleView;
import icons.IdeTalkCoreIcons;
import jetbrains.communicator.core.transport.MessageEvent;
import jetbrains.communicator.idea.BaseIncomingLocalMessage;
import jetbrains.communicator.util.CommunicatorStrings;

import javax.swing.*;

/**
 * @author Kir
 */
public class IncomingLocalMessage extends BaseIncomingLocalMessage {

  public IncomingLocalMessage(MessageEvent event) {
    super(event.getMessage(), event.getWhen());
  }

  @Override
  protected void outputMessage(ConsoleView consoleView) {
    printComment(consoleView);
  }

  @Override
  public String getTitle() {
    return CommunicatorStrings.getMsg("message");
  }

  @Override
  protected Icon getIcon() {
    return IdeTalkCoreIcons.Message;
  }
}
