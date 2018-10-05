// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.uiDesigner;

import com.intellij.ProjectTopics;
import com.intellij.flex.uiDesigner.mxml.ProjectComponentReferenceCounter;
import com.intellij.flex.uiDesigner.preview.MxmlPreviewToolWindowManager;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.components.ServiceDescriptor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.impl.ServiceManagerImpl;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.psi.xml.XmlComment;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.AppUIUtil;
import com.intellij.util.Consumer;
import com.intellij.util.concurrency.QueueProcessor;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import com.intellij.util.ui.update.MergingUpdateQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.swing.event.HyperlinkEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.flex.uiDesigner.DocumentFactoryManager.DocumentInfo;
import static com.intellij.flex.uiDesigner.LogMessageUtil.LOG;
import static com.intellij.flex.uiDesigner.RenderActionQueue.RenderAction;

public class DesignerApplicationManager {
  private static final ExtensionPointName<ServiceDescriptor> SERVICES =
    new ExtensionPointName<>("com.intellij.flex.uiDesigner.service");

  public static final File APP_DIR = new File(PathManager.getSystemPath(), "flashUIDesigner");

  public static final Topic<DocumentRenderedListener> MESSAGE_TOPIC =
    new Topic<>("Flash UI Designer document rendered event", DocumentRenderedListener.class);

  private DesignerApplication application;

  private final RenderActionQueue initialRenderQueue = new RenderActionQueue();

  private ServiceManagerImpl serviceManager;

  private static class MyServiceManagerImpl extends ServiceManagerImpl {
    MyServiceManagerImpl(@NotNull DesignerApplication newApp) {
      super(true);

      installEP(SERVICES, newApp);
    }
  }

  public static <T> T getService(@NotNull Class<T> serviceClass) {
    //noinspection unchecked
    return (T)getApplication().getPicoContainer().getComponentInstance(serviceClass.getName());
  }

  public static DesignerApplicationManager getInstance() {
    return ServiceManager.getService(DesignerApplicationManager.class);
  }

  public static DesignerApplication getApplication() {
    return getInstance().application;
  }

  @TestOnly
  static ExtensionPoint<ServiceDescriptor> getExtensionPoint() {
    return Extensions.getArea(null).getExtensionPoint(SERVICES);
  }

  void disposeApplication() {
    LOG.assertTrue(application != null);
    final DesignerApplication disposedApp = application;
    application = null;
    AppUIUtil.invokeOnEdt(() -> {
      try {
        Disposer.dispose(disposedApp);
      }
      finally {
        Disposer.dispose(serviceManager);
        serviceManager = null;
      }
    });
  }

  void setApplication(@NotNull DesignerApplication newApp) {
    LOG.assertTrue(application == null);
    LOG.assertTrue(serviceManager == null);
    Disposer.register(ApplicationManager.getApplication(), newApp);
    serviceManager = new MyServiceManagerImpl(newApp);
    application = newApp;
  }

  public boolean isInitialRendering() {
    return !initialRenderQueue.isEmpty();
  }

  private static boolean checkFlexSdkVersion(final Sdk sdk) {
    String version = sdk.getVersionString();
    if (StringUtil.isEmpty(version)) {
      LOG.warn("Flex SDK " + sdk.getName() + " version is empty, try to read flex-sdk-description.xml");
      VirtualFile sdkHomeDirectory = sdk.getHomeDirectory();
      if (sdkHomeDirectory == null) {
        LOG.warn("Flex SDK " + sdk.getName() + " home directory is null, cannot read flex-sdk-description.xml");
        return false;
      }

      version = FlexSdkUtils.doReadFlexSdkVersion(sdkHomeDirectory);
      if (StringUtil.isEmpty(version)) {
        LOG.warn("Flex SDK " + sdk.getName() + " version is empty and result of read flex-sdk-description.xml is also empty");
        return false;
      }

      setVersionString(sdk, version);
    }

    if (StringUtil.compareVersionNumbers(version, "4.5.1") >= 0) {
      return true;
    }

    if (version.length() < 5 || version.charAt(0) < '4') {
      return false;
    }

    if (version.charAt(0) == '4') {
      int build = FlexSdkUtils.getFlexSdkRevision(version);
      if (version.charAt(2) == '1') {
        return build == 16076;
      }
      else if (version.charAt(2) == '5' && version.charAt(4) == '0') {
        return build == 20967;
      }
      else {
        return version.charAt(2) >= '5';
      }
    }

    return true;
  }

  private static void setVersionString(Sdk sdk, String version) {
    WriteAction.run(() -> {
      SdkModificator modificator = sdk.getSdkModificator();
      modificator.setVersionString(version);
      modificator.commitChanges();
    });
  }

