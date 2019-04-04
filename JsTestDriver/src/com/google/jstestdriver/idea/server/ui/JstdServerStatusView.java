package com.google.jstestdriver.idea.server.ui;

import com.google.jstestdriver.idea.server.JstdBrowserInfo;
import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.idea.server.JstdServerLifeCycleAdapter;
import com.google.jstestdriver.idea.server.JstdServerSettings;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.SideBorder;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.SwingHelper;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public class JstdServerStatusView {

  private final JEditorPane myInfoPane;
  private final JLabel myBrowsersLabel;
  private final JPanel myRoot;
  private final Disposable myDisposable;

  public JstdServerStatusView(@NotNull Disposable disposable) {
    myDisposable = disposable;
    myInfoPane = createInfoPane();
    myBrowsersLabel = new JLabel();
    JPanel panel = SwingHelper.newHorizontalPanel(
      Component.CENTER_ALIGNMENT,
      myInfoPane,
      Box.createHorizontalGlue(),
      myBrowsersLabel
    );
    panel.setBorder(BorderFactory.createEmptyBorder(3, 7, 5, 12));
    JPanel wrap = new JPanel(new BorderLayout(0, 0));
    wrap.add(panel, BorderLayout.CENTER);
    wrap.setBorder(new SideBorder(UIUtil.getBorderColor(), SideBorder.BOTTOM));
    myRoot = wrap;
  }

  @NotNull
  public JComponent getComponent() {
    return myRoot;
  }

  public void attachToServer(@NotNull final JstdServer server) {
    setInfoBodyHtml("Starting up...");
    server.addLifeCycleListener(new JstdServerLifeCycleAdapter() {
      @Override
      public void onServerStarted() {
        JstdServerSettings settings = server.getSettings();
        String url = "http://127.0.0.1:" + settings.getPort() + "/capture";
        setInfoBodyHtml("To capture a browser open <a href='" + url + "'>" + url + "</a>");
        updateCapturedBrowsersCount(server);
      }

      @Override
      public void onServerStopped() {
        setInfoBodyHtml("Not running");
        updateCapturedBrowsersCount(server);
      }

      @Override
      public void onBrowserCaptured(@NotNull JstdBrowserInfo info) {
        updateCapturedBrowsersCount(server);
      }

      @Override
      public void onBrowserPanicked(@NotNull JstdBrowserInfo info) {
        updateCapturedBrowsersCount(server);
      }
    }, myDisposable);
  }

  private void updateCapturedBrowsersCount(@NotNull JstdServer server) {
    final int capturedBrowsers;
    if (server.isStopped()) {
      capturedBrowsers = 0;
    }
    else {
      capturedBrowsers = server.getCapturedBrowsers().size();
    }
    myBrowsersLabel.setText("Captured browsers: " + capturedBrowsers);
  }

  private void setInfoBodyHtml(@NotNull String htmlBody) {
    String styleTag = UIUtil.getCssFontDeclaration(UIUtil.getLabelFont());
    myInfoPane.setText("<html>" + styleTag + "<body>" + htmlBody + "</body></html>");
  }

  @NotNull
  private static JEditorPane createInfoPane() {
    JEditorPane textPane = new JEditorPane();
    textPane.setFont(UIUtil.getLabelFont());
    textPane.setContentType(UIUtil.HTML_MIME);
    textPane.setEditable(false);
    textPane.setOpaque(false);
    textPane.setBackground(UIUtil.TRANSPARENT_COLOR);
    installLinkHandler(textPane);
    return textPane;
  }

  private static void installLinkHandler(@NotNull JEditorPane pane) {
    final Ref<String> urlRef = Ref.create(null);
    pane.addHyperlinkListener(new HyperlinkListener() {
      @Override
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
          urlRef.set(null);
        }
        else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
          urlRef.set(e.getDescription());
        }
        else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          BrowserUtil.browse(e.getDescription());
        }
      }
    });
    final CopyLinkAction copyLinkAction = new CopyLinkAction();
    final OpenLinkInBrowser openLinkInBrowserAction = new OpenLinkInBrowser();
    final DefaultActionGroup group = new DefaultActionGroup(openLinkInBrowserAction, copyLinkAction);
    pane.addMouseListener(new PopupHandler() {
      @Override
      public void invokePopup(Component comp, int x, int y) {
        String url = urlRef.get();
        copyLinkAction.setUrl(url);
        openLinkInBrowserAction.setUrl(url);
        if (url != null) {
          ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.UNKNOWN, group);
          popupMenu.getComponent().show(comp, x, y);
        }
      }
    });
  }

  private static class OpenLinkInBrowser extends AnAction {

    private String myUrl;

    private OpenLinkInBrowser() {
      super("Open Link in Browser", null, PlatformIcons.WEB_ICON);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
      e.getPresentation().setEnabledAndVisible(myUrl != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
      String url = myUrl;
      if (url != null) {
        BrowserUtil.browse(url);
      }
    }

    public void setUrl(@Nullable String url) {
      myUrl = url;
    }
  }

  private static class CopyLinkAction extends AnAction {

    private String myUrl;

    private CopyLinkAction() {
      super("Copy Link Address", null, PlatformIcons.COPY_ICON);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
      e.getPresentation().setEnabledAndVisible(myUrl != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
      String url = myUrl;
      if (url != null) {
        Transferable content = new StringSelection(url);
        CopyPasteManager.getInstance().setContents(content);
      }
    }

    public void setUrl(@Nullable String url) {
      myUrl = url;
    }
  }
}
