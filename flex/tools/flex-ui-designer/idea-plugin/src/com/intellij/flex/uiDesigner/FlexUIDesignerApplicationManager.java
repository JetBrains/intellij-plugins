package com.intellij.flex.uiDesigner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.flex.uiDesigner.io.IOUtil;
import com.intellij.flex.uiDesigner.io.InfoList;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.libraries.*;
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

import java.io.*;
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

  protected Client client = createClient();
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

  public Client getClient() {
    return client;
  }

  protected Client createClient() {
    return new Client();
  }

  @Override
  public void dispose() {
    closeClosable(client);
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
    if (client != null) {
      closeClosable(client);
    }
    
    Application application = ApplicationManager.getApplication();
    if (!application.isDisposed()) {
      application.getMessageBus().syncPublisher(MESSAGE_TOPIC).applicationClosed();
    }
  }

  public void openDocument(@NotNull final Project project, @NotNull final Module module, @NotNull final XmlFile psiFile, boolean debug) {
    assert !documentOpening;
    documentOpening = true;

    if (server == null || server.isClosed()) {
      run(project, module, psiFile, debug);
    }
    else {
      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        @Override
        public void run() {
          try {
            if (!client.isModuleRegistered(module)) {
              try {
                initLibrarySets(project, module);
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
    DesignerApplicationUtil.AdlRunConfiguration adlRunConfiguration = DesignerApplicationUtil.findSuitableFlexSdk();
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

    server = new Server(new PendingOpenDocumentTask(project, module, psiFile), this);
    DesignerApplicationUtil.AdlRunTask task = new DesignerApplicationUtil.AdlRunTask(adlRunConfiguration) {
      @Override
      public void run() {
        try {
          copyAppFiles();
        
          adlProcessHandler = DesignerApplicationUtil.runAdl(runConfiguration, appDir.getPath() + "/" + DESCRIPTOR_XML,
                                                      server.listen(), new Consumer<Integer>() {
              @Override
              public void consume(Integer integer) {
                adlProcessHandler = null;
                if (onAdlExit != null) {
                  ApplicationManager.getApplication().invokeLater(onAdlExit);
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
    
    if (appDir == null) {
      appDir = new File(PathManager.getSystemPath(), "flexUIDesigner");
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
    IOUtil.saveStream(appUrlConnection.getInputStream(), appFile);

    //noinspection ResultOfMethodCallIgnored
    appFile.setLastModified(lastModified);
  }

  private void initLibrarySets(@NotNull final Project project, @NotNull final Module module) throws IOException, InitException {
    final LibraryCollector libraryCollector = new LibraryCollector();
    final StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter(16384);
    stringWriter.startChange();

    final ProblemsHolder problemsHolder = new ProblemsHolder();
    try {
      ApplicationManager.getApplication().runReadAction(new Runnable() {
        @Override
        public void run() {
          libraryCollector.collect(module, new LibraryStyleInfoCollector(project, module, stringWriter, problemsHolder));
        }
      });
    }
    catch (Throwable e) {
      stringWriter.rollbackChange();
      throw new InitException(e, "error.collect.libraries");
    }

    final LibrarySet externalLibrarySet;
    final InfoList<Project, ProjectInfo> registeredProjects = client.getRegisteredProjects();
    if (!registeredProjects.contains(project)) {
      String librarySetId = project.getLocationHash();
      try {
        externalLibrarySet = new LibrarySet(librarySetId, ApplicationDomainCreationPolicy.ONE, new SwcDependenciesSorter(appDir)
          .sort(libraryCollector.getExternalLibraries(), librarySetId, libraryCollector.getFlexSdkVersion()));
      }
      catch (Throwable e) {
        throw new InitException(e, "error.sort.libraries");
      }

      registeredProjects.add(new ProjectInfo(project, externalLibrarySet));

      client.openProject(project);
      client.registerLibrarySet(externalLibrarySet, stringWriter);
    }
    else {
      stringWriter.finishChange();
      //noinspection UnusedAssignment
      externalLibrarySet = registeredProjects.getInfo(project).getLibrarySet();
      // todo merge existing libraries and new. create new custom external library set for myModule, 
      // if we have different version of the artifact
    }

    ModuleInfo moduleInfo = new ModuleInfo(module);
    stringWriter.startChange();
    try {
      ModuleInfoUtil.collectLocalStyleHolders(moduleInfo, libraryCollector.getFlexSdkVersion(), stringWriter, problemsHolder);
    }
    catch (Throwable e) {
      stringWriter.rollbackChange();
      throw new InitException(e, "error.collect.local.style.holders");
    }

    if (!problemsHolder.isEmpty()) {
      DocumentProblemManager.getInstance().report(module.getProject(), problemsHolder);
    }
    client.registerModule(project, moduleInfo, new String[]{externalLibrarySet.getId()}, stringWriter);
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

      if (!ServiceManager.getService(StringRegistry.class).isEmpty()) {
        try {
          client.initStringRegistry();
        }
        catch (IOException e) {
          LOG.error(e); // never can be, because socket out is null at this moment
        }
      }

      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        @Override
        public void run() {
          try {
            initLibrarySets(myProject, myModule);
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
      client.setOut(out);
    }

    @Override
    public void run() {
      clientOpened = true;
      if (libraryAndModuleInitialized) {
        openDocument();
      }
    }

    private void openDocument() {
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

      final InfoList<Project, ProjectInfo> registeredProjects = client.getRegisteredProjects();
      if (registeredProjects.contains(project)) {
        try {
          client.closeProject(project);
        }
        catch (IOException e) {
          LOG.error(e);
        }
        finally {
          registeredProjects.remove(project);
        }
      }
    }

    @Override
    public void projectClosing(Project project) {
    }
  }
}