  public void renderIfNeed(@NotNull XmlFile psiFile, @Nullable Consumer<DocumentInfo> handler) {
    renderIfNeed(psiFile, handler, null, false);
  }

  public void renderIfNeed(@NotNull XmlFile psiFile,
                           @Nullable final Consumer<DocumentInfo> handler,
                           @Nullable ActionCallback renderRejectedCallback,
                           final boolean debug) {
    boolean needInitialRender = isApplicationClosed();
    DocumentInfo documentInfo = null;
    if (!needInitialRender) {
      Document[] unsavedDocuments = FileDocumentManager.getInstance().getUnsavedDocuments();
      if (unsavedDocuments.length > 0) {
        renderDocumentsAndCheckLocalStyleModification(unsavedDocuments);
      }

      documentInfo = DocumentFactoryManager.getInstance().getNullableInfo(psiFile);
      needInitialRender = documentInfo == null;
    }

    if (!needInitialRender) {
      if (handler == null) {
        return;
      }

      Application app = ApplicationManager.getApplication();
      if (app.isDispatchThread()) {
        final DocumentInfo finalDocumentInfo = documentInfo;
        app.executeOnPooledThread(() -> handler.consume(finalDocumentInfo));
      }
      else {
        handler.consume(documentInfo);
      }
      return;
    }

    synchronized (initialRenderQueue) {
      AsyncResult<DocumentInfo> renderResult = initialRenderQueue.findResult(psiFile);
      if (renderResult == null) {
        renderResult = new AsyncResult<>();
        if (renderRejectedCallback != null) {
          renderResult.notifyWhenRejected(renderRejectedCallback);
        }

        initialRenderQueue.add(new RenderAction<AsyncResult<DocumentInfo>>(psiFile.getProject(), psiFile.getViewProvider().getVirtualFile(), renderResult) {
          @Override
          protected boolean isNeedEdt() {
            // ProgressManager requires dispatch thread
            return true;
          }

          @Override
          protected void doRun() {
            assert project != null;
            if (project.isDisposed()) {
              return;
            }

            assert file != null;
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (!(psiFile instanceof XmlFile)) {
              return;
            }

            Module module = ModuleUtilCore.findModuleForFile(file, project);
            if (module != null) {
              renderDocument(module, (XmlFile)psiFile, debug, result);
            }
          }
        });
      }

      if (handler != null) {
        renderResult.doWhenDone(handler);
      }
      renderResult.doWhenDone(createDocumentRenderedNotificationDoneHandler(false));
    }
  }

  public static Consumer<DocumentInfo> createDocumentRenderedNotificationDoneHandler(final boolean syncTimestamp) {
    return info -> {
      Document document = FileDocumentManager.getInstance().getCachedDocument(info.getElement());
      if (document != null) {
        if (syncTimestamp) {
          info.documentModificationStamp = document.getModificationStamp();
        }
        ApplicationManager.getApplication().getMessageBus().syncPublisher(MESSAGE_TOPIC).documentRendered(info);
      }
    };
  }

  @NotNull
  public AsyncResult<BufferedImage> getDocumentImage(@NotNull XmlFile psiFile) {
    final AsyncResult<BufferedImage> result = new AsyncResult<>();
    renderIfNeed(psiFile, documentInfo -> Client.getInstance().getDocumentImage(documentInfo, result), result, false);
    return result;
  }

  public void openDocument(@NotNull final XmlFile psiFile, final boolean debug) {
    renderIfNeed(psiFile, documentInfo -> Client.getInstance().selectComponent(documentInfo.getId(), 0), null, debug);
  }

  @TestOnly
  public AsyncResult<DocumentInfo> renderDocument(@NotNull final Module module, @NotNull final XmlFile psiFile) {
    final AsyncResult<DocumentInfo> result = new AsyncResult<>();
    renderDocument(module, psiFile, false, result);
    return result;
  }

  private void renderDocument(@NotNull final Module module, @NotNull final XmlFile psiFile, boolean debug, AsyncResult<DocumentInfo> result) {
    final boolean appClosed = isApplicationClosed();
    boolean hasError = true;
    try {
      if (appClosed || !Client.getInstance().isModuleRegistered(module)) {
        final Sdk sdk = FlexUtils.getSdkForActiveBC(module);
        if (sdk == null || !checkFlexSdkVersion(sdk)) {
          reportInvalidFlexSdk(module, debug, sdk);
          result.setRejected();
          return;
        }
      }

      hasError = false;
    }
    finally {
      if (hasError) {
        result.setRejected();
      }
    }

    final RenderDocumentTask renderDocumentTask = new RenderDocumentTask(psiFile, result);
    ProgressManager.getInstance().run(appClosed
                                      ? new DesignerApplicationLauncher(module, renderDocumentTask, debug)
                                      : new DocumentTaskExecutor(module, renderDocumentTask));
  }

