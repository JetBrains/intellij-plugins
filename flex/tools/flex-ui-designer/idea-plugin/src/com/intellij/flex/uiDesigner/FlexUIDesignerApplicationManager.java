package com.intellij.flex.uiDesigner;

import com.intellij.ProjectTopics;
import com.intellij.execution.ExecutionException;
import com.intellij.facet.FacetManager;
import com.intellij.flex.uiDesigner.io.ErrorSocketManager;
import com.intellij.flex.uiDesigner.io.IOUtil;
import com.intellij.flex.uiDesigner.io.MessageSocketManager;
import com.intellij.flex.uiDesigner.io.StringRegistry;
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
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.ModuleAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.ui.ProjectJdksEditor;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Consumer;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.flex.uiDesigner.DesignerApplicationUtil.*;

public class FlexUIDesignerApplicationManager implements Disposable {
  public static final Topic<FlexUIDesignerApplicationListener> MESSAGE_TOPIC =
    new Topic<FlexUIDesignerApplicationListener>("Flex UI Designer Application open and close events",
                                                 FlexUIDesignerApplicationListener.class);

  static final Logger LOG = Logger.getInstance(FlexUIDesignerApplicationManager.class.getName());

  public static final String DESIGNER_SWF = "designer.swf";
  public static final String DESCRIPTOR_XML = "descriptor.xml";
  private static final String CHECK_DESCRIPTOR_XML = "check-descriptor.xml";

  public static final File APP_DIR = new File(PathManager.getSystemPath(), "flexUIDesigner");
  private static final String CHECK_DESCRIPTOR_PATH = APP_DIR + File.separator + CHECK_DESCRIPTOR_XML;

  private MyOSProcessHandler adlProcessHandler;

  ProjectManagerListener projectManagerListener;

  private boolean documentOpening;

  private Disposable appParentDisposable;

  private static class ParentDisposable implements Disposable {
    @Override
    public void dispose() {
    }
  }

  public boolean isDocumentOpening() {
    return documentOpening;
  }

  public static FlexUIDesignerApplicationManager getInstance() {
    return ServiceManager.getService(FlexUIDesignerApplicationManager.class);
  }

  public boolean disposeOnApplicationClosed(Disposable disposable) {
    if (appParentDisposable == null) {
      Disposer.dispose(disposable);
      return false;
    }
    else {
      Disposer.register(appParentDisposable, disposable);
      return true;
    }
  }

  @Override
  public void dispose() {
    try {
      IOUtil.close(Client.getInstance());
    }
    finally {
      destroyAdlProcess();
    }
  }

  private void destroyAdlProcess() {
    if (adlProcessHandler != null) {
      adlProcessHandler.destroyProcess();
    }
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

    final boolean appClosed = isAppClosed();
    if (appClosed || !Client.getInstance().isModuleRegistered(module)) {
      Sdk sdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module);
      if (sdk == null || !checkFlexSdkVersion(sdk.getVersionString())) {
        reportInvalidFlexSdk(module, debug, sdk);
        return;
      }
    }

    documentOpening = true;

