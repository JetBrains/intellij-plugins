package com.intellij.flex.uiDesigner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.facet.FacetManager;
import com.intellij.flex.uiDesigner.io.IOUtil;
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.ui.ProjectJdksEditor;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Consumer;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FlexUIDesignerApplicationManager implements Disposable {
  public static final Topic<FlexUIDesignerApplicationListener> MESSAGE_TOPIC =
    new Topic<FlexUIDesignerApplicationListener>("Flex UI Designer Application open and close events",
                                                 FlexUIDesignerApplicationListener.class);

  static final Logger LOG = Logger.getInstance(FlexUIDesignerApplicationManager.class.getName());

  public static final String DESIGNER_SWF = "designer.swf";
  public static final String DESCRIPTOR_XML = "descriptor.xml";
  static final String CHECK_DESCRIPTOR_XML = "check-descriptor.xml";

  public static final File APP_DIR = new File(PathManager.getSystemPath(), "flexUIDesigner");
  private static final String CHECK_DESCRIPTOR_PATH = APP_DIR + File.separator + CHECK_DESCRIPTOR_XML;

  private ProcessHandler adlProcessHandler;
  private Server server;

  ProjectManagerListener projectManagerListener;

  private boolean documentOpening;

  public boolean isDocumentOpening() {
    return documentOpening;
  }

  public static FlexUIDesignerApplicationManager getInstance() {
    return ServiceManager.getService(FlexUIDesignerApplicationManager.class);
  }

  @Override
  public void dispose() {
    IOUtil.close(Client.getInstance());
    IOUtil.close(server);

    destroyAdlProcess();
  }

  public void destroyAdlProcess() {
    if (adlProcessHandler != null) {
      adlProcessHandler.destroyProcess();
    }
  }

  public void serverClosed() {
    documentOpening = false;
    IOUtil.close(Client.getInstance());

    Application application = ApplicationManager.getApplication();
    if (!application.isDisposed()) {
      application.getMessageBus().syncPublisher(MESSAGE_TOPIC).applicationClosed();
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

  public void openDocument(@NotNull final Module module, @NotNull final XmlFile psiFile, boolean debug) {
    LOG.assertTrue(!documentOpening);

    final boolean appClosed = server == null || server.isClosed();
    if (appClosed || !Client.getInstance().isModuleRegistered(module)) {
      Sdk sdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module);
      if (sdk == null || !checkFlexSdkVersion(sdk.getVersionString())) {
        reportInvalidFlexSdk(module, debug, sdk);
        return;
      }
    }

    documentOpening = true;

    if (appClosed) {
      run(module, psiFile, debug);
    }
    else {
      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        @Override
        public void run() {
          Client client = Client.getInstance();
          try {
            if (!client.isModuleRegistered(module)) {
              try {
                LibraryManager.getInstance().initLibrarySets(module, APP_DIR);
              }
              catch (InitException e) {
                LOG.error(e.getCause());
                DocumentProblemManager.getInstance().report(module.getProject(), e.getMessage());
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

  private static void reportInvalidFlexSdk(final Module module, boolean debug, @Nullable Sdk sdk) {
    FlexFacet flexFacet =
      module.getModuleType() == FlexModuleType.getInstance() ? null : FacetManager.getInstance(module).getFacetByType(FlexFacet.ID);
    String moduleOrFacetName = FlexUtils.getPresentableName(module, flexFacet);
    String message;
    if (sdk == null) {
      message = FlexUIDesignerBundle.message("module.sdk.is.not.specified", moduleOrFacetName);
    }
    else {
      message = FlexUIDesignerBundle.message("module.sdk.is.not.compatible", sdk.getVersionString(), moduleOrFacetName);
    }

    showBalloon(debug, message, module.getProject(), new Consumer<String>() {
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
    assert !documentOpening;
    documentOpening = true;
    if (server == null || server.isClosed()) {
      return;
    }

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

  private void run(@NotNull final Module module, @NotNull XmlFile psiFile, boolean debug) {
    DesignerApplicationUtil.AdlRunConfiguration adlRunConfiguration;
    try {
      adlRunConfiguration = DesignerApplicationUtil.findSuitableFlexSdk(CHECK_DESCRIPTOR_PATH);
      if (adlRunConfiguration == null) {
        String message = FlexUIDesignerBundle.message(SystemInfo.isLinux ? "no.sdk.to.launch.designer.linux" : "no.sdk.to.launch.designer");
        showBalloon(debug, message, module.getProject(), new Consumer<String>() {
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
        documentOpening = false;
        return;
      }

      final List<String> arguments = new ArrayList<String>();

      server = new Server(new PendingOpenDocumentTask(module, psiFile), this);
      arguments.add(String.valueOf(server.listen()));
      arguments.add(String.valueOf(server.errorListen()));

      if (DebugPathManager.IS_DEV) {
        final String fudHome = DebugPathManager.getFudHome();

        if (ApplicationManager.getApplication().isUnitTestMode()) {
          arguments.add("-p");
          arguments.add(fudHome + "/test-app-plugin/target/test-1.0-SNAPSHOT.swf");
        }

        arguments.add("-cdd");
        arguments.add(fudHome + "/flex-injection/target");
      }

      adlRunConfiguration.arguments = arguments;
    }
    catch (Throwable e) {
      LOG.error(e);
      documentOpening = false;
      return;
    }

    DesignerApplicationUtil.AdlRunTask task = new DesignerApplicationUtil.AdlRunTask(adlRunConfiguration) {
      @Override
      public void run() {
        try {
          copyAppFiles();

          adlProcessHandler = DesignerApplicationUtil.runAdl(runConfiguration, APP_DIR.getPath() + "/" + DESCRIPTOR_XML, new Consumer<Integer>() {
            @Override
            public void consume(Integer exitCode) {
              adlProcessHandler = null;
              if (onAdlExit != null) {
                ApplicationManager.getApplication().invokeLater(onAdlExit);
              }

              if (exitCode != 0) {
                LOG.error("ADL exited with error code " + exitCode);
              }

              serverClosed();
            }
          });
        }
        catch (IOException e) {
          LOG.error(e);
          try {
            server.close();
          }
          catch (IOException ignored) {
          }
        }
        finally {
          documentOpening = false;
        }
      }
    };

    if (debug) {
      adlRunConfiguration.debug = true;
      try {
        DesignerApplicationUtil.runDebugger(module, task);
      }
      catch (ExecutionException e) {
        LOG.error(e);
      }
    }
    else {
      task.run();
    }
  }

  private void copyAppFiles() throws IOException {
    if (projectManagerListener == null) {
      projectManagerListener = new MyProjectManagerListener();
      ProjectManager.getInstance().addProjectManagerListener(projectManagerListener, ApplicationManager.getApplication());
    }

    if (DebugPathManager.IS_DEV) {
      return;
    }

    final ClassLoader classLoader = getClass().getClassLoader();
    IOUtil.saveStream(classLoader.getResource(DESCRIPTOR_XML), new File(APP_DIR, DESCRIPTOR_XML));
    IOUtil.saveStream(classLoader.getResource(DESIGNER_SWF), new File(APP_DIR, DESIGNER_SWF));
  }

  private static void showBalloon(boolean debug, String text, Project project, final Consumer<String> handler) {
    String title = FlexUIDesignerBundle.message(
      debug ? "action.FlexUIDesigner.DebugDesignView.text" : "action.FlexUIDesigner.RunDesignView.text");

    Notification notification =
      new Notification(FlexUIDesignerBundle.message("plugin.name"), title, text, NotificationType.ERROR, new NotificationListener() {
        @Override
        public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
          if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
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

  class PendingOpenDocumentTask implements Runnable {
    private final Module myModule;
    private final XmlFile myPsiFile;

    private boolean libraryAndModuleInitialized;
    private boolean clientOpened;

    public PendingOpenDocumentTask(@NotNull Module module, @NotNull XmlFile psiFile) {
      myModule = module;
      myPsiFile = psiFile;

      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        @Override
        public void run() {
          LibraryManager.getInstance().garbageCollection(APP_DIR);

          try {
            if (!ServiceManager.getService(StringRegistry.class).isEmpty()) {
              Client.getInstance().initStringRegistry();
            }
            LibraryManager.getInstance().initLibrarySets(myModule, APP_DIR);
          }
          catch (IOException e) {
            IOUtil.close(server);
            serverClosed();
            LOG.error(e);
          }
          catch (InitException e) {
            LOG.error(e.getCause());
            DocumentProblemManager.getInstance().report(myModule.getProject(), e.getMessage());
          }

          libraryAndModuleInitialized = true;
          if (clientOpened) {
            openDocument();
          }
        }
      });
    }

    public void setOut(OutputStream out) {
      Client.getInstance().setOut(out);
    }

    @Override
    public void run() {
      clientOpened = true;
      if (libraryAndModuleInitialized) {
        openDocument();
      }
    }

    private void openDocument() {
      Client client = Client.getInstance();
      try {
        client.flush();
        client.openDocument(myModule, myPsiFile);
        client.flush();

        ApplicationManager.getApplication().getMessageBus().syncPublisher(MESSAGE_TOPIC).initialDocumentOpened();
      }
      catch (IOException e) {
        IOUtil.close(server);
        serverClosed();
        LOG.error(e);
      }
      finally {
        documentOpening = false;
      }
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
      if (server == null || server.isClosed()) {
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
}