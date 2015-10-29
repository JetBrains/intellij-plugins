package org.intellij.plugins.markdown.ui.preview;

import com.intellij.CommonBundle;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Alarm;
import com.intellij.util.messages.MessageBusConnection;
import org.intellij.markdown.ast.ASTNode;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.parser.LinkMap;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.plugins.markdown.lang.parser.MarkdownParserManager;
import org.intellij.plugins.markdown.settings.MarkdownApplicationSettings;
import org.intellij.plugins.markdown.settings.MarkdownCssSettings;
import org.intellij.plugins.markdown.settings.MarkdownPreviewSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

public class MarkdownPreviewFileEditor extends UserDataHolderBase implements FileEditor {
  private final static long PARSING_CALL_TIMEOUT_MS = 50L;
  @NotNull
  private final JPanel myHtmlPanelWrapper;
  @Nullable
  private MarkdownHtmlPanel myPanel;
  @Nullable
  private MarkdownHtmlPanelProvider.ProviderInfo myLastPanelProviderInfo;
  @NotNull
  private final VirtualFile myFile;
  @Nullable
  private final Document myDocument;
  @NotNull
  private final Alarm myAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);

  @Nullable
  private Runnable myLastScrollRequest = null;

  private int myLastScrollOffset;

  public MarkdownPreviewFileEditor(@NotNull VirtualFile file) {
    myFile = file;
    myDocument = FileDocumentManager.getInstance().getDocument(myFile);

    if (myDocument != null) {
      myDocument.addDocumentListener(new DocumentAdapter() {

        @Override
        public void beforeDocumentChange(DocumentEvent e) {
          myAlarm.cancelAllRequests();
        }

        @Override
        public void documentChanged(final DocumentEvent e) {
          myAlarm.addRequest(new Runnable() {
            @Override
            public void run() {
              //myLastScrollOffset = e.getOffset();
              updateHtml(true);
            }
          }, PARSING_CALL_TIMEOUT_MS);
        }
      }, this);
    }

    myHtmlPanelWrapper = new JPanel(new BorderLayout());

    final MarkdownApplicationSettings settings = MarkdownApplicationSettings.getInstance();
    setUpPanel(settings);
    getPanelGuaranteed().setHtml("<html></html>");
    getPanelGuaranteed().render();

    MessageBusConnection settingsConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
    MarkdownApplicationSettings.SettingsChangedListener settingsChangedListener =
      new MarkdownApplicationSettings.SettingsChangedListener() {
        @Override
        public void onSettingsChange(@NotNull MarkdownApplicationSettings settings) {
          setUpPanel(settings);
          updatePanelCssSettings(settings.getMarkdownCssSettings());
        }
      };
    settingsConnection.subscribe(MarkdownApplicationSettings.SettingsChangedListener.TOPIC, settingsChangedListener);
    settingsChangedListener.onSettingsChange(settings);
  }

  public void scrollToSrcOffset(final int offset) {

    if (myLastScrollRequest != null) {
      myAlarm.cancelRequest(myLastScrollRequest);
    }
    myLastScrollRequest = new Runnable() {
      @Override
      public void run() {
        myLastScrollOffset = offset;
        getPanelGuaranteed().scrollToMarkdownSrcOffset(myLastScrollOffset);
      }
    };
    myAlarm.addRequest(myLastScrollRequest, PARSING_CALL_TIMEOUT_MS);
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myHtmlPanelWrapper;
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return getPanelGuaranteed().getComponent();
  }

  @NotNull
  @Override
  public String getName() {
    return "Markdown HTML Preview";
  }

  @NotNull
  @Override
  public FileEditorState getState(@NotNull FileEditorStateLevel level) {
    return FileEditorState.INSTANCE;
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
    myAlarm.cancelAllRequests();
    myAlarm.addRequest(new Runnable() {
      @Override
      public void run() {
        updateHtml(true);
      }
    }, 0);
  }

  private void setUpPanel(@NotNull MarkdownApplicationSettings settings) {
    final MarkdownHtmlPanel newPanel = retrievePanel(settings);

    if (newPanel == myPanel) {
      return;
    }

    if (myPanel != null) {
      myHtmlPanelWrapper.remove(myPanel.getComponent());
    }
    myPanel = newPanel;
    myHtmlPanelWrapper.add(myPanel.getComponent(), BorderLayout.CENTER);
  }

  @NotNull
  private MarkdownHtmlPanel retrievePanel(@NotNull MarkdownApplicationSettings settings) {
    final MarkdownHtmlPanelProvider.ProviderInfo providerInfo = settings.getMarkdownPreviewSettings().getHtmlPanelProviderInfo();
    if (providerInfo.equals(myLastPanelProviderInfo)) {
      return getPanelGuaranteed();
    }

    MarkdownHtmlPanelProvider provider = MarkdownHtmlPanelProvider.createFromInfo(providerInfo);
    if (!provider.isAvailable()) {
      Messages.showMessageDialog(
        "Tried to use preview panel provider (" + providerInfo.getName() + "), but it is unavailable. Reverting to default.",
        CommonBundle.getErrorTitle(),
        Messages.getErrorIcon()
      );

      settings.setMarkdownPreviewSettings(new MarkdownPreviewSettings(settings.getMarkdownPreviewSettings().getSplitEditorLayout(),
                                                                      MarkdownPreviewSettings.DEFAULT.getHtmlPanelProviderInfo()));

      provider = MarkdownHtmlPanelProvider.getProviders()[0];
    }

    myLastPanelProviderInfo = settings.getMarkdownPreviewSettings().getHtmlPanelProviderInfo();
    return provider.createHtmlPanel();
  }

  @NotNull
  private MarkdownHtmlPanel getPanelGuaranteed() {
    if (myPanel == null) {
      throw new IllegalStateException("Panel is guaranteed to be not null now");
    }
    return myPanel;
  }

  private void updateHtml(boolean preserveScrollOffset) {
    if (!myFile.isValid() || myDocument == null) {
      return;
    }

    String text = myDocument.getText();
    final ASTNode parsedTree = new MarkdownParser(MarkdownParserManager.FLAVOUR).buildMarkdownTreeFromString(text);
    final String html = new HtmlGenerator(text,
                                          parsedTree,
                                          MarkdownParserManager.FLAVOUR,
                                          LinkMap.Builder.buildLinkMap(parsedTree, text),
                                          true)
      .generateHtml();

    getPanelGuaranteed().setHtml("<html><head></head>" + html + "</html>");

    if (preserveScrollOffset) {
      getPanelGuaranteed().scrollToMarkdownSrcOffset(myLastScrollOffset);
    }
  }

  private void updatePanelCssSettings(@NotNull MarkdownCssSettings cssSettings) {
    getPanelGuaranteed().setCSS(cssSettings.isTextEnabled() ? cssSettings.getStylesheetText() : null,
                                cssSettings.isUriEnabled() ? cssSettings.getStylesheetUri() : null);
    getPanelGuaranteed().render();
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

  @Nullable
  @Override
  public StructureViewBuilder getStructureViewBuilder() {
    return null;
  }

  @Override
  public void dispose() {
  }
}
