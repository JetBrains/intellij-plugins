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

import com.google.inject.Inject;
import com.google.inject.name.Named;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.text.MessageFormat.format;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
@SuppressWarnings("serial")
public class InfoPanel extends JPanel {

  public static String getHostName() {
    try {
      InetAddress addr = InetAddress.getByName(null);
      return addr.getHostName();
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }

  @Inject
  public InfoPanel(@Named("port") int port , ResourceBundle messageBundle) {
    final String serverUrl = format("http://{0}:{1,number,###}/capture", getHostName(), port);
    final String captureMsg = format(messageBundle.getString("captureLabel"));

    setLayout(new BorderLayout());
    add(new JLabel(captureMsg), NORTH);
    Icon clipboard = new ImageIcon(getClass().getResource("clipboard.png"));
    final JLabel captureLinkLabel = new JLabel(clipboard) {{
      setToolTipText("Copy to clipboard");
      final MouseListener copyToClipboard = new MouseAdapter() {
        private Cursor currentCursor;

        @Override
        public void mouseClicked(MouseEvent e) {
          super.mouseClicked(e);
          try {
            new ClipboardCopier().toClipboard(serverUrl);
          } catch (Exception e1) {
            throw new RuntimeException(e1);
          }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
          super.mouseEntered(e);
          currentCursor = getCursor();
          setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public void mouseExited(MouseEvent e) {
          super.mouseExited(e);
          setCursor(currentCursor);
        }
      };
      addMouseListener(copyToClipboard);
    }};
    add(new JPanel() {{
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      add(captureLinkLabel);
      add(new JTextField(serverUrl) {{ setEditable(false); }});
    }}, SOUTH);
  }
  
  private static class ClipboardCopier implements ClipboardOwner {
    public void toClipboard(String value) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        try {
          sm.checkSystemClipboardAccess();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      Toolkit tk = Toolkit.getDefaultToolkit();
      StringSelection st = new StringSelection(value);
      Clipboard cp = tk.getSystemClipboard();
      cp.setContents(st, this);
    }

    public void lostOwnership(java.awt.datatransfer.Clipboard clipboard, Transferable contents) {
      // this doesn't seem to be important
    }
  }
}