  public boolean isApplicationClosed() {
    return application == null;
  }

  private static void reportInvalidFlexSdk(final Module module, boolean debug, @Nullable Sdk sdk) {
    String message = sdk == null
                     ? FlashUIDesignerBundle.message("module.sdk.is.not.specified", module.getName())
                     : FlashUIDesignerBundle.message("module.sdk.is.not.compatible", sdk.getVersionString(), module.getName());

    notifyUser(debug, message, module.getProject(), id -> {
      if ("edit".equals(id)) {
        FlexSdkUtils.openModuleConfigurable(module);
      }
      else {
        LOG.error("unexpected id: " + id);
      }
    });
  }

  public void renderDocumentsAndCheckLocalStyleModification(Document[] documents) {
    renderDocumentsAndCheckLocalStyleModification(documents, false, false);
  }

  public void renderDocumentsAndCheckLocalStyleModification(final Document[] documents, final boolean onlyStyle, boolean reportProblems) {
    synchronized (initialRenderQueue) {
      final AtomicBoolean result = new AtomicBoolean();
      if (!initialRenderQueue.isEmpty()) {
        initialRenderQueue.processActions(renderAction -> {
          if (renderAction.file == null) {
            ComplexRenderAction action = (ComplexRenderAction)renderAction;
            if (onlyStyle == action.onlyStyle) {
              action.merge(documents);
              result.set(true);
              return false;
            }
          }
          return true;
        });
      }

      if (!result.get()) {
        initialRenderQueue.add(new ComplexRenderAction(documents, onlyStyle, reportProblems));
      }
    }
  }

  public static String getOpenActionTitle(boolean debug) {
    return FlashUIDesignerBundle
      .message(debug ? "action.FlashUIDesigner.DebugDesignView.text" : "action.FlashUIDesigner.RunDesignView.text");
  }

  public static void notifyUser(boolean debug, @NotNull String text, @NotNull Module module) {
    notifyUser(debug, text, module.getProject(), null);
  }

