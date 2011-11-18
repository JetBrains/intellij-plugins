package com.intellij.flex.uiDesigner;

import com.intellij.facet.FacetManager;
import com.intellij.flex.uiDesigner.libraries.InitException;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ServiceDescriptor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.impl.ServiceManagerImpl;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Consumer;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.swing.event.HyperlinkEvent;

import java.io.File;
import java.util.List;

import static com.intellij.flex.uiDesigner.LogMessageUtil.LOG;

public class DesignerApplicationManager extends ServiceManagerImpl implements Disposable {
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

  public DesignerApplication getApplication() {
    return application;
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
    Disposer.register(this, application);
    installEP(SERVICES, application);
    this.application = application;
  }

  public boolean isDocumentOpening() {
    return documentOpening;
  }

  public static DesignerApplicationManager getInstance() {
    return ServiceManager.getService(DesignerApplicationManager.class);
  }

  @Override
  public void dispose() {
  }

  private static boolean checkFlexSdkVersion(final String version) {
    if (version == null || version.length() < 5 || version.charAt(0) < '4') {
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

  public void openDocument(@NotNull final Module module, @NotNull final XmlFile psiFile, final boolean debug) {
    LOG.assertTrue(!documentOpening);

    final boolean appClosed = isApplicationClosed();
    if (appClosed || !Client.getInstance().isModuleRegistered(module)) {
      Sdk sdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module);
      if (sdk == null || !checkFlexSdkVersion(sdk.getVersionString())) {
        reportInvalidFlexSdk(module, debug, sdk);
        return;
      }
    }

    documentOpening = true;

    if (appClosed) {
      ProgressManager.getInstance().run(new DesignerApplicationLauncher(module, debug, new FirstOpenDocumentTask(psiFile)));
    }
    else {
      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        @Override
        public void run() {
          Client client = Client.getInstance();
          try {
            final ProblemsHolder problemsHolder = new ProblemsHolder();
            if (!client.isModuleRegistered(module)) {
              try {
                final List<XmlFile> unregisteredDocumentReferences = LibraryManager.getInstance().initLibrarySets(module, true, problemsHolder);
                if (unregisteredDocumentReferences != null &&
                    !client.registerDocumentReferences(unregisteredDocumentReferences, module, problemsHolder)) {
                  return;
                }
              }
              catch (InitException e) {
                DesignerApplicationLauncher.processInitException(e, module, debug);
              }
            }

            client.openDocument(module, psiFile, problemsHolder);
            client.flush();
          }
          finally {
            documentOpening = false;
          }
        }
      });
    }
  }

  public boolean isApplicationClosed() {
    return application == null;
  }

  private static void reportInvalidFlexSdk(final Module module, boolean debug, @Nullable Sdk sdk) {
    FlexFacet flexFacet =
      ModuleType.get(module) == FlexModuleType.getInstance() ? null : FacetManager.getInstance(module).getFacetByType(FlexFacet.ID);
    String moduleOrFacetName = FlexUtils.getPresentableName(module, flexFacet);
    String message;
    if (sdk == null) {
      message = FlashUIDesignerBundle.message("module.sdk.is.not.specified", moduleOrFacetName);
    }
    else {
      message = FlashUIDesignerBundle.message("module.sdk.is.not.compatible", sdk.getVersionString(), moduleOrFacetName);
    }

    notifyUser(debug, message, module.getProject(), new Consumer<String>() {
      @Override
      public void consume(String id) {
        if ("edit".equals(id)) {
          FlexSdkUtils.openModuleOrFacetConfigurable(module);
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
          assert client.isModuleRegistered(module);
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
    return FlashUIDesignerBundle.message(debug ? "action.FlashUIDesigner.DebugDesignView.text" : "action.FlashUIDesigner.RunDesignView.text");
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

  private class FirstOpenDocumentTask implements DesignerApplicationLauncher.PostTask {
    private final XmlFile psiFile;

    public FirstOpenDocumentTask(@NotNull final XmlFile psiFile) {
      this.psiFile = psiFile;
    }

    public void end() {
      documentOpening = false;
    }

    @Override
    public boolean run(List<XmlFile> unregisteredDocumentReferences, ProgressIndicator indicator, ProblemsHolder problemsHolder) {
      indicator.setText(FlashUIDesignerBundle.message("open.document"));
      Client client = Client.getInstance();
      if (!client.flush()) {
        return false;
      }

      final Module module = ModuleUtil.findModuleForPsiElement(psiFile);
      if (unregisteredDocumentReferences != null && !client.registerDocumentReferences(unregisteredDocumentReferences, module,
        problemsHolder)) {
        return false;
      }

      final Semaphore semaphore = new Semaphore();
      final MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect(
        DesignerApplicationManager.getInstance().getApplication());
      connection.subscribe(SocketInputHandler.MESSAGE_TOPIC, new SocketInputHandler.DocumentOpenedListener() {
        @Override
        public void documentOpened() {
          semaphoreUp();
        }

        @Override
        public void errorOccured() {
          semaphoreUp();
        }

        private void semaphoreUp() {
          connection.disconnect();
          semaphore.up();
        }
      });

      semaphore.down();
      if (client.openDocument(module, psiFile, true, problemsHolder)) {
        if (!client.flush()) {
          return false;
        }
        indicator.setText(FlashUIDesignerBundle.message("loading.libraries"));
        semaphore.waitFor();
      }
      else {
        semaphore.up();
        connection.disconnect();
      }

      return true;
    }
  }
}