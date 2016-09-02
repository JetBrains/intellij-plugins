package org.intellij.plugins.markdown.ui.preview.javafx;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.ui.JBColor;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.JBUI;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.JCefPane;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class JavaFxHtmlPanel extends MarkdownHtmlPanel {

  private static final NotNullLazyValue<String> MY_SCRIPTING_LINES = new NotNullLazyValue<String>() {
    @NotNull
    @Override
    protected String compute() {
      final Class<JavaFxHtmlPanel> clazz = JavaFxHtmlPanel.class;
      //noinspection StringBufferReplaceableByString
      return new StringBuilder()
        .append("<script src=\"").append(clazz.getResource("scrollToElement.js")).append("\"></script>\n")
        .append("<script src=\"").append(clazz.getResource("processLinks.js")).append("\"></script>\n")
        .toString();
    }
  };
  
  @NotNull
  private final JPanel myPanelWrapper;
  @NotNull
  private final List<Runnable> myInitActions = new ArrayList<>();
  private final JCefPane myCefPane;
  //private final Component myUIComponent;
  //@Nullable
  //private JFXPanel myPanel;
  //@Nullable
  //private WebView myWebView;
  @Nullable
  private String myInlineCss;
  @NotNull
  private String[] myCssUris = ArrayUtil.EMPTY_STRING_ARRAY;
  @NotNull
  private String myLastRawHtml = "";
  //@NotNull
  //private final ScrollPreservingListener myScrollPreservingListener = new ScrollPreservingListener();
  //@NotNull
  //private final BridgeSettingListener myBridgeSettingListener = new BridgeSettingListener();

  //private final CefBrowser browser_;

  public JavaFxHtmlPanel() {

    myPanelWrapper = new JPanel(new BorderLayout());
    myPanelWrapper.setBackground(JBColor.background());
    myCefPane = new JCefPane("http://picocontainer.com/introduction.html", true, false);
    myCefPane.getBrowser().setZoomLevel(JBUI.scale(2.f));
    myPanelWrapper.add(myCefPane, BorderLayout.CENTER);

    //
    ////System.setProperty("prism.lcdtext", "false");
    ////System.setProperty("prism.text", "t2k");
    //
    //
    CefMessageRouter msgRouter = CefMessageRouter.create();
    msgRouter.addHandler(new CefMessageRouterHandlerAdapter() {
      @Override
      public boolean onQuery(CefBrowser browser, long id, String request, boolean persistent, CefQueryCallback callback) {
        if (request.startsWith("openInExternalBrowser:")) {
          JavaPanelBridge.openInExternalBrowser(request.substring("openInExternalBrowser:".length()));
          return false;
        }
        else {
          return true;
        }
      }
    }, true);
    myCefPane.getClient().addMessageRouter(msgRouter);

    //ApplicationManager.getApplication().invokeLater(() -> PlatformImpl.startup(() -> {
    //  myWebView = new WebView();
    //
    //  updateFontSmoothingType(myWebView,
    //                          MarkdownApplicationSettings.getInstance().getMarkdownPreviewSettings().isUseGrayscaleRendering());
    //  myWebView.setContextMenuEnabled(false);
    //
    //  final WebEngine engine = myWebView.getEngine();
    //  engine.getLoadWorker().stateProperty().addListener(myBridgeSettingListener);
    //  engine.getLoadWorker().stateProperty().addListener(myScrollPreservingListener);
    //
    //  final Scene scene = new Scene(myWebView);
    //
    //  ApplicationManager.getApplication().invokeLater(() -> {
    //    myPanel = new JFXPanelWrapper();
    //    myPanel.setScene(scene);
    //
    //    setHtml("");
    //    for (Runnable action : myInitActions) {
    //      Platform.runLater(action);
    //    }
    //    myInitActions.clear();
    //
    //    myPanelWrapper.add(myPanel, BorderLayout.CENTER);
    //    myPanelWrapper.repaint();
    //  });
    //}));

    //subscribeForGrayscaleSetting();
  }
  
  private void runInPlatformWhenAvailable(@NotNull Runnable runnable) {
    ApplicationManager.getApplication().assertIsDispatchThread();
    //if (myPanel == null) {
    //  myInitActions.add(runnable);
    //}
    //else {
      runnable.run();
      //Platform.runLater(runnable);
    //}
  }

  //private void subscribeForGrayscaleSetting() {
  //  MessageBusConnection settingsConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
  //  MarkdownApplicationSettings.SettingsChangedListener settingsChangedListener =
  //    new MarkdownApplicationSettings.SettingsChangedListener() {
  //      @Override
  //      public void onSettingsChange(@NotNull final MarkdownApplicationSettings settings) {
  //        runInPlatformWhenAvailable(() -> {
  //          if (myWebView != null) {
  //            updateFontSmoothingType(myWebView, settings.getMarkdownPreviewSettings().isUseGrayscaleRendering());
  //          }
  //        });
  //      }
  //    };
  //  settingsConnection.subscribe(MarkdownApplicationSettings.SettingsChangedListener.TOPIC, settingsChangedListener);
  //}

  //private static void updateFontSmoothingType(@NotNull WebView view, boolean isGrayscale) {
  //  final FontSmoothingType typeToSet;
  //  if (isGrayscale) {
  //    typeToSet = FontSmoothingType.GRAY;
  //  }
  //  else {
  //    typeToSet = FontSmoothingType.LCD;
  //  }
  //  view.fontSmoothingTypeProperty().setValue(typeToSet);
  //}

  @NotNull
  @Override
  public JComponent getComponent() {
    return myPanelWrapper;
  }

  @Override
  public void setHtml(@NotNull String html) {
    myLastRawHtml = html;
    final String htmlToRender = prepareHtml(html);

    runInPlatformWhenAvailable(() -> myCefPane.getBrowser().loadString(htmlToRender, "http://example.com"));
  }

  private String prepareHtml(@NotNull String html) {
    return html
      .replace("<head>", "<head>" + getCssLines(myInlineCss, myCssUris))
      .replace("</body>", getScriptingLines() + "</body>");
  }

  @Override
  public void setCSS(@Nullable String inlineCss, @NotNull String... fileUris) {
    myInlineCss = inlineCss;
    myCssUris = fileUris;
    setHtml(myLastRawHtml);
  }

  @Override
  public void render() {
    runInPlatformWhenAvailable(() -> {
      //myCefPane.getBrowser().reload();
      myCefPane.repaint();
    });
  }

  @Override
  public void scrollToMarkdownSrcOffset(final int offset) {
    runInPlatformWhenAvailable(() -> {
      //browser_.executeJavaScript("if ('__IntelliJTools' in window) " +
      //                           "__IntelliJTools.scrollToOffset(" + offset + ", '" + HtmlGenerator.Companion.getSRC_ATTRIBUTE_NAME() + "');", null, 0);


    });
  }

  @Override
  public void dispose() {
    runInPlatformWhenAvailable(() -> {
      myCefPane.dispose();

    });
  }

  //@NotNull
  //private WebView getWebViewGuaranteed () {
  //  if (myWebView == null) {
  //    throw new IllegalStateException("WebView should be initialized by now. Check the caller thread");
  //  }
  //  return myWebView;
  //}

  @NotNull
  private static String getScriptingLines() {
      return MY_SCRIPTING_LINES.getValue();
  }

  @SuppressWarnings("unused")
  public static class JavaPanelBridge {
    public static void openInExternalBrowser(@NotNull String link) {
      if (!BrowserUtil.isAbsoluteURL(link)) {
        try {
          link = new URI("http", link, null).toURL().toString();
        }
        catch (Exception ignore) {
        }
      }

      BrowserUtil.browse(link);
    }

    public void log(@Nullable String text) {
      Logger.getInstance(JavaPanelBridge.class).warn(text);
    }
  }
  
  //private class BridgeSettingListener implements ChangeListener<State> {
  //  @Override
  //  public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
  //      JSObject win
  //        = (JSObject)getWebViewGuaranteed().getEngine().executeScript("window");
  //      win.setMember("JavaPanelBridge", new JavaPanelBridge());
  //  }
  //}
  //
  //private class ScrollPreservingListener implements ChangeListener<State> {
  //  volatile int myScrollY = 0;
  //
  //  @Override
  //  public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
  //    if (newValue == State.RUNNING) {
  //      final Object result =
  //        getWebViewGuaranteed().getEngine().executeScript("document.documentElement.scrollTop || document.body.scrollTop");
  //      if (result instanceof Number) {
  //        myScrollY = ((Number)result).intValue();
  //      }
  //    }
  //    else if (newValue == State.SUCCEEDED) {
  //      getWebViewGuaranteed().getEngine()
  //        .executeScript("document.documentElement.scrollTop = document.body.scrollTop = " + myScrollY);
  //    }
  //  }
  //}
}
