package com.intellij.flex.uiDesigner.preview;

import com.intellij.flex.uiDesigner.*;
import com.intellij.flex.uiDesigner.io.IOUtil;
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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerAdapter;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.Alarm;
import com.intellij.util.Consumer;
import com.intellij.util.PlatformIcons;
import com.intellij.util.Processor;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class MxmlPreviewToolWindowManager implements ProjectComponent {
  private static final String SETTINGS_TOOL_WINDOW_VISIBLE = "mxml.preview.tool.window.visible";
  private static final String LAST_PREVIEW_IMAGE_FILE_NAME = "lastPreview";

  private final Project project;
  private final FileEditorManager fileEditorManager;

  private final Alarm toolWindowUpdateAlarm;

  private ToolWindow toolWindow;
  private MxmlPreviewToolWindowForm toolWindowForm;

  private boolean toolWindowVisible;
  private int loadingDecoratorStarted;
  private boolean lastPreviewChecked;

  public MxmlPreviewToolWindowManager(final Project project, final FileEditorManager fileEditorManager) {
    this.project = project;
    this.fileEditorManager = fileEditorManager;

    toolWindowUpdateAlarm = new Alarm();
  }

  public VirtualFile getServedFile() {
    return toolWindowVisible && toolWindowForm != null ? toolWindowForm.getFile() : null;
  }

  @Override
  public void projectOpened() {
    project.getMessageBus().connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new MyFileEditorManagerListener());
  }

  public void projectClosed() {
    if (toolWindowForm == null) {
      return;
    }

    try {
      VirtualFile file = toolWindowForm.getFile();
      if (file != null) {
        saveLastImage(file);
      }
    }
    finally {
      Disposer.dispose(toolWindowForm.getPreviewPanel());
      toolWindowForm = null;
      toolWindow = null;
      toolWindowVisible = false;
    }
  }

  private void saveLastImage(final VirtualFile file) {
    BufferedImage image = toolWindowForm.getPreviewPanel().getImage();
    if (image != null) {
      try {
        IOUtil.saveImage(toolWindowForm.getPreviewPanel().getImage(),
                         new File(DesignerApplicationManager.APP_DIR, LAST_PREVIEW_IMAGE_FILE_NAME), new Consumer<DataOutputStream>() {
          @Override
          public void consume(DataOutputStream out) {
            try {
              out.writeLong(file.getTimeStamp());
              out.writeUTF(file.getUrl());
            }
            catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        });
      }
      catch (Throwable e) {
        LogMessageUtil.LOG.warn("Can't save image for last document", e);
      }
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
    String toolWindowId = FlashUIDesignerBundle.message("mxml.preview.tool.window.title");
    toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(toolWindowId, false, ToolWindowAnchor.RIGHT, project, false);
    toolWindow.setIcon(PlatformIcons.UI_FORM_ICON);

    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
    toolWindowVisible = propertiesComponent.getBoolean(SETTINGS_TOOL_WINDOW_VISIBLE, false);
    if (toolWindowVisible) {
      toolWindow.show(null);
    }
    else {
      toolWindow.hide(null);
    }

    ((ToolWindowManagerEx)ToolWindowManager.getInstance(project)).addToolWindowManagerListener(new ToolWindowManagerAdapter() {
      @Override
      public void stateChanged() {
        //noinspection ConstantConditions
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

          if (!lastPreviewChecked) {
            lastPreviewChecked = true;
            if (checkLastImage()) {
              return;
            }
          }

          render();
        }
        else {
          propertiesComponent.unsetValue(SETTINGS_TOOL_WINDOW_VISIBLE);
        }
      }
    });

    JPanel contentPanel = toolWindowForm.getContentPanel();
    ContentManager contentManager = toolWindow.getContentManager();
    Content content = contentManager.getFactory().createContent(contentPanel, null, false);
    content.setCloseable(false);
    content.setPreferredFocusableComponent(contentPanel);
    contentManager.addContent(content);
    contentManager.setSelectedContent(content, true);

    MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect(project);
    connection.subscribe(DesignerApplicationManager.MESSAGE_TOPIC, new DesignerApplicationManager.DocumentRenderedListener() {
      private boolean isApplicable(DocumentFactoryManager.DocumentInfo info) {
        return toolWindowVisible &&
               toolWindowForm.getFile() != null &&
               info.equals(DocumentFactoryManager.getInstance().getNullableInfo(toolWindowForm.getFile()));
      }

      @Override
      public void documentRendered(DocumentFactoryManager.DocumentInfo info) {
        if (isApplicable(info) && !toolWindowForm.waitingForGetDocument.get()) {
          UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
              render(false);
            }
          });
        }
      }

      @Override
      public void errorOccured() {
      }
    });
  }

  private boolean checkLastImage() {
    File file = new File(DesignerApplicationManager.APP_DIR, LAST_PREVIEW_IMAGE_FILE_NAME);
    if (!file.exists()) {
      return false;
    }

    try {
      final VirtualFile virtualFile = toolWindowForm.getFile();
      BufferedImage image = IOUtil.readImage(file,
                                             new Processor<DataInputStream>() {
                                               @Override
                                               public boolean process(DataInputStream in) {
                                                 try {
                                                   return in.readLong() == virtualFile.getTimeStamp() &&
                                                          in.readUTF().equals(virtualFile.getUrl());
                                                 }
                                                 catch (IOException e) {
                                                   throw new RuntimeException(e);
                                                 }
                                               }
                                             });
      if (image != null) {
        toolWindowForm.getPreviewPanel().setImage(image);
        return true;
      }
    }
    catch (IOException e) {
      LogMessageUtil.LOG.warn("Can't read image for last document", e);
    }

    return false;
  }

  private void render() {
    render(true);
  }

  private void render(final boolean isSlow) {
    if (!toolWindowVisible) {
      return;
    }

    final VirtualFile file = toolWindowForm.getFile();
    if (file == null) {
      return;
    }

    if (isSlow) {
      toolWindowForm.getPreviewPanel().getLoadingDecorator().startLoading(false);
      loadingDecoratorStarted++;
    }

    toolWindowForm.waitingForGetDocument.set(true);
    @SuppressWarnings("ConstantConditions")
    AsyncResult<BufferedImage> result = DesignerApplicationManager.getInstance().getDocumentImage((XmlFile)PsiManager.getInstance(project).findFile(file));
    result.doWhenDone(new QueuedAsyncResultHandler<BufferedImage>() {
      @Override
      protected boolean isExpired() {
        //noinspection ConstantConditions
        return toolWindowForm == null || toolWindowForm.getFile() != file;
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
        toolWindowForm.waitingForGetDocument.set(false);
        if (isSlow && --loadingDecoratorStarted == 0 && toolWindowForm != null) {
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
    toolWindowUpdateAlarm.cancelAllRequests();
    toolWindowUpdateAlarm.addRequest(new Runnable() {
      @Override
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

        VirtualFile psiFile = newEditor == null ? null : FileDocumentManager.getInstance().getFile(newEditor.getDocument());
        if (psiFile == null) {
          return;
        }

        final boolean doRender = toolWindowForm.getFile() != psiFile;
        if (doRender) {
          toolWindowForm.setFile(psiFile);
        }

        if (toolWindowVisible) {
          render();
        }
      }
    }, 300);
  }
}
