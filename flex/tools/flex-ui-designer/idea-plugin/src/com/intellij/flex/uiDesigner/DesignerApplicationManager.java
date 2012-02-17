package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.mxml.ProjectComponentReferenceCounter;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.*;
import com.intellij.openapi.components.ServiceDescriptor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.impl.ServiceManagerImpl;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Consumer;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.swing.event.HyperlinkEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.flex.uiDesigner.LogMessageUtil.LOG;

public class DesignerApplicationManager extends ServiceManagerImpl {
  private static final ExtensionPointName<ServiceDescriptor> SERVICES =
    new ExtensionPointName<ServiceDescriptor>("com.intellij.flex.uiDesigner.service");
  public static final File APP_DIR = new File(PathManager.getSystemPath(), "flashUIDesigner");

  private boolean documentOpening;
  private DesignerApplication application;

  public static <T> T getService(@NotNull Class<T> serviceClass) {
    //noinspection unchecked
    return (T)getInstance().application.getPicoContainer().getComponentInstance(serviceClass.getName());
  }

  public DesignerApplicationManager() {
    super(true);
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
      ideaApp.invokeLater(runnable, new Condition() {
        @Override
        public boolean value(Object o) {
          return ideaApp.isDisposed();
        }
      });
    }
  }

  void setApplication(DesignerApplication application) {
    LOG.assertTrue(this.application == null);
    Disposer.register(ApplicationManager.getApplication(), application);
    installEP(SERVICES, application);
    this.application = application;
  }

  public boolean isDocumentOpening() {
    return documentOpening;
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

  public void renderDocument(@NotNull final XmlFile psiFile, Consumer<BufferedImage> consumer) {
    //noinspection ConstantConditions
    openDocument(ModuleUtil.findModuleForPsiElement(psiFile), psiFile, false, consumer);
  }

  public void openDocument(@NotNull final Module module, @NotNull final XmlFile psiFile, final boolean debug) {
    openDocument(module, psiFile, debug, null);
  }

  public void openDocument(@NotNull final Module module, @NotNull final XmlFile psiFile, final boolean debug, final @Nullable Consumer<BufferedImage> consumer) {
    LOG.assertTrue(!documentOpening);
    documentOpening = true;

    final boolean appClosed = isApplicationClosed();
    boolean hasError = true;
    try {
      if (appClosed || !Client.getInstance().isModuleRegistered(module)) {
        final Sdk sdk = FlexUtils.getSdkForActiveBC(module);
        if (sdk == null || !checkFlexSdkVersion(sdk)) {
          reportInvalidFlexSdk(module, debug, sdk);
          return;
        }
      }

      hasError = false;
    }
    finally {
      if (hasError) {
        documentOpening = false;
      }
    }

    final OpenDocumentTask openDocumentTask = new OpenDocumentTask(psiFile, consumer);
    ProgressManager.getInstance().run(
      appClosed ? new DesignerApplicationLauncher(module, debug, openDocumentTask) : new DocumentTaskExecutor(module, openDocumentTask));
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

  public void updateDocumentFactory(final int factoryId, @NotNull final Module module, @NotNull final XmlFile psiFile) {
    LOG.assertTrue(!documentOpening);
    if (isApplicationClosed()) {
      return;
    }

    documentOpening = true;

    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        Client client = Client.getInstance();
        try {
          LOG.assertTrue(client.isModuleRegistered(module));
          client.updateDocumentFactory(factoryId, module, psiFile);
          client.flush();
        }
        finally {
          documentOpening = false;
        }
      }
    });
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

  private static class OpenDocumentTask extends DesignerApplicationLauncher.PostTask {
    private final XmlFile psiFile;
    private final Consumer<BufferedImage> consumer;

    public OpenDocumentTask(@NotNull final XmlFile psiFile, @Nullable Consumer<BufferedImage> consumer) {
      this.psiFile = psiFile;
      this.consumer = consumer;
    }

    @Override
    public void dispose() {
      super.dispose();

      DesignerApplicationManager.getInstance().documentOpening = false;

      if (consumer instanceof Disposable) {
        Disposer.dispose((Disposable)consumer);
      }
    }

    @Override
    public boolean run(Module module,
                       ProjectComponentReferenceCounter projectComponentReferenceCounter,
                       ProgressIndicator indicator,
                       ProblemsHolder problemsHolder) {
      indicator.setText(FlashUIDesignerBundle.message("open.document"));
      Client client = Client.getInstance();
      if (!client.flush()) {
        return false;
      }

      if (projectComponentReferenceCounter != null &&
          !client.registerDocumentReferences(projectComponentReferenceCounter.unregistered, module, problemsHolder)) {
        return false;
      }

      final Ref<BufferedImage> result = new Ref<BufferedImage>();
      final AtomicBoolean done = new AtomicBoolean(false);
      final MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect(this);
      connection.subscribe(SocketInputHandler.MESSAGE_TOPIC, new SocketInputHandler.DocumentRenderedListener() {
        @Override
        public void documentRendered(int id, BufferedImage image) {
          DocumentFactoryManager.DocumentInfo info = DocumentFactoryManager.getInstance().getNullableInfo(psiFile.getVirtualFile());
          if (info != null && info.getId() == id) {
            result.set(image);
            up();
          }
        }

        @Override
        public void errorOccured() {
          up();
        }

        private void up() {
          connection.disconnect();
          done.set(true);
        }
      });

      if (client.openDocument(module, psiFile, problemsHolder)) {
        if (!client.flush()) {
          return false;
        }

        indicator.setText(FlashUIDesignerBundle.message("loading.libraries"));
        while (!done.get()) {
          try {
            Thread.sleep(5);
          }
          catch (InterruptedException e) {
            return false;
          }

          indicator.checkCanceled();

          if (DesignerApplicationManager.getInstance().isApplicationClosed()) {
            return false;
          }
        }
      }
      else {
        connection.disconnect();
      }

      if (consumer != null) {
        consumer.consume(result.get());
      }
      return true;
    }
  }
}