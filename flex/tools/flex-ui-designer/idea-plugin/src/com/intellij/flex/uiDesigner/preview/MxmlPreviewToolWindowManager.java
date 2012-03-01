package com.intellij.flex.uiDesigner.preview;

import com.intellij.flex.uiDesigner.*;
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
import com.intellij.util.ui.UIUtil;
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

  private boolean toolWindowVisible;

  private int loadingDecoratorStarted;

  public MxmlPreviewToolWindowManager(final Project project, final FileEditorManager fileEditorManager) {
    this.project = project;
    this.fileEditorManager = fileEditorManager;

    toolWindowUpdateQueue = new MergingUpdateQueue("mxml.preview", 300, true, null, project);
  }

  @Override
  public void projectOpened() {
    project.getMessageBus().connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new MyFileEditorManagerListener());
  }

  public void projectClosed() {
    if (toolWindowForm != null) {
      Disposer.dispose(toolWindowForm.getPreviewPanel());
      toolWindowForm = null;
      toolWindow = null;
      toolWindowVisible = false;
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
      @Override
      public void stateChanged() {
        if (project.isDisposed() || toolWindow == null || !toolWindow.isAvailable()) {
          return;
        }

        final boolean currentVisible = toolWindow.isVisible();
        if (currentVisible == toolWindowVisible) {
          return;
        }

        toolWindowVisible = currentVisible;

        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        if (currentVisible) {
          propertiesComponent.setValue(SETTINGS_TOOL_WINDOW_VISIBLE, "true");
          render();
        }
        else {
          propertiesComponent.unsetValue(SETTINGS_TOOL_WINDOW_VISIBLE);
        }
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
        if (toolWindowVisible &&
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

  private void render() {
    if (!toolWindowVisible) {
      return;
    }

    final XmlFile psiFile = toolWindowForm.getFile();
    if (psiFile == null) {
      return;
    }

    toolWindowForm.getPreviewPanel().getLoadingDecorator().startLoading(false);
    loadingDecoratorStarted++;
    AsyncResult<BufferedImage> result = DesignerApplicationManager.getInstance().getDocumentImage(psiFile);
    result.doWhenDone(new QueuedAsyncResultHandler<BufferedImage>() {
      @Override
      protected boolean isExpired() {
        //noinspection ConstantConditions
        return toolWindowForm == null || toolWindowForm.getFile() != psiFile;
      }

      @Override
      public void process(final BufferedImage image) {
        UIUtil.invokeLaterIfNeeded(new Runnable() {
          @Override
          public void run() {
            toolWindowForm.getPreviewPanel().setImage(image);
          }
        });
      }
    });
    result.doWhenProcessed(new Runnable() {
      @Override
      public void run() {
        //noinspection ConstantConditions
        if (--loadingDecoratorStarted == 0 && toolWindowForm != null) {
          toolWindowForm.getPreviewPanel().getLoadingDecorator().stopLoading();
        }
      }
    });
  }

  private boolean isApplicableEditor(Editor editor) {
    final Document document = editor.getDocument();
    return DesignerApplicationManager.isApplicable(project, PsiDocumentManager.getInstance(project).getPsiFile(document));
  }

  private class MyFileEditorManagerListener extends FileEditorManagerAdapter implements Runnable {
    private boolean waitingForSmartMode;

    public void selectionChanged(FileEditorManagerEvent event) {
      final FileEditor newFileEditor = event.getNewEditor();
      Editor mxmlEditor = null;
      if (newFileEditor instanceof TextEditor) {
        final Editor editor = ((TextEditor)newFileEditor).getEditor();
        if (DesignerApplicationManager.dumbAwareIsApplicable(project,
                                                             PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument()))) {
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
    toolWindowUpdateQueue.queue(new Update("update mxml preview") {
      public void run() {
        if (!project.isOpen() || project.isDisposed()) {
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
        if (toolWindowVisible) {
          render();
        }
        else if (propertiesComponent.getBoolean(SETTINGS_TOOL_WINDOW_VISIBLE, false)) {
          toolWindow.show(null);
        }
      }
    });
  }
}
