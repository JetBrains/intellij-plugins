package org.intellij.plugins.markdown.ui.preview.javafx;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.impl.http.HttpVirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.javafx.JavaFxHtmlPanel;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBusConnection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.plugins.markdown.MarkdownBundle;
import org.intellij.plugins.markdown.lang.references.MarkdownAnchorReference;
import org.intellij.plugins.markdown.settings.MarkdownApplicationSettings;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;
import org.intellij.plugins.markdown.ui.preview.MarkdownSplitEditor;
import org.intellij.plugins.markdown.ui.preview.PreviewStaticServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Objects;

public class MarkdownJavaFxHtmlPanel extends JavaFxHtmlPanel implements MarkdownHtmlPanel {

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
  @NotNull
  private final ScrollPreservingListener myScrollPreservingListener = new ScrollPreservingListener();
  @NotNull
  private final BridgeSettingListener myBridgeSettingListener = new BridgeSettingListener();

  public MarkdownJavaFxHtmlPanel() {
    super();
    runInPlatformWhenAvailable(() -> {
      if (myWebView != null) {
        updateFontSmoothingType(myWebView, MarkdownApplicationSettings.getInstance().getMarkdownPreviewSettings().isUseGrayscaleRendering());
      }
    });

    subscribeForGrayscaleSetting();
  }

  protected void registerListeners(@NotNull WebEngine engine) {
    engine.getLoadWorker().stateProperty().addListener(myBridgeSettingListener);
    engine.getLoadWorker().stateProperty().addListener(myScrollPreservingListener);
  }

