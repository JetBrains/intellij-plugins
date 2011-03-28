package com.intellij.flex.uiDesigner;

import com.intellij.execution.ExecutionException;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.projectRoots.ui.ProjectJdksEditor;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Consumer;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class FlexUIDesignerApplicationManager implements Disposable {
  public static final Topic<FlexUIDesignerApplicationListener> MESSAGE_TOPIC =
    new Topic<FlexUIDesignerApplicationListener>("Flex UI Designer Application open and close events",
                                                 FlexUIDesignerApplicationListener.class);

  private static final Key<ProjectInfo> PROJECT_INFO = Key.create("FUD_PROJECT_INFO");

  static final Logger LOG = Logger.getInstance(FlexUIDesignerApplicationManager.class.getName());
  
  public static final String DESIGNER_SWF = "designer.swf";
  public static final String DESCRIPTOR_XML = "descriptor.xml";

  private Client client;
  private Process adlProcess;
  private Server server;

  ProjectManagerListener projectManagerListener;

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

  @Override
  public void dispose() {
    if (server != null) {
      try {
        if (client != null) {
          client.close();
        }
        server.close();
      }
      catch (IOException e) {
        LOG.error(e);
      }

      if (adlProcess != null) {
        adlProcess.destroy();
      }
    }
  }

  public void serverClosed() throws IOException {
    documentOpening = false;
    if (client != null) {
      client.close();
      for (Project project : ProjectManager.getInstance().getOpenProjects()) {
        project.putUserData(PROJECT_INFO, null);
      }
    }
  }

  public void openDocument(@NotNull final Project project, @NotNull final Module module, @NotNull final XmlFile psiFile, boolean debug) {
    assert !documentOpening;
    documentOpening = true;

    if (server == null || server.isClosed()) {
      LOG.assertTrue(project.getUserData(PROJECT_INFO) == null);
      run(project, module, psiFile, debug);
    }
    else {
      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        @Override
        public void run() {
          try {
            if (!client.isModuleRegistered(module)) {
              initLibrarySets(project, module);
            }

            client.openDocument(module, psiFile);
            client.flush();

            assert documentOpening;
            documentOpening = false;
          }
          catch (IOException e) {
            LOG.error(e);
          }
        }
      });
    }
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
        
          adlProcess = DesignerApplicationUtil.runAdl(runConfiguration, appDir.getPath() + "/" + DESCRIPTOR_XML,
                                                      server.listen(), new Consumer<Integer>() {
              @Override
              public void consume(Integer integer) {
                adlProcess = null;
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
    saveStream(classLoader.getResourceAsStream(DESCRIPTOR_XML), new File(appDir, DESCRIPTOR_XML));
    saveStream(appUrlConnection.getInputStream(), appFile);

    //noinspection ResultOfMethodCallIgnored
    appFile.setLastModified(lastModified);
  }

  private void initLibrarySets(@NotNull final Project project, @NotNull final Module module) throws IOException {
    final LibraryCollector libraryCollector = new LibraryCollector();
    final StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter(16384);
    stringWriter.startChange();

    ApplicationManager.getApplication().runReadAction(new Runnable() {
      @Override
      public void run() {
        final LibraryStyleInfoCollector styleInfoCollector = new LibraryStyleInfoCollector(project, module, stringWriter);
        libraryCollector.collect(module, new Consumer<OriginalLibrary>() {
          @Override
          public void consume(OriginalLibrary originalLibrary) {
            styleInfoCollector.collect(originalLibrary);
          }
        });
      }
    });

    final LibrarySet externalLibrarySet;
    ProjectInfo projectInfo = project.getUserData(PROJECT_INFO);
    if (projectInfo == null) {
      String librarySetId = project.getLocationHash();
      externalLibrarySet = new LibrarySet(librarySetId, ApplicationDomainCreationPolicy.ONE, new SwcDependenciesSorter(appDir)
        .sort(libraryCollector.getExternalLibraries(), librarySetId, libraryCollector.getFlexSdkVersion()));
      projectInfo = new ProjectInfo(externalLibrarySet);
      project.putUserData(PROJECT_INFO, projectInfo);

      client.openProject(project);
      client.registerLibrarySet(externalLibrarySet, stringWriter);
    }
    else {
      //noinspection UnusedAssignment
      externalLibrarySet = projectInfo.getLibrarySet();
      // todo merge existing libraries and new. create new custom external library set for myModule, 
      // if we have different version of the artifact
    }

    final ModuleInfo moduleInfo = new ModuleInfo(module);
    stringWriter.startChange();
    ModuleInfoUtil.collectLocalStyleHolders(moduleInfo, libraryCollector.getFlexSdkVersion(), stringWriter);

    client.registerModule(project, moduleInfo, new String[]{externalLibrarySet.getId()}, stringWriter);
  }

  class PendingOpenDocumentTask implements Runnable {
    private final Project myProject;
    private final Module myModule;
    private final XmlFile myPsiFile;

    public PendingOpenDocumentTask(@NotNull Project project, @NotNull Module module, @NotNull XmlFile psiFile) {
      myProject = project;
      myModule = module;
      myPsiFile = psiFile;
    }

    public void setOutput(OutputStream outputStream) {
      if (client == null) {
        client = new Client(outputStream);
      }
      else {
        client.setOutput(outputStream);
      }
    }

    @Override
    public void run() {
      try {
        client.initStringRegistry();
        initLibrarySets(myProject, myModule);
        client.openDocument(myModule, myPsiFile);
        client.flush();

        ApplicationManager.getApplication().getMessageBus().syncPublisher(MESSAGE_TOPIC).initialDocumentOpened();
      }
      catch (IOException e) {
        try {
          server.close();
          serverClosed();
        }
        catch (IOException innerError) {
          LOG.error(innerError);
        }
        LOG.error(e);
      }
      finally {
        documentOpening = false;
      }
    }
  }

  public void reportProblem(final Project project, String message) {
    reportProblem(project, message, MessageType.ERROR);
  }

  @SuppressWarnings({"MethodMayBeStatic"})
  public void reportProblem(final Project project, String message, MessageType messageType) {
    final Balloon balloon = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(message, messageType, null).setShowCallout(false)
      .setHideOnAction(false).createBalloon();
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        Window window = WindowManager.getInstance().getFrame(project);
        if (window == null) {
          window = JOptionPane.getRootFrame();
        }
        if (window instanceof IdeFrameImpl) {
          ((IdeFrameImpl)window).getBalloonLayout().add(balloon);
        }
      }
    });
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

      ProjectInfo projectInfo = project.getUserData(PROJECT_INFO);
      if (projectInfo != null) {
        project.putUserData(PROJECT_INFO, null);
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

  private static void saveStream(InputStream input, File output) throws IOException {
    FileOutputStream os = new FileOutputStream(output);
    try {
      FileUtil.copy(input, os);
    }
    finally {
      os.close();
    }
  }
}

@SuppressWarnings({"UnusedDeclaration"})
enum ApplicationDomainCreationPolicy {
  ONE, MULTIPLE
}