    if (appClosed) {
      ProgressManager.getInstance().run(new FirstOpenDocumentTask(psiFile, module, debug));
    }
    else {
      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        @Override
        public void run() {
          Client client = Client.getInstance();
          try {
            if (!client.isModuleRegistered(module)) {
              try {
                LibraryManager.getInstance().initLibrarySets(module);
              }
              catch (InitException e) {
                LOG.error(e.getCause());
                notifyUser(debug, e.getMessage(), module);
              }
            }

            client.openDocument(module, psiFile);
            client.flush();
          }
          catch (IOException e) {
            LOG.error(e);
          }
          finally {
            documentOpening = false;
          }
        }
      });
    }
  }

  private boolean isAppClosed() {
    return adlProcessHandler == null;
  }

  private static void reportInvalidFlexSdk(final Module module, boolean debug, @Nullable Sdk sdk) {
    FlexFacet flexFacet =
      ModuleType.get(module) == FlexModuleType.getInstance() ? null : FacetManager.getInstance(module).getFacetByType(FlexFacet.ID);
    String moduleOrFacetName = FlexUtils.getPresentableName(module, flexFacet);
    String message;
    if (sdk == null) {
      message = FlexUIDesignerBundle.message("module.sdk.is.not.specified", moduleOrFacetName);
    }
    else {
      message = FlexUIDesignerBundle.message("module.sdk.is.not.compatible", sdk.getVersionString(), moduleOrFacetName);
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
    if (isAppClosed()) {
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
        catch (IOException e) {
          LOG.error(e);
        }
        finally {
          documentOpening = false;
        }
      }
    });

  }

  private void attachApplicationLevelListeners() {
    projectManagerListener = new MyProjectManagerListener();
    Application application = ApplicationManager.getApplication();
    ProjectManager.getInstance().addProjectManagerListener(projectManagerListener, application);

    application.getMessageBus().connect(application).subscribe(ProjectTopics.MODULES,
        new ModuleAdapter() {
          @Override
          public void moduleRemoved(Project project, Module module) {
            if (!isAppClosed() || Client.getInstance().isModuleRegistered(module)) {
              Client.getInstance().unregisterModule(module);
            }
          }
        });
  }

  static void copyAppFiles() throws IOException {
    if (DebugPathManager.IS_DEV) {
      File home = new File(DebugPathManager.getFudHome());
      IOUtil.saveStream(new File(home, "app-loader/target/app-loader-1.0-SNAPSHOT.swf"), new File(APP_DIR, DESIGNER_SWF));
      IOUtil.saveStream(new File(home, "designer/src/main/resources/descriptor.xml"), new File(APP_DIR, DESCRIPTOR_XML));
      IOUtil.saveStream(new File(home, "designer/src/main/resources/check-descriptor.xml"), new File(APP_DIR, CHECK_DESCRIPTOR_XML));
    }
    else {
      final ClassLoader classLoader = FlexUIDesignerApplicationManager.class.getClassLoader();
      IOUtil.saveStream(classLoader.getResource(DESCRIPTOR_XML), new File(APP_DIR, DESCRIPTOR_XML));
      IOUtil.saveStream(classLoader.getResource(DESIGNER_SWF), new File(APP_DIR, DESIGNER_SWF));
      IOUtil.saveStream(classLoader.getResource(CHECK_DESCRIPTOR_XML), new File(APP_DIR, CHECK_DESCRIPTOR_XML));
    }
  }

  private static String getOpenActionTitle(boolean debug) {
    return FlexUIDesignerBundle.message(debug ? "action.FlexUIDesigner.DebugDesignView.text" : "action.FlexUIDesigner.RunDesignView.text");
  }

  private static void notifyUser(boolean debug, @NotNull String text, @NotNull Module module) {
    notifyUser(debug, text, module.getProject(), null);
  }

  private static void notifyUser(boolean debug, @NotNull String text, @NotNull Project project, @Nullable final Consumer<String> handler) {
    Notification notification = new Notification(FlexUIDesignerBundle.message("plugin.name"), getOpenActionTitle(debug), text,
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

  private class AdlExitHandler implements Consumer<Integer> {
    @Override
    public void consume(Integer exitCode) {
      adlProcessHandler = null;
      if (exitCode != 0) {
        LOG.error("ADL exited with error code " + exitCode);
      }

      notifyAboutAppClosed();
      Disposer.dispose(FlexUIDesignerApplicationManager.this);
    }
  }

  private void notifyAboutAppClosed() {
    try {
      Application application = ApplicationManager.getApplication();
      if (!application.isDisposed()) {
        application.getMessageBus().syncPublisher(MESSAGE_TOPIC).applicationClosed();
      }
    }
    finally {
      if (appParentDisposable != null) {
        disposeAppParentDisposable();
      }
    }
  }

  private void disposeAppParentDisposable() {
    Disposer.dispose(appParentDisposable);
    appParentDisposable = null;
  }

  public class FirstOpenDocumentTask extends Task.Backgroundable {
    private final Module module;
    private final XmlFile psiFile;
    private final boolean debug;

    private boolean libraryAndModuleInitialized;
    private boolean clientOpened;
    private int adlExitCode = -1;

    private final Semaphore semaphore = new Semaphore();
    private ProgressIndicator indicator;
    
    public XmlFile[] unregisteredDocumentReferences;

    public FirstOpenDocumentTask(@NotNull final XmlFile psiFile, final @NotNull Module module, final boolean debug) {
      super(module.getProject(), getOpenActionTitle(debug));

      this.module = module;
      this.psiFile = psiFile;
      this.debug = debug;
    }

    public void clientOpened() {
      clientOpened = true;
      checkCanceled();
      if (libraryAndModuleInitialized) {
        openDocument();
      }
    }

    public void clientSocketNotAccepted() {
      cancel();
    }

    private void openDocument() {
      indicator.setText(FlexUIDesignerBundle.message("open.document"));
      Client client = Client.getInstance();
      try {
        client.flush();

        final ProblemsHolder problemsHolder = new ProblemsHolder();
        final RequiredAssetsInfo requiredAssetsInfo = new RequiredAssetsInfo();
        if (unregisteredDocumentReferences != null) {
          client.registerDocumentReferences(unregisteredDocumentReferences, module, problemsHolder, requiredAssetsInfo);
        }

        final MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect(appParentDisposable);
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

        client.openDocument(module, psiFile, true, problemsHolder, requiredAssetsInfo);
        client.flush();

        indicator.setText(FlexUIDesignerBundle.message("load.libraries"));
      }
      catch (IOException e) {
        LOG.error(e);
        cancel();
      }
    }

    @Override
    public void run(@NotNull final ProgressIndicator indicator) {
      this.indicator = indicator;
      AdlRunConfiguration adlRunConfiguration;
      try {
        indicator.setText(FlexUIDesignerBundle.message("copy.app.files"));
        copyAppFiles();

        adlRunConfiguration = findSuitableFlexSdk(CHECK_DESCRIPTOR_PATH);
        if (adlRunConfiguration == null) {
          String message = FlexUIDesignerBundle.message(
              SystemInfo.isLinux ? "no.sdk.to.launch.designer.linux" : "no.sdk.to.launch.designer");
          notifyUser(debug, message, module.getProject(), new Consumer<String>() {
            @Override
            public void consume(String id) {
              if ("edit".equals(id)) {
                new ProjectJdksEditor(null, module.getProject(), WindowManager.getInstance().suggestParentWindow(module.getProject()))
                    .show();
              }
              else {
                LOG.error("unexpected id: " + id);
              }
            }
          });
          indicator.cancel();
          return;
        }

        indicator.checkCanceled();

        runInitializeLibrariesAndModuleThread();

        appParentDisposable = new ParentDisposable();

        final List<String> arguments = new ArrayList<String>();
        arguments.add(String.valueOf(new MessageSocketManager(this).listen()));
        arguments.add(String.valueOf(new ErrorSocketManager().listen()));

        if (DebugPathManager.IS_DEV) {
          addTestPlugin(arguments);
        }

        adlRunConfiguration.arguments = arguments;
      }
      catch (Throwable e) {
        LOG.error(e);
        return;
      }

      final AdlRunTask task = new AdlRunTask(adlRunConfiguration) {
        @Override
        public void run() {
          checkCanceled();

          if (projectManagerListener == null) {
            attachApplicationLevelListeners();
          }
          
          try {
            adlProcessHandler = runAdl(runConfiguration, APP_DIR.getPath() + "/" + DESCRIPTOR_XML,
                new Consumer<Integer>() {
                  @Override
                  public void consume(Integer exitCode) {
                    adlProcessHandler = null;

                    // even 0 is not correct exit code, why adl exited while open socket?
                    LOG.error("ADL exited with error code " + exitCode);
                    adlExitCode = exitCode;
                    if (libraryAndModuleInitialized) {
                      cancel();
                    }
                  }
                });
          }
          catch (IOException e) {
            LOG.error(e);
            cancel();
          }
        }
      };

      if (debug) {
        adlRunConfiguration.debug = true;
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          @Override
          public void run() {
            try {
              runDebugger(module, task);
            }
            catch (ExecutionException e) {
              LOG.error(e);
            }
          }
        });
      }
      else {
        task.run();
      }

      semaphore.down();
      semaphore.waitFor();

      // Why in test mode ProgressManager.run() doesn't call onCancel/onSuccess?
      if (isHeadless()) {
        if (indicator.isCanceled()) {
          onCancel();
        }
        else {
          onSuccess();
        }
      }
    }

    private void runInitializeLibrariesAndModuleThread() {
      LibraryManager.getInstance().setAppDir(APP_DIR);

      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        @Override
        public void run() {
          indicator.setText(FlexUIDesignerBundle.message("delete.old.libraries"));
          LibraryManager.getInstance().garbageCollection();
          checkCanceled();

          try {
            if (!StringRegistry.getInstance().isEmpty()) {
              Client.getInstance().initStringRegistry();
            }
            indicator.setText(FlexUIDesignerBundle.message("collect.libraries"));
            unregisteredDocumentReferences = LibraryManager.getInstance().initLibrarySets(module);
          }
          catch (IOException e) {
            LOG.error(e);
            cancel();
            return;
          }
          catch (InitException e) {
            LOG.error(e.getCause());
            notifyUser(debug, e.getMessage(), module);
            return;
          }

          libraryAndModuleInitialized = true;
          checkCanceled();
          if (clientOpened) {
            openDocument();
          }
          else if (adlExitCode != -1) {
            cancel();
          }
        }
      });
    }

    private void checkCanceled() {
      if (indicator.isCanceled()) {
        semaphore.up();
        indicator.checkCanceled();
      }
    }

    private void cancel() {
      indicator.cancel();
      semaphore.up();

      if (adlExitCode != -1) {
        notifyUser(debug, FlexUIDesignerBundle.message("cannot.launch.designer", adlExitCode), module);
      }
    }

    @Override
    public void onCancel() {
      semaphore.up();

      try {
        Disposer.dispose(FlexUIDesignerApplicationManager.this);
      }
      finally {
        documentOpening = false;
        notifyAboutAppClosed();
      }
    }

    @Override
    public void onSuccess() {
      documentOpening = false;
      adlProcessHandler.adlExitHandler = new AdlExitHandler();
      ApplicationManager.getApplication().getMessageBus().syncPublisher(MESSAGE_TOPIC).initialDocumentOpened();
    }
  }

  private class MyProjectManagerListener implements ProjectManagerListener {
    @Override
    public void projectOpened(Project project) {
    }

    @Override
    public boolean canCloseProject(Project project) {
      return true;
    }

    @Override
    public void projectClosed(Project project) {
      if (isAppClosed()) {
        return;
      }

      Client client = Client.getInstance();
      if (client.getRegisteredProjects().contains(project)) {
        try {
          client.closeProject(project);
        }
        catch (IOException e) {
          LOG.error(e);
        }
      }
    }

    @Override
    public void projectClosing(Project project) {
    }
  }

  static void addTestPlugin(List<String> arguments) {
    final String fudHome = DebugPathManager.getFudHome();
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      arguments.add("-p");
      arguments.add(fudHome + "/test-app-plugin/target/test-1.0-SNAPSHOT.swf");
    }

    arguments.add("-cdd");
    arguments.add(fudHome + "/flex-injection/target");
  }


}