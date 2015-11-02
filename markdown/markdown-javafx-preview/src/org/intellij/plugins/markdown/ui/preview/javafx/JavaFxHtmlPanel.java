package org.intellij.plugins.markdown.ui.preview.javafx;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.ArrayUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;
import org.intellij.plugins.markdown.util.MarkdownPluginUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.regex.Pattern;

// Instantiated by reflection
@SuppressWarnings("unused")
public class JavaFxHtmlPanel extends MarkdownHtmlPanel {

  public static final Pattern SVG_REPLACE_PATTERN = Pattern.compile("(<img[^>]+src=\"[^\"]+\\.)svg([^\"]*\"[^>]*>)");
  @NotNull
  private final JFXPanel myPanel;
  @Nullable
  private WebView myWebView;
  @Nullable
  private String myInlineCss;
  @NotNull
  private String[] myCssUris = ArrayUtil.EMPTY_STRING_ARRAY;
  @NotNull
  private String myLastRawHtml = "";

  public JavaFxHtmlPanel() {
    myPanel = new JFXPanel();

    Platform.setImplicitExit(false);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        myWebView = new WebView();

        myWebView.getEngine().getLoadWorker().stateProperty().addListener(
          new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
              if (newValue == State.SUCCEEDED) {
                JSObject win
                  = (JSObject) getWebViewGuaranteed().getEngine().executeScript("window");
                win.setMember("JavaPanelBridge", new JavaPanelBridge());
              }
            }
          });

        myWebView.getEngine().loadContent("<html><body>" + getScriptingLines() + "</body></html>");
        myWebView.setContextMenuEnabled(false);

        final Scene scene = new Scene(myWebView);
        myPanel.setScene(scene);
      }
    });
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myPanel;
  }

  @Override
  public void setHtml(@NotNull String html) {
    myLastRawHtml = html;
    final String htmlToRender = prepareHtml(html);

    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        getWebViewGuaranteed().getEngine().loadContent(htmlToRender);
      }
    });
  }

  private String prepareHtml(@NotNull String html) {
    String result = html
      .replace("<head>", "<head>" + getCssLines(myInlineCss, myCssUris))
      .replace("</body>", getScriptingLines() + "</body>");

    // temp workaround for RUBY-17329
    if (SystemInfo.isJetbrainsJvm && SystemInfo.isMac) {
      result = SVG_REPLACE_PATTERN.matcher(result).replaceAll("$1_svg_$2");
    }
    return result;
  }

  @Override
  public void setCSS(@Nullable String inlineCss, @NotNull String... fileUris) {
    myInlineCss = inlineCss;
    myCssUris = fileUris;
    setHtml(myLastRawHtml);
  }

  @Override
  public void render() {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        getWebViewGuaranteed().getEngine().reload();
        myPanel.repaint();
      }
    });
  }

  @Override
  public void scrollToMarkdownSrcOffset(final int offset) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        getWebViewGuaranteed().getEngine().executeScript(
          "if ('__IntelliJTools' in window) " +
          "__IntelliJTools.scrollToOffset(" + offset + ", '" + HtmlGenerator.Companion.getSRC_ATTRIBUTE_NAME() + "');"

        );
      }
    });
  }

  @Override
  public void dispose() {
  }

  @NotNull
  private WebView getWebViewGuaranteed () {
    if (myWebView == null) {
      throw new IllegalStateException("WebView should be initialized by now. Check the caller thread");
    }
    return myWebView;
  }

  @NotNull
  private static String getScriptingLines() {
    try {
      final String libPath = MarkdownPluginUtil.getMarkdownPluginPath() + "/lib";
      return new StringBuilder()
        .append("<script src=\"").append(new File(libPath + "/scrollToElement.js").toURI()).append("\"></script>\n")
        .append("<script src=\"").append(new File(libPath + "/processLinks.js").toURI()).append("\"></script>\n")
        .toString();
    }
    catch (FileNotFoundException e) {
      return "";
    }
  }

  public static class JavaPanelBridge {
    public void openInExternalBrowser(@NotNull String link) {
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
}
