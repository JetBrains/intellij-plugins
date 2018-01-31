package org.intellij.plugins.markdown.ui.preview;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Alarm;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.plugins.markdown.settings.MarkdownApplicationSettings;
import org.intellij.plugins.markdown.settings.MarkdownCssSettings;
import org.intellij.plugins.markdown.ui.preview.javafx.JavaFxHtmlPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

public class MarkdownPreviewFileEditor extends UserDataHolderBase implements FileEditor {
  private final static long PARSING_CALL_TIMEOUT_MS = 50L;

  private final static long RENDERING_DELAY_MS = 20L;

  final static NotNullLazyValue<PolicyFactory> SANITIZER_VALUE = new NotNullLazyValue<PolicyFactory>() {
    @NotNull
    @Override
    protected PolicyFactory compute() {
      return Sanitizers.BLOCKS
        .and(Sanitizers.FORMATTING)
        .and(new HtmlPolicyBuilder()
               .allowUrlProtocols("file", "http", "https").allowElements("img")
               .allowAttributes("alt", "src", "title").onElements("img")
               .allowAttributes("border", "height", "width").onElements("img")
               .toFactory())
        .and(new HtmlPolicyBuilder()
               .allowUrlProtocols("file", "http", "https", "mailto").allowElements("a")
               .allowAttributes("href", "title").onElements("a")
               .toFactory())
        .and(Sanitizers.TABLES)
        .and(new HtmlPolicyBuilder()
               .allowElements("body", "pre")
               .allowAttributes(HtmlGenerator.Companion.getSRC_ATTRIBUTE_NAME()).globally().toFactory())
        .and(new HtmlPolicyBuilder()
               .allowElements("code", "tr")
               .allowAttributes("class").onElements("code", "tr")
               .toFactory());
    }
  };
  @NotNull
  private final JPanel myHtmlPanelWrapper;
  @NotNull
  private MarkdownHtmlPanel myPanel;
  @NotNull
  private final VirtualFile myFile;
  @Nullable
  private final Document myDocument;
  @NotNull
  private final Alarm myPooledAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
  @NotNull
  private final Alarm mySwingAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);

  private final Object REQUESTS_LOCK = new Object();
  @Nullable
  private Runnable myLastScrollRequest = null;
  @Nullable
  private Runnable myLastHtmlOrRefreshRequest = null;

  private volatile int myLastScrollOffset;
  @NotNull
  private String myLastRenderedHtml = "";

  public MarkdownPreviewFileEditor(@NotNull VirtualFile file) {
    myFile = file;
    myDocument = FileDocumentManager.getInstance().getDocument(myFile);

    if (myDocument != null) {
      myDocument.addDocumentListener(new DocumentListener() {

        @Override
        public void beforeDocumentChange(DocumentEvent e) {
          myPooledAlarm.cancelAllRequests();
        }

        @Override
        public void documentChanged(final DocumentEvent e) {
          myPooledAlarm.addRequest(() -> {
            //myLastScrollOffset = e.getOffset();
            updateHtml(true);
          }, PARSING_CALL_TIMEOUT_MS);
        }
      }, this);
    }

    myHtmlPanelWrapper = new JPanel(new BorderLayout());

    final MarkdownApplicationSettings settings = MarkdownApplicationSettings.getInstance();
    myPanel = detachOldPanelAndCreateAndAttachNewOne(myHtmlPanelWrapper, null);
    updatePanelCssSettings(myPanel, settings.getMarkdownCssSettings());

    MessageBusConnection settingsConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
    MarkdownApplicationSettings.SettingsChangedListener settingsChangedListener = new MyUpdatePanelOnSettingsChangedListener();
    settingsConnection.subscribe(MarkdownApplicationSettings.SettingsChangedListener.TOPIC, settingsChangedListener);
  }

  public void scrollToSrcOffset(final int offset) {
    // Do not scroll if html update request is online
    // This will restrain preview from glitches on editing
    if (!myPooledAlarm.isEmpty()) {
      myLastScrollOffset = offset;
      return;
    }

    synchronized (REQUESTS_LOCK) {
      if (myLastScrollRequest != null) {
        mySwingAlarm.cancelRequest(myLastScrollRequest);
      }
      myLastScrollRequest = () -> {
        myLastScrollOffset = offset;
        myPanel.scrollToMarkdownSrcOffset(myLastScrollOffset);
        synchronized (REQUESTS_LOCK) {
          myLastScrollRequest = null;
        }
      };
      mySwingAlarm.addRequest(myLastScrollRequest, RENDERING_DELAY_MS, ModalityState.stateForComponent(getComponent()));
    }
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myHtmlPanelWrapper;
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return myPanel.getComponent();
  }

  @NotNull
  @Override
  public String getName() {
    return "Markdown HTML Preview";
  }

  @Override
  public void setState(@NotNull FileEditorState state) {
  }

  @Override
  public boolean isModified() {
    return false;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public void selectNotify() {
    myPooledAlarm.cancelAllRequests();
    myPooledAlarm.addRequest(() -> updateHtml(true), 0);
  }

  /**
   * Is always run from pooled thread
   */
  private void updateHtml(final boolean preserveScrollOffset) {
    if (!myFile.isValid() || myDocument == null || Disposer.isDisposed(this)) {
      return;
    }

    final String html = MarkdownUtil.generateMarkdownHtml(myFile, myDocument.getText());

    // EA-75860: The lines to the top may be processed slowly; Since we're in pooled thread, we can be disposed already.
    if (!myFile.isValid() || Disposer.isDisposed(this)) {
      return;
    }

    synchronized (REQUESTS_LOCK) {
      if (myLastHtmlOrRefreshRequest != null) {
        mySwingAlarm.cancelRequest(myLastHtmlOrRefreshRequest);
      }
      myLastHtmlOrRefreshRequest = () -> {
        final String currentHtml = "<html><head></head>" + SANITIZER_VALUE.getValue().sanitize(html) + "</html>";
        if (!currentHtml.equals(myLastRenderedHtml)) {
          myLastRenderedHtml = currentHtml;
          myPanel.setHtml(myLastRenderedHtml);

          if (preserveScrollOffset) {
            scrollToSrcOffset(myLastScrollOffset);
          }
        }

        myPanel.render();
        synchronized (REQUESTS_LOCK) {
          myLastHtmlOrRefreshRequest = null;
        }
      };
      mySwingAlarm.addRequest(myLastHtmlOrRefreshRequest, RENDERING_DELAY_MS, ModalityState.stateForComponent(getComponent()));
    }
  }

  @Override
  public void deselectNotify() {
  }

  @Override
  public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
  }

  @Override
  public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
  }

  @Nullable
  @Override
  public BackgroundEditorHighlighter getBackgroundHighlighter() {
    return null;
  }

  @Nullable
  @Override
  public FileEditorLocation getCurrentLocation() {
    return null;
  }

  @Override
  public void dispose() {
    Disposer.dispose(myPanel);
  }

  @NotNull
  private static MarkdownHtmlPanel detachOldPanelAndCreateAndAttachNewOne(@NotNull JPanel panelWrapper,
                                                                          @Nullable MarkdownHtmlPanel oldPanel) {
    ApplicationManager.getApplication().assertIsDispatchThread();
    if (oldPanel != null) {
      panelWrapper.remove(oldPanel.getComponent());
      Disposer.dispose(oldPanel);
    }

    final MarkdownHtmlPanel newPanel = new JavaFxHtmlPanel();
    panelWrapper.add(newPanel.getComponent(), BorderLayout.CENTER);
    panelWrapper.repaint();

    return newPanel;
  }

  private static void updatePanelCssSettings(@NotNull MarkdownHtmlPanel panel, @NotNull final MarkdownCssSettings cssSettings) {
    ApplicationManager.getApplication().assertIsDispatchThread();

    final String inlineCss = cssSettings.isTextEnabled() ? cssSettings.getStylesheetText() : null;
    final String customCssURI = cssSettings.isUriEnabled()
                                ? cssSettings.getStylesheetUri()
                                : MarkdownCssSettings.getDefaultCssSettings(UIUtil.isUnderDarcula()).getStylesheetUri();

    panel.setCSS(inlineCss, customCssURI);

    panel.render();
  }


  private class MyUpdatePanelOnSettingsChangedListener implements MarkdownApplicationSettings.SettingsChangedListener {
    @Override
    public void beforeSettingsChanged(@NotNull MarkdownApplicationSettings settings) {
      mySwingAlarm.addRequest(() -> {
        myPanel = detachOldPanelAndCreateAndAttachNewOne(myHtmlPanelWrapper, myPanel);
        myPanel.setHtml(myLastRenderedHtml);
        updatePanelCssSettings(myPanel, settings.getMarkdownCssSettings());
      }, 0, ModalityState.stateForComponent(getComponent()));
    }
  }
}
