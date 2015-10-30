package org.intellij.plugins.markdown.ui.preview.javafx;

import com.intellij.util.ArrayUtil;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

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

  public JavaFxHtmlPanel() {
    myPanel = new JFXPanel();

    Platform.setImplicitExit(false);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        myWebView = new WebView();
        myWebView.getEngine().loadContent("<html></html>");

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
    final String htmlToRender = html.replace("<head>", "<head>" + getCssLines(myInlineCss, myCssUris));
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        getWebViewGuaranteed().getEngine().loadContent(htmlToRender);
      }
    });
  }

  @Override
  public void setCSS(@Nullable String inlineCss, @NotNull String... fileUris) {
    myInlineCss = inlineCss;
    myCssUris = fileUris;
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
  public void scrollToMarkdownSrcOffset(int offset) {
    // TODO
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
}
