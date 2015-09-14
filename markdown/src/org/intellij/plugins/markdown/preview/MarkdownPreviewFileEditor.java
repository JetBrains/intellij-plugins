package org.intellij.plugins.markdown.preview;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Alarm;
import com.intellij.util.messages.MessageBusConnection;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.ast.ASTNode;
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.lexer.MarkdownLexer;
import org.intellij.markdown.parser.LinkMap;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.markdown.parser.sequentialparsers.LexerBasedTokensCache;
import org.intellij.markdown.parser.sequentialparsers.TokensCache;
import org.intellij.plugins.markdown.lang.parser.MarkdownParserManager;
import org.intellij.plugins.markdown.settings.MarkdownApplicationSettings;
import org.intellij.plugins.markdown.settings.MarkdownCssSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public class MarkdownPreviewFileEditor extends UserDataHolderBase implements FileEditor {
  private final static long PARSING_CALL_TIMEOUT_MS = 50L;
  @NotNull
  private final MarkdownHtmlPanel myPanel;
  @NotNull
  private final VirtualFile myFile;
  @Nullable
  private final Document myDocument;
  @NotNull
  private final Alarm myAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
  @NotNull
  private final MarkdownApplicationSettings.SettingsChangedListener mySettingsChangedListener =
    new MarkdownApplicationSettings.SettingsChangedListener() {
      @Override
      public void onSettingsChange(@NotNull MarkdownApplicationSettings settings) {
        updatePanelCssSettings(settings.getMarkdownCssSettings());
      }
    };

  public MarkdownPreviewFileEditor(@NotNull VirtualFile file) {
    myFile = file;
    myDocument = FileDocumentManager.getInstance().getDocument(myFile);

    if (myDocument != null) {
      myDocument.addDocumentListener(new DocumentAdapter() {
        @Override
        public void documentChanged(DocumentEvent e) {
          myAlarm.cancelAllRequests();
          myAlarm.addRequest(new Runnable() {
            @Override
            public void run() {
              updateHtml();
            }
          }, PARSING_CALL_TIMEOUT_MS);
        }
      }, this);
    }

    myPanel = new MarkdownHtmlPanel();
    myPanel.setHtml("<html></html>");

    MessageBusConnection settingsConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
    settingsConnection.subscribe(MarkdownApplicationSettings.SettingsChangedListener.TOPIC, mySettingsChangedListener);
    mySettingsChangedListener.onSettingsChange(MarkdownApplicationSettings.getInstance());
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myPanel.getComponent();
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
    updateHtml();
    myAlarm.cancelAllRequests();
    myAlarm.addRequest(new Runnable() {
      @Override
      public void run() {
      }
    }, 0);
  }

  private void updateHtml() {
    if (!myFile.isValid() || myDocument == null) {
      return;
    }

    String text = myDocument.getText();
    final ASTNode parsedTree = new MarkdownParser(MarkdownParserManager.FLAVOUR).buildMarkdownTreeFromString(text);
    final String html = new HtmlGenerator(text, parsedTree, MarkdownParserManager.FLAVOUR, LinkMap.Builder.buildLinkMap(parsedTree, text))
      .generateHtml();

    myPanel.setHtml("<html><head></head>" + html + "</html>");
  }

  private void updatePanelCssSettings(@NotNull MarkdownCssSettings cssSettings) {
    myPanel.setCssFile(cssSettings.isUriEnabled() ? cssSettings.getStylesheetUri() : null);
    myPanel.setCssInlineText(cssSettings.isTextEnabled() ? cssSettings.getStylesheetText() : null);
    myPanel.refresh();
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
