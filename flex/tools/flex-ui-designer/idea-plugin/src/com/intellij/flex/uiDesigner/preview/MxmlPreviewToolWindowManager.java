package com.intellij.flex.uiDesigner.preview;

import com.intellij.flex.uiDesigner.DesignerApplicationManager;
import com.intellij.flex.uiDesigner.DocumentFactoryManager;
import com.intellij.flex.uiDesigner.FlashUIDesignerBundle;
import com.intellij.flex.uiDesigner.SocketInputHandler;
import com.intellij.flex.uiDesigner.actions.RunDesignViewAction;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.LoadingDecorator;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerAdapter;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.PlatformIcons;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class MxmlPreviewToolWindowManager implements ProjectComponent {
  private static final String SETTINGS_TOOL_WINDOW_VISIBLE = "mxml.preview.tool.window.visible";

  private final Project project;
  private final FileEditorManager fileEditorManager;

  private final MergingUpdateQueue toolWindowUpdateQueue;

  private ToolWindow toolWindow;
  private MxmlPreviewToolWindowForm toolWindowForm;

  private boolean toolWindowReady = false;
  private boolean toolWindowDisposed = false;

  public MxmlPreviewToolWindowManager(final Project project, final FileEditorManager fileEditorManager) {
    this.project = project;
    this.fileEditorManager = fileEditorManager;

    toolWindowUpdateQueue = new MergingUpdateQueue("mxml.preview", 300, true, null, project);
  }

  @Override
  public void projectOpened() {
    StartupManager.getInstance(project).registerPostStartupActivity(new Runnable() {
      public void run() {
        toolWindowReady = true;
      }
    });

    final MessageBusConnection connection = project.getMessageBus().connect(project);
    connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new MyFileEditorManagerListener());
  }

  public void projectClosed() {
    if (toolWindowForm != null) {
      Disposer.dispose(toolWindowForm.getPreviewPanel());
      toolWindowForm = null;
      toolWindow = null;
      toolWindowDisposed = true;
    }
  }

  @NotNull
  @NonNls
  public String getComponentName() {
    return "MxmlPreviewToolWindowManager";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  private void initToolWindow() {
    toolWindowForm = new MxmlPreviewToolWindowForm(project, this);
    final String toolWindowId = FlashUIDesignerBundle.message("mxml.preview.tool.window.title");
    toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(toolWindowId, false, ToolWindowAnchor.RIGHT, project, false);
    toolWindow.setIcon(PlatformIcons.UI_FORM_ICON);

    ((ToolWindowManagerEx)ToolWindowManager.getInstance(project)).addToolWindowManagerListener(new ToolWindowManagerAdapter() {
      private boolean visible = false;

      @Override
      public void stateChanged() {
        if (project.isDisposed() || toolWindow == null || !toolWindow.isAvailable()) {
          return;
        }

        final boolean currentVisible = toolWindow.isVisible();
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        if (currentVisible) {
          propertiesComponent.setValue(SETTINGS_TOOL_WINDOW_VISIBLE, "true");
        }
        else {
          propertiesComponent.unsetValue(SETTINGS_TOOL_WINDOW_VISIBLE);
        }

        if (currentVisible && !visible) {
          render();
        }

        visible = currentVisible;
      }
    });

    final JPanel contentPanel = toolWindowForm.getContentPanel();
    final ContentManager contentManager = toolWindow.getContentManager();
    final Content content = contentManager.getFactory().createContent(contentPanel, null, false);
    content.setCloseable(false);
    content.setPreferredFocusableComponent(contentPanel);
    contentManager.addContent(content);
    contentManager.setSelectedContent(content, true);
    toolWindow.setAvailable(false, null);

    MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect(project);
    connection.subscribe(SocketInputHandler.MESSAGE_TOPIC, new SocketInputHandler.DocumentRenderedListener() {
      @Override
      public void documentRenderedOnAutoSave(DocumentFactoryManager.DocumentInfo info) {
        if (!toolWindowDisposed &&
            toolWindowForm != null &&
            toolWindowForm.getFile() != null &&
            info.equals(DocumentFactoryManager.getInstance().getNullableInfo(toolWindowForm.getFile()))) {
          render();
        }
      }

      @Override
      public void errorOccured() {
      }
    });
  }

  private void setDocumentImage(final BufferedImage image) {
    if (toolWindowForm == null || toolWindowForm.getFile() == null) {
      return;
    }

    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        toolWindowForm.getPreviewPanel().setImage(image);
      }
    });
  }

  private void render() {
    if (toolWindow == null || !toolWindow.isVisible()) {
      return;
    }

    final XmlFile psiFile = toolWindowForm.getFile();
    if (psiFile == null) {
      return;
    }

    final LoadingDecorator loadingDecorator = toolWindowForm.getPreviewPanel().getLoadingDecorator();
    loadingDecorator.startLoading(false);
    AsyncResult<BufferedImage> result = DesignerApplicationManager.getInstance().getDocumentImage(psiFile);
    result.doWhenDone(new AsyncResult.Handler<BufferedImage>() {
      @Override
      public void run(BufferedImage image) {
        setDocumentImage(image);
      }
    });
    result.doWhenProcessed(new Runnable() {
      @Override
      public void run() {
        loadingDecorator.stopLoading();
      }
    });
  }

  private boolean isApplicableEditor(Editor editor) {
    final Document document = editor.getDocument();
    return RunDesignViewAction.isApplicable(project, PsiDocumentManager.getInstance(project).getPsiFile(document));
  }

  private class MyFileEditorManagerListener extends FileEditorManagerAdapter implements Runnable {
    private boolean waitingForSmartMode;

    public void selectionChanged(FileEditorManagerEvent event) {
      final FileEditor newFileEditor = event.getNewEditor();
      Editor mxmlEditor = null;
      if (newFileEditor instanceof TextEditor) {
        final Editor editor = ((TextEditor)newFileEditor).getEditor();
        if (RunDesignViewAction.dumbAwareIsApplicable(project, PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument()))) {
          mxmlEditor = editor;
        }
      }

      if (mxmlEditor == null) {
        processFileEditorChange(null);
        return;
      }

      final DumbService dumbService = DumbService.getInstance(project);
      if (dumbService.isDumb()) {
        openWhenSmart(dumbService);
      }
      else {
        if (!isApplicableEditor(mxmlEditor)) {
          mxmlEditor = null;
        }

        processFileEditorChange(mxmlEditor);
      }
    }

    private void openWhenSmart(DumbService dumbService) {
      if (!waitingForSmartMode) {
        waitingForSmartMode = true;
        dumbService.runWhenSmart(this);
      }
    }

    @Override
    public void run() {
      waitingForSmartMode = false;
      final AccessToken token = ReadAction.start();
      final Editor selectedTextEditor;
      try {
        selectedTextEditor = fileEditorManager.getSelectedTextEditor();
      }
      finally {
        token.finish();
      }

      if (selectedTextEditor != null && isApplicableEditor(selectedTextEditor)) {
        processFileEditorChange(selectedTextEditor);
      }
    }
  }

  private void processFileEditorChange(@Nullable final Editor newEditor) {
    toolWindowUpdateQueue.cancelAllUpdates();
    toolWindowUpdateQueue.queue(new Update("update") {
      public void run() {
        if (!toolWindowReady || toolWindowDisposed) {
          return;
        }

        if (toolWindow == null) {
          if (newEditor == null) {
            return;
          }
          initToolWindow();
          // idea inspection bug
          //noinspection ConstantConditions
          assert toolWindow != null;
        }

        final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        final boolean hideForNonMxmlFiles = propertiesComponent.getBoolean("mxml.preview.tool.window.hideForNonMxmlFiles", true);

        XmlFile psiFile = newEditor == null ? null : (XmlFile)PsiDocumentManager.getInstance(project).getPsiFile(newEditor.getDocument());
        if (psiFile == null) {
          toolWindowForm.setFile(null);
          toolWindow.setAvailable(!hideForNonMxmlFiles, null);
          return;
        }

        final boolean doRender = toolWindowForm.getFile() != psiFile;
        if (doRender) {
          toolWindowForm.setFile(psiFile);
        }

        toolWindow.setAvailable(true, null);
        if (toolWindow.isVisible()) {
          render();
        }
        else if (propertiesComponent.getBoolean(SETTINGS_TOOL_WINDOW_VISIBLE, false)) {
          toolWindow.show(null);
        }
      }
    });
  }
}