  private void subscribeForGrayscaleSetting() {
    MessageBusConnection settingsConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
    MarkdownApplicationSettings.SettingsChangedListener settingsChangedListener =
      new MarkdownApplicationSettings.SettingsChangedListener() {
        @Override
        public void beforeSettingsChanged(@NotNull final MarkdownApplicationSettings settings) {
          runInPlatformWhenAvailable(() -> {
            if (myWebView != null) {
              updateFontSmoothingType(myWebView, settings.getMarkdownPreviewSettings().isUseGrayscaleRendering());
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

  @Override
  public void setHtml(@NotNull String html) {
    myLastRawHtml = html;
    super.setHtml(html);
  }

  @NotNull
  @Override
  protected String prepareHtml(@NotNull String html) {
    return ImageRefreshFix.setStamps(html
                                       .replace("<head>", "<head>"
                                                          + "<meta http-equiv=\"Content-Security-Policy\" content=\"" + myCSP + "\"/>"
                                                          + MarkdownHtmlPanel.getCssLines(null, myCssUris) + "\n" + getScriptingLines()));
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
    runInPlatformWhenAvailable(() -> {
      getWebViewGuaranteed().getEngine().executeScript(
        "if ('__IntelliJTools' in window) " +
        "__IntelliJTools.scrollToOffset(" + offset + ", '" + HtmlGenerator.Companion.getSRC_ATTRIBUTE_NAME() + "');"
      );
      final Object result = getWebViewGuaranteed().getEngine().executeScript(
        "document.documentElement.scrollTop || document.body.scrollTop");
      if (result instanceof Number) {
        myScrollPreservingListener.myScrollY = ((Number)result).intValue();
      }
    });
  }

  @Override
  public void dispose() {
    runInPlatformWhenAvailable(() -> {
      getWebViewGuaranteed().getEngine().getLoadWorker().stateProperty().removeListener(myScrollPreservingListener);
      getWebViewGuaranteed().getEngine().getLoadWorker().stateProperty().removeListener(myBridgeSettingListener);
    });
  }

  @NotNull
  private static String getScriptingLines() {
    return MY_SCRIPTING_LINES.getValue();
  }

  @SuppressWarnings("unused")
  public static class JavaPanelBridge {
    static final JavaPanelBridge INSTANCE = new JavaPanelBridge();
    private static final NotificationGroup MARKDOWN_NOTIFICATION_GROUP =
      NotificationGroup.toolWindowGroup(MarkdownBundle.message("markdown.navigate.to.header.group"), ToolWindowId.MESSAGES_WINDOW);

    public void openInExternalBrowser(@NotNull String link) {
      String fileURI = link;
      String anchor = null;
      if (link.contains("#")) {
        fileURI = Objects.requireNonNull(StringUtil.substringBefore(link, "#"));
        anchor = Objects.requireNonNull(StringUtil.substringAfter(link, "#"));
      }

      VirtualFile targetFile = VirtualFileManager.getInstance().findFileByUrl(fileURI);
      if (targetFile == null || targetFile instanceof HttpVirtualFile) {
        SafeOpener.openLink(link);
      }
      else {
        openLocalFile(targetFile, anchor);
      }
    }

    private static void openLocalFile(@NotNull VirtualFile targetFile, @Nullable String anchor) {
      Project project = ProjectUtil.guessProjectForFile(targetFile);
      if (project == null) return;

      if (anchor == null) {
        FileEditorManager.getInstance(project).openFile(targetFile, true);
        return;
      }

      final JFrame frame = WindowManager.getInstance().getFrame(project);
      final Point mousePosition = Objects.requireNonNull(frame).getMousePosition();
      if (mousePosition == null) return;
      RelativePoint point = new RelativePoint(frame, mousePosition);

      ApplicationManager.getApplication().invokeLater(() -> {
        Collection<PsiElement> headers = ProgressManager.getInstance().runProcess(
          () -> MarkdownAnchorReference.Companion.getPsiHeaders(project, anchor, PsiManager.getInstance(project).findFile(targetFile)),
          ProgressIndicatorProvider.getGlobalProgressIndicator());

        if (headers.isEmpty()) {
          showCannotNavigateNotification(project, anchor, point);
        }
        else if (headers.size() == 1) {
          navigateToHeader(project, targetFile, Objects.requireNonNull(ContainerUtil.getFirstItem(headers)));
        }
        else {
          showHeadersPopup(headers, point);
        }
      });
    }

    private static void showCannotNavigateNotification(@NotNull Project project, @NotNull String anchor, @NotNull RelativePoint point) {
      BalloonBuilder balloonBuilder = JBPopupFactory.getInstance()
        .createHtmlTextBalloonBuilder(MarkdownBundle.message("markdown.navigate.to.header.no.headers", anchor), MessageType.WARNING,
                                      null);
      final Balloon balloon = balloonBuilder.createBalloon();
      balloon.show(point, Balloon.Position.below);
      Disposer.register(project, balloon);
    }

    private static void navigateToHeader(@NotNull Project project, @NotNull VirtualFile targetFile, @NotNull PsiElement item) {
      MarkdownSplitEditor splitEditor = Objects.requireNonNull(
        ObjectUtils.tryCast(FileEditorManager.getInstance(project).getSelectedEditor(targetFile), MarkdownSplitEditor.class));

      boolean oldAutoScrollPreview = splitEditor.isAutoScrollPreview();

      if (!oldAutoScrollPreview) splitEditor.setAutoScrollPreview(true);
      PsiNavigateUtil.navigate(item);

      if (!oldAutoScrollPreview) splitEditor.setAutoScrollPreview(false);
    }

    private static void showHeadersPopup(@NotNull Collection<PsiElement> headers,
                                         @NotNull RelativePoint point) {
      ListPopupStep headersPopup =
        new BaseListPopupStep<PsiElement>(MarkdownBundle.message("markdown.navigate.to.header"), ContainerUtil.newArrayList(headers)) {
          @NotNull
          @Override
          public String getTextFor(PsiElement value) {
            Document document = FileDocumentManager.getInstance().getDocument(value.getContainingFile().getVirtualFile());
            String name = value.getContainingFile().getVirtualFile().getName();

            return value.getText() + " (" + name + ":" + (Objects.requireNonNull(document).getLineNumber(value.getTextOffset()) + 1) + ")";
          }

          @Override
          public PopupStep onChosen(final PsiElement selectedValue, boolean finalChoice) {
            return doFinalStep(() -> PsiNavigateUtil.navigate(selectedValue));
          }
        };

      JBPopupFactory.getInstance().createListPopup(headersPopup).show(point);
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
}
