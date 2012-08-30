/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.communicator.idea.sendMessage;

import com.intellij.execution.ui.ConsoleView;
import icons.IdetalkCoreIcons;
import jetbrains.communicator.core.transport.MessageEvent;
import jetbrains.communicator.idea.BaseIncomingLocalMessage;
import jetbrains.communicator.util.StringUtil;

import javax.swing.*;

/**
 * @author Kir
 */
public class IncomingLocalMessage extends BaseIncomingLocalMessage {

  public IncomingLocalMessage(MessageEvent event) {
    super(event.getMessage(), event.getWhen());
  }

  protected void outputMessage(ConsoleView consoleView) {
    printComment(consoleView);
  }

  public String getTitle() {
    return StringUtil.getMsg("message");
  }

  protected Icon getIcon() {
    return IdetalkCoreIcons.Message;
  }
}
