// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

  @Override
  protected ConsoleViewContentType getTextAttributes() {
    return new ConsoleViewContentType("SelfText",
        new TextAttributes(new JBColor(Gray._100, Gray._140), UIUtil.getListBackground(), UIUtil.getListBackground(), null, Font.PLAIN)) {
    };
  }

  @Override
  protected Icon getIcon() {
    return null;
  }
}
