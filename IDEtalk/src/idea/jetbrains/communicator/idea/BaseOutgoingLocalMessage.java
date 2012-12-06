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
package jetbrains.communicator.idea;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

/**
 * @author Kir
 */
public abstract class BaseOutgoingLocalMessage extends BaseLocalMessage {

  public BaseOutgoingLocalMessage(String messageText) {
    super(messageText, new Date());
  }

  protected ConsoleViewContentType getTextAttributes() {
    return new ConsoleViewContentType("SelfText",
        new TextAttributes(new JBColor(Gray._100, Gray._140), UIUtil.getListBackground(), UIUtil.getListBackground(), null, Font.PLAIN)) {
    };
  }

  protected Icon getIcon() {
    return null;
  }
}
