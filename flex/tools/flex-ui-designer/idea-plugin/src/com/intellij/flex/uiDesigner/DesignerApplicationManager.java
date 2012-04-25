package com.intellij.flex.uiDesigner;

import com.intellij.ProjectTopics;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.libraries.FlexLibrarySet;
import com.intellij.flex.uiDesigner.mxml.ProjectComponentReferenceCounter;
import com.intellij.flex.uiDesigner.preview.MxmlPreviewToolWindowManager;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.*;
import com.intellij.openapi.components.ServiceDescriptor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.impl.ServiceManagerImpl;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.*;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.css.CssFile;
import com.intellij.psi.css.CssFileType;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Consumer;
import com.intellij.util.Processor;
import com.intellij.util.concurrency.QueueProcessor;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import com.intellij.util.ui.update.MergingUpdateQueue;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TObjectObjectProcedure;
import gnu.trove.TObjectProcedure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.swing.event.HyperlinkEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.flex.uiDesigner.DocumentFactoryManager.DocumentInfo;
import static com.intellij.flex.uiDesigner.LogMessageUtil.LOG;
import static com.intellij.flex.uiDesigner.RenderActionQueue.RenderAction;

public class DesignerApplicationManager extends ServiceManagerImpl {
  private static final ExtensionPointName<ServiceDescriptor> SERVICES =
    new ExtensionPointName<ServiceDescriptor>("com.intellij.flex.uiDesigner.service");

  public static final File APP_DIR = new File(PathManager.getSystemPath(), "flashUIDesigner");

  public static final Topic<DocumentRenderedListener> MESSAGE_TOPIC =
    new Topic<DocumentRenderedListener>("Flash UI Designer document rendered event", DocumentRenderedListener.class);

  private DesignerApplication application;

  private final RenderActionQueue initialRenderQueue = new RenderActionQueue();

  public DesignerApplicationManager() {
    super(true);
  }

