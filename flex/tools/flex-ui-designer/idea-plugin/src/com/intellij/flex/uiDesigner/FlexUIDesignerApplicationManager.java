package com.intellij.flex.uiDesigner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.flex.uiDesigner.io.IOUtil;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.libraries.LibraryCollector;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.projectRoots.ui.ProjectJdksEditor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Consumer;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class FlexUIDesignerApplicationManager implements Disposable {
  public static final Topic<FlexUIDesignerApplicationListener> MESSAGE_TOPIC =
    new Topic<FlexUIDesignerApplicationListener>("Flex UI Designer Application open and close events",
                                                 FlexUIDesignerApplicationListener.class);

  static final Logger LOG = Logger.getInstance(FlexUIDesignerApplicationManager.class.getName());
  
  public static final String DESIGNER_SWF = "designer.swf";
  public static final String DESCRIPTOR_XML = "descriptor.xml";
  public static final String CHECK_DESCRIPTOR_XML = "check-descriptor.xml";

  public ProcessHandler adlProcessHandler;
  private Server server;

  ProjectManagerListener projectManagerListener;

  public File getAppDir() {
    return appDir;
  }

  private File appDir;

  private boolean documentOpening;

  public boolean isDocumentOpening() {
    return documentOpening;
  }

  public static FlexUIDesignerApplicationManager getInstance() {
    return ServiceManager.getService(FlexUIDesignerApplicationManager.class);
  }

  @Override
  public void dispose() {
    closeClosable(Client.getInstance());
    closeClosable(server);

    if (adlProcessHandler != null) {
      adlProcessHandler.destroyProcess();
    }
  }
  
  private static void closeClosable(Closable closable) {
    try {
      if (closable != null) {
        closable.close();
      }
    }
    catch (IOException e) {
      LOG.error(e);
    }
  }

  public void serverClosed() {
    documentOpening = false;
    closeClosable(Client.getInstance());
    
    Application application = ApplicationManager.getApplication();
    if (!application.isDisposed()) {
      application.getMessageBus().syncPublisher(MESSAGE_TOPIC).applicationClosed();
    }
  }

  private boolean checkFlexSdkVersion(final String version) {
    if (version == null || version.length() < 5 || version.charAt(0) < '4') {
      return false;
    }

    if (version.charAt(0) == '4') {
      int build = FlexSdkUtils.getFlexSdkRevision(version);
      if (version.charAt(2) == '1') {
        return build == 16076;
      }
      else if (version.charAt(2) == '5' || version.charAt(4) == '0') {
        return build == 20967;
      }
    }

    return true;
  }

  public void openDocument(@NotNull final Project project, @NotNull final Module module, @NotNull final XmlFile psiFile, boolean debug) {
    assert !documentOpening;

    final boolean appClosed = server == null || server.isClosed();
    if (appClosed || !Client.getInstance().isModuleRegistered(module)) {
      final String version = LibraryCollector.getFlexVersion(module);
      if (!checkFlexSdkVersion(version)) {
        DocumentProblemManager.getInstance().reportWithTitle(project, FlexUIDesignerBundle.message("error.fdk.not.supported", version));
        return;
      }
    }

    documentOpening = true;

    if (appClosed) {
      run(project, module, psiFile, debug);
    }
    else {
      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        @Override
        public void run() {
          Client client = Client.getInstance();
          try {
            if (!client.isModuleRegistered(module)) {
              try {
                LibraryManager.getInstance().initLibrarySets(module, appDir);
              }
              catch (InitException e) {
                LOG.error(e.getCause());
                DocumentProblemManager.getInstance().reportWithTitle(project, e.getMessage());
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

  private void run(@NotNull final Project project, @NotNull final Module module, @NotNull XmlFile psiFile, boolean debug) {
    DesignerApplicationUtil.AdlRunConfiguration adlRunConfiguration;
    try {
      adlRunConfiguration = DesignerApplicationUtil.findSuitableFlexSdk();
      if (adlRunConfiguration == null) {
        final String message = FlexUIDesignerBundle.message("error.suitable.fdk.not.found", SystemInfo.isLinux ? FlexUIDesignerBundle
          .message("error.suitable.fdk.not.found.linux") : "");
        Messages.showErrorDialog(project, message, FlexUIDesignerBundle.message(
          debug ? "action.FlexUIDesigner.RunDesignView.text" : "action.FlexUIDesigner.DebugDesignView.text"));
        final ProjectJdksEditor editor = new ProjectJdksEditor(null, project, WindowManager.getInstance().suggestParentWindow(project));
        editor.show();
        if (editor.isOK()) {
          adlRunConfiguration = DesignerApplicationUtil.findSuitableFlexSdk();
        }

        if (adlRunConfiguration == null) {
          // TODO discuss: show error balloon saying 'Cannot find suitable SDK...'?
          documentOpening = false;
          return;
        }
      }

      if (DebugPathManager.IS_DEV) {
        final String fudHome = DebugPathManager.getFudHome();
        final List<String> arguments = new ArrayList<String>();
        if (ApplicationManager.getApplication().isUnitTestMode()) {
          arguments.add("-p");
          arguments.add(fudHome + "/test-app-plugin/target/test-1.0-SNAPSHOT.swf");
        }

        arguments.add("-cdd");
        arguments.add(fudHome + "/flex-injection/target");

        adlRunConfiguration.arguments = arguments;
      }

      if (appDir == null) {
        appDir = new File(PathManager.getSystemPath(), "flexUIDesigner");
      }

      server = new Server(new PendingOpenDocumentTask(project, module, psiFile), this);
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
        
          adlProcessHandler = DesignerApplicationUtil.runAdl(runConfiguration, appDir.getPath() + "/" + DESCRIPTOR_XML,
                                                      server.listen(), new Consumer<Integer>() {
              @Override
              public void consume(Integer exitCode) {
                adlProcessHandler = null;
                if (onAdlExit != null) {
                  ApplicationManager.getApplication().invokeLater(onAdlExit);
                }

                if (exitCode != 0) {
                  LOG.error("ADL exited with error code " + exitCode);
                }

                documentOpening = false;
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
      ProjectManager.getInstance().addProjectManagerListener(projectManagerListener);
    }

    if (DebugPathManager.IS_DEV) {
      return;
    }

    final ClassLoader classLoader = getClass().getClassLoader();
    final URL appUrl = classLoader.getResource(DESIGNER_SWF);
    LOG.assertTrue(appUrl != null);
    final URLConnection appUrlConnection = appUrl.openConnection();
    final long lastModified = appUrlConnection.getLastModified();
    final File appFile = new File(appDir, DESIGNER_SWF);
    if (appFile.lastModified() >= lastModified) {
      return;
    }

    //noinspection ResultOfMethodCallIgnored
    appDir.mkdirs();
    IOUtil.saveStream(classLoader.getResourceAsStream(DESCRIPTOR_XML), new File(appDir, DESCRIPTOR_XML));
    IOUtil.saveStream(classLoader.getResourceAsStream(CHECK_DESCRIPTOR_XML), new File(appDir, CHECK_DESCRIPTOR_XML));
    IOUtil.saveStream(appUrlConnection.getInputStream(), appFile);

    //noinspection ResultOfMethodCallIgnored
    appFile.setLastModified(lastModified);
  }


  class PendingOpenDocumentTask implements Runnable {
    private final Project myProject;
    private final Module myModule;
    private final XmlFile myPsiFile;

    private boolean libraryAndModuleInitialized;
    private boolean clientOpened;

    public PendingOpenDocumentTask(@NotNull Project project, @NotNull Module module, @NotNull XmlFile psiFile) {
      myProject = project;
      myModule = module;
      myPsiFile = psiFile;

      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        @Override
        public void run() {
          try {
            if (!ServiceManager.getService(StringRegistry.class).isEmpty()) {
              Client.getInstance().initStringRegistry();
            }
            LibraryManager.getInstance().initLibrarySets(myModule, appDir);
          }
          catch (IOException e) {
            try {
              server.close();
            }
            catch (IOException innerError) {
              LOG.error(innerError);
            }
            finally {
              serverClosed();
            }
            LOG.error(e);
          }
          catch (InitException e) {
            LOG.error(e.getCause());
            DocumentProblemManager.getInstance().reportWithTitle(myProject, e.getMessage());
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
        try {
          server.close();
        }
        catch (IOException innerError) {
          LOG.error(innerError);
        }
        finally {
          serverClosed();
        }
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