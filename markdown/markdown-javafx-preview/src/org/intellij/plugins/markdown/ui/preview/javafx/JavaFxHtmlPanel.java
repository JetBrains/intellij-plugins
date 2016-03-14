package org.intellij.plugins.markdown.ui.preview.javafx;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ArrayUtil;
import com.intellij.util.messages.MessageBusConnection;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.plugins.markdown.settings.MarkdownApplicationSettings;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;
import org.intellij.plugins.markdown.util.MarkdownPluginUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

// Instantiated by reflection
@SuppressWarnings("unused")
public class JavaFxHtmlPanel extends MarkdownHtmlPanel {

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
  @NotNull
  private final ScrollPreservingListener myScrollPreservingListener = new ScrollPreservingListener();
  @NotNull
  private final BridgeSettingListener myBridgeSettingListener = new BridgeSettingListener();

  public JavaFxHtmlPanel() {
    //System.setProperty("prism.lcdtext", "false");
    //System.setProperty("prism.text", "t2k");
    myPanel = new JFXPanelWrapper();

    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        myWebView = new WebView();

        updateFontSmoothingType(myWebView, MarkdownApplicationSettings.getInstance().getMarkdownPreviewSettings().isUseGrayscaleRendering());
        myWebView.setContextMenuEnabled(false);

        final WebEngine engine = myWebView.getEngine();
        engine.getLoadWorker().stateProperty().addListener(myBridgeSettingListener);
        engine.getLoadWorker().stateProperty().addListener(myScrollPreservingListener);

        engine.loadContent("<html><body>" + getScriptingLines() + "</body></html>");

        final Scene scene = new Scene(myWebView);
        myPanel.setScene(scene);
      }
    });

    subscribeForGrayscaleSetting();
  }

  private void subscribeForGrayscaleSetting() {
    MessageBusConnection settingsConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
    MarkdownApplicationSettings.SettingsChangedListener settingsChangedListener =
      new MarkdownApplicationSettings.SettingsChangedListener() {
        @Override
        public void onSettingsChange(@NotNull final MarkdownApplicationSettings settings) {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              if (myWebView != null) {
                updateFontSmoothingType(myWebView, settings.getMarkdownPreviewSettings().isUseGrayscaleRendering());
              }
            }
          });
        }
      };
    settingsConnection.subscribe(MarkdownApplicationSettings.SettingsChangedListener.TOPIC, settingsChangedListener);
  }

  private static void updateFontSmoothingType(@NotNull WebView view, boolean isGrayscale) {
    final FontSmoothingType typeToSet;
    if (isGrayscale) {
      typeToSet = FontSmoothingType.GRAY;
    }
    else {
      typeToSet = FontSmoothingType.LCD;
    }
    view.fontSmoothingTypeProperty().setValue(typeToSet);
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
        final Object result = getWebViewGuaranteed().getEngine().executeScript(
          "document.documentElement.scrollTop || document.body.scrollTop");
        if (result instanceof Number) {
          myScrollPreservingListener.myScrollY = ((Number)result).intValue();
        }
      }
    });
  }

  @Override
  public void dispose() {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        getWebViewGuaranteed().getEngine().getLoadWorker().stateProperty().removeListener(myScrollPreservingListener);
        getWebViewGuaranteed().getEngine().getLoadWorker().stateProperty().removeListener(myBridgeSettingListener);
      }
    });
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
      //noinspection StringBufferReplaceableByString
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
  
  private class BridgeSettingListener implements ChangeListener<State> {
    @Override
    public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
        JSObject win
          = (JSObject)getWebViewGuaranteed().getEngine().executeScript("window");
        win.setMember("JavaPanelBridge", new JavaPanelBridge());
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
          .executeScript("document.documentElement.scrollTop = document.body.scrollTop = " + myScrollY);
      }
    }
  } 
}
