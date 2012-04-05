package com.intellij.flex.uiDesigner.preview;

import com.intellij.flex.uiDesigner.*;
import com.intellij.flex.uiDesigner.io.*;
import com.intellij.flex.uiDesigner.mxml.MxmlUtil;
import com.intellij.flex.uiDesigner.mxml.PrimitiveWriter;
import com.intellij.flex.uiDesigner.mxml.XmlAttributeValueProvider;
import com.intellij.flex.uiDesigner.mxml.XmlElementValueProvider;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.FlexReferenceContributor;
import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
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
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.Alarm;
import com.intellij.util.Consumer;
import com.intellij.util.PlatformIcons;
import com.intellij.util.Processor;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import com.intellij.xml.XmlAttributeDescriptor;
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

  @Override
  public void projectOpened() {
    project.getMessageBus().connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new MyFileEditorManagerListener());

    final Alarm syncAlarm = new Alarm();
    PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {
      public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
        //update(event);
      }

      public void childRemoved(@NotNull PsiTreeChangeEvent event) {
        update(event);
      }

      public void childAdded(@NotNull PsiTreeChangeEvent event) {
        update(event);
      }

      public void childReplaced(@NotNull PsiTreeChangeEvent event) {
        update(event);
      }

      @Override
      public void propertyChanged(@NotNull PsiTreeChangeEvent event) {
        //super.propertyChanged(event);
      }

      private void update(final PsiTreeChangeEvent event) {
        if (!toolWindowVisible || event.getFile() == null || toolWindowForm.getFile() != event.getFile()) {
          return;
        }

        syncAlarm.cancelAllRequests();
        syncAlarm.addRequest(new Synchronizer(event), 100, ModalityState.NON_MODAL);
      }
    }, project);
  }

  public void projectClosed() {
    if (toolWindowForm != null) {

      XmlFile file = toolWindowForm.getFile();
      if (file != null) {
        final VirtualFile virtualFile = file.getViewProvider().getVirtualFile();
        BufferedImage image = toolWindowForm.getPreviewPanel().getImage();
        if (image != null) {
          try {
            IOUtil.saveImage(toolWindowForm.getPreviewPanel().getImage(),
                             new File(DesignerApplicationManager.APP_DIR, LAST_PREVIEW_IMAGE_FILE_NAME), new Consumer<DataOutputStream>() {
              @Override
              public void consume(DataOutputStream out) {
                try {
                  out.writeLong(virtualFile.getTimeStamp());
                  out.writeUTF(virtualFile.getUrl());
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

  private boolean checkLastImage() {
    File file = new File(DesignerApplicationManager.APP_DIR, LAST_PREVIEW_IMAGE_FILE_NAME);
    if (!file.exists()) {
      return false;
    }

    try {
      final VirtualFile virtualFile = toolWindowForm.getFile().getViewProvider().getVirtualFile();
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

  private void render(boolean isSlow) {
    if (!toolWindowVisible) {
      return;
    }

    final XmlFile psiFile = toolWindowForm.getFile();
    if (psiFile == null) {
      return;
    }

    if (isSlow) {
      toolWindowForm.getPreviewPanel().getLoadingDecorator().startLoading(false);
      loadingDecoratorStarted++;
    }

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

    if (isSlow) {
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

        if (!toolWindow.isAvailable()) {
          toolWindowVisible = toolWindow.isVisible();
          toolWindow.setAvailable(true, null);
        }

        if (toolWindowVisible) {
          render();
        }
        else if (propertiesComponent.getBoolean(SETTINGS_TOOL_WINDOW_VISIBLE, false)) {
          toolWindow.show(null);
        }
      }
    }, 300);
  }

  private final class Synchronizer implements Runnable {
    private final PsiTreeChangeEvent event;

    public Synchronizer(PsiTreeChangeEvent event) {
      this.event = event;
    }

    @Nullable
    private XmlElementValueProvider findSupportedTarget() {
      if (DesignerApplicationManager.getInstance().isApplicationClosed()) {
        return null;
      }

      PsiElement element = event.getParent();
      // if we change attribute value via line marker, so, event.getParent() will be XmlAttribute instead of XmlAttributeValue
      while (!(element instanceof XmlAttribute)) {
        element = element.getParent();
        if (element instanceof XmlTag || element instanceof PsiFile || element == null) {
          return null;
        }
      }

      XmlAttribute attribute = (XmlAttribute)element;
      if (JavaScriptSupportLoader.MXML_URI3.equals(attribute.getNamespace())) {
        return null;
      }

      XmlAttributeDescriptor xmlDescriptor = attribute.getDescriptor();
      if (!(xmlDescriptor instanceof AnnotationBackedDescriptor)) {
        return null;
      }

      AnnotationBackedDescriptor descriptor = (AnnotationBackedDescriptor)xmlDescriptor;
      if (descriptor.isPredefined() || MxmlUtil.isIdLanguageAttribute(attribute, descriptor)) {
        return null;
      }

      // todo incremental sync for state-specific attributes
      PsiReference[] references = attribute.getReferences();
      if (references.length > 1) {
        for (int i = references.length - 1; i > -1; i--) {
          PsiReference psiReference = references[i];
          if (psiReference instanceof FlexReferenceContributor.StateReference) {
            return null;
          }
        }
      }
      else {
        String prefix = attribute.getName() + '.';
        for (XmlAttribute anotherAttribute : attribute.getParent().getAttributes()) {
          if (anotherAttribute != attribute && anotherAttribute.getName().startsWith(prefix)) {
            return null;
          }
        }
      }

      XmlAttributeValueProvider valueProvider = new XmlAttributeValueProvider(attribute);
      // skip binding
      PsiLanguageInjectionHost injectedHost = valueProvider.getInjectedHost();
      if (injectedHost != null && InjectedLanguageUtil.hasInjections(injectedHost)) {
        return null;
      }

      return valueProvider;
    }

    @Override
    public void run() {
      if (!DesignerApplicationManager.getInstance().isInitialRendering() && !incrementalSync()) {
        render();
      }
    }

    private boolean incrementalSync() {
      final XmlElementValueProvider valueProvider = findSupportedTarget();
      if (valueProvider == null) {
        return false;
      }

      DocumentFactoryManager.DocumentInfo info = DocumentFactoryManager.getInstance().getInfo(valueProvider.getElement());
      XmlTag tag = (XmlTag)valueProvider.getElement().getParent();
      if (!(tag.getDescriptor() instanceof ClassBackedElementDescriptor)) {
        return false;
      }

      int componentId = info.rangeMarkerIndexOf(tag);
      if (componentId == -1) {
        return false;
      }

      final AnnotationBackedDescriptor descriptor = (AnnotationBackedDescriptor)valueProvider.getPsiMetaData();
      assert descriptor != null;
      final String typeName = descriptor.getTypeName();
      final String type = descriptor.getType();
      if (type == null) {
        return !typeName.equals(FlexAnnotationNames.EFFECT);
      }
      else if (type.equals(JSCommonTypeNames.FUNCTION_CLASS_NAME) || typeName.equals(FlexAnnotationNames.EVENT)) {
        return true;
      }

      final StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter();
      final PrimitiveAmfOutputStream dataOut = new PrimitiveAmfOutputStream(new ByteArrayOutputStreamEx(16));
      PrimitiveWriter writer = new PrimitiveWriter(dataOut, stringWriter);
      boolean needRollbackStringWriter = true;
      try {
        if (descriptor.isAllowsPercentage()) {
          String value = valueProvider.getTrimmed();
          final boolean hasPercent;
          if (value.isEmpty() || ((hasPercent = value.endsWith("%")) && value.length() == 1)) {
            return true;
          }

          final String name;
          if (hasPercent) {
            name = descriptor.getPercentProxy();
            value = value.substring(0, value.length() - 1);
          }
          else {
            name = descriptor.getName();
          }

          stringWriter.write(name, dataOut);
          dataOut.writeAmfDouble(value);
        }
        else {
          stringWriter.write(descriptor.getName(), dataOut);
          if (!writer.writeIfApplicable(valueProvider, dataOut, descriptor)) {
            needRollbackStringWriter = false;
            stringWriter.rollback();
            return false;
          }
        }

        needRollbackStringWriter = false;
      }
      catch (InvalidPropertyException e) {
        return true;
      }
      catch (NumberFormatException e) {
        return true;
      }
      finally {
        if (needRollbackStringWriter) {
          stringWriter.rollback();
        }
      }

      final DocumentFactoryManager.DocumentInfo finalInfo = info;
      Client.getInstance().updatePropertyOrStyle(info.getId(), componentId, new Consumer<AmfOutputStream>() {
        @Override
        public void consume(AmfOutputStream stream) {
          stringWriter.writeTo(stream);
          stream.write(descriptor.isStyle());
          dataOut.writeTo(stream);
        }
      }).doWhenDone(new Runnable() {
        @Override
        public void run() {
          Document document = FileDocumentManager.getInstance().getCachedDocument(finalInfo.getElement());
          if (document != null) {
            finalInfo.documentModificationStamp = document.getModificationStamp();
            UIUtil.invokeLaterIfNeeded(new Runnable() {
              @Override
              public void run() {
                render(false);
              }
            });
          }
        }
      });

      return true;
    }
  }
}
