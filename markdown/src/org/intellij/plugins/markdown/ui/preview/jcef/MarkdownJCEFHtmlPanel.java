// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.intellij.plugins.markdown.ui.preview.jcef;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.ui.jcef.JCEFHtmlPanel;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import netscape.javascript.JSObject;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.plugins.markdown.MarkdownBundle;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;
import org.intellij.plugins.markdown.ui.preview.PreviewStaticServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MarkdownJCEFHtmlPanel extends JCEFHtmlPanel implements MarkdownHtmlPanel {

  private static final NotNullLazyValue<String> MY_SCRIPTING_LINES = new NotNullLazyValue<String>() {
    @NotNull
    @Override
    protected String compute() {
      return SCRIPTS.stream()
        .map(s -> "<script src=\"" + PreviewStaticServer.getScriptUrl(s) + "\"></script>")
        .reduce((s, s2) -> s + "\n" + s2)
        .orElseGet(String::new);
    }
  };

  @NotNull
  private String[] myCssUris = ArrayUtil.EMPTY_STRING_ARRAY;
  @NotNull
  private String myCSP = "";
  @NotNull
  private String myLastRawHtml = "";
  /*
  @NotNull
  private final ScrollPreservingListener myScrollPreservingListener = new ScrollPreservingListener();
  @NotNull
  private final BridgeSettingListener myBridgeSettingListener = new BridgeSettingListener();
  */

  public MarkdownJCEFHtmlPanel() {
    super();
  }

  @Override
  public void setHtml(@NotNull String html) {
    myLastRawHtml = html;
    super.setHtml(html);
  }

  @NotNull
  @Override
  protected String prepareHtml(@NotNull String html) {
    //return ImageRefreshFix.setStamps(html
    //                                   .replace("<head>", "<head>"
    //                                                      + "<meta http-equiv=\"Content-Security-Policy\" content=\"" + myCSP + "\"/>"
    //                                                      + MarkdownHtmlPanel.getCssLines(null, myCssUris) + "\n" + getScriptingLines()));
    return super.prepareHtml(html);
  }

  @Override
  public void setCSS(@Nullable String inlineCss, @NotNull String... fileUris) {
    PreviewStaticServer.getInstance().setInlineStyle(inlineCss);
    myCssUris = inlineCss == null ? fileUris
                                  : ArrayUtil
                  .mergeArrays(fileUris, PreviewStaticServer.getStyleUrl(PreviewStaticServer.INLINE_CSS_FILENAME));
    myCSP = PreviewStaticServer.createCSP(ContainerUtil.map(SCRIPTS, s -> PreviewStaticServer.getScriptUrl(s)),
                                          ContainerUtil.concat(
                                            ContainerUtil.map(STYLES, s -> PreviewStaticServer.getStyleUrl(s)),
                                            ContainerUtil.filter(fileUris, s -> s.startsWith("http://") || s.startsWith("https://"))
                                          ));
    setHtml(myLastRawHtml);
  }

  @Override
  public void scrollToMarkdownSrcOffset(final int offset) {
    getBrowser().executeJavaScript(
        "if ('__IntelliJTools' in window) " +
        "__IntelliJTools.scrollToOffset(" + offset + ", '" + HtmlGenerator.Companion.getSRC_ATTRIBUTE_NAME() + "');",
      getBrowser().getURL(), 0);
    //final Object result = getBrowser().executeJavaScript(
    //    "document.documentElement.scrollTop || (document.body && document.body.scrollTop)",
    //    getBrowser().getURL(), 0);
    //if (result instanceof Number) {
    //  myScrollPreservingListener.myScrollY = ((Number)result).intValue();
    //}
  }

  @Override
  public void dispose() {
    //runInPlatformWhenAvailable(() -> {
    //  getWebViewGuaranteed().getEngine().getLoadWorker().stateProperty().removeListener(myScrollPreservingListener);
    //  getWebViewGuaranteed().getEngine().getLoadWorker().stateProperty().removeListener(myBridgeSettingListener);
    //});
  }

  @NotNull
  private static String getScriptingLines() {
    return MY_SCRIPTING_LINES.getValue();
  }

  /*
  @SuppressWarnings("unused")
  public static class JavaPanelBridge {
    static final JavaPanelBridge INSTANCE = new JavaPanelBridge();
    private static final NotificationGroup MARKDOWN_NOTIFICATION_GROUP =
      NotificationGroup.toolWindowGroup(MarkdownBundle.message("markdown.navigate.to.header.group"), ToolWindowId.MESSAGES_WINDOW);

    public void openInExternalBrowser(@NotNull String link) {
      SafeOpener.openLink(link);
    }

    public void log(@Nullable String text) {
      Logger.getInstance(JavaPanelBridge.class).warn(text);
    }
  }

  private class BridgeSettingListener implements ChangeListener<State> {
    @Override
    public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
      JSObject win
        = (JSObject)getWebViewGuaranteed().getEngine().executeScript("window");
      win.setMember("JavaPanelBridge", JavaPanelBridge.INSTANCE);
    }
  }

  private class ScrollPreservingListener implements ChangeListener<State> {
    volatile int myScrollY = 0;

    @Override
    public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
      if (newValue == State.RUNNING) {
        final Object result =
          getWebViewGuaranteed().getEngine().executeScript("document.documentElement.scrollTop || document.body.scrollTop");
        if (result instanceof Number) {
          myScrollY = ((Number)result).intValue();
        }
      }
      else if (newValue == State.SUCCEEDED) {
        getWebViewGuaranteed().getEngine()
          .executeScript("document.documentElement.scrollTop = ({} || document.body).scrollTop = " + myScrollY);
      }
    }
  }
  */
}
