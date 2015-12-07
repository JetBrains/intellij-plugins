package org.intellij.plugins.markdown.ui.preview;

import com.intellij.CommonBundle;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Alarm;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
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

  private final static long RENDERING_DELAY_MS = 20L;
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
  private final Alarm myPooledAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
  @NotNull
  private final Alarm mySwingAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);

  private final Object REQUESTS_LOCK = new Object();
  @Nullable
  private Runnable myLastScrollRequest = null;
  @Nullable
  private Runnable myLastHtmlOrRefreshRequest = null;

  private int myLastScrollOffset;
  @NotNull
  private String myLastRenderedHtml = "";

  public MarkdownPreviewFileEditor(@NotNull VirtualFile file) {
    myFile = file;
    myDocument = FileDocumentManager.getInstance().getDocument(myFile);

    if (myDocument != null) {
      myDocument.addDocumentListener(new DocumentAdapter() {

        @Override
        public void beforeDocumentChange(DocumentEvent e) {
          myPooledAlarm.cancelAllRequests();
        }

        @Override
        public void documentChanged(final DocumentEvent e) {
          myPooledAlarm.addRequest(new Runnable() {
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
    // Do not scroll if html update request is online
    // This will restrain preview from glitches on editing
    if (!myPooledAlarm.isEmpty()) {
      return;
    }
    
    synchronized (REQUESTS_LOCK) {
      if (myLastScrollRequest != null) {
        mySwingAlarm.cancelRequest(myLastScrollRequest);
      }
      myLastScrollRequest = new Runnable() {
        @Override
        public void run() {
          myLastScrollOffset = offset;
          getPanelGuaranteed().scrollToMarkdownSrcOffset(myLastScrollOffset);
          synchronized (REQUESTS_LOCK) {
            myLastScrollRequest = null;
          }
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
    myPooledAlarm.cancelAllRequests();
    myPooledAlarm.addRequest(new Runnable() {
      @Override
      public void run() {
        updateHtml(true);
      }
    }, 0);
  }

  private void setUpPanel(@NotNull MarkdownApplicationSettings settings) {
    final MarkdownHtmlPanelProvider newPanelProvider = retrievePanelProvider(settings);

    if (newPanelProvider == null) {
      return;
    }

    mySwingAlarm.addRequest(new Runnable() {
      @Override
      public void run() {
        final MarkdownHtmlPanel newPanel = newPanelProvider.createHtmlPanel();
        if (myPanel != null) {
          myHtmlPanelWrapper.remove(myPanel.getComponent());
          Disposer.dispose(myPanel);
        }
        myPanel = newPanel;
        myHtmlPanelWrapper.add(myPanel.getComponent(), BorderLayout.CENTER);
        myPanel.setHtml(myLastRenderedHtml);
        myHtmlPanelWrapper.repaint();
      }
    }, 0, ModalityState.stateForComponent(myHtmlPanelWrapper));
  }

  @Nullable("Null means leave current panel")
  private MarkdownHtmlPanelProvider retrievePanelProvider(@NotNull MarkdownApplicationSettings settings) {
    final MarkdownHtmlPanelProvider.ProviderInfo providerInfo = settings.getMarkdownPreviewSettings().getHtmlPanelProviderInfo();
    if (providerInfo.equals(myLastPanelProviderInfo)) {
      return null;
    }

    MarkdownHtmlPanelProvider provider = MarkdownHtmlPanelProvider.createFromInfo(providerInfo);

    if (provider.isAvailable() != MarkdownHtmlPanelProvider.AvailabilityInfo.AVAILABLE) {
      settings.setMarkdownPreviewSettings(new MarkdownPreviewSettings(settings.getMarkdownPreviewSettings().getSplitEditorLayout(),
                                                                      MarkdownPreviewSettings.DEFAULT.getHtmlPanelProviderInfo(),
                                                                      settings.getMarkdownPreviewSettings().isUseGrayscaleRendering()));

      Messages.showMessageDialog(
        myHtmlPanelWrapper,
        "Tried to use preview panel provider (" + providerInfo.getName() + "), but it is unavailable. Reverting to default.",
        CommonBundle.getErrorTitle(),
        Messages.getErrorIcon()
      );

      provider = MarkdownHtmlPanelProvider.getProviders()[0];
    }

    myLastPanelProviderInfo = settings.getMarkdownPreviewSettings().getHtmlPanelProviderInfo();
    return provider;
  }

  @NotNull
  private MarkdownHtmlPanel getPanelGuaranteed() {
    if (myPanel == null) {
      throw new IllegalStateException("Panel is guaranteed to be not null now");
    }
    return myPanel;
  }

  /**
   * Is always run from pooled thread 
   */
  private void updateHtml(final boolean preserveScrollOffset) {
    if (!myFile.isValid() || myDocument == null || Disposer.isDisposed(this)) {
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

    // EA-75860: The lines to the top may be processed slowly; Since we're in pooled thread, we can be disposed already.
    if (!myFile.isValid() || Disposer.isDisposed(this)) {
      return;
    }
    
    synchronized (REQUESTS_LOCK) {
      if (myLastHtmlOrRefreshRequest != null) {
        mySwingAlarm.cancelRequest(myLastHtmlOrRefreshRequest);
      }
      myLastHtmlOrRefreshRequest = new Runnable() {
        @Override
        public void run() {
          final String currentHtml = "<html><head></head>" + html + "</html>";
          if (!currentHtml.equals(myLastRenderedHtml)) {
            myLastRenderedHtml = currentHtml;
            getPanelGuaranteed().setHtml(myLastRenderedHtml);

            if (preserveScrollOffset) {
              getPanelGuaranteed().scrollToMarkdownSrcOffset(myLastScrollOffset);
            }
          }

          getPanelGuaranteed().render();
          synchronized (REQUESTS_LOCK) {
            myLastHtmlOrRefreshRequest = null;
          }
        }
      };
      mySwingAlarm.addRequest(myLastHtmlOrRefreshRequest, RENDERING_DELAY_MS, ModalityState.stateForComponent(getComponent()));
    }
  }

  private void updatePanelCssSettings(@NotNull final MarkdownCssSettings cssSettings) {
    mySwingAlarm.addRequest(new Runnable() {
      @Override
      public void run() {
        //noinspection StringBufferReplaceableByString
        final String inlineCss = new StringBuilder()
          .append(cssSettings.isTextEnabled() ? cssSettings.getStylesheetText() + "\n" : "")
          .append("body {\n  font-size: ").append(JBUI.scale(100)).append("%;\n}")
          .toString();

        getPanelGuaranteed().setCSS(inlineCss,
                                    cssSettings.isUriEnabled() ? cssSettings.getStylesheetUri() : null);
        getPanelGuaranteed().render();
      }
    }, RENDERING_DELAY_MS, ModalityState.stateForComponent(getComponent()));
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
    if (myPanel != null) {
      Disposer.dispose(myPanel);
    }
  }
}