  static void notifyUser(boolean debug, @NotNull String text, @NotNull Project project, @Nullable final Consumer<? super String> handler) {
    Notification notification = new Notification(FlashUIDesignerBundle.message("plugin.name"), getOpenActionTitle(debug), text,
                                                 NotificationType.ERROR, handler == null ? null : new NotificationListener() {
      @Override
      public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
        if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
          return;
        }

        notification.expire();
        handler.consume(event.getDescription());
      }
    });
    notification.notify(project);
  }

  public static boolean isApplicable(Project project, PsiFile psiFile) {
    if (!dumbAwareIsApplicable(project, psiFile)) {
      return false;
    }

    final JSClass jsClass = XmlBackedJSClassFactory.getInstance().getXmlBackedClass(((XmlFile)psiFile).getRootTag());
    return jsClass != null && ActionScriptClassResolver.isParentClass(jsClass, FlexCommonTypeNames.FLASH_DISPLAY_OBJECT_CONTAINER);
  }

  public static boolean dumbAwareIsApplicable(Project project, PsiFile psiFile) {
    final VirtualFile file = psiFile == null ? null : psiFile.getViewProvider().getVirtualFile();
    if (file == null || !JavaScriptSupportLoader.isFlexMxmFile(file) || !ProjectRootManager.getInstance(project).getFileIndex().isInSourceContent(file)) {
      return false;
    }
    final XmlTag rootTag = ((XmlFile)psiFile).getRootTag();
    return rootTag != null && rootTag.getPrefixByNamespace(JavaScriptSupportLoader.MXML_URI3) != null;
  }

  public void projectRegistered(Project project) {
    PsiManager.getInstance(project).addPsiTreeChangeListener(new MyPsiTreeChangeListener(project), project);
  }

  void attachProjectAndModuleListeners(Disposable parentDisposable) {
    MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect(parentDisposable);
    connection.subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {
      @Override
      public void projectClosed(@NotNull Project project) {
        if (isApplicationClosed()) {
          return;
        }

        Client client = Client.getInstance();
        if (client.getRegisteredProjects().contains(project)) {
          client.closeProject(project);
        }
      }
    });

    // unregistered module is more complicated - we cannot just remove all document factories which belong to project as in case of close project
    // we must remove all document factories belong to module and all dependents (dependent may be from another module, so, we process moduleRemoved synchronous
    // one by one)
    connection.subscribe(ProjectTopics.MODULES, new ModuleListener() {
      @Override
      public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
        Client client = Client.getInstance();
        if (!client.isModuleRegistered(module)) {
          return;
        }

        if (unregisterTaskQueueProcessor == null) {
          unregisterTaskQueueProcessor = new QueueProcessor<>(module1 -> {
            boolean hasError = true;
            final ActionCallback callback;
            initialRenderQueue.suspend();
            try {
              callback = Client.getInstance().unregisterModule(module1);
              hasError = false;
            }
            finally {
              if (hasError) {
                initialRenderQueue.resume();
              }
            }

            callback.doWhenProcessed(() -> initialRenderQueue.resume());
          });
        }

        unregisterTaskQueueProcessor.add(module);
      }
    });
  }

  private QueueProcessor<Module> unregisterTaskQueueProcessor;

  private static class RenderDocumentTask extends DesignerApplicationLauncher.PostTask {
    private final XmlFile psiFile;
    private final AsyncResult<? super DocumentInfo> asyncResult;

    RenderDocumentTask(@NotNull XmlFile psiFile, @Nullable AsyncResult<? super DocumentInfo> asyncResult) {
      this.psiFile = psiFile;
      this.asyncResult = asyncResult;
    }

    @Override
    public void dispose() {
      super.dispose();

      if (asyncResult != null && !asyncResult.isProcessed()) {
        asyncResult.setRejected();
      }
    }

    @Override
    public boolean run(Module module,
                       @Nullable ProjectComponentReferenceCounter projectComponentReferenceCounter,
                       ProgressIndicator indicator,
                       ProblemsHolder problemsHolder) {
      indicator.setText(FlashUIDesignerBundle.message("rendering.document"));
      Client client = Client.getInstance();
      if (!client.flush()) {
        return false;
      }

      if (projectComponentReferenceCounter != null &&
          !client.registerDocumentReferences(projectComponentReferenceCounter.unregistered, module, problemsHolder)) {
        return false;
      }

      AsyncResult<DocumentInfo> renderResult = client.renderDocument(module, psiFile, problemsHolder);
      if (renderResult.isRejected()) {
        return false;
      }

      final AtomicBoolean processed = new AtomicBoolean(false);
      indicator.setText(FlashUIDesignerBundle.message("loading.libraries"));
      renderResult.doWhenDone((Consumer<DocumentInfo>)documentInfo -> {
        if (asyncResult != null) {
          asyncResult.setDone(documentInfo);
        }
      });
      renderResult.doWhenProcessed(() -> processed.set(true));

      while (!processed.get()) {
        try {
          //noinspection BusyWait
          Thread.sleep(5);
        }
        catch (InterruptedException ignored) {
          renderResult.setRejected();
          return false;
        }

        if (indicator.isCanceled() || getInstance().isApplicationClosed()) {
          renderResult.setRejected();
          return false;
        }
      }

      return true;
    }
  }

  private static class MyPsiTreeChangeListener extends PsiTreeChangeAdapter {
    private final MxmlPreviewToolWindowManager previewToolWindowManager;
    private final MergingUpdateQueue updateQueue;

    MyPsiTreeChangeListener(Project project) {
      previewToolWindowManager = project.getComponent(MxmlPreviewToolWindowManager.class);
      updateQueue = new MergingUpdateQueue("FlashUIDesigner.update", 100, true, null);
    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent event) {
      update(event);
    }

    @Override
    public void childAdded(@NotNull PsiTreeChangeEvent event) {
      update(event);
    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent event) {
      update(event);
    }

    private void update(PsiTreeChangeEvent event) {
      PsiFile psiFile = event.getFile();
      if (psiFile == null || event.getParent() instanceof XmlComment) {
        return;
      }

      if (getInstance().isApplicationClosed()) {
        if (psiFile.getViewProvider().getVirtualFile().equals(previewToolWindowManager.getServedFile())) {
          IncrementalDocumentSynchronizer.initialRender(getInstance(), (XmlFile)psiFile);
        }
        return;
      }

      if (psiFile instanceof XmlFile) {
        DocumentInfo info = DocumentFactoryManager.getInstance().getNullableInfo(psiFile);
        if (info == null && !psiFile.equals(previewToolWindowManager.getServedFile())) {
          return;
        }
      }
      else if (!(psiFile instanceof StylesheetFile)) {
        return;
      }

      updateQueue.queue(new IncrementalDocumentSynchronizer(event));
    }
  }
}