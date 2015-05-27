package org.intellij.plugins.markdown.preview;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Alarm;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.ast.ASTNode;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.lexer.MarkdownLexer;
import org.intellij.markdown.parser.LexerBasedTokensCache;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.markdown.parser.TokensCache;
import org.intellij.markdown.parser.dialects.commonmark.CommonMarkMarkerProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeListener;

public class MarkdownPreviewFileEditor extends UserDataHolderBase implements FileEditor {
  private final static long PARSING_CALL_TIMEOUT_MS = 100L;
  @NotNull
  private final JFXPanel myPanel;
  @Nullable
  private WebView myBrowser;
  @NotNull
  private final VirtualFile myFile;
  @Nullable
  private final Document myDocument;
  @NotNull
  private final Alarm myAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);

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

    myPanel = new JFXPanel();
    myPanel.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        adjustBrowserSize();
      }
    });

    Platform.setImplicitExit(false);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        initWebViewInComponent(myPanel);
      }
    });
  }

  private void initWebViewInComponent(@NotNull JFXPanel panel) {
    Group group = new Group();
    Scene scene = new Scene(group);
    panel.setScene(scene);

    myBrowser = new WebView();

    group.getChildren().add(myBrowser);
    myBrowser.setContextMenuEnabled(false);
    adjustBrowserSize();

    myBrowser.getEngine().loadContent("<html></html>");
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myPanel;
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return myPanel;
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

  private void adjustBrowserSize() {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        final Dimension size = myPanel.getSize();
        if (myBrowser != null) {
          myBrowser.setPrefSize(size.getWidth(), size.getHeight());
        }
      }
    });
  }

  private void updateHtml() {
    if (!myFile.isValid() || myBrowser == null || myDocument == null) {
      return;
    }

    String text = myDocument.getText();
    final TokensCache tokensCache = new LexerBasedTokensCache(new MarkdownLexer(text));
    final ASTNode parsedTree = new MarkdownParser(CommonMarkMarkerProcessor.Factory.INSTANCE$)
      .parse(MarkdownElementTypes.MARKDOWN_FILE, tokensCache);
    final String html = new HtmlGenerator(text, parsedTree).generateHtml();

    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        myBrowser.getEngine().getLoadWorker().cancel();
        myBrowser.getEngine().loadContent("<html>" + html + "</html>");
      }
    });
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
