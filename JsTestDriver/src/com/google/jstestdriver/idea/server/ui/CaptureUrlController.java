package com.google.jstestdriver.idea.server.ui;

import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.idea.MessageBundle;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;

/**
 * @author Sergey Simonchik
 */
public class CaptureUrlController {

  private static final Logger LOG = Logger.getInstance(CaptureUrlController.class);

  private final JTextField myCaptureUrlTextField;
  private final JComponent myComponent;
  private final ServerListener myServerListener;

  public CaptureUrlController() {
    myCaptureUrlTextField = createCaptureUrlTextField();

    CopyAction copyAction = new CopyAction();
    DefaultActionGroup actionGroup = new DefaultActionGroup();
    actionGroup.add(copyAction);

    ActionPopupMenu actionPopupMenu = ActionManager.getInstance()
      .createActionPopupMenu(JstdToolWindowPanel.PLACE, actionGroup);
    JPopupMenu popupMenu = actionPopupMenu.getComponent();
    myCaptureUrlTextField.setComponentPopupMenu(popupMenu);

    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel(MessageBundle.getCaptureUrlMessage()), new GridBagConstraints(
      0, 0,
      1, 1,
      0.0, 0.0,
      GridBagConstraints.CENTER,
      GridBagConstraints.NONE,
      new Insets(0, 0, 0, 0),
      0, 0
    ));
    panel.add(myCaptureUrlTextField, new GridBagConstraints(
      1, 0,
      1, 1,
      1.0, 0.0,
      GridBagConstraints.CENTER,
      GridBagConstraints.HORIZONTAL,
      new Insets(0, 0, 0, 0),
      0, 0
    ));
    final ActionButton copyButton = new ActionButton(
      copyAction,
      copyAction.getTemplatePresentation().clone(),
      JstdToolWindowPanel.PLACE,
      new Dimension(22, 22)
    );
    panel.add(copyButton, new GridBagConstraints(
      2, 0,
      1, 1,
      0.0, 0.0,
      GridBagConstraints.CENTER,
      GridBagConstraints.NONE,
      new Insets(0, 0, 0, 0),
      0, 0
    ));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 5, 0, 3));
    myComponent = panel;

    myServerListener = new EdtServerAdapter() {
      @Override
      public void serverStateChanged(boolean started) {
        if (started) {
          String serverUrl = MessageFormat.format("http://{0}:{1,number,###}/capture",
                                                  getHostName(), JstdToolWindowPanel.serverPort);
          myCaptureUrlTextField.setText(serverUrl);
          myCaptureUrlTextField.requestFocusInWindow();
          myCaptureUrlTextField.selectAll();
        } else {
          myCaptureUrlTextField.setText("");
        }
        copyButton.setEnabled(started);
      }
    };
  }

  @NotNull
  public JComponent getComponent() {
    return myComponent;
  }

  @NotNull
  public JTextField getCaptureUrlTextField() {
    return myCaptureUrlTextField;
  }

  @NotNull
  public ServerListener getServerListener() {
    return myServerListener;
  }

  private static String getHostName() {
    try {
      InetAddress address = InetAddress.getByName(null);
      return address.getHostName();
    } catch (UnknownHostException e) {
      throw new RuntimeException("Can't find hostname", e);
    }
  }

  @NotNull
  private static JTextField createCaptureUrlTextField() {
    final JTextField textField = new JTextField();
    textField.setEditable(false);
    textField.setBackground(Color.WHITE);
    textField.getCaret().setVisible(true);
    textField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    final Runnable selectAll = new Runnable() {
      @Override
      public void run() {
        textField.getCaret().setVisible(true);
        textField.selectAll();
      }
    };
    textField.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
          selectAll.run();
        }
      }
    });
    textField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        selectAll.run();
      }
    });
    return textField;
  }

  private class CopyAction extends AnAction {

    private CopyAction() {
      super("Copy URL", "Copy capturing URL", PlatformIcons.COPY_ICON);
      ShortcutSet shortcutSet = new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
      registerCustomShortcutSet(shortcutSet, myCaptureUrlTextField);
    }

    @Override
    public void update(AnActionEvent e) {
      boolean enabled = StringUtil.isNotEmpty(myCaptureUrlTextField.getText());
      e.getPresentation().setEnabled(enabled);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      String url = myCaptureUrlTextField.getText();
      new ClipboardCopier().toClipboard(url);
    }
  }

  private static class ClipboardCopier implements ClipboardOwner {

    public void toClipboard(String value) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        try {
          sm.checkSystemClipboardAccess();
        } catch (Exception e) {
          LOG.warn("[JsTestDriver] Can't copy capture url: no access to system clipboard", e);
          return;
        }
      }
      Toolkit tk = Toolkit.getDefaultToolkit();
      StringSelection st = new StringSelection(value);
      Clipboard cp = tk.getSystemClipboard();
      cp.setContents(st, this);
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
      // this doesn't seem to be important
    }
  }

}
