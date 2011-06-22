/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea.ui;

import javax.swing.*;
import java.awt.*;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
@SuppressWarnings("serial")
public class LogPanel extends JPanel implements LogPrinter {
  private JTextArea textArea;

  public LogPanel() {
    setLayout(new BorderLayout());
    textArea = new JTextArea() {{
      setFont(new Font("Courier New", Font.PLAIN, 12));
      setLineWrap(false);
      setEditable(false);
    }};

    add(new JLabel("Log:"), BorderLayout.NORTH);
    add(new JScrollPane(textArea) {{
      setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }}, BorderLayout.CENTER);

  }

  public void logLine(String logRecord) {
    textArea.append(logRecord + "\n");
    textArea.select(textArea.getText().length(), textArea.getText().length());
  }
}