  public static <T> T getService(@NotNull Class<T> serviceClass) {
    //noinspection unchecked
    return (T)getInstance().application.getPicoContainer().getComponentInstance(serviceClass.getName());
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

    final Application ideaApp = ApplicationManager.getApplication();
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        Disposer.dispose(disposedApp);
      }
    };

    if (ideaApp.isDispatchThread()) {
      runnable.run();
    }
    else {
      ideaApp.invokeLater(runnable, ideaApp.getDisposed());
    }
  }

  void setApplication(DesignerApplication application) {
    LOG.assertTrue(this.application == null);
    Disposer.register(ApplicationManager.getApplication(), application);
    installEP(SERVICES, application);
    this.application = application;
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

      final AccessToken token = WriteAction.start();
      try {
        SdkModificator modificator = sdk.getSdkModificator();
        modificator.setVersionString(version);
        modificator.commitChanges();
      }
      finally {
        token.finish();
      }
    }

    LOG.assertTrue(version != null);
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

  private static void doRenderDocumentsAndCheckLocalStyleModification(Document[] documents,
                                                                      boolean onlyStyle,
                                                                      AsyncResult<List<DocumentInfo>> result) {
    FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
    DocumentFactoryManager documentFactoryManager = DocumentFactoryManager.getInstance();
    final Client client = Client.getInstance();
    final List<DocumentInfo> documentInfos = new ArrayList<DocumentInfo>(documents.length);
    final THashMap<ModuleInfo, List<LocalStyleHolder>> changedLocalStyleHolders = new THashMap<ModuleInfo, List<LocalStyleHolder>>();
    for (Document document : documents) {
      final VirtualFile file = fileDocumentManager.getFile(document);
      if (file == null) {
        continue;
      }

      boolean isMxml = JavaScriptSupportLoader.isFlexMxmFile(file);
      if (isMxml || file.getFileType() == CssFileType.INSTANCE) {
        collectChangedLocalStyleSources(changedLocalStyleHolders, file);
      }

      final DocumentInfo info = isMxml ? documentFactoryManager.getNullableInfo(file) : null;
      if (info == null) {
        continue;
      }
      else if (onlyStyle) {
        info.documentModificationStamp = document.getModificationStamp();
        continue;
      }

      if (info.documentModificationStamp == document.getModificationStamp()) {
        info.documentModificationStamp = -1;
        continue;
      }

      final Project project = ProjectUtil.guessProjectForFile(file);
      if (project == null) {
        continue;
      }

      final Module module = ModuleUtil.findModuleForFile(file, project);
      if (module == null) {
        continue;
      }

      final XmlFile psiFile;
      final AccessToken token = ReadAction.start();
      try {
        psiFile = (XmlFile)PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile == null) {
          continue;
        }
      }
      finally {
        token.finish();
      }

      if (client.updateDocumentFactory(info.getId(), module, psiFile)) {
        info.documentModificationStamp = document.getModificationStamp();
        documentInfos.add(info);
      }
    }

    if (!changedLocalStyleHolders.isEmpty()) {
      final ProblemsHolder problemsHolder = new ProblemsHolder();
      final ProjectComponentReferenceCounter projectComponentReferenceCounter = new ProjectComponentReferenceCounter();
      try {
        changedLocalStyleHolders.forEachEntry(new TObjectObjectProcedure<ModuleInfo, List<LocalStyleHolder>>() {
          @Override
          public boolean execute(ModuleInfo moduleInfo, List<LocalStyleHolder> b) {
            //noinspection ConstantConditions
            List<LocalStyleHolder> oldList = moduleInfo.getLocalStyleHolders();
            FlexLibrarySet flexLibrarySet = moduleInfo.getFlexLibrarySet();
            final StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter();
            stringWriter.startChange();
            try {
              List<LocalStyleHolder> list = ModuleInfoUtil.collectLocalStyle(moduleInfo, flexLibrarySet.getVersion(), stringWriter,
                problemsHolder, projectComponentReferenceCounter,
                flexLibrarySet.assetCounterInfo.demanded);
              assert oldList != null;
              // todo we should't create list, we should check while collecting
              boolean hasChanges = true;
              if (list.size() == oldList.size()) {
                int diff = list.size();
                for (LocalStyleHolder holder : list) {
                  if (oldList.contains(holder)) {
                    diff--;
                  }
                }

                hasChanges = diff != 0;
                if (hasChanges) {
                  moduleInfo.setLocalStyleHolders(list);
                }
              }

              if (hasChanges) {
                client.fillAssetClassPoolIfNeed(flexLibrarySet);
                client.updateLocalStyleHolders(changedLocalStyleHolders, stringWriter);
                if (projectComponentReferenceCounter.hasUnregistered()) {
                  client.registerDocumentReferences(projectComponentReferenceCounter.unregistered, null, problemsHolder);
                }
              }
              else {
                stringWriter.rollback();
                changedLocalStyleHolders.remove(moduleInfo);
              }
            }
            catch (Throwable e) {
              stringWriter.rollback();
              LOG.error(e);
            }
            return true;
          }
        });
      }
      catch (Throwable e) {
        LOG.error(e);
      }

      if (!problemsHolder.isEmpty()) {
        DocumentProblemManager.getInstance().report(null, problemsHolder);
      }
    }

    client.renderDocumentAndDependents(documentInfos, changedLocalStyleHolders, result);
  }

  private static void collectChangedLocalStyleSources(final THashMap<ModuleInfo, List<LocalStyleHolder>> holders,
                                                      final VirtualFile file) {
    Client.getInstance().getRegisteredModules().forEach(new TObjectProcedure<ModuleInfo>() {
      @Override
      public boolean execute(ModuleInfo moduleInfo) {
        if (holders.containsKey(moduleInfo)) {
          return false;
        }

        List<LocalStyleHolder> styleHolders = moduleInfo.getLocalStyleHolders();
        if (styleHolders != null) {
          List<LocalStyleHolder> list = null;
          for (LocalStyleHolder styleHolder : styleHolders) {
            if (styleHolder.file.equals(file)) {
              if (list == null) {
                list = new ArrayList<LocalStyleHolder>();
                holders.put(moduleInfo, list);
              }

              list.add(styleHolder);
            }
          }

          if (list != null) {
            // well, local style applicable only for one module, so,
            // if we found for this module, there is no reason to continue search
            return false;
          }
        }

        return true;
      }
    });
  }

  public void runWhenRendered(@NotNull XmlFile psiFile, @NotNull AsyncResult.Handler<DocumentInfo> handler) {
    runWhenRendered(psiFile, handler, null, false);
  }

  public void runWhenRendered(@NotNull XmlFile psiFile,
                              @NotNull final AsyncResult.Handler<DocumentInfo> handler,
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
      Application app = ApplicationManager.getApplication();
      if (app.isDispatchThread()) {
        final DocumentInfo finalDocumentInfo = documentInfo;
        app.executeOnPooledThread(new Runnable() {
          @Override
          public void run() {
            handler.run(finalDocumentInfo);
          }
        });
      }
      else {
        handler.run(documentInfo);
      }
      return;
    }

    synchronized (initialRenderQueue) {
      AsyncResult<DocumentInfo> renderResult = initialRenderQueue.findResult(psiFile);
      if (renderResult == null) {
        renderResult = new AsyncResult<DocumentInfo>();
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
            if (project.isDisposed()) {
              return;
            }

            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (!(psiFile instanceof XmlFile)) {
              return;
            }

            Module module = ModuleUtil.findModuleForFile(file, project);
            if (module != null) {
              renderDocument(module, (XmlFile)psiFile, debug, result);
            }
          }
        });
      }

      renderResult.doWhenDone(handler);
    }
  }

  @NotNull
  public AsyncResult<BufferedImage> getDocumentImage(@NotNull XmlFile psiFile) {
    final AsyncResult<BufferedImage> result = new AsyncResult<BufferedImage>();
    runWhenRendered(psiFile, new AsyncResult.Handler<DocumentInfo>() {
      @Override
      public void run(DocumentInfo documentInfo) {
        Client.getInstance().getDocumentImage(documentInfo, result);
      }
    }, result, false);
    return result;
  }

  public void openDocument(@NotNull final XmlFile psiFile, final boolean debug) {
    runWhenRendered(psiFile, new AsyncResult.Handler<DocumentInfo>() {
      @Override
      public void run(DocumentInfo documentInfo) {
        Client.getInstance().selectComponent(documentInfo.getId(), 0);
      }
    }, null, debug);
  }

  public AsyncResult<DocumentInfo> renderDocument(@NotNull final Module module, @NotNull final XmlFile psiFile) {
    final AsyncResult<DocumentInfo> result = new AsyncResult<DocumentInfo>();
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

    notifyUser(debug, message, module.getProject(), new Consumer<String>() {
      @Override
      public void consume(String id) {
        if ("edit".equals(id)) {
          FlexSdkUtils.openModuleConfigurable(module);
        }
        else {
          LOG.error("unexpected id: " + id);
        }
      }
    });
  }

  public void renderDocumentsAndCheckLocalStyleModification(Document[] documents) {
    renderDocumentsAndCheckLocalStyleModification(documents, false);
  }

  public void renderDocumentsAndCheckLocalStyleModification(final Document[] documents, final boolean onlyStyle) {
    synchronized (initialRenderQueue) {
      final AtomicBoolean result = new AtomicBoolean();
      if (!initialRenderQueue.isEmpty()) {
        initialRenderQueue.processActions(new Processor<RenderAction>() {
          @Override
          public boolean process(RenderAction renderAction) {
            if (renderAction.file == null) {
              ComplexRenderAction action = (ComplexRenderAction)renderAction;
              if (onlyStyle == action.onlyStyle) {
                THashSet<Document> merged = new THashSet<Document>(action.documents.length + documents.length);
                Collections.addAll(merged, documents);
                action.documents = merged.toArray(new Document[merged.size()]);
                result.set(true);
                return false;
              }
            }
            return true;
          }
        });
      }

      if (!result.get()) {
        initialRenderQueue.add(new ComplexRenderAction(documents, onlyStyle));
      }
    }
  }

  private static class ComplexRenderAction extends RenderAction<AsyncResult<List<DocumentInfo>>> {
    private Document[] documents;
    private final boolean onlyStyle;

    protected ComplexRenderAction(Document[] documents, boolean onlyStyle) {
      super(null, null, new AsyncResult<List<DocumentInfo>>());
      this.documents = documents;
      this.onlyStyle = onlyStyle;
    }

    @Override
    protected boolean isNeedEdt() {
      return false;
    }

    @Override
    protected void doRun() {
      doRenderDocumentsAndCheckLocalStyleModification(documents, onlyStyle, result);
      result.doWhenDone(new AsyncResult.Handler<List<DocumentInfo>>() {
        @Override
        public void run(List<DocumentInfo> infos) {
          Application application = ApplicationManager.getApplication();
          if (application.isDisposed()) {
            return;
          }

          MessageBus messageBus = application.getMessageBus();
          for (DocumentInfo info : infos) {
            messageBus.syncPublisher(MESSAGE_TOPIC).documentRendered(info);
          }
        }
      });
    }
  }

  public static String getOpenActionTitle(boolean debug) {
    return FlashUIDesignerBundle
      .message(debug ? "action.FlashUIDesigner.DebugDesignView.text" : "action.FlashUIDesigner.RunDesignView.text");
  }

  public static void notifyUser(boolean debug, @NotNull String text, @NotNull Module module) {
    notifyUser(debug, text, module.getProject(), null);
  }

  static void notifyUser(boolean debug, @NotNull String text, @NotNull Project project, @Nullable final Consumer<String> handler) {
    Notification notification = new Notification(FlashUIDesignerBundle.message("plugin.name"), getOpenActionTitle(debug), text,
                                                 NotificationType.ERROR, handler == null ? null : new NotificationListener() {
      @Override
      public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
        if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
          return;
        }

        notification.expire();

        if ("help".equals(event.getDescription())) {
          HelpManager.getInstance().invokeHelp("flex.ui.designer.launch");
        }
        else {
          handler.consume(event.getDescription());
        }
      }
    });
    notification.notify(project);
  }

  public static boolean isApplicable(Project project, PsiFile psiFile) {
    if (!dumbAwareIsApplicable(project, psiFile)) {
      return false;
    }

    final JSClass jsClass = XmlBackedJSClassImpl.getXmlBackedClass(((XmlFile)psiFile).getRootTag());
    return jsClass != null && JSInheritanceUtil.isParentClass(jsClass, FlexCommonTypeNames.FLASH_DISPLAY_OBJECT_CONTAINER);
  }

  public static boolean dumbAwareIsApplicable(Project project, PsiFile psiFile) {
    final VirtualFile file = psiFile == null ? null : psiFile.getViewProvider().getVirtualFile();
    if (file == null || !JavaScriptSupportLoader.isFlexMxmFile(file) || !ProjectRootManager.getInstance(project).getFileIndex().isInSourceContent(file)) {
      return false;
    }
    final XmlTag rootTag = ((XmlFile)psiFile).getRootTag();
    return rootTag != null && rootTag.getPrefixByNamespace(JavaScriptSupportLoader.MXML_URI3) != null;
  }

  public void projectRegistered(final Project project) {
    PsiManager.getInstance(project).addPsiTreeChangeListener(new MyPsiTreeChangeAdapter(project), project);
  }

  void attachProjectAndModuleListeners(DesignerApplication designerApplication) {
    MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect(designerApplication);
    connection.subscribe(ProjectManager.TOPIC, new ProjectManagerAdapter() {
      @Override
      public void projectClosed(Project project) {
        if (isApplicationClosed()) {
          return;
        }

        Client client = Client.getInstance();
        if (client.getRegisteredProjects().contains(project)) {
          client.closeProject(project);
        }
      }
    });

    // unregisted module is more complicated — we cannot just remove all document factories which belong to project as in case of close project
    // we must remove all document factories belong to module and all dependents (dependent may be from another module, so, we process moduleRemoved synchronous
    // one by one)
    connection.subscribe(ProjectTopics.MODULES, new ModuleAdapter() {
      @Override
      public void moduleRemoved(Project project, Module module) {
        Client client = Client.getInstance();
        if (!client.isModuleRegistered(module)) {
          return;
        }

        if (unregisterTaskQueueProcessor == null) {
          unregisterTaskQueueProcessor = new QueueProcessor<Module>(new Consumer<Module>() {
            @Override
            public void consume(Module module) {
              boolean hasError = true;
              final ActionCallback callback;
              initialRenderQueue.suspend();
              try {
                callback = Client.getInstance().unregisterModule(module);
                hasError = false;
              }
              finally {
                if (hasError) {
                  initialRenderQueue.resume();
                }
              }

              callback.doWhenProcessed(new Runnable() {
                @Override
                public void run() {
                  initialRenderQueue.resume();
                }
              });
            }
          });
        }

        unregisterTaskQueueProcessor.add(module);
      }
    });
  }

  private QueueProcessor<Module> unregisterTaskQueueProcessor;

  private static class RenderDocumentTask extends DesignerApplicationLauncher.PostTask {
    private final XmlFile psiFile;
    private final AsyncResult<DocumentInfo> asyncResult;

    public RenderDocumentTask(@NotNull XmlFile psiFile, @Nullable AsyncResult<DocumentInfo> asyncResult) {
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

      final AsyncResult<DocumentInfo> renderResult = client.renderDocument(module, psiFile, problemsHolder);
      if (renderResult.isRejected()) {
        return false;
      }

      final AtomicBoolean processed = new AtomicBoolean(false);
      indicator.setText(FlashUIDesignerBundle.message("loading.libraries"));
      renderResult.doWhenDone(new AsyncResult.Handler<DocumentInfo>() {
        @Override
        public void run(DocumentInfo documentInfo) {
          if (asyncResult != null) {
            asyncResult.setDone(documentInfo);
          }
        }
      });
      renderResult.doWhenProcessed(new Runnable() {
        @Override
        public void run() {
          processed.set(true);
        }
      });

      while (!processed.get()) {
        try {
          //noinspection BusyWait
          Thread.sleep(5);
        }
        catch (InterruptedException e) {
          renderResult.setRejected();
          return false;
        }

        if (indicator.isCanceled() || DesignerApplicationManager.getInstance().isApplicationClosed()) {
          renderResult.setRejected();
          return false;
        }
      }

      return true;
    }
  }

  public interface DocumentRenderedListener {
    void documentRendered(DocumentInfo info);
    void errorOccured();
  }

  private class MyPsiTreeChangeAdapter extends PsiTreeChangeAdapter {
    private final MxmlPreviewToolWindowManager previewToolWindowManager;
    private final MergingUpdateQueue updateQueue;

    public MyPsiTreeChangeAdapter(Project project) {
      previewToolWindowManager = project.getComponent(MxmlPreviewToolWindowManager.class);
      updateQueue = new MergingUpdateQueue("FlashUIDesigner.update", 100, true, null);
    }

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
    }

    private void update(final PsiTreeChangeEvent event) {
      PsiFile psiFile = event.getFile();
      if (psiFile == null) {
        return;
      }

      if (isApplicationClosed()) {
        if (psiFile.getViewProvider().getVirtualFile() == previewToolWindowManager.getServedFile()) {
          IncrementalDocumentSynchronizer.initialRenderAndNotify(DesignerApplicationManager.this, (XmlFile)psiFile);
        }
        return;
      }

      if (psiFile instanceof XmlFile) {
        DocumentInfo info = DocumentFactoryManager.getInstance().getNullableInfo(psiFile);
        if (info == null && psiFile != previewToolWindowManager.getServedFile()) {
          return;
        }
      }
      else if (!(psiFile instanceof CssFile)) {
        return;
      }

      updateQueue.queue(new IncrementalDocumentSynchronizer(event));
    }
  }
}